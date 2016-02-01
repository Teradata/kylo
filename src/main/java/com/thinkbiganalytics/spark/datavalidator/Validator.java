package com.thinkbiganalytics.spark.datavalidator;


import com.thinkbiganalytics.spark.validation.FieldPolicy;
import com.thinkbiganalytics.spark.validation.FieldPolicyBuilder;
import com.thinkbiganalytics.spark.validation.HCatDataType;
import com.thinkbiganalytics.spark.standardization.StandardizationPolicy;
import com.thinkbiganalytics.spark.standardization.impl.AcceptsEmptyValues;
import com.thinkbiganalytics.spark.standardization.impl.DateTimeStandardizer;
import com.thinkbiganalytics.spark.standardization.impl.RemoveControlCharsStandardizer;
import com.thinkbiganalytics.spark.validation.impl.CreditCardValidator;
import com.thinkbiganalytics.spark.validation.impl.EmailValidator;
import com.thinkbiganalytics.spark.validation.impl.IPAddressValidator;
import com.thinkbiganalytics.spark.validation.impl.TimestampValidator;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
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
import org.datanucleus.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Cleanses and validates a table of strings according to defined field-level policies. Records are split
 * into good and bad.
 * <p>
 * blog.cloudera.com/blog/2015/07/how-to-do-data-quality-checks-using-apache-spark-dataframes/
 */
public class Validator implements Serializable {

    private static String REJECT_REASON_COL = "dlp_reject_reason";

    private static String VALID_INVALID_COL = "dlp_valid";

    /*
    Valid validation result
     */
    private static ValidationResult VALID_RESULT = new ValidationResult();

    /* Initialize Spark */
    private HiveContext hiveContext;
    //private SparkContext sparkContext;
    private SQLContext sqlContext;

    /*
    Valid target schema
     */
    private String validTableName;
    private String invalidTableName;
    private String feedTablename;
    private String refTablename;
    private String targetDatabase;
    private String partition;
    private String entity;

    private FieldPolicy[] policies;
    private HCatDataType[] schema;
    private Map<String, FieldPolicy> policyMap = new HashMap<String, FieldPolicy>();

    public Validator(String targetDatabase, String entity, String partition) {
        super();
        SparkContext sparkContext = SparkContext.getOrCreate();
        hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkContext);
        sqlContext = new SQLContext(sparkContext);

