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
 * Class to hold profile statistics for columns of string data type <br>
 * [Hive data types: STRING, VARCHAR]
 */
@SuppressWarnings("serial")
public class StringColumnStatistics extends StandardColumnStatistics {

    /* String specific metrics */
    private int maxLength;
    private int minLength;
    private String longestString;
    private String shortestString;
    private long emptyCount;
    private double percEmptyValues;
    private String minStringCase;
    private String maxStringCase;
    private String minStringICase;
    private String maxStringICase;

    /* Other variables */
    private String columnStringValue;
    private int columnStringLength;
    private boolean initializationFlag;


    /**
     * One-argument constructor
     *
     * @param columnField field schema
     */
    public StringColumnStatistics(StructField columnField, @Nonnull final ProfilerConfiguration profilerConfiguration) {
        super(columnField, profilerConfiguration);

        maxLength = Integer.MIN_VALUE;
        minLength = Integer.MAX_VALUE;
        String EMPTY_STRING = "";
        longestString = EMPTY_STRING;
        shortestString = EMPTY_STRING;
        emptyCount = 0;
        percEmptyValues = 0.0d;

        initializationFlag = false;
        minStringCase = EMPTY_STRING;
        maxStringCase = EMPTY_STRING;
        minStringICase = EMPTY_STRING;
        maxStringICase = EMPTY_STRING;

        columnStringValue = EMPTY_STRING;
        columnStringLength = 0;
    }


    /**
     * Calculate string-specific statistics by accommodating the value and frequency/count
     */
    @Override
    public void accomodate(Object columnValue, Long columnCount) {

        accomodateCommon(columnValue, columnCount);

        if (columnValue != null) {

            columnStringValue = String.valueOf(columnValue);
            columnStringLength = columnStringValue.length();

            if (maxLength < columnStringLength) {
                maxLength = columnStringLength;
                longestString = columnStringValue;
            }

            /* Empty strings not considered for:
             * - minLength, shortestString metrics
             * - minStringCase, maxStringCase, minStringICase, maxStringICase metrics
             */
            if (!columnStringValue.isEmpty()) {

                if (minLength > columnStringLength) {
                    minLength = columnStringLength;
                    shortestString = columnStringValue;
                }

                if (!initializationFlag) {
                    minStringCase = columnStringValue;
                    maxStringCase = columnStringValue;
                    minStringICase = columnStringValue;
                    maxStringICase = columnStringValue;
                    initializationFlag = true;
                } else {

                    if (minStringCase.compareTo(columnStringValue) > 0) {
                        minStringCase = columnStringValue;
                    }

                    if (maxStringCase.compareTo(columnStringValue) < 0) {
                        maxStringCase = columnStringValue;
                    }

                    if (minStringICase.compareToIgnoreCase(columnStringValue) > 0) {
                        minStringICase = columnStringValue;
                    }

                    if (maxStringICase.compareToIgnoreCase(columnStringValue) < 0) {
                        maxStringICase = columnStringValue;
                    }
                }

            }

            if (columnStringValue.isEmpty()) {
                emptyCount += columnCount;
            }

            doPercentageCalculations();
        }
    }


    /**
     * Combine with another column statistics
     */
    @Override
    public void combine(StandardColumnStatistics v_columnStatistics) {

        combineCommon(v_columnStatistics);

        StringColumnStatistics vString_columnStatistics = (StringColumnStatistics) v_columnStatistics;

        maxLength = Math.max(maxLength, vString_columnStatistics.maxLength);

        /* Empty strings not considered for:
         * - minLength, shortestString metrics
         * - minStringCase, maxStringCase, minStringICase, maxStringICase metrics
         */
        if ((minLength != Integer.MAX_VALUE) && (vString_columnStatistics.minLength != Integer.MAX_VALUE)) {

            if (minLength > vString_columnStatistics.minLength) {
                minLength = vString_columnStatistics.minLength;
                shortestString = vString_columnStatistics.shortestString;
            }
        } else if (minLength == Integer.MAX_VALUE) {
            minLength = vString_columnStatistics.minLength;
            shortestString = vString_columnStatistics.shortestString;
        }
        /*
        kept for readability
        else if (vString_columnStatistics.minLength == Integer.MAX_VALUE) {
                //no operation
        }*/
        else {
            minLength = 0;
        }

        if ((initializationFlag) && (vString_columnStatistics.initializationFlag)) {
            if (minStringCase.compareTo(vString_columnStatistics.minStringCase) > 0) {
                minStringCase = vString_columnStatistics.minStringCase;
            }

            if (maxStringCase.compareTo(vString_columnStatistics.maxStringCase) < 0) {
                maxStringCase = vString_columnStatistics.maxStringCase;
            }

            if (minStringICase.compareToIgnoreCase(vString_columnStatistics.minStringICase) > 0) {
                minStringICase = vString_columnStatistics.minStringICase;
            }

            if (maxStringICase.compareToIgnoreCase(vString_columnStatistics.maxStringICase) < 0) {
                maxStringICase = vString_columnStatistics.maxStringICase;
            }

        } else if (!initializationFlag) {
            minStringCase = vString_columnStatistics.minStringCase;
            maxStringCase = vString_columnStatistics.maxStringCase;
            minStringICase = vString_columnStatistics.minStringICase;
            maxStringICase = vString_columnStatistics.maxStringICase;
        }
        /*
        kept for readability
        else if (!vString_columnStatistics.initializationFlag) {
                //no operation.
        }
        else {
                //no operation.
        }
        */

        if (longestString.length() < vString_columnStatistics.longestString.length()) {
            longestString = vString_columnStatistics.longestString;
        }

        emptyCount += vString_columnStatistics.emptyCount;

        doPercentageCalculations();
    }


