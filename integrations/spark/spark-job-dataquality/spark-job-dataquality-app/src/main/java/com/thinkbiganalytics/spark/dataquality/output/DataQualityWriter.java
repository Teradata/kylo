package com.thinkbiganalytics.spark.dataquality.output;

/*-
 * #%L
 * thinkbig-spark-job-dataquality-app
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

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.hive.HiveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to write data quality result to Hive table using SparkSQL <br>
 * 
 * This will store the content, convert it to a DataSet and write to Hive. <br>
 * If the table does not already exists, it will create it.
 */
@SuppressWarnings("serial")
public class DataQualityWriter implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(DataQualityWriter.class);

    private static final List<DataQualityRow> dqRows = new ArrayList<>();
    private static DataQualityWriter dqWriter = null;
    private JavaSparkContext sc = null;
    private HiveContext hiveContext = null;

    /* no direct instantiation */
    private DataQualityWriter() {}

    /**
     * Get the instance of DataQualityWriter
     *
     * @return DataQualityWriter
     */
    public static DataQualityWriter getInstance() {
        if (dqWriter == null) {
            dqWriter = new DataQualityWriter();
        }
        return dqWriter;
    }

    /**
     * Get list of rows to write
     *
     * @return list of DataQualityRow
     */
    public List<DataQualityRow> getDataQualityRows() {
        return dqRows;
    }


    /**
     * Get count of output rows
     *
     * @return row count
     */
    public int getDataQualityRowCount() {
        return dqRows.size();
    }

    /**
     * Set Spark context
     *
     * @param sc JavaSparkContext
     */
    public void setSparkContext(JavaSparkContext sc) {
        this.sc = sc;
    }

    /**
     * Set Hive context
     *
     * @param hiveContext HiveContext
     */
    public void setHiveContext(HiveContext hiveContext) {
        this.hiveContext = hiveContext;
    }

    /**
     * Add row to write in output
     *
     * @param row row for output
     */
    public void addRow(DataQualityRow row) {
        dqRows.add(row);
    }

    /**
     * Add multiple rows to write in output
     *
     * @param rows list of rows for output
     */
    public void addRows(List<DataQualityRow> rows) {
        dqRows.addAll(rows);
    }

    /**
     * Write result to Hive table
     *
     * @param scs SparkContextService
     * @param outputTable Name of the destination Hive table
     * @param partitionValue Value of the partition for Hive. Currently the processing_dttm column
     * @return boolean indicating result of write
     */
    public boolean writeResultToTable(SparkContextService scs, String outputTable, String partitionValue) {

        boolean retVal = false;

        if (sc == null) {
            log.error("Error writing result: Spark context is not available.");
        } else if (hiveContext == null) {
            log.error("Error writing result: Hive context is not available.");
        } else if (dqRows.isEmpty()) {
            log.error("Error writing results: No data to write");
        } else {

            JavaRDD<DataQualityRow> dqRowsRDD = sc.parallelize(dqRows);
            DataSet dqRowsDF = scs.toDataSet(hiveContext.createDataFrame(dqRowsRDD, DataQualityRow.class));

            String tempTable = "DQ_DF_TABLE";
            dqRowsDF.registerTempTable(tempTable);

            createOutputTableIfNotExists(scs, outputTable);
            writeResultToOutputTable(scs, tempTable, outputTable, partitionValue);

            retVal = true;
        }

        return retVal;
    }

    /**
     * Creates target Hive table if it does not already exist
     * 
     * @param scs SparkContextService
     * @param outputTable Name of the destination Hive table
     */
    private void createOutputTableIfNotExists(SparkContextService scs, String outputTable) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + outputTable
                                + "\n"
                                + "(rule_name STRING, rule_description STRING, status BOOLEAN, rule_resultdetail STRING)\n"
                                + "PARTITIONED BY ( processing_dttm STRING)\n";

        scs.sql(hiveContext, createTableSQL);
    }

    /**
     * Writes the output to the destination Hive table
     * 
     * @param scs SparkContextService
     * @param tempTable Name of the temp table in Spark
     * @param outputTable Name of the destination Hive table
     * @param partitionValue Value of the partition for Hive. Currently the processing_dttm column
     */
    private void writeResultToOutputTable(SparkContextService scs, String tempTable, String outputTable, String partitionValue) {
        String insertTableSQL = "INSERT OVERWRITE TABLE " + outputTable
                                + " PARTITION ( processing_dttm = "
                                + partitionValue
                                + ")"
                                + " SELECT ruleName, description, status, resultDetail"
                                + " FROM "
                                + tempTable;

        scs.sql(hiveContext, insertTableSQL);
    }
}