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
            String sql = "SELECT " + selectStmt + " FROM " + feedTablename + " WHERE processing_dttm = '" + partition + "'";
            log.info("Executing query {}", sql);
            DataSet dataFrame = scs.sql(getHiveContext(), sql);
            JavaRDD<Row> rddData = dataFrame.javaRDD();

            // Extract schema from the source table
            StructType sourceSchema = createModifiedSchema(feedTablename);
            log.info("sourceSchema {}", sourceSchema);

            log.info("Persistence level: {}", params.getStorageLevel());

            // Validate and cleanse input rows
            JavaRDD<CleansedRowResult> cleansedRowResultRDD;
            if (params.getNumPartitions() <= 0) {
                cleansedRowResultRDD = rddData.map(new Function<Row, CleansedRowResult>() {
                    @Override
                    public CleansedRowResult call(Row row) throws Exception {
                        return cleanseAndValidateRow(row);
                    }
                }).persist(StorageLevel.fromString(params.getStorageLevel()));
            } else {
                log.info("Partition count: " + params.getNumPartitions());
                cleansedRowResultRDD = rddData.repartition(params.getNumPartitions()).map(new Function<Row, CleansedRowResult>() {
                    @Override
                    public CleansedRowResult call(Row row) throws Exception {
                        return cleanseAndValidateRow(row);
                    }
                }).persist(StorageLevel.fromString(params.getStorageLevel()));
            }

            // Return a new rdd based on whether values are valid or invalid
            JavaRDD<Row> newResultsRDD = cleansedRowResultRDD.map(new Function<CleansedRowResult, Row>() {
                @Override
                public Row call(CleansedRowResult cleansedRowResult) throws Exception {
                    return cleansedRowResult.row;
                }
            });

            // Counts of invalid columns, total valid rows and total invalid rows
            long[] fieldInvalidCounts = cleansedRowResultsValidationCounts(cleansedRowResultRDD, schema.length);

            final DataSet validatedDF = scs.toDataSet(getHiveContext(), newResultsRDD, sourceSchema);

            // Pull out just the valid or invalid records
            DataSet invalidDF = null;
            if (useDirectInsert) {
                invalidDF = validatedDF.filter(VALID_INVALID_COL + " = '0'").drop(VALID_INVALID_COL).toDF();
            } else {
                invalidDF = validatedDF.filter(VALID_INVALID_COL + " = '0'").drop(VALID_INVALID_COL).drop(PROCESSING_DTTM_COL).toDF();
            }
            writeToTargetTable(invalidDF, invalidTableName);

            // Write out the valid records (dropping the two columns)
            DataSet validDF = null;
            if (useDirectInsert) {
                validDF = validatedDF.filter(VALID_INVALID_COL + " = '1'").drop(VALID_INVALID_COL).drop(REJECT_REASON_COL).toDF();
            } else {
                validDF = validatedDF.filter(VALID_INVALID_COL + " = '1'").drop(VALID_INVALID_COL).drop(REJECT_REASON_COL).drop(PROCESSING_DTTM_COL).toDF();
            }
            writeToTargetTable(validDF, validTableName);

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

    /**
     * Creates a new RDD schema based on the source schema plus two additional columns for validation code and reason
     */
    private StructType createModifiedSchema(String sourceTable) {
        // Extract schema from the source table
        StructType schema = scs.toDataSet(getHiveContext(), feedTablename).schema();
        StructField[] fields = schema.fields();
        List<StructField> fieldsList = new Vector<>();
        for (int i = 0; i < fields.length; i++) {
            //Build a list of feed field names using the policy map
            List<String> policyMapFeedFieldNames = new ArrayList<>();

            for (Map.Entry<String, FieldPolicy> policyMapItem : policyMap.entrySet()) {
                policyMapFeedFieldNames.add(policyMapItem.getValue().getFeedField().toLowerCase());
            }

            if (policyMapFeedFieldNames.contains(fields[i].name().toLowerCase())) {
                log.info("Adding field {}", fields[i].name());
                fieldsList.add(fields[i]);
            } else {
                log.warn("Feed table field {} is not present in policy map", fields[i].name().toLowerCase());
            }
        }

        // Insert the two custom fields before the processing partition column
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
    public CleansedRowResult cleanseAndValidateRow(Row row) {
        int nulls = 1;

        // Create placeholder for the new values plus two columns for validation and reject_reason
        Object[] newValues = new Object[schema.length + 2];
        boolean rowValid = true;
        String sbRejectReason = null;
        List<ValidationResult> results = null;
        boolean[] columnsValid = new boolean[schema.length];

        // Iterate through columns to cleanse and validate
        for (int idx = 0; idx < schema.length; idx++) {
            ValidationResult result = VALID_RESULT;
            FieldPolicy fieldPolicy = policies[idx];
            HCatDataType dataType = schema[idx];
            boolean columnValid = true;

            // Extract the value (allowing for null or missing field for odd-ball data)
            Object val = (idx == row.length() || row.isNullAt(idx) ? null : row.get(idx));
            // Handle complex types by passing them through

            if (dataType.isUnchecked()) {
                if (val == null) {
                    nulls++;
                }
                newValues[idx] = val;
            } else {
                Object fieldValue = (val != null ? val : null);

                if (fieldValue == null) {
                    nulls++;
                }

                StandardizationAndValidationResult standardizationAndValidationResult = standardizeAndValidateField(fieldPolicy, fieldValue, dataType);
                fieldValue = standardizationAndValidationResult.getFieldValue();
                result = standardizationAndValidationResult.getFinalValidationResult();
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

        // Convert to reject reasons to JSON
        sbRejectReason = toJSONArray(results);

        // Record the results in the appended columns, move processing partition value last
        newValues[schema.length + 1] = newValues[schema.length - 1];
        newValues[schema.length] = sbRejectReason;
        newValues[schema.length - 1] = (rowValid ? "1" : "0");

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
    protected ValidationResult finalValidationCheck(FieldPolicy fieldPolicy, HCatDataType fieldDataType, String fieldValue) {

        boolean isEmpty = (StringUtils.isEmpty(fieldValue));
        if (isEmpty) {
            ValidationPolicy validator;
            if ((validator = fieldPolicy.getNotNullValidator()) != null) {
                ValidationResult result = validateValue(validator, fieldDataType, fieldValue, -1);
                if (result != VALID_RESULT) {
                    return result;
                }
            }
        } else if (!fieldPolicy.shouldSkipSchemaValidation()) {
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


    protected ValidationResult validateValue(ValidationPolicy validator, HCatDataType fieldDataType, String fieldValue, Integer idx) {
        try {
            // Resolve the type of parameter required by the validator. A cache is used to avoid cost of reflection.
            Class expectedParamClazz = resolveValidatorParamType(validator);
            Object nativeValue = fieldValue;
            if (expectedParamClazz != String.class) {
                nativeValue = fieldDataType.toNativeValue(fieldValue);
            }
            if (!validator.validate(nativeValue)) {

                //get any fields in this validator annotated with PolicyProperty

                return ValidationResult
                    .failFieldRule("rule", fieldDataType.getName(), validator.getClass().getSimpleName(),
                                   "Rule violation" + idx != null && idx >= 0 ? " for rule at index " + idx : "");
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
        int idx = 0;
        for (BaseFieldPolicy p : fieldPolicies) {
            if (p instanceof StandardizationPolicy) {
                StandardizationPolicy standardizationPolicy = (StandardizationPolicy) p;
                boolean isEmpty = ((value == null) || (StringUtils.isEmpty(value.toString())));
                boolean shouldStandardize = true;
                if (isEmpty && !(standardizationPolicy instanceof AcceptsEmptyValues)) {
                    shouldStandardize = false;
                }

                if (!standardizationPolicy.accepts(value)) {
                    shouldStandardize = false;
                }

                if (shouldStandardize) {
                    Object newValue = standardizationPolicy.convertRawValue(result.getFieldValue());
                    result.setFieldValue(newValue);
                }
            }

            if (p instanceof ValidationPolicy) {

                ValidationPolicy validationPolicy = (ValidationPolicy) p;
                ValidationResult validationResult = validateValue(validationPolicy, dataType, result.getFieldValueForValidation(), idx);
                //only need to add those that are invalid
                if (validationResult != VALID_RESULT) {
                    result.addValidationResult(validationResult);
                }
            }

        }
        ValidationResult finalValidationCheck = finalValidationCheck(fieldPolicy, dataType, result.getFieldValueForValidation());
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