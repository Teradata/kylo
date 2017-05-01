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

import java.io.Serializable;

/**
 * Class to represent a row in profile statistics output<br>
 * Format of output:<br>
 *
 * ColumnName, MetricType, MetricValue
 */
@SuppressWarnings({"unused", "serial"})
public class OutputRow implements Serializable {

    private String columnName;
    private String metricType;
    private String metricValue;

    /**
     * No-argument constructor
     */
    public OutputRow() {
        columnName = null;
        metricType = null;
        metricValue = null;
    }

    /**
     * Three-argument constructor to create a new row
     *
     * @param columnName  name of column
     * @param metricType  metric type
     * @param metricValue metric value
     */
    public OutputRow(String columnName, String metricType, String metricValue) {
        this.columnName = columnName;
        this.metricType = metricType;
        this.metricValue = metricValue;
    }

    /**
     * Get the column name
     *
     * @return column name
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Set the column name
     *
     * @param columnName column name
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Get the metric type
     *
     * @return metric type
     */
    public String getMetricType() {
        return metricType;
    }

    /**
     * Set the metric type
     *
     * @param metricType metric type
     */
    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    /**
     * Get the metric value
     *
     * @return metric value
     */
    public String getMetricValue() {
        return metricValue;
    }

    /**
     * Set the metric value
     *
     * @param metricValue metric value
     */
    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    /**
     * Set values for the row
     *
     * @param columnName  name of column
     * @param metricType  metric type
     * @param metricValue metric value
     */
    public void setValues(String columnName, String metricType, String metricValue) {
        this.columnName = columnName;
        this.metricType = metricType;
        this.metricValue = metricValue;
    }

    /**
     * Print verbose description of row to console
     */
    @Override
    public String toString() {
        return "OutputRow [columnName=" + columnName + ", metricType=" + metricType + ", metricValue=" + metricValue
               + "]";
    }
}
