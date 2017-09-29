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
import com.thinkbiganalytics.spark.datavalidator.functions.CleanseAndValidateRow;
import com.thinkbiganalytics.spark.datavalidator.functions.SumPartitionLevelCounts;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;


/**
 * Cleanses and validates a table of strings according to defined field-level policies. Records are split into good and bad. <p> blog.cloudera.com/blog/2015/07/how-to-do-data-quality-checks-using-apache-spark-dataframes/
 */
@Component
@SuppressWarnings("serial")
public class StandardDataValidator implements DataValidator, Serializable {

    private static final Logger log = LoggerFactory.getLogger(StandardDataValidator.class);

    /*
    Valid validation result
     */
    public static final ValidationResult VALID_RESULT = new ValidationResult();
    static final String REJECT_REASON_COL = "dlp_reject_reason";
    static final String PROCESSING_DTTM_COL = "processing_dttm";

    private final SparkContextService scs;

    private final IValidatorStrategy validatorStrategy;

    /**
     * Constructs a {@code StandardDataValidator}.
     */
    public StandardDataValidator(@Nonnull final IValidatorStrategy validatorStrategy, @Nonnull final SparkContextService scs) {
        this.validatorStrategy = validatorStrategy;
        this.scs = scs;
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
    public DataValidatorResult validateTable(@Nonnull final String databaseName, @Nonnull final String tableName, @Nonnull final String partition, final int numPartitions,
                                             @Nonnull final Map<String, FieldPolicy> policyMap, @Nonnull final HiveContext hiveContext) {
        // Extract fields from a source table
        StructField[] fields = resolveSchema(databaseName, tableName, hiveContext);
        FieldPolicy[] policies = resolvePolicies(fields, policyMap);

        String selectStmt = toSelectFields(policies);
        String sql = "SELECT " + selectStmt + " FROM " + HiveUtils.quoteIdentifier(databaseName, tableName) + " WHERE processing_dttm = '" + partition + "'";
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
    public void saveInvalidToTable(@Nonnull String databaseName, @Nonnull String tableName, @Nonnull DataValidatorResult result, @Nonnull HiveContext hiveContext) {
        // Return a new rdd based for Invalid Results
        //noinspection serial
        JavaRDD<CleansedRowResult> invalidResultRDD = result.getCleansedRowResultRDD().filter(new Function<CleansedRowResult, Boolean>() {
            @Override
            public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                return !cleansedRowResult.isRowValid();
            }
        });

        DataSet invalidDataFrame = getRows(invalidResultRDD, ModifiedSchema.getInvalidTableSchema(result.getSchema(), result.getPolicies()), hiveContext);
        writeToTargetTable(invalidDataFrame, databaseName, tableName, hiveContext);

        log.info("wrote values to the invalid Table  {}", tableName);
    }

    @Override
    public void saveProfileToTable(@Nonnull String databaseName, @Nonnull String tableName, @Nonnull String partition, @Nonnull DataValidatorResult result, @Nonnull HiveContext hiveContext) {
        // Counts of invalid columns, total valid rows and total invalid rows
        long[] fieldInvalidCounts = cleansedRowResultsValidationCounts(result.getCleansedRowResultRDD(), result.getSchema().length);
        long validCount = fieldInvalidCounts[result.getSchema().length];
        long invalidCount = fieldInvalidCounts[result.getSchema().length + 1];

        // Record the validation stats
        log.info("Valid count {} invalid count {}", validCount, invalidCount);
        writeStatsToProfileTable(databaseName, tableName, partition, validCount, invalidCount, fieldInvalidCounts, result.getSchema(), hiveContext);
    }

    @Override
    public void saveValidToTable(@Nonnull String databaseName, @Nonnull String tableName, @Nonnull DataValidatorResult result, @Nonnull HiveContext hiveContext) {
        // Return a new rdd based for Valid Results
        //noinspection serial
        JavaRDD<CleansedRowResult> validResultRDD = result.getCleansedRowResultRDD().filter(new Function<CleansedRowResult, Boolean>() {
            @Override
            public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                return cleansedRowResult.isRowValid();
            }
        });

        // Write out the valid records (dropping the two columns)
        StructType validTableSchema = scs.toDataSet(hiveContext, HiveUtils.quoteIdentifier(databaseName, tableName)).schema();
        DataSet validDataFrame = getRows(validResultRDD, ModifiedSchema.getValidTableSchema(result.getSchema(), validTableSchema.fields(), result.getPolicies()), hiveContext);
        validDataFrame = validDataFrame.drop(REJECT_REASON_COL).toDF();
        //Remove the columns from _valid that dont exist in the validTableName

        writeToTargetTable(validDataFrame, databaseName, tableName, hiveContext);

        log.info("wrote values to the valid Table  {}", tableName);
    }

