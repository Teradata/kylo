package com.thinkbiganalytics.spark.dataprofiler;

/*-
 * #%L
 * kylo-spark-job-profiler-api
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Analyses a data set and produces statistics for every column.
 */
public interface Profiler {

    /**
     * Analyses the specified data set and returns the column statistics.
     *
     * @param dataset               the data set
     * @param profilerConfiguration the configuration
     * @return the column statistics
     */
    @Nullable
    StatisticsModel profile(@Nonnull DataSet dataset, @Nonnull ProfilerConfiguration profilerConfiguration);
}
