package com.thinkbiganalytics.spark.datavalidator;

import com.thinkbiganalytics.policy.FieldPoliciesJsonTransformer;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicyBuilder;
import com.thinkbiganalytics.policy.standardization.AcceptsEmptyValues;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationResult;
import com.thinkbiganalytics.spark.validation.HCatDataType;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.*;

/**
 * Cleanses and validates a table of strings according to defined field-level policies. Records are split into good and bad. <p>
 * blog.cloudera.com/blog/2015/07/how-to-do-data-quality-checks-using-apache-spark-dataframes/
 */
public class Validator implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    private static String REJECT_REASON_COL = "dlp_reject_reason";

    private static String VALID_INVALID_COL = "dlp_valid";

    /*
    Valid validation result
     */
    private static ValidationResult VALID_RESULT = new ValidationResult();

    /* Initialize Spark */
    private HiveContext hiveContext;
    private SQLContext sqlContext;

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

    /**
     * Path to the file containing the JSON for the Field Policies. If called from NIFI it will pass it in as a command argument in
     * the Validate processor The JSON should conform to the array of FieldPolicy objects found in the
     * thinkbig-field-policy-rest-model module
     */
    private String fieldPolicyJsonPath;

    private final Vector<Accumulator<Integer>> accumList = new Vector<>();

    public Validator(String targetDatabase, String entity, String partition, String fieldPolicyJsonPath) {
        super();
        SparkContext sparkContext = SparkContext.getOrCreate();
        hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkContext);
        sqlContext = new SQLContext(sparkContext);

        this.validTableName = entity + "_valid";
        this.invalidTableName = entity + "_invalid";
        this.profileTableName = entity + "_profile";
        ;
        this.feedTablename = targetDatabase + "." + entity + "_feed";
        this.refTablename = targetDatabase + "." + validTableName;
        this.qualifiedProfileName = targetDatabase + "." + profileTableName;
        this.partition = partition;
        this.targetDatabase = targetDatabase;
        this.fieldPolicyJsonPath = fieldPolicyJsonPath;
        loadPolicies();
    }

    /**
     * read the JSON file path and return the JSON string
     */
    private String readFieldPolicyJsonFile() {
        log.info("Loading Field Policy JSON file at {} ", fieldPolicyJsonPath);
        String fieldPolicyJson = "[]";

        try (BufferedReader br = new BufferedReader(new FileReader(fieldPolicyJsonPath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            if (line == null) {
                log.error("Field Policy JSON file at {} is empty ", fieldPolicyJsonPath);
            }

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            fieldPolicyJson = sb.toString();
        } catch (Exception e) {
            //LOG THE ERROR
            log.error("Error reading Field Policy JSON Path {}", e.getMessage(), e);
        }
        return fieldPolicyJson;
    }

    /**
     * Load the Policies from JSON into the policy HashMap
     */
    private void loadPolicies() {

        String fieldPolicyJson = readFieldPolicyJsonFile();
        policyMap = new FieldPoliciesJsonTransformer(fieldPolicyJson).buildPolicies();
        log.info("Finished building FieldPolicies for JSON file: {} with entity that has {} fields ", fieldPolicyJsonPath,
                policyMap.size());
    }


    public void doValidate() {
        try {
            // Extract fields from a source table
            StructField[] fields = resolveSchema();
            this.schema = resolveDataTypes(fields);
            this.policies = resolvePolicies(fields);

            DataFrame dataFrame = hiveContext.sql("SELECT * FROM " + feedTablename + " WHERE processing_dttm = '" + partition + "'");
            JavaRDD<Row> rddData = dataFrame.javaRDD().cache();

            // Extract schema from the source table
            StructType sourceSchema = createModifiedSchema(feedTablename);

            // Initialize accumulators to track error statistics on each column
            JavaSparkContext sparkContext = new JavaSparkContext(SparkContext.getOrCreate());
            for (int i = 0; i < this.schema.length; i++) {
                this.accumList.add(sparkContext.accumulator(0));
            }

            // Return a new dataframe based on whether values are valid or invalid
            JavaRDD<Row> newResults = (rddData.map(new Function<Row, Row>() {
                @Override
                public Row call(Row row) throws Exception {
                    return cleanseAndValidateRow(row);
                }
            }));

            final DataFrame validatedDF = hiveContext.createDataFrame(newResults, sourceSchema).toDF();

            // Pull out just the valid or invalid records
            DataFrame invalidDF = validatedDF.filter(VALID_INVALID_COL + " = '0'").drop(VALID_INVALID_COL).toDF();
            writeToTargetTable(invalidDF, invalidTableName);

            // Write out the valid records (dropping our two columns)
            DataFrame
                    validDF =
                    validatedDF.filter(VALID_INVALID_COL + " = '1'").drop(VALID_INVALID_COL).drop("dlp_reject_reason").toDF();
            writeToTargetTable(validDF, validTableName);

            long invalidCount = invalidDF.count();
            long validCount = validDF.count();

            // Record the validation stats
            writeStatsToProfileTable(validCount, invalidCount);
            sparkContext.close();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void writeStatsToProfileTable(long validCount, long invalidCount) {

        try {

            System.out.println("VALIDATION STATS");
            // Create a temporary table we can use to copy data from. Writing directly to our partition from a spark dataframe doesn't work.
            String tempTable = profileTableName + "_" + System.currentTimeMillis();

            // Refactor this into something common with profile table
            List<StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField("columnname", DataTypes.StringType, true));
            fields.add(DataTypes.createStructField("metrictype", DataTypes.StringType, true));
            fields.add(DataTypes.createStructField("metricvalue", DataTypes.StringType, true));
            //fields.add(DataTypes.createStructField("processing_dttm", DataTypes.StringType, true));
            StructType statsSchema = DataTypes.createStructType(fields);

            final ArrayList<String> csvRows = new ArrayList<>();
            csvRows.add("(ALL),TOTAL_COUNT," + Long.toString(validCount + invalidCount));
            csvRows.add("(ALL),VALID_COUNT," + Long.toString(validCount));
            csvRows.add("(ALL),INVALID_COUNT," + Long.toString(invalidCount));

            // Write csv row for each columns
            for (int i = 0; i < accumList.size(); i++) {
                String csvRow = schema[i].getName() + ",INVALID_COUNT," + Long.toString(accumList.get(i).value());
                csvRows.add(csvRow);
            }

            JavaSparkContext jsc = new JavaSparkContext(SparkContext.getOrCreate());
            JavaRDD<Row> statsRDD = jsc.parallelize(csvRows)
                    .map(new Function<String, Row>() {
                        @Override
                        public Row call(String s) throws Exception {
                            return RowFactory.create(s.split("\\,"));
                        }
                    });

            DataFrame df = hiveContext.createDataFrame(statsRDD, statsSchema);
            df.registerTempTable(tempTable);

            String insertSQL = "INSERT OVERWRITE TABLE " + qualifiedProfileName
                    + " PARTITION (processing_dttm='" + partition + "')"
                    + " SELECT columnname, metrictype, metricvalue FROM " + tempTable;

            hiveContext.sql(insertSQL);
        } catch (Exception e) {
            System.out.println("FAILED TO ADD VALIDATION STATS");
            e.printStackTrace();
            ;
        }
    }

    /**
     * Creates a new RDD schema based on the source schema plus two additional columns for validation code and reason
     */
    private StructType createModifiedSchema(String sourceTable) {
        // Extract schema from the source table
        StructType schema = hiveContext.table(feedTablename).schema();
        StructField[] fields = schema.fields();
        List<StructField> fieldsList = new Vector<>();
        for (StructField field : fields) {
            fieldsList.add(field);
        }
        // Insert our two custom fields before the processing partition column
        fieldsList.add(fieldsList.size() - 1, new StructField(VALID_INVALID_COL, DataTypes.StringType, true, Metadata.empty()));
        fieldsList.add(fieldsList.size() - 1, new StructField(REJECT_REASON_COL, DataTypes.StringType, true, Metadata.empty()));

        return new StructType(fieldsList.toArray(new StructField[0]));
    }


    private void writeToTargetTable(DataFrame sourceDF, String targetTable) throws Exception {

        // Create a temporary table we can use to copy data from. Writing directly to our partition from a spark dataframe doesn't work.
        String tempTable = targetTable + "_" + System.currentTimeMillis();
        sourceDF.registerTempTable(tempTable);

        // Insert the data into our partition
        String qualifiedTable = targetDatabase + "." + targetTable;
        hiveContext.sql("INSERT OVERWRITE TABLE " + qualifiedTable + " PARTITION (processing_dttm='" + partition + "') SELECT * FROM "
                + tempTable);
    }

    /**
     * Spark function to perform both cleansing and validation of a data row based on data policies and the target datatype
     */

    public Row cleanseAndValidateRow(Row row) {
        int nulls = 1;

        // Create placeholder for our new values plus two columns for validation and reject_reason
        String[] newValues = new String[schema.length + 2];
        boolean valid = true;
        String sbRejectReason = null;
        List<ValidationResult> results = null;
        // Iterate through columns to cleanse and validate
        for (int idx = 0; idx < schema.length; idx++) {
            ValidationResult result = VALID_RESULT;
            FieldPolicy fieldPolicy = policies[idx];
            HCatDataType dataType = schema[idx];

            // Extract the value (allowing for null or missing field for odd-ball data)
            String fieldValue = (idx == row.length() || row.isNullAt(idx) ? null : row.getString(idx));
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
                            .failField("incompatible", fieldDataType.getName(), "Not convertible to " + fieldDataType.getNativeType());
                }
            }

            // Validate type using provided validators
            List<ValidationPolicy> validators = fieldPolicy.getValidators();
            if (validators != null) {
                for (ValidationPolicy validator : validators) {
                    if (!validator.validate(fieldValue)) {
                        return ValidationResult
                                .failFieldRule("rule", fieldDataType.getName(), validator.getClass().getSimpleName(), "Rule violation");
                    }
                }
            }
        }
        return VALID_RESULT;
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
            String colName = field.name();
            String dataType = field.dataType().simpleString();
            // System.out.println("Table [" + refTablename + "] Field [" + colName + "] dataType [" + dataType + "]");
            cols.add(HCatDataType.createFromDataType(colName, dataType));
        }
        return cols.toArray(new HCatDataType[0]);
    }

    protected StructField[] resolveSchema() {
        StructType schema = hiveContext.table(refTablename).schema();
        return schema.fields();
    }

    /**
     * Returns an array of field-leve policies for data validation and cleansing
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

    // Spark function that performs standardization of the values based on the cleansing policy
/*
    public Row cleanseRow(Row row) {
        String[] newValues = new String[schema.length];
        for (int idx = 0; idx < schema.length; idx++) {

            FieldPolicy fieldPolicy = policies[idx];
            newValues[idx] = null;
            if (idx < row.length()) {
                newValues[idx] = (row.isNullAt(idx) ? null : row.getString(idx));
                newValues[idx] = standardizeField(fieldPolicy, newValues[idx]);
            }
        }
        return RowFactory.create(newValues);
    }
*/

    public static void main(String[] args) {

        // Check how many arguments were passed in
        if (args.length != 4) {
            System.out.println("Proper Usage is: <targetDatabase> <entity> <partition>");
            System.exit(1);
        }
        try {
            Validator app = new Validator(args[0], args[1], args[2], args[3]);
            app.doValidate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

