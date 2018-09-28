package com.thinkbiganalytics.spark.dataprofiler;

/*-
 * #%L
 * kylo-spark-shell-client-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import org.apache.spark.sql.DataFrame;

import java.util.ArrayList;
import java.util.List;

public class LivyProfiler {

    private LivyProfiler() {
    } // do not allow instantiation

    public static List<OutputRow> profileDataFrame(SparkContextService sparkContextService,
                                                   Profiler profiler,
                                                   DataFrame dataFrame) {
        DataSet dataSet = sparkContextService.toDataSet(dataFrame);

        // Profile data set
        ProfilerConfiguration profilerConfiguration = new ProfilerConfiguration();
        profilerConfiguration.setNumberOfTopNValues(50);
        profilerConfiguration.setBins(35);
        final StatisticsModel dataStats = profiler.profile(dataSet, profilerConfiguration);

        // Add stats to result
        if (dataStats != null) {
            List<OutputRow> profile = new ArrayList<OutputRow>(dataStats.getColumnStatisticsMap().size());

            for (final ColumnStatistics columnStats : dataStats.getColumnStatisticsMap().values()) {
                profile.addAll(columnStats.getStatistics());
            }
            return profile;
        }

        // no stats
        return Lists.newArrayList();
    }
}
