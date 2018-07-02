package com.thinkbiganalytics.spark.dataprofiler.histo;
/*-
 * #%L
 * HistogramStatistics
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.spark.dataprofiler.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.DoubleFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import scala.Tuple2;

/**
 * Generates a histogram for the column and stores the result as a json string in the statistics model
 */
public class HistogramStatistics implements ColumnStatistics, Serializable {

    private static final Logger log = LoggerFactory.getLogger(HistogramStatistics.class);

    private static final long serialVersionUID = -6099650489540200379L;

    int bins;
    List<OutputRow> outputRows = new ArrayList<>();

    public HistogramStatistics(ProfilerConfiguration config) {
        this.bins = (config.getBins() != null && config.getBins() > 0 ? config.getBins() : 5);
    }

    public void accomodate(Integer columnIndex, JavaRDD<Row> javaRDD, StructField columnField) {
        try {
            if (isNumeric(columnField)) {

                Tuple2<double[], long[]> histogram = javaRDD.mapToDouble(new DoubleFunction<Row>() {
                    @Override
                    public double call(Row row) throws Exception {
                        return Double.parseDouble(row.get(0).toString());
                    }
                }).histogram(bins);

                ObjectMapper mapper = new ObjectMapper();
                String jsonHisto = mapper.writeValueAsString(histogram);

                OutputRow row = new OutputRow(columnField.name(), "HISTO", jsonHisto);
                this.outputRows.add(row);
            }
        } catch (Exception e) {
            log.warn("Histogram generation failed for column {}", columnField.name(), e);
        }
    }

    private boolean isNumeric(StructField columnField) {
        DataType columnDataType = columnField.dataType();

        switch (columnDataType.simpleString()) {
            case "tinyint":
                break;
            case "smallint":
                break;
            case "int":
                break;
            case "float":
                break;
            case "double":
                break;
            case "long":
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public List<OutputRow> getStatistics() {
        return this.outputRows;
    }

    public static void main(String[] args) {

    }
}
