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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Class to hold profile statistics for columns of bigdecimal data type <br>
 * [Hive data type: DECIMAL]
 */
@SuppressWarnings("serial")
public class BigDecimalColumnStatistics extends StandardColumnStatistics {

    /* BigDecimal specific metrics */
    private BigDecimal max;
    private BigDecimal min;
    private BigDecimal sum;

    /* Other variables */
    private BigDecimal columnBigDecimalValue;
    private BigDecimal columnBigDecimalCount;


    /**
     * One-argument constructor
     *
     * @param columnField field schema
     */
    public BigDecimalColumnStatistics(StructField columnField, @Nonnull final ProfilerConfiguration profilerConfiguration) {

        super(columnField, profilerConfiguration);

        max = BigDecimal.valueOf(Long.MIN_VALUE);
        min = BigDecimal.valueOf(Long.MAX_VALUE);

        sum = BigDecimal.ZERO;

        columnBigDecimalValue = BigDecimal.ZERO;
        columnBigDecimalCount = BigDecimal.ZERO;
    }


    /**
     * Calculate bigdecimal-specific statistics by accommodating the value and frequency/count
     */
    @Override
    public void accomodate(Object columnValue, Long columnCount) {

        accomodateCommon(columnValue, columnCount);

        if (columnValue != null) {

            columnBigDecimalValue = new BigDecimal(String.valueOf(columnValue));
            columnBigDecimalCount = new BigDecimal(columnCount);

            if (max.compareTo(columnBigDecimalValue) < 0) {
                max = columnBigDecimalValue;
            }

            if (min.compareTo(columnBigDecimalValue) > 0) {
                min = columnBigDecimalValue;
            }

            sum = sum.add(columnBigDecimalValue.multiply(columnBigDecimalCount));

        }

    }


    /**
     * Combine with another column statistics
     */
    @Override
    public void combine(StandardColumnStatistics v_columnStatistics) {

        combineCommon(v_columnStatistics);

        BigDecimalColumnStatistics vBigDecimal_columnStatistics = (BigDecimalColumnStatistics) v_columnStatistics;

        if (max.compareTo(vBigDecimal_columnStatistics.max) < 0) {
            max = vBigDecimal_columnStatistics.max;
        }

        if (min.compareTo(vBigDecimal_columnStatistics.min) > 0) {
            min = vBigDecimal_columnStatistics.min;
        }

        sum = sum.add(vBigDecimal_columnStatistics.sum);
    }


    /**
     * Print statistics to console
     */
    @Override
    public String getVerboseStatistics() {

        return "{\n" + getVerboseStatisticsCommon()
               + "\n"
               + "BigDecimalColumnStatistics ["
               + "max=" + max
               + ", min=" + min
               + ", sum=" + sum
               + "]\n}";
    }


    /**
     * Write statistics for output result table
     */
    @Override
    public List<OutputRow> getStatistics() {
        final List<OutputRow> rows = new ArrayList<>();

        writeStatisticsCommon(rows);


        if (allNulls()) {
            min = BigDecimal.ZERO;
            max = BigDecimal.ZERO;
            sum = BigDecimal.ZERO;
        }

        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX), String.valueOf(max)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN), String.valueOf(min)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.SUM), String.valueOf(sum)));
        return rows;
    }


    /**
     * Get maximum value
     *
     * @return max value
     */
    public BigDecimal getMax() {
        return max;
    }


    /**
     * Get minimum value
     *
     * @return min value
     */
    public BigDecimal getMin() {
        return min;
    }


    /**
     * Get sum
     *
     * @return sum
     */
    public BigDecimal getSum() {
        return sum;
    }

}