    /**
     * Validates the specified dataset and returns the results.
     *
     * @param policies the field policies
     * @param fields   the target schema
     */
    @Nonnull
    private DataValidatorResult validate(@Nonnull final DataSet dataset, @Nonnull final FieldPolicy[] policies, @Nonnull final StructField[] fields) {
        final HCatDataType[] schema = resolveDataTypes(fields);
        final JavaRDD<CleansedRowResult> cleansedRowResultRDD = dataset.javaRDD().map(new CleanseAndValidateRow(policies, schema));
        return new DataValidatorResult(cleansedRowResultRDD, policies, fields);
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

    private void writeStatsToProfileTable(String databaseName, String tableName, String partition, long validCount, long invalidCount, long[] fieldInvalidCounts, StructField[] schema,
                                          HiveContext hiveContext) {

        try {
            // Create a temporary table that can be used to copy data from. Writing directly to the partition from a spark dataframe doesn't work.
            String tempTable = tableName + "_" + System.currentTimeMillis();

            // Refactor this into something common with profile table
            List<StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField("columnname", DataTypes.StringType, true));
            fields.add(DataTypes.createStructField("metrictype", DataTypes.StringType, true));
            fields.add(DataTypes.createStructField("metricvalue", DataTypes.StringType, true));

            StructType statsSchema = DataTypes.createStructType(fields);

            final ArrayList<String> csvRows = new ArrayList<>();
            csvRows.add("(ALL),TOTAL_COUNT," + Long.toString(validCount + invalidCount));
            csvRows.add("(ALL),VALID_COUNT," + Long.toString(validCount));
            csvRows.add("(ALL),INVALID_COUNT," + Long.toString(invalidCount));

            // Write csv row for each columns
            for (int i = 0; i < fieldInvalidCounts.length; i++) {
                if (i < schema.length) {
                    String csvRow = schema[i].name() + ",INVALID_COUNT," + Long.toString(fieldInvalidCounts[i]);
                    csvRows.add(csvRow);
                }
            }

            JavaSparkContext jsc = new JavaSparkContext(hiveContext.sparkContext());
            JavaRDD<Row> statsRDD = jsc.parallelize(csvRows)
                .map(new Function<String, Row>() {
                    @Override
                    public Row call(String s) throws Exception {
                        return RowFactory.create((Object[]) s.split("\\,"));
                    }
                });

            DataSet df = scs.toDataSet(hiveContext, statsRDD, statsSchema);
            df.registerTempTable(tempTable);

            String insertSQL = "INSERT OVERWRITE TABLE " + HiveUtils.quoteIdentifier(databaseName, tableName)
                               + " PARTITION (processing_dttm='" + partition + "')"
                               + " SELECT columnname, metrictype, metricvalue FROM " + HiveUtils.quoteIdentifier(tempTable);

            log.info("Writing profile stats {}", insertSQL);
            scs.sql(hiveContext, insertSQL);
        } catch (Exception e) {
            log.error("Failed to insert validation stats", e);
            throw Throwables.propagate(e);
        }
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

    /**
     * Converts the table schema into the corresponding data type structures
     */
    private HCatDataType[] resolveDataTypes(StructField[] fields) {
        List<HCatDataType> cols = new ArrayList<>(fields.length);

        for (StructField field : fields) {
            String colName = field.name();
            String dataType = field.dataType().simpleString();
            cols.add(HCatDataType.createFromDataType(colName, dataType));
        }
        return cols.toArray(new HCatDataType[0]);
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
            String colName = field.name();
            FieldPolicy policy = policyMap.get(colName);
            if (policy == null) {
                policy = FieldPolicyBuilder.SKIP_VALIDATION;
            }
            pols.add(policy);
        }
        return pols.toArray(new FieldPolicy[0]);
    }
}
