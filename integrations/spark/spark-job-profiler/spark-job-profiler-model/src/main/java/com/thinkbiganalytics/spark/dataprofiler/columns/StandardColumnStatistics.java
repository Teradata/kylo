package com.thinkbiganalytics.spark.dataprofiler.columns;

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

import com.thinkbiganalytics.spark.dataprofiler.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.model.MetricType;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.apache.spark.sql.types.StructField;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Class to hold common profile statistics for columns of all data types
 */
@SuppressWarnings("serial")
public abstract class StandardColumnStatistics implements ColumnStatistics, Serializable {

    /* Schema information for column */
    final StructField columnField;
    /* Other variables */
    final DecimalFormat df;
    private final TopNDataList topNValues;
    /* Common metrics for all data types */
    long nullCount;
    long totalCount;
    private long uniqueCount;
    private double percNullValues;
    private double percUniqueValues;
    private double percDuplicateValues;
    private ProfilerConfiguration profilerConfiguration;


    /**
     * One-argument constructor
     *
     * @param columnField field schema
     */
    protected StandardColumnStatistics(StructField columnField, @Nonnull final ProfilerConfiguration profilerConfiguration) {
        this.columnField = columnField;
        nullCount = 0;
        totalCount = 0;
        uniqueCount = 0;
        percNullValues = 0.0d;
        percUniqueValues = 0.0d;
        percDuplicateValues = 0.0d;
        this.profilerConfiguration = profilerConfiguration;
        topNValues = new TopNDataList(profilerConfiguration.getNumberOfTopNValues());
        df = new DecimalFormat(getDecimalFormatPattern());
    }


    /**
     * Calculate common statistics by accommodating the value and frequency/count
     *
     * @param columnValue value
     * @param columnCount frequency/count
     */
    void accomodateCommon(Object columnValue, Long columnCount) {

        totalCount += columnCount;
        uniqueCount += 1;

        if (columnValue == null) {
            nullCount += columnCount;
        }

        doPercentageCalculationsCommon();

        topNValues.add(columnValue, columnCount);
    }


    /**
     * Combine with another column statistics
     *
     * @param v_columnStatistics column statistics to combine with
     */
    void combineCommon(StandardColumnStatistics v_columnStatistics) {

        totalCount += v_columnStatistics.totalCount;
        uniqueCount += v_columnStatistics.uniqueCount;
        nullCount += v_columnStatistics.nullCount;

        doPercentageCalculationsCommon();

        for (TopNDataItem dataItem :
            v_columnStatistics.topNValues.getTopNDataItemsForColumn()) {
            topNValues.add(dataItem.getValue(), dataItem.getCount());
        }
    }


    /**
     * Write column's schema information for output result table
     */
    private void writeColumnSchemaInformation(@Nonnull final List<OutputRow> rows) {
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.COLUMN_DATATYPE), String.valueOf(columnField.dataType())));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.COLUMN_NULLABLE), String.valueOf(columnField.nullable())));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.COLUMN_METADATA), String.valueOf(columnField.metadata())));
    }


    /**
     * Print column's schema information to console
     *
     * @return schema information
     */
    private String getVerboseColumnSchemaInformation() {

        return "ColumnInfo ["
               + "name=" + columnField.name()
               + ", datatype=" + columnField.dataType().simpleString()
               + ", nullable=" + columnField.nullable()
               + ", metadata=" + columnField.metadata()
               + "]";
    }


    /**
     * Write top n rows in column for output result table
     */
    private void writeTopNInformation(@Nonnull final List<OutputRow> rows) {
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.TOP_N_VALUES), topNValues.printTopNItems()));
    }


    /**
     * Print top n rows in column to console
     *
     * @return top n rows
     */
    private String getVerboseTopNInformation() {

        return "Top " + profilerConfiguration.getNumberOfTopNValues() + " values [\n"
               + topNValues.printTopNItems()
               + "]";
    }


    /**
     * Write common statistics information for output result table
     */
    void writeStatisticsCommon(@Nonnull final List<OutputRow> rows) {

        writeColumnSchemaInformation(rows);

        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.NULL_COUNT), String.valueOf(nullCount)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.TOTAL_COUNT), String.valueOf(totalCount)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.UNIQUE_COUNT), String.valueOf(uniqueCount)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.PERC_NULL_VALUES), df.format(percNullValues)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.PERC_UNIQUE_VALUES), df.format(percUniqueValues)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.PERC_DUPLICATE_VALUES), df.format(percDuplicateValues)));

        writeTopNInformation(rows);
    }


    /**
     * Print common statistics information to console
     *
     * @return common statistics
     */
    String getVerboseStatisticsCommon() {

        return getVerboseColumnSchemaInformation()
               + "\n"
               + "CommonStatistics ["
               + "nullCount=" + nullCount
               + ", totalCount=" + totalCount
               + ", uniqueCount=" + uniqueCount
               + ", percNullValues=" + df.format(percNullValues)
               + ", percUniqueValues=" + df.format(percUniqueValues)
               + ", percDuplicateValues=" + df.format(percDuplicateValues)
               + "]"
               + "\n"
               + getVerboseTopNInformation();
    }


    /*
     * Do percentage calculations for common metrics
     */
    private void doPercentageCalculationsCommon() {

        percNullValues = ((double) nullCount / totalCount) * 100;
        percUniqueValues = ((double) uniqueCount / totalCount) * 100;
        percDuplicateValues = 100.0d - percUniqueValues;
    }


    /*
     * Build format to display decimals up to configured number of digits
     */
    private String getDecimalFormatPattern() {

        StringBuilder format = new StringBuilder();
        format.append("#.");

        for (int i = 0; i < profilerConfiguration.getDecimalDigitsToDisplayConsoleOutput(); i++) {
            format.append("#");
        }

        return format.toString();
    }


    /**
     * Get null count
     *
     * @return null count
     */
    public long getNullCount() {
        return nullCount;
    }


    /**
     * Get total count (includes nulls and empty values)
     *
     * @return total count
     */
    public long getTotalCount() {
        return totalCount;
    }


    /**
     * Get unique count (null and empty are considered a unique value each)
     *
     * @return unique count
     */
    public long getUniqueCount() {
        return uniqueCount;
    }


    /**
     * Get percentage of null values
     *
     * @return percentage of null values
     */
    public double getPercNullValues() {
        return percNullValues;
    }


    /**
     * Get percentage of unique values
     *
     * @return percentage of unique values
     */
    public double getPercUniqueValues() {
        return percUniqueValues;
    }


    /**
     * Get percentage of duplicate values
     *
     * @return percentage of duplicate values
     */
    public double getPercDuplicateValues() {
        return percDuplicateValues;
    }


    /**
     * Get top n values (in order of frequency)
     *
     * @return top n values
     */
    public TopNDataList getTopNValues() {
        return topNValues;
    }

    /*
     * Methods to be implemented by data type specific column statistics classes that:
     * 1) extend this class
     * 2) may implement additional metrics
     *
     */
    public abstract void accomodate(Object columnValue, Long columnCount);

    public abstract void combine(StandardColumnStatistics v_columnStatistics);

    public abstract List<OutputRow> getStatistics();

    public abstract String getVerboseStatistics();

    /*
     * Check if all values are null
     */
    boolean allNulls() {
        return (totalCount == nullCount);
    }

}
