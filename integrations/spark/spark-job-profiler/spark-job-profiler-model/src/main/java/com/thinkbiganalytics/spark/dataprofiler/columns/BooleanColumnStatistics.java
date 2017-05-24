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

import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.model.MetricType;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import org.apache.spark.sql.types.StructField;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Class to hold profile statistics for columns of boolean data type <br>
 * [Hive data type: BOOLEAN]
 */
@SuppressWarnings("serial")
public class BooleanColumnStatistics extends StandardColumnStatistics {

    /* Boolean specific metrics */
    private long trueCount;
    private long falseCount;

    /* Other variables */
    private boolean columnBooleanValue;


    /**
     * One-argument constructor
     *
     * @param columnField field schema
     */
    public BooleanColumnStatistics(StructField columnField, @Nonnull final ProfilerConfiguration profilerConfiguration) {

        super(columnField, profilerConfiguration);

        trueCount = 0L;
        falseCount = 0L;

        columnBooleanValue = Boolean.TRUE;
    }


    /**
     * Calculate boolean-specific statistics by accommodating the value and frequency/count
     */
    @Override
    public void accomodate(Object columnValue, Long columnCount) {

        accomodateCommon(columnValue, columnCount);

        if (columnValue != null) {

            columnBooleanValue = Boolean.valueOf(String.valueOf(columnValue));

            if (columnBooleanValue == Boolean.TRUE) {
                trueCount += columnCount;
            } else {
                falseCount += columnCount;
            }
        }

    }


    /**
     * Combine with another column statistics
     */
    @Override
    public void combine(StandardColumnStatistics v_columnStatistics) {

        combineCommon(v_columnStatistics);

        BooleanColumnStatistics vBoolean_columnStatistics = (BooleanColumnStatistics) v_columnStatistics;

        trueCount += vBoolean_columnStatistics.trueCount;
        falseCount += vBoolean_columnStatistics.falseCount;

    }


    /**
     * Print statistics to console
     */
    @Override
    public String getVerboseStatistics() {

        return "{\n" + getVerboseStatisticsCommon()
               + "\n"
               + "BooleanColumnStatistics ["
               + "trueCount=" + trueCount
               + ", falseCount=" + falseCount
               + "]\n}";
    }


    /**
     * Write statistics for output result table
     */
    @Override
    public List<OutputRow> getStatistics() {
        final List<OutputRow> rows = new ArrayList<>();

        writeStatisticsCommon(rows);

        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.TRUE_COUNT), String.valueOf(trueCount)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.FALSE_COUNT), String.valueOf(falseCount)));
        return rows;
    }


    /**
     * Get TRUE count
     *
     * @return TRUE count
     */
    public long getTrueCount() {
        return trueCount;
    }


    /**
     * Get FALSE count
     *
     * @return FALSE count
     */
    public long getFalseCount() {
        return falseCount;
    }

}
