package com.thinkbiganalytics.spark.dataprofiler.output;

/*-
 * #%L
 * thinkbig-spark-job-profiler-app
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

import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerSparkContextService;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.hive.HiveContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//import org.apache.spark.sql.Row;


/**
 * Class to write profile statistics result to Hive table
 *
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
    public boolean writeResultToTable(JavaSparkContext p_sc, HiveContext p_hiveContext, ProfilerSparkContextService scs) {
        
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
            DataSet outputRowsDF = scs.toDataSet(hiveContext, outputRowsRDD, OutputRow.class);
            //outputRowsDF.write().mode(SaveMode.Overwrite).saveAsTable(outputTable);

            // Since Spark doesn't support partitions, write to temp table, then write to partitioned table
            String tempTable = ProfilerConfiguration.OUTPUT_TABLE_NAME + "_" + System.currentTimeMillis();
            outputRowsDF.registerTempTable(tempTable);
                      
            createOutputTableIfNotExists(scs);
            writeResultToOutputTable(scs, tempTable);
            retVal = true;
        }

        return retVal;
    }


    /* Create output table if does not exist */
    private void createOutputTableIfNotExists(SparkContextService scs) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + HiveUtils.quoteIdentifier(ProfilerConfiguration.OUTPUT_DB_NAME, ProfilerConfiguration.OUTPUT_TABLE_NAME) + "\n"
        		+ "(columnname STRING, metricname STRING, metricvalue STRING)\n"
        		+ "PARTITIONED BY (" + ProfilerConfiguration.OUTPUT_TABLE_PARTITION_COLUMN_NAME + " STRING)\n"
        		+ "ROW FORMAT DELIMITED\n"
        		+ "FIELDS TERMINATED BY ','\n"
        		+ "STORED AS TEXTFILE";

        scs.sql(hiveContext, createTableSQL);
    }


    /* Write to output table */
    private void writeResultToOutputTable(SparkContextService scs, String tempTable) {
    	String insertTableSQL = "INSERT INTO TABLE " + HiveUtils.quoteIdentifier(ProfilerConfiguration.OUTPUT_DB_NAME, ProfilerConfiguration.OUTPUT_TABLE_NAME)
                                + " PARTITION (" + HiveUtils.quoteIdentifier(ProfilerConfiguration.OUTPUT_TABLE_PARTITION_COLUMN_NAME) + "="
                                + HiveUtils.quoteString(ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY) + ")"
                                + " SELECT columnname,metrictype,metricvalue FROM " + HiveUtils.quoteIdentifier(tempTable);

        scs.sql(hiveContext, insertTableSQL);

    	System.out.println("[PROFILER-INFO] Metrics written to Hive table: "
				+ ProfilerConfiguration.OUTPUT_DB_NAME + "." + ProfilerConfiguration.OUTPUT_TABLE_NAME
				+ " Partition: (" + ProfilerConfiguration.OUTPUT_TABLE_PARTITION_COLUMN_NAME + "='" + ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY + "')"
				+ " [" + outputRows.size() + " rows]");
    }

}
