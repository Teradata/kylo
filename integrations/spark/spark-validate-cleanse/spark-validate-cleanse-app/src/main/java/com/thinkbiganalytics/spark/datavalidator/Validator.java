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
import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.annotations.AnnotationFieldNameResolver;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.policy.BaseFieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicyBuilder;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.standardization.AcceptsEmptyValues;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.NotNullValidator;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationResult;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.datavalidator.functions.SumPartitionLevelCounts;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;
import com.thinkbiganalytics.spark.util.InvalidFormatException;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
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
 * Cleanses and validates a table of strings according to defined field-level policies. Records are split into good and bad.
 * <p> blog.cloudera.com/blog/2015/07/how-to-do-data-quality-checks-using-apache-spark-dataframes/
 */
@Component
public class Validator implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    @Autowired
    IValidatorStrategy validatorStrategy;

    public void setValidatorStrategy(IValidatorStrategy strategy) {
        this.validatorStrategy = strategy;
    }

    /*
    Valid validation result
     */
    protected static ValidationResult VALID_RESULT = new ValidationResult();
    private static String REJECT_REASON_COL = "dlp_reject_reason";
    private static String VALID_INVALID_COL = "dlp_valid";
    private static String PROCESSING_DTTM_COL = "processing_dttm";

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

    private Map<String, HCatDataType> refTableSchemaNameHCatDataTypeMap = new HashMap<>();
    private Map<String, FieldPolicy> policyMap = new HashMap<>();
    /*
    Cache for performance. Validators accept different parameters (numeric,string, etc) so we need to resolve the type using reflection
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
    private CommandLineParams params;

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
            System.out.println("You can optionally add: --storageLevel rdd_persistence_level_value");
            System.out.println("You can optionally add: --numPartitions number_of_rdd_partitions");
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
            hiveContext = new HiveContext(sparkContext);

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
            for (HCatDataType dataType : this.schema) {
                refTableSchemaNameHCatDataTypeMap.put(dataType.getName(), dataType);
            }

            String selectStmt = toSelectFields();
            String sql = "SELECT " + selectStmt + " FROM " + feedTablename + " WHERE processing_dttm = '" + partition + "'";
            log.info("Executing query {}", sql);
            DataSet sourceDF = scs.sql(getHiveContext(), sql);
            JavaRDD<Row> sourceRDD = sourceDF.javaRDD();

            ModifiedSchema modifiedSchema = new ModifiedSchema(feedTablename, refTablename);

            // Extract schema from the source table.  This will be used for the invalidDataFrame
            StructType invalidSchema = modifiedSchema.getInvalidTableSchema();

            //Extract the schema from the target table.  This will be used for the validDataFrame
            StructType validSchema = modifiedSchema.getValidTableSchema();

            log.info("invalidSchema {}", invalidSchema);

            log.info("validSchema {}", validSchema);

            log.info("Persistence level: {}", params.getStorageLevel());

            // Validate and cleanse input rows
            JavaRDD<CleansedRowResult> cleansedRowResultRDD;
            if (params.getNumPartitions() <= 0) {
                cleansedRowResultRDD = sourceRDD.map(new Function<Row, CleansedRowResult>() {
                    @Override
                    public CleansedRowResult call(Row row) throws Exception {
                        return cleanseAndValidateRow(row);
                    }
                }).persist(StorageLevel.fromString(params.getStorageLevel()));
            } else {
                log.info("Partition count: " + params.getNumPartitions());
                cleansedRowResultRDD = sourceRDD.repartition(params.getNumPartitions()).map(new Function<Row, CleansedRowResult>() {
                    @Override
                    public CleansedRowResult call(Row row) throws Exception {
                        return cleanseAndValidateRow(row);
                    }
                }).persist(StorageLevel.fromString(params.getStorageLevel()));
            }

            // Return a new rdd based for Valid Results
            JavaRDD<Row> validResultRDD = cleansedRowResultRDD.filter(new Function<CleansedRowResult, Boolean>() {
                @Override
                public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                    return cleansedRowResult.rowIsValid;
                }
            }).map(new Function<CleansedRowResult, Row>() {
                @Override
                public Row call(CleansedRowResult cleansedRowResult) throws Exception {
                    return cleansedRowResult.row;
                }
            });

            // Return a new rdd based for Invalid Results
            JavaRDD<Row> invalidResultRDD = cleansedRowResultRDD.filter(new Function<CleansedRowResult, Boolean>() {
                @Override
                public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                    return cleansedRowResult.rowIsValid == false;
                }
            }).map(new Function<CleansedRowResult, Row>() {
                @Override
                public Row call(CleansedRowResult cleansedRowResult) throws Exception {
                    return cleansedRowResult.row;
                }
            });

            // Counts of invalid columns, total valid rows and total invalid rows
            long[] fieldInvalidCounts = cleansedRowResultsValidationCounts(cleansedRowResultRDD, schema.length);

            //Create the 2 new Data Frames for the invalid and valid results
            final DataSet invalidDF = scs.toDataSet(getHiveContext(), invalidResultRDD, invalidSchema);

            final DataSet validatedDF = scs.toDataSet(getHiveContext(), validResultRDD, validSchema);

            DataSet invalidDataFrame = null;
            // ensure the dataframe matches the correct schema
            if (useDirectInsert) {
                invalidDataFrame = invalidDF;
            } else {
                invalidDataFrame = invalidDF.drop(PROCESSING_DTTM_COL).toDF();
            }

            writeToTargetTable(invalidDataFrame, invalidTableName);

            log.info("wrote values to the invalid Table  {}", invalidTableName);

            // Write out the valid records (dropping the two columns)
            DataSet validDataFrame = null;
            if (useDirectInsert) {
                validDataFrame = validatedDF.drop(REJECT_REASON_COL).toDF();
            } else {
                validDataFrame = validatedDF.drop(REJECT_REASON_COL).drop(PROCESSING_DTTM_COL).toDF();
            }
            //Remove the columns from _valid that dont exist in the validTableName

            writeToTargetTable(validDataFrame, validTableName);

            log.info("wrote values to the valid Table  {}", validTableName);

            long validCount = fieldInvalidCounts[schema.length];
            long invalidCount = fieldInvalidCounts[schema.length + 1];

            cleansedRowResultRDD.unpersist();

            log.info("Valid count {} invalid count {}", validCount, invalidCount);

            // Record the validation stats
            writeStatsToProfileTable(validCount, invalidCount, fieldInvalidCounts);

        } catch (Exception e) {
            log.error("Failed to perform validation", e);
            System.exit(1);
        }
    }

    protected String toSelectFields(FieldPolicy[] policies1) {
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

    private String toSelectFields() {
        return toSelectFields(this.policies);
    }

    private void writeStatsToProfileTable(long validCount, long invalidCount, long[] fieldInvalidCounts) {

        try {
            // Create a temporary table that can be used to copy data from. Writing directly to the partition from a spark dataframe doesn't work.
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
                    String csvRow = schema[i].getName() + ",INVALID_COUNT," + Long.toString(fieldInvalidCounts[i]);
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
            scs.sql(getHiveContext(), insertSQL);
        } catch (Exception e) {
            log.error("Failed to insert validation stats", e);
            throw new RuntimeException(e);
        }
    }


    private class ModifiedSchema {

        /**
         * the name of the feed table
         */
        private String feedTable;

        /**
         * the name of the valid table
         */
        private String validTable;

        /**
         * A map of the feedFieldName to validFieldName
         */
        Map<String, String> feedFieldToValidFieldMap = new HashMap<>();


        /**
         * A map of the feedFieldName to validFieldName
         */
        Map<String, String> validFieldToFeedFieldMap = new HashMap<>();

        /**
         * List of all the feedFieldNames that are part of the policyMap
         */
        List<String> policyMapFeedFieldNames = new ArrayList<>();

        /**
         * List of all the validFieldNames that are part of the policyMap
         */
        List<String> policyMapValidFieldNames = new ArrayList<>();

        /**
         * List of all those validFieldNames that have a standardizer on them
         */
        List<String> validFieldsWithStandardizers = new ArrayList<>();

        /**
         * Map of the lower feed Field name to the field type
         */
        Map<String, StructField> feedFieldsMap = new HashMap<>();

        /**
         * Map of the lower feed valid name to the field type
         */
        Map<String, StructField> validFieldsMap = new HashMap<>();

        private StructType invalidTableSchema;

        private StructType validTableSchema;

        public ModifiedSchema(String feedTable, String validTable) {
            this.feedTable = feedTable;
            this.validTable = validTable;
            buildSchemas();
        }

        private void initializeMetadata() {
            for (Map.Entry<String, FieldPolicy> policyMapItem : policyMap.entrySet()) {
                String feedFieldName = policyMapItem.getValue().getFeedField().toLowerCase();
                String fieldName = policyMapItem.getValue().getField().toLowerCase();
                policyMapFeedFieldNames.add(feedFieldName);
                policyMapValidFieldNames.add(fieldName);
                feedFieldToValidFieldMap.put(feedFieldName, fieldName);
                validFieldToFeedFieldMap.put(fieldName, feedFieldName);
                if (policyMapItem.getValue().hasStandardizationPolicies()) {
                    validFieldsWithStandardizers.add(fieldName);
                }
            }
        }

        private StructType buildFeedSchema(StructType feedTableSchema) {
            List<StructField> feedFieldsList = new Vector<>();

            StructField[] feedFields = feedTableSchema.fields();
            for (int i = 0; i < feedFields.length; i++) {
                String lowerFieldName = feedFields[i].name().toLowerCase();
                StructField field = feedFields[i];

                if (policyMapFeedFieldNames.contains(lowerFieldName)) {
                    addToList(field, feedFieldsList);
                    feedFieldsMap.put(lowerFieldName, field);
                } else {
                    log.warn("Feed table field {} is not present in policy map", lowerFieldName);
                }

            }
            return finalizeFieldsList(feedFieldsList);
        }

        private StructType buildValidSchema(StructType feedTableSchema, StructType validTableSchema) {
            List<StructField> fieldsList = new Vector<>();

            StructField[] validFields = validTableSchema.fields();
            for (int i = 0; i < validFields.length; i++) {
                String lowerFieldName = validFields[i].name().toLowerCase();
                StructField field = validFields[i];
                validFieldsMap.put(lowerFieldName, field);
            }

            StructField[] feedFields = feedTableSchema.fields();
            for (int i = 0; i < feedFields.length; i++) {
                String lowerFeedFieldName = feedFields[i].name().toLowerCase();
                if (policyMapFeedFieldNames.contains(lowerFeedFieldName)) {
                    StructField field = feedFields[i];
                    //get the corresponding valid table field name
                    String lowerFieldName = validFieldToFeedFieldMap.get(lowerFeedFieldName);
                    //if we are standardizing then use the field type matching the _valid table
                    if (validFieldsWithStandardizers.contains(lowerFieldName)) {
                        //get the valid table
                        field = validFieldsMap.get(lowerFieldName);
                        HCatDataType dataType = refTableSchemaNameHCatDataTypeMap.get(lowerFeedFieldName);
                        if (dataType != null && dataType.isDateOrTimestamp()) {
                            field = new StructField(field.name(), DataTypes.StringType, field.nullable(), field.metadata());
                        }
                    }
                    addToList(field, fieldsList);
                } else {
                    log.warn("Valid table field {} is not present in policy map", lowerFeedFieldName);
                }

            }
            return finalizeFieldsList(fieldsList);
        }

        private void addToList(StructField field, List<StructField> fieldList) {
            if (!field.name().equalsIgnoreCase(PROCESSING_DTTM_COL) && !field.name().equalsIgnoreCase(REJECT_REASON_COL)) {
                fieldList.add(field);
            }
        }

        /**
         * Add in the REJECT_REASON_COL to the fields list after the PROCESSING_DTTM
         */
        private StructType finalizeFieldsList(List<StructField> fieldsList) {
            // Insert the two custom fields before the processing partition column
            fieldsList.add(new StructField(PROCESSING_DTTM_COL, DataTypes.StringType, true, Metadata.empty()));
            //  fieldsList.add(fieldsList.size() - 1, new StructField(VALID_INVALID_COL, DataTypes.StringType, true, Metadata.empty()));
            fieldsList.add(fieldsList.size() - 1, new StructField(REJECT_REASON_COL, DataTypes.StringType, true, Metadata.empty()));

            return new StructType(fieldsList.toArray(new StructField[0]));
        }


        public void buildSchemas() {

            initializeMetadata();

            StructType feedTableSchema = scs.toDataSet(getHiveContext(), feedTable).schema();
            StructType validTableSchema = scs.toDataSet(getHiveContext(), validTable).schema();

            this.invalidTableSchema = buildFeedSchema(feedTableSchema);
            this.validTableSchema = buildValidSchema(feedTableSchema, validTableSchema);

        }

        public StructType getInvalidTableSchema() {
            return invalidTableSchema;
        }

        public void setInvalidTableSchema(StructType invalidTableSchema) {
            this.invalidTableSchema = invalidTableSchema;
        }

        public StructType getValidTableSchema() {
            return validTableSchema;
        }

        public void setValidTableSchema(StructType validTableSchema) {
            this.validTableSchema = validTableSchema;
        }
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
            // Legacy way: Create a temporary table we can use to copy data from. Writing directly to the partition from a spark dataframe doesn't work.
            String tempTable = targetTable + "_" + System.currentTimeMillis();
            sourceDF.registerTempTable(tempTable);

            // Insert the data into the partition
            final String sql = "INSERT OVERWRITE TABLE " + qualifiedTable + " PARTITION (processing_dttm='" + partition + "') SELECT * FROM " + HiveUtils.quoteIdentifier(tempTable);
            log.info("Writing to target {}", sql);
            scs.sql(getHiveContext(), sql);
        }
    }

    /**
     * Spark function to perform both cleansing and validation of a data row based on data policies and the target datatype
     */
    private CleansedRowResult cleanseAndValidateRow(Row row) {
        int nulls = 1;

        // Create placeholder for the new values plus one columns for reject_reason
        Object[] newValues = new Object[schema.length + 1];
        boolean rowValid = true;
        String sbRejectReason = null;
        List<ValidationResult> results = null;
        boolean[] columnsValid = new boolean[schema.length];

        Map<Integer, Object> originalValues = new HashMap<>();

        // Iterate through columns to cleanse and validate
        for (int idx = 0; idx < schema.length; idx++) {
            ValidationResult result = VALID_RESULT;
            FieldPolicy fieldPolicy = policies[idx];
            HCatDataType dataType = schema[idx];
            boolean columnValid = true;
            boolean isBinaryType = dataType.getConvertibleType().equals(byte[].class);

            // Extract the value (allowing for null or missing field for odd-ball data)
            Object val = (idx == row.length() || row.isNullAt(idx) ? null : row.get(idx));
            // Handle complex types by passing them through

            if (dataType.isUnchecked()) {
                if (val == null) {
                    nulls++;
                }
                newValues[idx] = val;
                originalValues.put(idx, val);
            } else {
                Object fieldValue = (val != null ? val : null);
                boolean isEmpty = ((fieldValue == null) || (StringUtils.isEmpty(fieldValue.toString())));

                if (fieldValue == null) {
                    nulls++;
                }
                originalValues.put(idx, fieldValue);

                StandardizationAndValidationResult standardizationAndValidationResult = standardizeAndValidateField(fieldPolicy, fieldValue, dataType);
                result = standardizationAndValidationResult.getFinalValidationResult();

                //only apply the standardized result value if the routine is valid
                fieldValue = result.isValid() ? standardizationAndValidationResult.getFieldValue() : fieldValue;

                //reevaluate the isEmpty flag
                isEmpty = ((fieldValue == null) || (StringUtils.isEmpty(fieldValue.toString())));

                //if the field is a binary type, but cant be converted set it to null.
                //hive will auto convert byte[] or String fields to a target binary type.
                if (result.isValid() && isBinaryType && !(fieldValue instanceof byte[]) && !(fieldValue instanceof String)) {
                    //set it to null
                    fieldValue = null;
                } else if ((dataType.isNumeric() || isBinaryType) && isEmpty ) {
                    //if its a numeric column and the field is empty then set it to null as well
                    fieldValue = null;
                }
                newValues[idx] = fieldValue;

                if (!result.isValid()) {
                    rowValid = false;
                    results = (results == null ? new Vector<ValidationResult>() : results);
                    results.addAll(standardizationAndValidationResult.getValidationResults());
                    //results.add(result);
                    columnValid = false;
                }

            }

            // Record fact that we there was an invalid column
            columnsValid[idx] = columnValid;
        }
        // Return success unless all values were null.  That would indicate a blank line in the file.
        if (nulls >= schema.length) {
            rowValid = false;
            results = (results == null ? new Vector<ValidationResult>() : results);
            results.add(ValidationResult.failRow("empty", "Row is empty"));
        }

        if (rowValid == false) {
            for (int idx = 0; idx < schema.length; idx++) {
                //if the value is not able to match the invalid schema and the datatype has changed then replace with original value
                //the _invalid table schema matches the source, not the destination
                if (newValues[idx] == null || originalValues.get(idx) == null || newValues[idx].getClass() != originalValues.get(idx).getClass()) {
                    newValues[idx] = originalValues.get(idx);
                }
                //otherwise the data has changed, but its still the same data type so we can keep the newly changed value

            }
        }

        // Convert to reject reasons to JSON
        sbRejectReason = toJSONArray(results);

        // Record the results in the appended columns, move processing partition value last
        newValues[schema.length] = newValues[schema.length - 1]; //PROCESSING_DTTM_COL
        newValues[schema.length - 1] = sbRejectReason;   //REJECT_REASON_COL
        //   newValues[schema.length - 1] = (rowValid ? "1" : "0");  //VALID_INVALID_COL

        CleansedRowResult cleansedRowResult = new CleansedRowResult();
        cleansedRowResult.row = RowFactory.create(newValues);
        cleansedRowResult.columnsValid = columnsValid;
        cleansedRowResult.rowIsValid = rowValid;
        return cleansedRowResult;
    }


    /**
     * Performs counts of invalid columns, total valid and total invalid on a JavaRDD<CleansedRowResults>
     */
    public long[] cleansedRowResultsValidationCounts(JavaRDD<CleansedRowResult> cleansedRowResultJavaRDD, int schemaLength) {

        final int schemaLen = schemaLength;

        // Maps each partition in the JavaRDD<CleansedRowResults> to a long[] of invalid column counts and total valid/invalid counts
        JavaRDD<long[]> partitionCounts = validatorStrategy.getCleansedRowResultPartitionCounts(cleansedRowResultJavaRDD, schemaLen);

        // Sums up all partitions validation counts into one long[]
        long[] finalCounts = partitionCounts.reduce(new SumPartitionLevelCounts());

        return finalCounts;
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
     * Perform validation using both schema validation the validation policies
     */
    protected ValidationResult finalValidationCheck(FieldPolicy fieldPolicy, HCatDataType fieldDataType, Object fieldValue) {

        boolean isEmpty = ((fieldValue instanceof String && StringUtils.isEmpty((String) fieldValue)) || fieldValue == null);
        if (!isEmpty && !fieldPolicy.shouldSkipSchemaValidation()) {
            if (!fieldDataType.isValueConvertibleToType(fieldValue)) {
                return ValidationResult
                    .failField("incompatible", fieldDataType.getName(),
                               "Not convertible to " + fieldDataType.getNativeType());
            }
        }

        return VALID_RESULT;
    }

    /**
     * Extract the @PolicyProperty annotated fields
     *
     * @param policy the policy (validator or standardizer to parse)
     * @return a string of the fileds and values
     */
    private String getFieldPolicyDetails(BaseFieldPolicy policy) {
        //cache the list
        AnnotationFieldNameResolver annotationFieldNameResolver = new AnnotationFieldNameResolver(PolicyProperty.class);
        List<AnnotatedFieldProperty> list = annotationFieldNameResolver.getProperties(policy.getClass());
        StringBuffer sb = null;
        for (AnnotatedFieldProperty<PolicyProperty> annotatedFieldProperty : list) {
            PolicyProperty prop = annotatedFieldProperty.getAnnotation();
            String value = null;
            if (sb != null) {
                sb.append(",");
            }
            if (sb == null) {
                sb = new StringBuffer();
            }
            sb.append(StringUtils.isBlank(prop.displayName()) ? prop.name() : prop.displayName());

            try {
                Object fieldValue = FieldUtils.readField(annotatedFieldProperty.getField(), policy, true);
                if (fieldValue != null) {
                    value = fieldValue.toString();
                }
            } catch (IllegalAccessException e) {

            }
            sb.append(" = ");
            sb.append(value == null ? "<null> " : value);
        }
        return sb != null ? sb.toString() : "";
    }


    protected ValidationResult validateValue(ValidationPolicy validator, HCatDataType fieldDataType, Object fieldValue, Integer idx) {
        try {
            // Resolve the type of parameter required by the validator. A cache is used to avoid cost of reflection.
            Class expectedParamClazz = resolveValidatorParamType(validator);
            Object nativeValue = fieldValue;
            if (expectedParamClazz != String.class && fieldValue instanceof String) {
                nativeValue = fieldDataType.toNativeValue(fieldValue.toString());
            }
            if (!validator.validate(nativeValue)) {

                //get any fields in this validator annotated with PolicyProperty

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

    /* Resolve the type of param required by the validator. A cache is used to avoid cost of reflection */
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


    protected StandardizationAndValidationResult standardizeAndValidateField(FieldPolicy fieldPolicy, Object value, HCatDataType dataType) {
        StandardizationAndValidationResult result = new StandardizationAndValidationResult(value);

        List<BaseFieldPolicy> fieldPolicies = fieldPolicy.getAllPolicies();

        int standardizerCount = 0;
        int validationCount = 0;
        for (BaseFieldPolicy p : fieldPolicies) {
            if (p instanceof StandardizationPolicy) {
                standardizerCount++;
            } else {
                validationCount++;
            }
        }
        int idx = 0;
        boolean validateNullValues = false;
        int processedStandardizers = 0;
        for (BaseFieldPolicy p : fieldPolicies) {
            boolean isEmpty = ((result.getFieldValue() == null) || (StringUtils.isEmpty(result.getFieldValue().toString())));
            if (p instanceof StandardizationPolicy) {
                processedStandardizers++;
                StandardizationPolicy standardizationPolicy = (StandardizationPolicy) p;
                boolean shouldStandardize = true;
                if (isEmpty && !(standardizationPolicy instanceof AcceptsEmptyValues)) {
                    shouldStandardize = false;
                }

                if (!standardizationPolicy.accepts(result.getFieldValue())) {
                    shouldStandardize = false;
                }

                if (shouldStandardize) {
                    Object newValue = standardizationPolicy.convertRawValue(result.getFieldValue());

                    //If this is the last standardizer for this field and the standardized value is returned as a String, and target column is not String, then validate and convert it to correct type
                    if (newValue != null && dataType.getConvertibleType() != newValue.getClass() && standardizerCount == processedStandardizers) {
                        try {
                            //Date and timestamp fields can be valid as strings
                            boolean isValueOk = dataType.isStringValueValidForHiveType(newValue.toString());
                            if (!isValueOk) {
                                //if the current string is not in a correct format attempt to convert it
                                try {
                                    newValue = dataType.toNativeValue(newValue.toString());
                                } catch (RuntimeException e) {
                                    result.addValidationResult(ValidationResult
                                                                   .failField("incompatible", dataType.getName(),
                                                                              "Not convertible to " + dataType.getNativeType()));
                                }
                            }
                        } catch (InvalidFormatException e) {
                            log.warn("Could not convert value {} to correct type {}", newValue.toString(), dataType.getConvertibleType().getName());
                        }
                    }
                    result.setFieldValue(newValue);
                }
            }

            if (p instanceof ValidationPolicy) {
                ValidationPolicy validationPolicy = (ValidationPolicy) p;
                //run through the validator if the value is not null, or if we are allowed to validate nulls based upon a previous
                //not null validator
                if (!isEmpty || (isEmpty && validateNullValues) || (isEmpty && validationPolicy instanceof NotNullValidator)) {
                    ValidationResult validationResult = validateValue(validationPolicy, dataType, result.getFieldValue(), idx);
                    if (isEmpty && validationPolicy instanceof NotNullValidator) {
                        validateNullValues = validationPolicy != VALID_RESULT;
                    }
                    //only need to add those that are invalid
                    if (validationResult != VALID_RESULT) {
                        result.addValidationResult(validationResult);
                        break; //exit out of processing if invalid records found.
                    }
                }
                //reset the failOnEmpty flag back to false
                if (!(validationPolicy instanceof NotNullValidator)) {
                    validateNullValues = false;
                }
            }

        }
        ValidationResult finalValidationCheck = finalValidationCheck(fieldPolicy, dataType, result.getFieldValue());
        if (finalValidationCheck != VALID_RESULT) {
            result.addValidationResult(finalValidationCheck);
        }

        return result;
    }


    /**
     * Converts the table schema into the corresponding data type structures
     */
    protected HCatDataType[] resolveDataTypes(StructField[] fields) {
        List<HCatDataType> cols = new Vector<>();

        for (StructField field : fields) {
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
}