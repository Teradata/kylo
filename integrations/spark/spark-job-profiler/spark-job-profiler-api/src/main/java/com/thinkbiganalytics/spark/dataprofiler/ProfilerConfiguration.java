package com.thinkbiganalytics.spark.dataprofiler;

/*-
 * #%L
 * kylo-spark-job-profiler-api
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

import java.io.Serializable;

/**
 * Helper class to hold parameters for profiler
 */
@SuppressWarnings("unused")
public class ProfilerConfiguration implements Serializable {

    private static final long serialVersionUID = -6099960489540200374L;

    private Integer decimalDigitsToDisplayConsoleOutput = 4;
    private String inputAndOutputTablePartitionKey = "partitionKey";
    private String inputTablePartitionColumnName = "processing_dttm";
    private Integer numberOfTopNValues = 3;
    private String outputDbName = "default";
    private String outputTableName = "profilestats";
    private String outputTablePartitionColumnName = "processing_dttm";
    private String sqlDialect = "hiveql";  // Hive supported HQL

    /**
     * Number of decimals to print out in console<br>
     * (not considered when writing to table)
     */
    public Integer getDecimalDigitsToDisplayConsoleOutput() {
        return decimalDigitsToDisplayConsoleOutput;
    }

    public void setDecimalDigitsToDisplayConsoleOutput(Integer decimalDigitsToDisplayConsoleOutput) {
        this.decimalDigitsToDisplayConsoleOutput = decimalDigitsToDisplayConsoleOutput;
    }

    /**
     * Partition key to read and write to
     */
    public String getInputAndOutputTablePartitionKey() {
        return inputAndOutputTablePartitionKey;
    }

    public void setInputAndOutputTablePartitionKey(String inputAndOutputTablePartitionKey) {
        this.inputAndOutputTablePartitionKey = inputAndOutputTablePartitionKey;
    }

    /**
     * Partition column name for input table
     */
    public String getInputTablePartitionColumnName() {
        return inputTablePartitionColumnName;
    }

    public void setInputTablePartitionColumnName(String inputTablePartitionColumnName) {
        this.inputTablePartitionColumnName = inputTablePartitionColumnName;
    }

    /**
     * N for top-N values to store in result table<br>
     * A required command line parameter
     */
    public Integer getNumberOfTopNValues() {
        return numberOfTopNValues;
    }

    public void setNumberOfTopNValues(Integer numberOfTopNValues) {
        this.numberOfTopNValues = numberOfTopNValues;
    }

    /**
     * Name of database to write result to
     */
    public String getOutputDbName() {
        return outputDbName;
    }

    public void setOutputDbName(String outputDbName) {
        this.outputDbName = outputDbName;
    }

    /**
     * Name of table to write result to<br>
     * A required command line parameter
     */
    public String getOutputTableName() {
        return outputTableName;
    }

    public void setOutputTableName(String outputTableName) {
        this.outputTableName = outputTableName;
    }

    /**
     * Partition column name for output table
     */
    public String getOutputTablePartitionColumnName() {
        return outputTablePartitionColumnName;
    }

    public void setOutputTablePartitionColumnName(String outputTablePartitionColumnName) {
        this.outputTablePartitionColumnName = outputTablePartitionColumnName;
    }

    /**
     * Gets the flavor of queries to run.
     */
    public String getSqlDialect() {
        return sqlDialect;
    }

    public void setSqlDialect(String sqlDialect) {
        this.sqlDialect = sqlDialect;
    }
}
