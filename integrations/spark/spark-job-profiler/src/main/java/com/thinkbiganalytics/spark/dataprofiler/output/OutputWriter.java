package com.thinkbiganalytics.spark.dataprofiler.output;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
//import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;

import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to write profile statistics result to Hive table
 *
 * @author jagrut sharma
 */
@SuppressWarnings("serial")
public class OutputWriter implements Serializable {

    private JavaSparkContext sc = null;
    private HiveContext hiveContext = null;

    private static final List<OutputRow> outputRows = new ArrayList<>();
    private static OutputWriter outputWriter = null;


    /* no direct instantiation */
    private OutputWriter() {
    }


    /**
     * Get the instance of OutputWriter
     *
     * @return OutputWriter
     */
    public static OutputWriter getInstance() {

        if (outputWriter == null) {
            outputWriter = new OutputWriter();
        }

        return outputWriter;
    }


    /**
     * Get list of rows to write
     *
     * @return list of OutputRow
     */
    public List<OutputRow> getOutputRows() {
        return outputRows;
    }


    /**
     * Get count of output rows
     *
     * @return row count
     */
    public int getOutputRowCount() {
        return outputRows.size();
    }


    /*
     * Helper method:
     * Check if output configuration (db, table, partition column, partition key) has been set
     */
    private boolean checkOutputConfigSettings() {

        return !((ProfilerConfiguration.OUTPUT_DB_NAME == null)
                || (ProfilerConfiguration.OUTPUT_TABLE_NAME == null)
                || (ProfilerConfiguration.OUTPUT_TABLE_PARTITION_COLUMN_NAME == null)
                || (ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY == null));
    }


    /**
     * Set Spark context
     *
     * @param p_sc JavaSparkContext
     */
    public void setSparkContext(JavaSparkContext p_sc) {
        sc = p_sc;
    }


    /**
     * Set Hive context
     *
     * @param p_hiveContext HiveContext
     */
    public void setHiveContext(HiveContext p_hiveContext) {
        hiveContext = p_hiveContext;
    }


    /**
     * Add row to write in output
     *
     * @param row row for output
     */
    public void addRow(OutputRow row) {
        outputRows.add(row);
    }


    /**
     * Add multiple rows to write in output
     *
     * @param rows list of rows for output
     */
    public void addRows(List<OutputRow> rows) {
        outputRows.addAll(rows);
    }


    /**
     * Write result to Hive table
     *
     * @param p_sc          JavaSparkContext
     * @param p_hiveContext HiveContext
     * @return boolean indicating result of write
     */
    public boolean writeResultToTable(JavaSparkContext p_sc, HiveContext p_hiveContext) {
        
    	sc = p_sc;
        hiveContext = p_hiveContext;
        boolean retVal = false;

        if (!checkOutputConfigSettings()) {
            System.out.println("Error writing result: Output database/table/partition column/partition key not set.");
        } 
        else if (sc == null) {
            System.out.println("Error writing result: Spark context is not available.");
        } 
        else if (hiveContext == null) {
            System.out.println("Error writing result: Hive context is not available.");
        } 
        else {

            JavaRDD<OutputRow> outputRowsRDD = sc.parallelize(outputRows);
            DataFrame outputRowsDF = hiveContext.createDataFrame(outputRowsRDD, OutputRow.class);
            //outputRowsDF.write().mode(SaveMode.Overwrite).saveAsTable(outputTable);

            // Since Spark doesn't support partitions, write to temp table, then write to partitioned table
            String tempTable = ProfilerConfiguration.OUTPUT_TABLE_NAME + "_" + ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY;
            outputRowsDF.registerTempTable(tempTable);
                      
            createOutputTableIfNotExists();
            writeResultToOutputTable(tempTable);          
            retVal = true;
        }

        return retVal;
    }
    
    
    /* Create output table if does not exist */
    private void createOutputTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + ProfilerConfiguration.OUTPUT_DB_NAME + "." + ProfilerConfiguration.OUTPUT_TABLE_NAME + "\n"
        		+ "(columnname STRING, metricname STRING, metricvalue STRING)\n"
        		+ "PARTITIONED BY (" + ProfilerConfiguration.OUTPUT_TABLE_PARTITION_COLUMN_NAME + " STRING)\n"
        		+ "ROW FORMAT DELIMITED\n"
        		+ "FIELDS TERMINATED BY ','\n"
        		+ "STORED AS TEXTFILE";

        hiveContext.sql(createTableSQL);
    }
    
    
    /* Write to output table */
    private void writeResultToOutputTable(String tempTable) {
    	String insertTableSQL = "INSERT INTO TABLE " 
				+ ProfilerConfiguration.OUTPUT_DB_NAME + "." + ProfilerConfiguration.OUTPUT_TABLE_NAME 
				+ " PARTITION (" + ProfilerConfiguration.OUTPUT_TABLE_PARTITION_COLUMN_NAME + "='" + ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY + "') "
				+ "SELECT columnname,metrictype,metricvalue FROM " + tempTable;
    	
    	hiveContext.sql(insertTableSQL);
    	
    	System.out.println("[PROFILER-INFO] Metrics written to Hive table: " 
				+ ProfilerConfiguration.OUTPUT_DB_NAME + "." + ProfilerConfiguration.OUTPUT_TABLE_NAME 
				+ " Partition: (" + ProfilerConfiguration.OUTPUT_TABLE_PARTITION_COLUMN_NAME + "='" + ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY + "')"
				+ " [" + outputRows.size() + " rows]");
    }
    
}