package com.thinkbiganalytics.spark.spark_launcher;


import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * Validates a table of strings conforms to a reference typed schema. The records are split
 * into valid and invalid.
 * <p>
 * blog.cloudera.com/blog/2015/07/how-to-do-data-quality-checks-using-apache-spark-dataframes/
 */
public class ValidateRecords implements Serializable {

    /* Initialize Spark */
    private HiveContext hiveContext;
    //private SparkContext sparkContext;
    private SQLContext sqlContext;

    /*
    Valid target schema
     */
    private HCatDataType[] schema;
    private String validTableName;
    private String invalidTableName;
    private String feedTablename;
    private String refTablename;
    private String targetDatabase;
    private String partition;
    private String entity;

    public ValidateRecords(String targetDatabase, String entity, String partition) {
        super();
        SparkContext sparkContext = SparkContext.getOrCreate();
        hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkContext);
        sqlContext = new SQLContext(sparkContext);

        this.entity = entity;
        this.validTableName = entity + "_valid";
        this.invalidTableName = entity + "_invalid";
        ;
        this.feedTablename = targetDatabase + "." + entity + "_feed";
        this.refTablename = targetDatabase + "." + entity;
        this.partition = partition;
        this.targetDatabase = targetDatabase;
    }

    public void doValidate() {
        try {
            schema = resolveSchema();

            DataFrame dataFrame = hiveContext.sql("SELECT * FROM " + feedTablename + " WHERE processing_dttm = '" + partition + "'");
            JavaRDD<Row> rddData = dataFrame.javaRDD().cache();

            filterAndWriteResults(rddData, true, validTableName);
            filterAndWriteResults(rddData, false, invalidTableName);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void filterAndWriteResults(JavaRDD<Row> rddData, final boolean valid, String targetTable) throws Exception {
        System.out.println("-- WOW ");
        // Filter either good or bad records and convert to a data frame
        JavaRDD<Row> results = rddData.filter(new Function<Row, Boolean>() {
            public Boolean call(Row row) {
                return validateRow(row) == valid;
            }
        });

        StructType schema = hiveContext.table(feedTablename).schema();
        DataFrame newDF = hiveContext.createDataFrame(results, schema);

        // Create a temporary table we can use to copy data from. Writing directly to our partition from a spark dataframe doesn't work.
        String tempTable = targetTable + "_" + System.currentTimeMillis();
        newDF.registerTempTable(tempTable);

        // Insert the data into our partition
        try {
            String qualifiedTable = targetDatabase + "." + targetTable;
            //hiveContext.sql("ALTER TABLE " + qualifiedTable + " ADD IF NOT EXISTS PARTITION (processing_dttm='" + partition + "') LOCATION '/etl/" + targetDatabase + "/" + entity + "/" + partition + "/" + targetTable + "/'");
            hiveContext.sql("INSERT OVERWRITE TABLE " + qualifiedTable + " PARTITION (processing_dttm='" + partition + "') SELECT * FROM " + tempTable);
        } catch (Exception e) {
            System.out.println("ERROR SQL");
            e.printStackTrace();
        }
        System.out.println("Exiting " + targetTable);
    }

    public boolean validateRow(Row row) {
        int nulls = 1;
        for (int idx = 0; idx < schema.length; idx++) {
            if (idx < row.length() && !row.isNullAt(idx)) {
                String val = row.getString(idx);
                boolean result = schema[idx].isConvertible(val);
                if (!result) {
                    System.out.println("Failed validation on " + schema[idx].getName() + " value: " + val);
                    return false;
                }
            } else {
                nulls++;
            }
        }
        return (nulls >= schema.length ? false : true);
    }

    private HCatDataType[] resolveSchema() {
        List<HCatDataType> cols = new Vector<>();

        StructType schema = hiveContext.table(refTablename).schema();
        StructField[] fields = schema.fields();
        for (StructField field : fields) {
            String colName = field.name();
            String dataType = field.dataType().simpleString();
            cols.add(HCatDataType.createFromDataType(colName, dataType));
        }
        return cols.toArray(new HCatDataType[0]);
    }

    public static void main(String[] args) {

        // Check how many arguments were passed in
        if (args.length != 3) {
            System.out.println("Proper Usage is: <targetDatabase> <entity> <partition>");
            System.exit(1);
        }
        try {
            ValidateRecords app = new ValidateRecords(args[0], args[1], args[2]);
            app.doValidate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

