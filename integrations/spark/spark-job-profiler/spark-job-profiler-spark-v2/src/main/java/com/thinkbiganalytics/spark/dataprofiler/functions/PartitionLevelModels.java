package com.thinkbiganalytics.spark.dataprofiler.functions;

/*-
 * #%L
 * thinkbig-spark-job-profiler-spark-v2
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

import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.types.StructField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import scala.Tuple2;


/**
 * Get partition-level statistics models
 */
public class PartitionLevelModels implements
                                  FlatMapFunction<Iterator<Tuple2<Tuple2<Integer, Object>, Integer>>, StatisticsModel> {

    private Map<Integer, StructField> schemaMap = new HashMap<>();


    public PartitionLevelModels(Broadcast<Map<Integer, StructField>> bSchemaMap) {
        schemaMap = bSchemaMap.value();
    }

    @Override
    public Iterator<StatisticsModel> call(Iterator<Tuple2<Tuple2<Integer, Object>, Integer>> iter) throws Exception {
        StatisticsModel statisticsModel = new StatisticsModel();

        while (iter.hasNext()) {
            Tuple2<Tuple2<Integer, Object>, Integer> item = iter.next();
            Integer columnIndex = item._1()._1();
            Object columnValue = item._1()._2();
            Long count = (long) item._2();
            statisticsModel.add(columnIndex, columnValue, count, schemaMap.get(columnIndex));
        }

        List<StatisticsModel> listStatisticsModels = new ArrayList<>();
        listStatisticsModels.add(statisticsModel);
        return listStatisticsModels.iterator();
    }
}