        this.entity = entity;
        this.validTableName = entity + "_valid";
        this.invalidTableName = entity + "_invalid";
        ;
        this.feedTablename = targetDatabase + "." + entity + "_feed";
        this.refTablename = targetDatabase + "." + validTableName;
        this.partition = partition;
        this.targetDatabase = targetDatabase;
        loadPolicies();
    }

    private void loadPolicies() {

        policyMap.put("registration_dttm", FieldPolicyBuilder.newBuilder().addValidator(TimestampValidator.instance()).build());
        policyMap.put("id", FieldPolicyBuilder.newBuilder().disallowNullOrEmpty().build());
        //policyMap.put("first_name", FieldPolicyBuilder.SKIP_VALIDATION);
        //policyMap.put("last_name",  FieldPolicyBuilder.SKIP_VALIDATION);
        policyMap.put("email", FieldPolicyBuilder.newBuilder().addValidator(EmailValidator.instance()).build());
        policyMap.put("gender", FieldPolicyBuilder.newBuilder().constrainValues("Male", "Female").build());
        policyMap.put("ip_address", FieldPolicyBuilder.newBuilder().addValidator(IPAddressValidator.instance()).build());
        policyMap.put("cc", FieldPolicyBuilder.newBuilder().addValidator(CreditCardValidator.instance()).build());
        //policyMap.put("country", FieldPolicyBuilder.SKIP_VALIDATION);
        policyMap.put("birthdate", FieldPolicyBuilder.newBuilder().addStandardizer(new DateTimeStandardizer("MM/dd/YYYY", DateTimeStandardizer.OutputFormats.DATE_ONLY)).build()); //addValidator(TimestampValidator.instance()).build());
        //policyMap.put("salary", FieldPolicyBuilder.SKIP_VALIDATION);
        //policyMap.put("title", FieldPolicyBuilder.SKIP_VALIDATION);
        policyMap.put("comments", FieldPolicyBuilder.newBuilder().addStandardizer(RemoveControlCharsStandardizer.instance()).constrainLength(0, 255).build());
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
            StructType schema = createModifiedSchema(feedTablename);

            // Return a new dataframe based on whether values are valid or invalid
            JavaRDD<Row> newResults = (rddData.map(new Function<Row, Row>() {
                @Override
                public Row call(Row row) throws Exception {
                    return cleanseAndValidateRow(row);
                }
            }));

            final DataFrame validatedDF = hiveContext.createDataFrame(newResults, schema).toDF();

            // Pull out just the valid or invalid records
            DataFrame invalidDF = validatedDF.filter(VALID_INVALID_COL + " = '0'").drop(VALID_INVALID_COL).toDF();
            writeToTargetTable(invalidDF, invalidTableName);

            // Write out the valid records (dropping our two columns)
            DataFrame validDF = validatedDF.filter(VALID_INVALID_COL + " = '1'").drop(VALID_INVALID_COL).drop("dlp_reject_reason").toDF();
            writeToTargetTable(validDF, validTableName);


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
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
        hiveContext.sql("INSERT OVERWRITE TABLE " + qualifiedTable + " PARTITION (processing_dttm='" + partition + "') SELECT * FROM " + tempTable);
    }

    /**
     * Spark function to perform both cleansing and validation of a data row based on data policies
     * and the target datatype
     */

    public Row cleanseAndValidateRow(Row row) {
        int nulls = 1;

        // Create placeholder for our new values plus two columns for validation and reject_reason
        String[] newValues = new String[schema.length + 2];
        boolean valid = true;
        StringBuffer sbRejectReason = new StringBuffer();
        // Iterate through columns to cleanse and validate
        for (int idx = 0; idx < schema.length; idx++) {
            FieldPolicy fieldPolicy = policies[idx];
            HCatDataType dataType = schema[idx];

            // Extract the value (allowing for null or missing field for odd-ball data)
            String fieldValue = (idx == row.length() || row.isNullAt(idx) ? null : row.getString(idx));
            if (StringUtils.isEmpty(fieldValue)) nulls++;

            // Perform cleansing operations
            fieldValue = standardizeField(fieldPolicy, fieldValue);
            newValues[idx] = fieldValue;

            // Record results in our appended columns
            ValidationResult result = validateField(fieldPolicy, dataType, fieldValue);
            if (!result.isValid()) {
                valid = false;
                sbRejectReason.append(result.rejectReason);
            }
        }
        // Return success unless all values were null.  That would indicate a blank line in the file
        if (nulls >= schema.length) {
            valid = false;
            sbRejectReason = new StringBuffer("{Empty Row}");
        }
        // Record the results in our appended columns, move processing partition value last
        newValues[schema.length + 1] = newValues[schema.length - 1];
        newValues[schema.length] = sbRejectReason.toString();
        newValues[schema.length - 1] = (valid ? "1" : "0");

        return RowFactory.create(newValues);
    }

    // Spark function that performs standardization of the values based on the cleansing policy
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

    /**
     * Perform validation using both schema validation our validation policies
     */
    protected ValidationResult validateField(FieldPolicy fieldPolicy, HCatDataType fieldDataType, String fieldValue) {

        boolean isEmpty = (StringUtils.isEmpty(fieldValue));
        if (isEmpty) {
            if (!fieldPolicy.isNullable()) {
                return ValidationResult.fail("{Null violation: field [" + fieldDataType.getName() + "] cannot be null" + "]}");
            }
        } else {

            // Verify new value is compatible with the target Hive schema e.g. integer, double (unless checking is disabled)
            if (!fieldPolicy.shouldSkipSchemaValidation()) {
                if (!fieldDataType.isValueConvertibleToType(fieldValue)) {
                    return ValidationResult.fail("{Invalid data type: field [" + fieldDataType.getName() + "] cannot be converted to type [" + fieldDataType.getNativeType() + "]}");
                }
            }

            // Validate type using provided validators
            List<com.thinkbiganalytics.spark.validation.Validator> validators = fieldPolicy.getValidators();
            if (validators != null) {
                for (com.thinkbiganalytics.spark.validation.Validator validator : validators) {
                    if (!validator.validate(fieldValue)) {
                        return ValidationResult.fail("{ Validation failure: field [" + fieldDataType.getName() + "] rule [" + validator.getClass().getSimpleName() + "]]}");
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
                if (isEmpty && !(standardizationPolicy instanceof AcceptsEmptyValues)) continue;
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
            System.out.println("Table [" + refTablename + "] Field [" + colName + "] dataType [" + dataType + "]");
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

    /**
     * Represents the result of a single validation of a field
     */
    static class ValidationResult implements Serializable {
        boolean result = true;
        String rejectReason;

        static ValidationResult fail(String rejectReason) {
            ValidationResult o = new ValidationResult();
            o.rejectReason = rejectReason;
            o.result = false;
            return o;
        }

        boolean isValid() {
            return result;
        }

    }

    public static void main(String[] args) {

        // Check how many arguments were passed in
        if (args.length != 3) {
            System.out.println("Proper Usage is: <targetDatabase> <entity> <partition>");
            System.exit(1);
        }
        try {
            Validator app = new Validator(args[0], args[1], args[2]);
            app.doValidate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

