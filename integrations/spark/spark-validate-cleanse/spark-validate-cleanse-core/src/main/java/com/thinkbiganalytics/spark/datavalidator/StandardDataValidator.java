package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Throwables;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicyBuilder;
import com.thinkbiganalytics.policy.validation.ValidationResult;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.datavalidator.functions.CleanseAndValidateRow;
import com.thinkbiganalytics.spark.datavalidator.functions.SumPartitionLevelCounts;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.thinkbiganalytics.spark.datavalidator.functions.CleanseAndValidateRow.PROCESSING_DTTM_COL;
import static com.thinkbiganalytics.spark.datavalidator.functions.CleanseAndValidateRow.REJECT_REASON_COL;


/**
 * Cleanses and validates a table of strings according to defined field-level policies. Records are split into good and bad. <p> blog.cloudera.com/blog/2015/07/how-to-do-data-quality-checks-using-apache-spark-dataframes/
 */
@SuppressWarnings("serial")
public class StandardDataValidator implements DataValidator, Serializable {

    private static final Logger log = LoggerFactory.getLogger(StandardDataValidator.class);

    /*
    Valid validation result
     */
    public static final ValidationResult VALID_RESULT = new ValidationResult();

    /**
     * Column name indicate all columns.
     */
    private static final String ALL_COLUMNS = "(ALL)";

    /**
     * Metric type for invalid row count.
     */
    private static final String INVALID_COUNT = "INVALID_COUNT";

    /**
     * Metric type for total row count.
     */
    private static final String TOTAL_COUNT = "TOTAL_COUNT";

    /**
     * Metric type for valid row count.
     */
    private static final String VALID_COUNT = "VALID_COUNT";

    private final SparkContextService scs;

    private final IValidatorStrategy validatorStrategy;

    /**
     * Constructs a {@code StandardDataValidator}.
     */
    public StandardDataValidator(@Nonnull final IValidatorStrategy validatorStrategy, @Nonnull final SparkContextService scs) {
        this.validatorStrategy = validatorStrategy;
        this.scs = scs;
    }

    @Override
    public List<OutputRow> getProfileStats(@Nonnull final DataValidatorResult result) {
        final List<OutputRow> stats = new ArrayList<>();
        final long[] validationCounts = cleansedRowResultsValidationCounts(result.getCleansedRowResultRDD(), result.getSchema().length() - 1);

        // Calculate global stats
        final long validCount = validationCounts[result.getSchema().length() - 1];
        final long invalidCount = validationCounts[result.getSchema().length()];
        log.info("Valid count {} invalid count {}", validCount, invalidCount);

        stats.add(new OutputRow(ALL_COLUMNS, TOTAL_COUNT, Long.toString(validCount + invalidCount)));
        stats.add(new OutputRow(ALL_COLUMNS, VALID_COUNT, Long.toString(validCount)));
        stats.add(new OutputRow(ALL_COLUMNS, INVALID_COUNT, Long.toString(invalidCount)));

        // Calculate column stats
        final StructField[] fields = result.getSchema().fields();
        for (int i = 0; i < validationCounts.length && i < fields.length - 1; i++) {
            stats.add(new OutputRow(fields[i].name(), INVALID_COUNT, Long.toString(validationCounts[i])));
        }

        return stats;
    }

    @Nonnull
    @Override
    public DataValidatorResult validate(@Nonnull final DataSet dataset, @Nonnull final Map<String, FieldPolicy> policyMap) {
        final StructField[] fields = dataset.schema().fields();
        final FieldPolicy[] policies = resolvePolicies(fields, policyMap);
        return validate(dataset, policies, fields);
    }

