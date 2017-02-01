package com.thinkbiganalytics.spark.dataprofiler.functions;

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

import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;

import org.apache.spark.api.java.function.Function2;

/**
 * Combine statistics models for two partitions
 */

@SuppressWarnings("serial")
public class CombineModels implements Function2<StatisticsModel, StatisticsModel, StatisticsModel> {

    public StatisticsModel call(StatisticsModel model1, StatisticsModel model2) throws Exception {

        model1.combine(model2);

        return model1;
    }
}