    /*
     * Calculate percentage metrics
     */
    private void doPercentageCalculations() {
        percEmptyValues = ((double) emptyCount / totalCount) * 100;
    }


    /**
     * Write statistics for output result table
     */
    @Override
    public List<OutputRow> getStatistics() {
        final List<OutputRow> rows = new ArrayList<>();

        writeStatisticsCommon(rows);

        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX_LENGTH), String.valueOf(maxLength)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN_LENGTH), String.valueOf(minLength)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.LONGEST_STRING), String.valueOf(longestString)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.SHORTEST_STRING), String.valueOf(shortestString)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.EMPTY_COUNT), String.valueOf(emptyCount)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.PERC_EMPTY_VALUES), df.format(percEmptyValues)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN_STRING_CASE), String.valueOf(minStringCase)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX_STRING_CASE), String.valueOf(maxStringCase)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN_STRING_ICASE), String.valueOf(minStringICase)));
        rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX_STRING_ICASE), String.valueOf(maxStringICase)));
        return rows;
    }


    /**
     * Print statistics to console
     */
    @Override
    public String getVerboseStatistics() {

        return "{\n" + getVerboseStatisticsCommon()
               + "\n"
               + "StringColumnStatistics ["
               + "maxLength=" + maxLength
               + ", minLength=" + minLength
               + ", longestString=" + longestString
               + ", shortestString=" + shortestString
               + ", emptyCount=" + emptyCount
               + ", percEmptyValues=" + df.format(percEmptyValues)
               + ", minStringCaseSensitive=" + minStringCase
               + ", maxStringCaseSensitive=" + maxStringCase
               + ", minStringCaseInsensitive=" + minStringICase
               + ", maxStringCaseInsensitive=" + maxStringICase
               + "]\n}";
    }


    /**
     * Get length of longest string
     *
     * @return max length
     */
    public int getMaxLength() {
        return maxLength;
    }


    /**
     * Get length of shortest string
     *
     * @return min length
     */
    public int getMinLength() {
        return minLength;
    }


    /**
     * Get value of longest string
     *
     * @return longest string
     */
    public String getLongestString() {
        return longestString;
    }


    /**
     * Get value of shortest string (empty string is still considered a string)
     *
     * @return shortest string
     */
    public String getShortestString() {
        return shortestString;
    }


    /**
     * Get count of empty strings
     *
     * @return empty string count
     */
    public long getEmptyCount() {
        return emptyCount;
    }


    /**
     * Get percentage of empty strings
     *
     * @return perc empty strings
     */
    public double getPercEmptyValues() {
        return percEmptyValues;
    }


    /**
     * Get min string (lexical) (case-sensitive)
     *
     * @return min string
     */
    public String getMinStringCase() {
        return minStringCase;
    }


    /**
     * Get max string (lexical) (case-sensitive)
     *
     * @return max string
     */
    public String getMaxStringCase() {
        return maxStringCase;
    }


    /**
     * Get min string (lexical) (case-insensitive)
     *
     * @return min string
     */
    public String getMinStringICase() {
        return minStringICase;
    }


    /**
     * Get max string (lexical) (case-insensitive)
     *
     * @return max string
     */
    public String getMaxStringICase() {
        return maxStringICase;
    }

}