    @Nonnull
    @Override
    public DataValidatorResult validateTable(@Nonnull final String databaseName, @Nonnull final String sourceTableName, @Nonnull final String targetTableName, @Nonnull final String partition,
                                             final int numPartitions, @Nonnull final Map<String, FieldPolicy> policyMap, @Nonnull final HiveContext hiveContext) {
        // Extract fields from a source table
        StructField[] fields = resolveSchema(databaseName, targetTableName, hiveContext);
        FieldPolicy[] policies = resolvePolicies(fields, policyMap);

        String selectStmt = toSelectFields(policies);
        String sql = "SELECT " + selectStmt + " FROM " + HiveUtils.quoteIdentifier(databaseName, sourceTableName) + " WHERE processing_dttm = '" + partition + "'";
        log.info("Executing query {}", sql);
        DataSet sourceDF = scs.sql(hiveContext, sql);

        // Repartition if necessary
        if (numPartitions > 0) {
            log.info("Partition count: {}", numPartitions);
            sourceDF = sourceDF.repartition(numPartitions);
        }

        return validate(sourceDF, policies, fields);
    }

    @Override
    public void saveInvalidToTable(@Nonnull final String databaseName, @Nonnull final String tableName, @Nonnull final DataValidatorResult result, @Nonnull final HiveContext hiveContext) {
        // Return a new rdd based for Invalid Results
        //noinspection serial
        JavaRDD<CleansedRowResult> invalidResultRDD = result.getCleansedRowResultRDD().filter(new Function<CleansedRowResult, Boolean>() {
            @Override
            public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                return !cleansedRowResult.isRowValid();
            }
        });

        final StructType invalidSchema = new StructType(resolveSchema(databaseName, tableName, hiveContext));
        DataSet invalidDataFrame = getRows(invalidResultRDD, invalidSchema, hiveContext);
        writeToTargetTable(invalidDataFrame, databaseName, tableName, hiveContext);

