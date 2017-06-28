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

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.types.StructField;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class to hold profile statistics for columns of timestamp data type <br>
 * [Hive data type: TIMESTAMP]
 */
@SuppressWarnings("serial")
public class TimestampColumnStatistics extends StandardColumnStatistics {

    /**
     * Records the maximum value of the column
     */
    @Nullable
    private Timestamp maxTimestamp;

    /**
     * Records the minimum value of the column
     */
    @Nullable
    private Timestamp minTimestamp;

    /**
     * Constructs a {@code TimestampColumnStatistics} for profiling the the specified field.
     *
     * @param columnField           the field to be profiled
     * @param profilerConfiguration the profiler configuration
     */
    public TimestampColumnStatistics(@Nonnull final StructField columnField, @Nonnull final ProfilerConfiguration profilerConfiguration) {
        super(columnField, profilerConfiguration);
    }

    /**
     * Adds the specified value to the statistics for this column.
     *
     * @param columnValue the column value to be profiled
     * @param columnCount the number of rows containing the value
     */
    @Override
    public void accomodate(@Nullable final Object columnValue, @Nonnull Long columnCount) {
        // Update common statistics
        accomodateCommon(columnValue, columnCount);

        // Update timestamp-specific statistics
        String stringValue = (columnValue != null) ? columnValue.toString() : null;

        if (!StringUtils.isEmpty(stringValue)) {
            Timestamp timestamp = Timestamp.valueOf(stringValue);
            if (maxTimestamp == null || maxTimestamp.before(timestamp)) {
                maxTimestamp = timestamp;
            }
            if (minTimestamp == null || minTimestamp.after(timestamp)) {
                minTimestamp = timestamp;
            }
        }
    }

    /**
     * Merges the specified statistics into this object.
     *
     * @param v_columnStatistics the statistics to be merged
     */
    @Override
    public void combine(@Nonnull final StandardColumnStatistics v_columnStatistics) {
        // Combine common statistics
        combineCommon(v_columnStatistics);

        // Combine timestamp-specific statistics
        TimestampColumnStatistics vTimestamp_columnStatistics = (TimestampColumnStatistics) v_columnStatistics;

        if (maxTimestamp == null || (vTimestamp_columnStatistics.maxTimestamp != null && maxTimestamp.before(vTimestamp_columnStatistics.maxTimestamp))) {
            maxTimestamp = vTimestamp_columnStatistics.maxTimestamp;
        }
        if (minTimestamp == null || (vTimestamp_columnStatistics.minTimestamp != null && minTimestamp.after(vTimestamp_columnStatistics.minTimestamp))) {
            minTimestamp = vTimestamp_columnStatistics.minTimestamp;
        }
    }

    /**
     * Returns the statistics as a string.
     *
     * @return the statistics
     */
    @Nonnull
    @Override
    public String getVerboseStatistics() {
        return "{\n" + getVerboseStatisticsCommon() + "\n"
               + "TimestampColumnStatistics [maxTimestamp=" + (maxTimestamp != null ? maxTimestamp : "") + ", minTimestamp=" + (minTimestamp != null ? minTimestamp : "") + "]\n}";
    }

    /**
     * Writes the statistics to an output table.
     */
    @Override
    public List<OutputRow> getStatistics() {
        final List<OutputRow> rows = new ArrayList<>();

        // Write common statistics
        writeStatisticsCommon(rows);

        // Write timestamp-specific statistics
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX_TIMESTAMP), (maxTimestamp != null) ? maxTimestamp.toString() : ""));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN_TIMESTAMP), (minTimestamp != null) ? minTimestamp.toString() : ""));
        return rows;
    }

    /**
     * Get latest timestamp
     *
     * @return latest timestamp
     */
    @Nullable
    public Timestamp getMaxTimestamp() {
        return maxTimestamp;
    }

    /**
     * Get earliest timestamp
     *
     * @return earliest timestamp
     */
    @Nullable
    public Timestamp getMinTimestamp() {
        return minTimestamp;
    }
}
