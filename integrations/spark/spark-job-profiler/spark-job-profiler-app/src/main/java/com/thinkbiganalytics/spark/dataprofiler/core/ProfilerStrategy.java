package com.thinkbiganalytics.spark.dataprofiler.core;

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

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.types.StructField;

import java.util.Map;

/**
 * Created by ru186002 on 10/11/2016.
 */
public interface ProfilerStrategy {

    /**
     * Profile statistics for data frame
     * @param set data frame to analyze
     * @param bSchemaMap
     * @return StatisticsModel
     */
    StatisticsModel profileStatistics(DataSet set, Broadcast<Map<Integer,StructField>> bSchemaMap);
}
