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

import com.beust.jcommander.JCommander;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicyBuilder;
import com.thinkbiganalytics.policy.standardization.AcceptsEmptyValues;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationResult;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;
import com.thinkbiganalytics.spark.util.InvalidFormatException;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Cleanses and validates a table of strings according to defined field-level policies. Records are split into good and bad. <p> blog.cloudera.com/blog/2015/07/how-to-do-data-quality-checks-using-apache-spark-dataframes/
 */
@Component
public class Validator implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    private static String REJECT_REASON_COL = "dlp_reject_reason";
    private static String VALID_INVALID_COL = "dlp_valid";
    private static String PROCESSING_DTTM_COL = "processing_dttm";

    /*
    Valid validation result
     */
    protected static ValidationResult VALID_RESULT = new ValidationResult();

    /* Initialize Spark */
    private HiveContext hiveContext;

    // Optimization to write directly from dataframe to the table vs. temporary table (not tested with < 1.6.x)
    private boolean useDirectInsert = true;

    /*
    Valid target schema
     */
    private String validTableName;
    private String invalidTableName;
    private String feedTablename;
    private String refTablename;
    private String profileTableName;
    private String qualifiedProfileName;
    private String targetDatabase;
    private String partition;

    private FieldPolicy[] policies;
    private HCatDataType[] schema;
    private Map<String, FieldPolicy> policyMap = new HashMap<>();

    /*
    Cache for performance. Validators accept different paramters (numeric,string, etc) so we need to resolve the type using reflection
     */
    private Map<Class, Class> validatorParamType = new HashMap<>();

    @Autowired
    private SparkContextService scs;

    @Autowired
    private FieldPolicyLoader loader;

    /**
     * Path to the file containing the JSON for the Field Policies. If called from NIFI it will pass it in as a command argument in the Validate processor The JSON should conform to the array of
     * FieldPolicy objects found in the thinkbig-field-policy-rest-model module
     */
    private String fieldPolicyJsonPath;

    private final Vector<Accumulator<Integer>> accumList = new Vector<>();
    private CommandLineParams params;

    public void setArguments(String targetDatabase, String entity, String partition, String fieldPolicyJsonPath) {
        this.validTableName = entity + "_valid";
        this.invalidTableName = entity + "_invalid";
        this.profileTableName = entity + "_profile";
        this.feedTablename = HiveUtils.quoteIdentifier(targetDatabase, entity + "_feed");
        this.refTablename = HiveUtils.quoteIdentifier(targetDatabase, validTableName);
        this.qualifiedProfileName = HiveUtils.quoteIdentifier(targetDatabase, profileTableName);
        this.partition = partition;
        this.targetDatabase = targetDatabase;
        this.fieldPolicyJsonPath = fieldPolicyJsonPath;
    }

    protected HiveContext getHiveContext() {
        return hiveContext;
    }

    public void doValidate() {
        try {
            SparkContext sparkContext = SparkContext.getOrCreate();
            hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkContext);

            for (Param param : params.getHiveParams()) {
                log.info("Adding Hive parameter {}={}", param.getName(), param.getValue());
                hiveContext.setConf(param.getName(), param.getValue());
            }

            log.info("Deployment Mode - " + sparkContext.getConf().get("spark.submit.deployMode"));
            policyMap = loader.loadFieldPolicy(fieldPolicyJsonPath);

            // Extract fields from a source table
            StructField[] fields = resolveSchema();
            this.schema = resolveDataTypes(fields);
            this.policies = resolvePolicies(fields);

            String selectStmt = toSelectFields();
            String sql = "SELECT "+selectStmt+" FROM " + feedTablename + " WHERE processing_dttm = '" + partition + "'";
            log.info("Executing query {}", sql);
            DataSet dataFrame = scs.sql(getHiveContext(), sql);
            JavaRDD<Row> rddData = dataFrame.javaRDD();

            // Extract schema from the source table
            StructType sourceSchema = createModifiedSchema(feedTablename);
            log.info("sourceSchema {}", sourceSchema);

            // Initialize accumulators to track error statistics on each column
            JavaSparkContext javaSparkContext = new JavaSparkContext(SparkContext.getOrCreate());
            for (int i = 0; i < this.schema.length; i++) {
                this.accumList.add(javaSparkContext.accumulator(0));
            }

            // Return a new dataframe based on whether values are valid or invalid
            JavaRDD<Row> newResults = rddData.map(new Function<Row, Row>() {
                @Override
                public Row call(Row row) throws Exception {
                    return cleanseAndValidateRow(row);
                }
            });

            newResults.persist(StorageLevel.fromString(params.getStorageLevel()));
            newResults.count();

            Integer[] fieldInvalidCounts = new Integer[this.schema.length];
            for (int i = 0; i < this.schema.length; i++) {
                fieldInvalidCounts[i] = this.accumList.get(i).value();
            }

            final DataSet validatedDF = scs.toDataSet(getHiveContext(), newResults, sourceSchema);

            // Pull out just the valid or invalid records
            DataSet invalidDF = null;
            if (useDirectInsert) {
                invalidDF = validatedDF.filter(VALID_INVALID_COL + " = '0'").drop(VALID_INVALID_COL).toDF();
            } else {
                invalidDF = validatedDF.filter(VALID_INVALID_COL + " = '0'").drop(VALID_INVALID_COL).drop(PROCESSING_DTTM_COL).toDF();
            }
            //invalidDF.show(1);
            writeToTargetTable(invalidDF, invalidTableName);

            // Write out the valid records (dropping our two columns)
            DataSet validDF = null;
            if (useDirectInsert) {
                validDF = validatedDF.filter(VALID_INVALID_COL + " = '1'").drop(VALID_INVALID_COL).drop(REJECT_REASON_COL).toDF();
            } else {
                validDF = validatedDF.filter(VALID_INVALID_COL + " = '1'").drop(VALID_INVALID_COL).drop(REJECT_REASON_COL).drop(PROCESSING_DTTM_COL).toDF();
            }
            writeToTargetTable(validDF, validTableName);

            long invalidCount = invalidDF.count();
            long validCount = validDF.count();

            newResults.unpersist();

            log.info("Valid count {} invalid count {}", validCount, invalidCount);

            // Record the validation stats
            writeStatsToProfileTable(validCount, invalidCount, fieldInvalidCounts);
            javaSparkContext.close();


        } catch (Exception e) {
            log.error("Failed to perform validation", e);
            System.exit(1);
        }
    }

    protected String toSelectFields(FieldPolicy[] policies1) {
        List<String> fields = new ArrayList<>();
        log.info("Building select statement for # of policies {}",policies1.length);
        for (int i = 0; i < policies1.length; i++) {
            if (policies1[i].getField() != null) {
                log.info("policy [{}] name {} feedName", i, policies1[i].getField(), policies1[i].getFeedField());
                String feedField = StringUtils.defaultIfEmpty(policies1[i].getFeedField(), policies1[i].getField());
                fields.add("`" + feedField + "` as `" + policies1[i].getField()+"`");
            }
        }
        fields.add("`processing_dttm`");
        return StringUtils.join(fields.toArray(new String[0]), ",");
    }

    private String toSelectFields() {
        return toSelectFields(this.policies);
    }

    private void writeStatsToProfileTable(long validCount, long invalidCount, Integer[] fieldInvalidCounts) {

        try {
            // Create a temporary table we can use to copy data from. Writing directly to our partition from a spark dataframe doesn't work.
            String tempTable = profileTableName + "_" + System.currentTimeMillis();

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
                    String csvRow = schema[i].getName() + ",INVALID_COUNT," + Integer.toString(fieldInvalidCounts[i]);
                    csvRows.add(csvRow);
                }
            }

            JavaSparkContext jsc = new JavaSparkContext(SparkContext.getOrCreate());
            JavaRDD<Row> statsRDD = jsc.parallelize(csvRows)
                .map(new Function<String, Row>() {
                    @Override
                    public Row call(String s) throws Exception {
                        return RowFactory.create(s.split("\\,"));
                    }
                });

            DataSet df = scs.toDataSet(getHiveContext(), statsRDD, statsSchema);
            df.registerTempTable(tempTable);

            String insertSQL = "INSERT OVERWRITE TABLE " + qualifiedProfileName
                               + " PARTITION (processing_dttm='" + partition + "')"
                               + " SELECT columnname, metrictype, metricvalue FROM " + HiveUtils.quoteIdentifier(tempTable);
            log.info("Writing profile stats {}", insertSQL);

            log.info("Writing profile stats {}", insertSQL);
            scs.sql(getHiveContext(), insertSQL);
        } catch (Exception e) {
            log.error("Failed to insert validation stats", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new RDD schema based on the source schema plus two additional columns for validation code and reason
     */
    private StructType createModifiedSchema(String sourceTable) {
        // Extract schema from the source table
        StructType schema = scs.toDataSet(getHiveContext(), feedTablename).schema();
        StructField[] fields = schema.fields();
        List<StructField> fieldsList = new Vector<>();
        for (int i = 0; i < fields.length; i++) {
            if (policyMap.containsKey(fields[i].name().toLowerCase())) {
                log.info("Adding field {}",fields[i].name());
                fieldsList.add(fields[i]);
            }
        }
        //Collections.addAll(fieldsList, fields);
        // Insert our two custom fields before the processing partition column
        fieldsList.add(new StructField(PROCESSING_DTTM_COL, DataTypes.StringType, true, Metadata.empty()));
        fieldsList.add(fieldsList.size() - 1, new StructField(VALID_INVALID_COL, DataTypes.StringType, true, Metadata.empty()));
        fieldsList.add(fieldsList.size() - 1, new StructField(REJECT_REASON_COL, DataTypes.StringType, true, Metadata.empty()));

        return new StructType(fieldsList.toArray(new StructField[0]));
    }


    private void writeToTargetTable(DataSet sourceDF, String targetTable) throws Exception {
        final String qualifiedTable = HiveUtils.quoteIdentifier(targetDatabase, targetTable);

        // Direct insert into the table partition vs. writing into a temporary table
        if (useDirectInsert) {

            getHiveContext().setConf("hive.exec.dynamic.partition", "true");
            getHiveContext().setConf("hive.exec.dynamic.partition.mode", "nonstrict");
            // Required for ORC and Parquet
            getHiveContext().setConf("set hive.optimize.index.filter", "false");

            sourceDF.writeToTable(PROCESSING_DTTM_COL, qualifiedTable);
            return;
        } else {
            // Legacy
            // Create a temporary table we can use to copy data from. Writing directly to our partition from a spark dataframe doesn't work.
            String tempTable = targetTable + "_" + System.currentTimeMillis();
            sourceDF.registerTempTable(tempTable);

            // Insert the data into our partition
            final String sql = "INSERT OVERWRITE TABLE " + qualifiedTable + " PARTITION (processing_dttm='" + partition + "') SELECT * FROM " + HiveUtils.quoteIdentifier(tempTable);
            log.info("Writing to target {}", sql);
            scs.sql(getHiveContext(), sql);
        }
    }

    /**
     * Spark function to perform both cleansing and validation of a data row based on data policies and the target datatype
     */

    public Row cleanseAndValidateRow(Row row) {
        int nulls = 1;

        // Create placeholder for our new values plus two columns for validation and reject_reason
        Object[] newValues = new Object[schema.length + 2];
        boolean valid = true;
        String sbRejectReason = null;
        List<ValidationResult> results = null;
        // Iterate through columns to cleanse and validate
        for (int idx = 0; idx < schema.length; idx++) {
            ValidationResult result = VALID_RESULT;
            FieldPolicy fieldPolicy = policies[idx];
            HCatDataType dataType = schema[idx];

            // Extract the value (allowing for null or missing field for odd-ball data)
            Object val = (idx == row.length() || row.isNullAt(idx) ? null : row.get(idx));
            // Handle complex types by passing them through

            if (dataType.isUnchecked() || (!(val instanceof String))) {
                if (val == null) {
                    nulls++;
                }
                //log.info("Unchecked type with val {}", val);
                newValues[idx] = val;
            } else {
                //log.info("Checked type with type {} val {}", dataType.getName(), val);
                String fieldValue = (val != null ? val.toString() : null);
                if (StringUtils.isEmpty(fieldValue)) {
                    nulls++;
                }
                // Perform cleansing operations
                fieldValue = standardizeField(fieldPolicy, fieldValue);
                newValues[idx] = fieldValue;

                // Record results in our appended columns
                result = validateField(fieldPolicy, dataType, fieldValue);
                if (!result.isValid()) {
                    valid = false;
                    results = (results == null ? new Vector<ValidationResult>() : results);
                    results.add(result);
                    // Record fact that we had an invalid column
                    accumList.get(idx).add(1);
                }
            }
        }
        // Return success unless all values were null.  That would indicate a blank line in the file
        if (nulls >= schema.length) {
            valid = false;
            results = (results == null ? new Vector<ValidationResult>() : results);
            results.add(ValidationResult.failRow("empty", "Row is empty"));
        }

        // Convert to reject reasons to JSON
        sbRejectReason = toJSONArray(results);

        // Record the results in our appended columns, move processing partition value last
        newValues[schema.length + 1] = newValues[schema.length - 1];
        newValues[schema.length] = sbRejectReason;
        newValues[schema.length - 1] = (valid ? "1" : "0");
        // log.info("Creating row with values {}", newValues);
        return RowFactory.create(newValues);
    }

    private String toJSONArray(List<ValidationResult> results) {
        // Convert to reject reasons to JSON
        StringBuffer sb = null;
        if (results != null) {
            sb = new StringBuffer();
            for (ValidationResult result : results) {
                if (sb.length() > 0) {
                    sb.append(",");
                } else {
                    sb.append("[");
                }
                sb.append(result.toJSON());
            }
            sb.append("]");
        }
        return (sb == null ? "" : sb.toString());
    }

    /**
     * Perform validation using both schema validation our validation policies
     */
    protected ValidationResult validateField(FieldPolicy fieldPolicy, HCatDataType fieldDataType, String fieldValue) {

        boolean isEmpty = (StringUtils.isEmpty(fieldValue));
        if (isEmpty) {
            if (!fieldPolicy.isNullable()) {
                return ValidationResult.failField("null", fieldDataType.getName(), "Cannot be null");
            }
        } else {

            // Verify new value is compatible with the target Hive schema e.g. integer, double (unless checking is disabled)
            if (!fieldPolicy.shouldSkipSchemaValidation()) {
                if (!fieldDataType.isValueConvertibleToType(fieldValue)) {
                    return ValidationResult
                        .failField("incompatible", fieldDataType.getName(),
                                   "Not convertible to " + fieldDataType.getNativeType());
                }
            }

            // Validate type using provided validators
            List<ValidationPolicy> validators = fieldPolicy.getValidators();
            if (validators != null) {
                for (ValidationPolicy validator : validators) {
                    ValidationResult result = validateValue(validator, fieldDataType, fieldValue);
                    if (result != VALID_RESULT) {
                        return result;
                    }
                }
            }
        }
        return VALID_RESULT;
    }

    protected ValidationResult validateValue(ValidationPolicy validator, HCatDataType fieldDataType, String fieldValue) {
        try {
            // Resolve the type of parameter required by the validator. We use a cache to avoid cost of reflection
            Class expectedParamClazz = resolveValidatorParamType(validator);
            Object nativeValue = fieldValue;
            if (expectedParamClazz != String.class) {
                nativeValue = fieldDataType.toNativeValue(fieldValue);
            }
            if (!validator.validate(nativeValue)) {
                return ValidationResult
                    .failFieldRule("rule", fieldDataType.getName(), validator.getClass().getSimpleName(),
                                   "Rule violation");
            }
            return VALID_RESULT;
        } catch (InvalidFormatException | ClassCastException e) {
            return ValidationResult
                .failField("incompatible", fieldDataType.getName(),
                           "Not convertible to " + fieldDataType.getNativeType());
        }

    }

    /* Resolve the type of param required by the validator. We use a cache to avoid cost of reflection */
    protected Class resolveValidatorParamType(ValidationPolicy validator) {
        Class expectedParamClazz = validatorParamType.get(validator.getClass());
        if (expectedParamClazz == null) {
            // Cache for future references

            Object t = validator.getClass().getGenericInterfaces()[0];
            if (t instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) t;
                expectedParamClazz = (Class) type.getActualTypeArguments()[0];
            } else {
                expectedParamClazz = String.class;
            }
            validatorParamType.put(validator.getClass(), expectedParamClazz);
        }
        return expectedParamClazz;
    }

    /**
     * Applies the standardization policies
     */
    protected String standardizeField(FieldPolicy fieldPolicy, String value) {
        String newValue = value;
        List<StandardizationPolicy> standardizationPolicies = fieldPolicy.getStandardizationPolicies();
        if (standardizationPolicies != null) {
            boolean isEmpty = (StringUtils.isEmpty(value));
            for (StandardizationPolicy standardizationPolicy : standardizationPolicies) {
                if (isEmpty && !(standardizationPolicy instanceof AcceptsEmptyValues)) {
                    continue;
                }
                newValue = standardizationPolicy.convertValue(newValue);
            }
        }
        return newValue;
    }

    /**
     * Converts the table schema into our corresponding data type structures
     */
    protected HCatDataType[] resolveDataTypes(StructField[] fields) {
        List<HCatDataType> cols = new Vector<>();

        for (StructField field : fields) {
            // log.info("Found field {}",field);
            String colName = field.name();
            String dataType = field.dataType().simpleString();
            cols.add(HCatDataType.createFromDataType(colName, dataType));
        }
        return cols.toArray(new HCatDataType[0]);
    }

    protected StructField[] resolveSchema() {
        StructType schema = scs.toDataSet(hiveContext, refTablename).schema();
        return schema.fields();
    }

    /**
     * Returns an array of field-level policies for data validation and cleansing
     */
    protected FieldPolicy[] resolvePolicies(StructField[] fields) {
        List<FieldPolicy> pols = new Vector<>();

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

    private void addParameters(CommandLineParams params) {
        this.params = params;
    }

    static CommandLineParams parseRemainingParameters(String[] args, int from) {
        CommandLineParams params = new CommandLineParams();
        new JCommander(params, Arrays.copyOfRange(args, from, args.length));
        return params;
    }

    public static void main(String[] args) {
        log.info("Running Spark Validator with the following command line args (comma separated):" + StringUtils.join(args, ","));

        // Check how many arguments were passed in
        if (args.length < 4) {
            System.out.println("Proper Usage is: <targetDatabase> <entity> <partition> <path-to-policy-file>");
            System.out.println("You can optionally add: --hiveConf hive.setting=value --hiveConf hive.other.setting=value");
            System.out.println("You provided " + args.length + " args which are (comma separated): " + StringUtils.join(args, ","));
            System.exit(1);
        }
        try {
            ApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark");
            Validator app = ctx.getBean(Validator.class);
            app.setArguments(args[0], args[1], args[2], args[3]);
            app.addParameters(parseRemainingParameters(args, 4));
            app.doValidate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}

