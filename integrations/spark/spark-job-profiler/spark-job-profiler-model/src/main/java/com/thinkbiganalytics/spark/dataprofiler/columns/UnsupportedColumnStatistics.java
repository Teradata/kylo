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
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import org.apache.spark.sql.types.StructField;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Class to hold profile statistics for columns of unsupported data type<br>
 * [Hive data types: CHAR, BINARY, ARRAY, MAP, STRUCT, UNIONTYPE]
 */
@SuppressWarnings("serial")
public class UnsupportedColumnStatistics extends StandardColumnStatistics {

    /**
     * One-argument constructor
     *
     * @param columnField field schema
     */
    public UnsupportedColumnStatistics(StructField columnField, @Nonnull final ProfilerConfiguration profilerConfiguration) {
        super(columnField, profilerConfiguration);
    }


    /**
     * Calculate unsupported type-specific statistics by accommodating the value and frequency/count<br>
     * No additional statistics computed
     */
    @Override
    public void accomodate(Object columnValue, Long columnCount) {

    }

    /**
     * Combine with another column statistics <br>
     * No additional statistics combined
     */
    @Override
    public void combine(StandardColumnStatistics v_columnStatistics) {

    }

    /**
     * Print statistics to console
     */
    @Override
    public String getVerboseStatistics() {

        return "{\n" + getVerboseStatisticsCommon()
               + "\n"
               + "UnsupportedColumnType [no additional statistics"
               + "]\n}";

    }

    /**
     * Write statistics for output result table<br>
     * No additional statistics written
     */
    @Override
    public List<OutputRow> getStatistics() {
        final List<OutputRow> rows = new ArrayList<>();
        writeStatisticsCommon(rows);
        return rows;
    }
}