        log.info("wrote values to the invalid Table  {}", tableName);
    }

    @Override
    public void saveProfileToTable(@Nonnull final String databaseName, @Nonnull final String tableName, @Nonnull final String partition, @Nonnull final DataValidatorResult result,
                                   @Nonnull final HiveContext hiveContext) {
        try {
            // Create a temporary table that can be used to copy data from. Writing directly to the partition from a spark dataframe doesn't work.
            final String tempTable = tableName + "_" + System.currentTimeMillis();

            // Refactor this into something common with profile table
            @SuppressWarnings("squid:S2095") final JavaRDD<OutputRow> statsRDD = JavaSparkContext.fromSparkContext(hiveContext.sparkContext()).parallelize(getProfileStats(result));
            final DataSet df = scs.toDataSet(hiveContext, statsRDD, OutputRow.class);
            df.registerTempTable(tempTable);

            final String insertSQL = "INSERT OVERWRITE TABLE " + HiveUtils.quoteIdentifier(databaseName, tableName)
                                     + " PARTITION (processing_dttm='" + partition + "')"
                                     + " SELECT columnname, metrictype, metricvalue FROM " + HiveUtils.quoteIdentifier(tempTable);

            log.info("Writing profile stats {}", insertSQL);
            scs.sql(hiveContext, insertSQL);
        } catch (final Exception e) {
            log.error("Failed to insert validation stats", e);
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void saveValidToTable(@Nonnull final String databaseName, @Nonnull final String sourceTableName, @Nonnull final String targetTableName, @Nonnull final DataValidatorResult result,
                                 @Nonnull final HiveContext hiveContext) {
        // Return a new rdd based for Valid Results
        //noinspection serial
        JavaRDD<CleansedRowResult> validResultRDD = result.getCleansedRowResultRDD().filter(new Function<CleansedRowResult, Boolean>() {
            @Override
            public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                return cleansedRowResult.isRowValid();
            }
        });

        // Write out the valid records (dropping the two columns)
        final StructType feedTableSchema = scs.toDataSet(hiveContext, HiveUtils.quoteIdentifier(databaseName, sourceTableName)).schema();
        StructType validTableSchema = scs.toDataSet(hiveContext, HiveUtils.quoteIdentifier(databaseName, targetTableName)).schema();
        DataSet validDataFrame = getRows(validResultRDD, ModifiedSchema.getValidTableSchema(feedTableSchema.fields(), validTableSchema.fields(), result.getPolicies()), hiveContext);
        validDataFrame = validDataFrame.drop(REJECT_REASON_COL).toDF();
        //Remove the columns from _valid that dont exist in the validTableName

        writeToTargetTable(validDataFrame, databaseName, targetTableName, hiveContext);

        log.info("wrote values to the valid Table  {}", targetTableName);
    }

    /**
     * Validates the specified dataset and returns the results.
     *
     * @param policies the field policies
     * @param fields   the target schema
     */
    @Nonnull
    private DataValidatorResult validate(@Nonnull final DataSet dataset, @Nonnull final FieldPolicy[] policies, @Nonnull final StructField[] fields) {
        final CleanseAndValidateRow function = new CleanseAndValidateRow(policies, fields);
        final JavaRDD<CleansedRowResult> cleansedRowResultRDD = dataset.javaRDD().map(function);
        return new DataValidatorResult(cleansedRowResultRDD, policies, function.getSchema());
    }

    private DataSet getRows(@Nonnull final JavaRDD<CleansedRowResult> results, @Nonnull final StructType schema, @Nonnull final HiveContext hiveContext) {
        JavaRDD<Row> rows = results.map(new Function<CleansedRowResult, Row>() {
            @Override
            public Row call(CleansedRowResult cleansedRowResult) throws Exception {
                return cleansedRowResult.getRow();
            }
        });
        return scs.toDataSet(hiveContext, rows, schema);
    }

    private String toSelectFields(FieldPolicy[] policies1) {
        List<String> fields = new ArrayList<>();
        log.info("Building select statement for # of policies {}", policies1.length);
        for (int i = 0; i < policies1.length; i++) {
            if (policies1[i].getField() != null) {
                log.info("policy [{}] name {} feedName {}", i, policies1[i].getField(), policies1[i].getFeedField());
                String feedField = StringUtils.defaultIfEmpty(policies1[i].getFeedField(), policies1[i].getField());
                fields.add("`" + feedField + "` as `" + policies1[i].getField() + "`");
            }
        }
        fields.add("`processing_dttm`");
        return StringUtils.join(fields.toArray(new String[0]), ",");
    }

    private void writeToTargetTable(DataSet sourceDF, String databaseName, String targetTable, HiveContext hiveContext) {
        final String qualifiedTable = HiveUtils.quoteIdentifier(databaseName, targetTable);

        // Direct insert into the table partition vs. writing into a temporary table
        hiveContext.setConf("hive.exec.dynamic.partition", "true");
        hiveContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict");
        // Required for ORC and Parquet
        hiveContext.setConf("set hive.optimize.index.filter", "false");

        sourceDF.writeToTable(PROCESSING_DTTM_COL, qualifiedTable);
    }

    /**
     * Performs counts of invalid columns, total valid and total invalid on a JavaRDD<CleansedRowResults>
     */
    long[] cleansedRowResultsValidationCounts(JavaRDD<CleansedRowResult> cleansedRowResultJavaRDD, int schemaLength) {

        // Maps each partition in the JavaRDD<CleansedRowResults> to a long[] of invalid column counts and total valid/invalid counts
        JavaRDD<long[]> partitionCounts = validatorStrategy.getCleansedRowResultPartitionCounts(cleansedRowResultJavaRDD, schemaLength);

        // Sums up all partitions validation counts into one long[]
        return partitionCounts.reduce(new SumPartitionLevelCounts());
    }

    @Nonnull
    private StructField[] resolveSchema(@Nonnull final String databaseName, @Nonnull final String tableName, @Nonnull final HiveContext hiveContext) {
        StructType schema = scs.toDataSet(hiveContext, HiveUtils.quoteIdentifier(databaseName, tableName)).schema();
        return schema.fields();
    }

    /**
     * Returns an array of field-level policies for data validation and cleansing
     */
    private FieldPolicy[] resolvePolicies(StructField[] fields, Map<String, FieldPolicy> policyMap) {
        List<FieldPolicy> pols = new ArrayList<>(fields.length);

        for (StructField field : fields) {
            String colName = field.name().toLowerCase();
            FieldPolicy policy = policyMap.get(colName);
            if (policy == null) {
                policy = FieldPolicyBuilder.SKIP_VALIDATION;
            }
            pols.add(policy);
        }
        return pols.toArray(new FieldPolicy[0]);
    }
}
