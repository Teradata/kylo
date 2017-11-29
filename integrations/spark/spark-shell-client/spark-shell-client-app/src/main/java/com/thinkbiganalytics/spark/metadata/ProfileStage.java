package com.thinkbiganalytics.spark.metadata;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.thinkbiganalytics.spark.dataprofiler.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.Profiler;
import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.model.TransformResult;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Profiles the data and adds it to the result.
 */
public class ProfileStage implements Function<TransformResult, TransformResult> {

    /**
     * Data profiler.
     */
    @Nonnull
    private final Profiler profiler;

    /**
     * Constructs a {@code ProfileStage}.
     */
    public ProfileStage(@Nonnull final Profiler profiler) {
        this.profiler = profiler;
    }

    @Nonnull
    @Override
    public TransformResult apply(@Nullable final TransformResult result) {
        Preconditions.checkNotNull(result);

        // Profile data set
        final StatisticsModel dataStats = profiler.profile(result.getDataSet(), new ProfilerConfiguration());

        // Add stats to result
        if (dataStats != null) {
            final List<OutputRow> profile = (result.getProfile() != null) ? new ArrayList<>(result.getProfile()) : new ArrayList<OutputRow>(dataStats.getColumnStatisticsMap().size());

            for (final ColumnStatistics columnStats : dataStats.getColumnStatisticsMap().values()) {
                profile.addAll(columnStats.getStatistics());
            }

            result.setProfile(profile);
        }

        return result;
    }
}
