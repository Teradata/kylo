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
 * Class to hold profile statistics for columns of integer data type <br>
 * [Hive data type: INTEGER]
 */
@SuppressWarnings("serial")
public class IntegerColumnStatistics extends StandardColumnStatistics {

    /* Integer specific metrics */
    private int max;
    private int min;
    private long sum;
    private double mean;
    private double stddev;
    private double variance;

    /* Other variables */
    private double sumOfSquares;
    private long oldTotalCount;
    private long oldNullCount;
    private double oldMean;
    private double oldStdDev;
    private double oldSumOfSquares;

    private int columnIntegerValue;


    /**
     * One-argument constructor
     *
     * @param columnField field schema
     */
    public IntegerColumnStatistics(StructField columnField, @Nonnull final ProfilerConfiguration profilerConfiguration) {

        super(columnField, profilerConfiguration);

        max = Integer.MIN_VALUE;
        min = Integer.MAX_VALUE;
        sum = 0L;
        mean = 0.0d;
        stddev = 0.0d;
        variance = 0.0d;

        sumOfSquares = 0.0d;
        oldTotalCount = 0L;
        oldNullCount = 0L;
        oldMean = 0.0d;
        oldStdDev = 0.0d;
        oldSumOfSquares = 0.0d;

        columnIntegerValue = 0;
    }


    /**
     * Calculate integer-specific statistics by accommodating the value and frequency/count
     */
    @Override
    public void accomodate(Object columnValue, Long columnCount) {

        accomodateCommon(columnValue, columnCount);

        if (columnValue != null) {

            columnIntegerValue = Integer.valueOf(String.valueOf(columnValue));

            if (max < columnIntegerValue) {
                max = columnIntegerValue;
            }

            if (min > columnIntegerValue) {
                min = columnIntegerValue;
            }

            sum += (columnIntegerValue * columnCount);

            oldMean = 0.0d;
            oldSumOfSquares = 0.0d;

            for (int i = 1; i <= columnCount; i++) {
                oldMean = mean;
                oldSumOfSquares = sumOfSquares;

                mean = oldMean + ((columnIntegerValue - oldMean) / (totalCount - columnCount + i - nullCount));
                sumOfSquares = oldSumOfSquares + ((columnIntegerValue - mean) * (columnIntegerValue - oldMean));
            }

            variance = sumOfSquares / (totalCount - nullCount);
            stddev = Math.sqrt(variance);

        }
    }


    /**
     * Combine with another column statistics
     */
    @Override
    public void combine(StandardColumnStatistics v_columnStatistics) {

        saveMetricsForStdDevCalc();

        combineCommon(v_columnStatistics);

        IntegerColumnStatistics vInteger_columnStatistics = (IntegerColumnStatistics) v_columnStatistics;

        max = Math.max(max, vInteger_columnStatistics.max);
        min = Math.min(min, vInteger_columnStatistics.min);
        sum += vInteger_columnStatistics.sum;
        mean = (double) sum / (totalCount - nullCount);
        stddev = getCombinedStdDev(vInteger_columnStatistics);
        variance = Math.pow(stddev, 2);
    }


    /*
     * Save values for running statistical calculations
     */
    private void saveMetricsForStdDevCalc() {
        oldTotalCount = totalCount;
        oldNullCount = nullCount;
        oldMean = mean;
        oldStdDev = stddev;
    }


    /*
     * Get combined standard deviations from two standard deviations
     */
    private double getCombinedStdDev(IntegerColumnStatistics vInteger_columnStatistics) {

        double meanComb = (
                              ((oldTotalCount - oldNullCount) * oldMean) +
                              ((vInteger_columnStatistics.totalCount - vInteger_columnStatistics.nullCount) * vInteger_columnStatistics.mean)
                          )
                          / ((oldTotalCount - oldNullCount) + (vInteger_columnStatistics.totalCount - vInteger_columnStatistics.nullCount));

        double term1 = (oldTotalCount - oldNullCount) * Math.pow(oldStdDev, 2);
        double term2 = (vInteger_columnStatistics.totalCount - vInteger_columnStatistics.nullCount) * Math.pow(vInteger_columnStatistics.stddev, 2);
        double term3 = (oldTotalCount - oldNullCount) * Math.pow((oldMean - meanComb), 2);
        double term4 = (vInteger_columnStatistics.totalCount - vInteger_columnStatistics.nullCount) * Math.pow((vInteger_columnStatistics.mean - meanComb), 2);
        double term5 = (oldTotalCount - oldNullCount) + (vInteger_columnStatistics.totalCount - vInteger_columnStatistics.nullCount);
        return (Math.sqrt((term1 + term2 + term3 + term4) / term5));
    }


    /**
     * Print statistics to console
     */
    @Override
    public String getVerboseStatistics() {

        return "{\n" + getVerboseStatisticsCommon()
               + "\n"
               + "IntegerColumnStatistics ["
               + "max=" + max
               + ", min=" + min
               + ", sum=" + sum
               + ", mean=" + df.format(mean)
               + ", stddev=" + df.format(stddev)
               + ", variance=" + df.format(variance)
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
            min = 0;
            max = 0;
            sum = 0;
            mean = 0;
            stddev = 0;
            variance = 0;
        }

        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX), String.valueOf(max)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN), String.valueOf(min)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.SUM), String.valueOf(sum)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MEAN), String.valueOf(df.format(mean))));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.STDDEV), String.valueOf(df.format(stddev))));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.VARIANCE), String.valueOf(df.format(variance))));
        return rows;
    }


    /**
     * Get maximum value
     *
     * @return max value
     */
    public int getMax() {
        return max;
    }


    /**
     * Get minimum value
     *
     * @return min value
     */
    public int getMin() {
        return min;
    }


    /**
     * Get sum
     *
     * @return sum
     */
    public long getSum() {
        return sum;
    }


    /**
     * Get mean (average)
     *
     * @return mean
     */
    public double getMean() {
        return mean;
    }


    /**
     * Get standard deviation (population)
     *
     * @return standard deviation (population)
     */
    public double getStddev() {
        return stddev;
    }


    /**
     * Get variance (population)
     *
     * @return variance (population)
     */
    public double getVariance() {
        return variance;
    }
}
