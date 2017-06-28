package com.thinkbiganalytics.spark.dataprofiler.model;

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
import com.thinkbiganalytics.spark.dataprofiler.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.columns.BigDecimalColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DateColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DoubleColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.IntegerColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.LongColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ShortColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StandardColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StringColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.UnsupportedColumnStatistics;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;


/**
 * Class to store the profile statistics
 */
@SuppressWarnings("serial")
public class StandardStatisticsModel implements Serializable, StatisticsModel {

    private static final Logger log = LoggerFactory.getLogger(StandardStatisticsModel.class);
    private final Map<Integer, StandardColumnStatistics> columnStatisticsMap = new HashMap<>();

    @Nonnull
    private final ProfilerConfiguration profilerConfiguration;

    public StandardStatisticsModel(@Nonnull final ProfilerConfiguration profilerConfiguration) {
        this.profilerConfiguration = profilerConfiguration;
    }

    /**
     * Include a column value in calculation of profile statistics for the column
     *
     * @param columnIndex numeric index of column (0-based)
     * @param columnValue value in column
     * @param columnCount number of times value is found in column
     * @param columnField schema information of the column
     */
    public void add(Integer columnIndex, Object columnValue, Long columnCount, StructField columnField) {

        StandardColumnStatistics newColumnStatistics;
        DataType columnDataType = columnField.dataType();

        switch (columnDataType.simpleString()) {

            /* === Group 1 ===*/

            /*
             * Hive datatype: 		TINYINT
             * SparkSQL datatype: 	        tinyint
             * Java datatype:		Byte
             */
            case "tinyint":
                newColumnStatistics = new ByteColumnStatistics(columnField, profilerConfiguration);
                break;


            /*
             * Hive datatype: 		SMALLINT
             * SparkSQL datatype: 	        smallint
             * Java datatype:		Short
             */
            case "smallint":
                newColumnStatistics = new ShortColumnStatistics(columnField, profilerConfiguration);
                break;


            /*
             * Hive datatype: 		INT
             * SparkSQL datatype: 	        int
             * Java datatype:		Int
             */
            case "int":
                newColumnStatistics = new IntegerColumnStatistics(columnField, profilerConfiguration);
                break;


            /*
             * Hive datatype: 		BIGINT
             * SparkSQL datatype: 	        bigint
             * Java datatype:		Long
             */
            case "bigint":
                newColumnStatistics = new LongColumnStatistics(columnField, profilerConfiguration);
                break;



            /* === Group 2 === */

            /*
             * Hive datatype: 		FLOAT
             * SparkSQL datatype: 	        float
             * Java datatype:		Float
             */
            case "float":
                newColumnStatistics = new FloatColumnStatistics(columnField, profilerConfiguration);
                break;


            /*
             * Hive datatype: 		DOUBLE
             * SparkSQL datatype: 	        double
             * Java datatype:		Double
             */
            case "double":
                newColumnStatistics = new DoubleColumnStatistics(columnField, profilerConfiguration);
                break;



            /* === Group 3 === */

            /*
             * Hive datatypes: 		STRING, VARCHAR
             * SparkSQL datatype: 	        string
             * Java datatype:		String
             */
            case "string":
                newColumnStatistics = new StringColumnStatistics(columnField, profilerConfiguration);
                break;



            /* === Group 4 === */

            /*
             * Hive datatype: 		BOOLEAN
             * SparkSQL datatype: 	        boolean
             * Java datatype:		Boolean
             */
            case "boolean":
                newColumnStatistics = new BooleanColumnStatistics(columnField, profilerConfiguration);
                break;



            /* === Group 5 === */

            /*
             * Hive datatype: 		DATE
             * SparkSQL datatype: 	        date
             * Java datatype:		java.sql.Date
             */
            case "date":
                newColumnStatistics = new DateColumnStatistics(columnField, profilerConfiguration);
                break;


            /*
             * Hive datatype: 		TIMESTAMP
             * SparkSQL datatype: 	        timestamp
             * Java datatype:		java.sql.Timestamp
             */
            case "timestamp":
                newColumnStatistics = new TimestampColumnStatistics(columnField, profilerConfiguration);
                break;



            /* === Group 6 === */

            default:
            /*
             * Hive datatype: 		DECIMAL
             * SparkSQL datatype: 	        decimal
             * Java datatype:		java.math.BigDecimal
             *
             * Handle the decimal type here since it comes with scale and precision e.g. decimal(7,5)
             */
                String decimalTypeRegex = "decimal\\S+";
                if (columnDataType.simpleString().matches(decimalTypeRegex)) {
                    newColumnStatistics = new BigDecimalColumnStatistics(columnField, profilerConfiguration);
                }

                /*
                 * Hive datatypes: CHAR, BINARY, ARRAY, MAP, STRUCT, UNIONTYPE
                 */
                else {
                    if (log.isWarnEnabled()) {
                        log.warn("[PROFILER-INFO] Unsupported data type: {}", columnDataType.simpleString());
                    }
                    newColumnStatistics = new UnsupportedColumnStatistics(columnField, profilerConfiguration);
                }
        }

        if (!columnStatisticsMap.containsKey(columnIndex)) {
            columnStatisticsMap.put(columnIndex, newColumnStatistics);
        }

        StandardColumnStatistics currentColumnStatistics = columnStatisticsMap.get(columnIndex);
        currentColumnStatistics.accomodate(columnValue, columnCount);
    }


    /**
     * Combine another statistics model
     *
     * @param statisticsModel model to combine with
     */
    public void combine(StandardStatisticsModel statisticsModel) {

        for (Integer k_columnIndex : statisticsModel.columnStatisticsMap.keySet()) {

            StandardColumnStatistics columnStatistics = columnStatisticsMap.get(k_columnIndex);
            StandardColumnStatistics v_columnStatistics = statisticsModel.columnStatisticsMap.get(k_columnIndex);

            if (columnStatistics != null) {

                columnStatistics.combine(v_columnStatistics);

            } else {
                columnStatisticsMap.put(k_columnIndex, v_columnStatistics);
            }
        }
    }


    /**
     * Print the profile statistics on console
     *
     * @return profile model string
     */
    private String printModel() {

        StringBuilder sb = new StringBuilder();
        sb.append("====== Statistics Model ======");
        sb.append("\n");

        for (Map.Entry<Integer, StandardColumnStatistics> entry : columnStatisticsMap.entrySet()) {
            sb.append("=== Column #")
                .append(entry.getKey())
                .append("\n");

            sb.append(entry.getValue().getVerboseStatistics())
                .append("\n");
        }

        sb.append("==============================");
        return sb.toString();
    }


    /**
     * Print the profile statistics on console
     */
    @Override
    public String toString() {
        return printModel();
    }


    /**
     * Get the column statistics map (column number mapped to column statistics)
     *
     * @return column statistics map
     */
    @Override
    @SuppressWarnings({"unchecked", "squid:S1905"})
    public Map<Integer, ColumnStatistics> getColumnStatisticsMap() {
        return (Map) columnStatisticsMap;
    }
}
