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
import com.thinkbiganalytics.spark.dataprofiler.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.StatisticsModel;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Class to write profile statistics result to Hive table
 */
@SuppressWarnings("serial")
public class OutputWriter implements Serializable {

    /**
     * Write the profile statistics to Hive.
     */
    public static void writeModel(@Nonnull final StatisticsModel model, @Nonnull final ProfilerConfiguration profilerConfiguration, @Nonnull final SQLContext sqlContext,
                                  @Nonnull final SparkContextService scs) {
        final OutputWriter writer = new OutputWriter(profilerConfiguration);

        for (final ColumnStatistics column : model.getColumnStatisticsMap().values()) {
            writer.addRows(column.getStatistics());
        }

        writer.writeResultToTable(sqlContext, scs);
    }

    private final List<OutputRow> outputRows = new ArrayList<>();

    @Nonnull
    private final ProfilerConfiguration profilerConfiguration;

    private OutputWriter(@Nonnull final ProfilerConfiguration profilerConfiguration) {
        this.profilerConfiguration = profilerConfiguration;
    }

    /**
     * Helper method:
     * Check if output configuration (db, table, partition column, partition key) has been set
     */
    private boolean checkOutputConfigSettings() {
        return !((profilerConfiguration.getOutputDbName() == null)
                 || (profilerConfiguration.getOutputTableName() == null)
                 || (profilerConfiguration.getOutputTablePartitionColumnName() == null)
                 || (profilerConfiguration.getInputAndOutputTablePartitionKey() == null));
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
     * @return boolean indicating result of write
     */
    @SuppressWarnings("unchecked")
    public boolean writeResultToTable(@Nonnull final SQLContext sqlContext, @Nonnull final SparkContextService scs) {
        boolean retVal = false;

        if (!checkOutputConfigSettings()) {
            System.out.println("Error writing result: Output database/table/partition column/partition key not set.");
        } else if (sqlContext == null) {
            System.out.println("Error writing result: Spark context is not available.");
        } else {

            @SuppressWarnings("squid:S2095") final JavaRDD<OutputRow> outputRowsRDD = JavaSparkContext.fromSparkContext(sqlContext.sparkContext()).parallelize(outputRows);
            DataSet outputRowsDF = scs.toDataSet(sqlContext, outputRowsRDD, OutputRow.class);
            //outputRowsDF.write().mode(SaveMode.Overwrite).saveAsTable(outputTable);

            // Since Spark doesn't support partitions, write to temp table, then write to partitioned table
            String tempTable = profilerConfiguration.getOutputTableName() + "_" + System.currentTimeMillis();
            outputRowsDF.registerTempTable(tempTable);

            createOutputTableIfNotExists(sqlContext, scs);
            writeResultToOutputTable(sqlContext, scs, tempTable);
            retVal = true;
        }

        return retVal;
    }


    /**
     * Create output table if does not exist
     */
    private void createOutputTableIfNotExists(@Nonnull final SQLContext sqlContext, @Nonnull final SparkContextService scs) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + HiveUtils.quoteIdentifier(profilerConfiguration.getOutputDbName(), profilerConfiguration.getOutputTableName()) + "\n"
                                + "(columnname STRING, metricname STRING, metricvalue STRING)\n"
                                + "PARTITIONED BY (" + profilerConfiguration.getOutputTablePartitionColumnName() + " STRING)\n"
                                + "ROW FORMAT DELIMITED\n"
                                + "FIELDS TERMINATED BY ','\n"
                                + "STORED AS TEXTFILE";

        scs.sql(sqlContext, createTableSQL);
    }


    /**
     * Write to output table
     */
    private void writeResultToOutputTable(@Nonnull final SQLContext sqlContext, @Nonnull final SparkContextService scs, @Nonnull final String tempTable) {
        String insertTableSQL = "INSERT INTO TABLE " + HiveUtils.quoteIdentifier(profilerConfiguration.getOutputDbName(), profilerConfiguration.getOutputTableName())
                                + " PARTITION (" + HiveUtils.quoteIdentifier(profilerConfiguration.getOutputTablePartitionColumnName()) + "="
                                + HiveUtils.quoteString(profilerConfiguration.getInputAndOutputTablePartitionKey()) + ")"
                                + " SELECT columnname,metrictype,metricvalue FROM " + HiveUtils.quoteIdentifier(tempTable);

        scs.sql(sqlContext, insertTableSQL);

        System.out.println("[PROFILER-INFO] Metrics written to Hive table: "
                           + profilerConfiguration.getOutputDbName() + "." + profilerConfiguration.getOutputTableName()
                           + " Partition: (" + profilerConfiguration.getOutputTablePartitionColumnName() + "='" + profilerConfiguration.getInputAndOutputTablePartitionKey() + "')"
                           + " [" + outputRows.size() + " rows]");
    }
}
