package com.thinkbiganalytics.kylo.spark.job.tasks;

/*-
 * #%L
 * Spark Shell Core
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

import com.thinkbiganalytics.kylo.spark.job.DefaultSparkJobStatus;
import com.thinkbiganalytics.kylo.spark.job.SparkJobCacheService;
import com.thinkbiganalytics.kylo.spark.job.SparkJobStatus;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;

import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Converts a {@code SparkJobResponse} to a {@code SparkJobStatus}.
 *
 * <p>The Spark job results are stored in the {@link SparkJobCacheService}.</p>
 *
 * @see SparkJobCacheService for ideas to improve caching
 */
public class JobStatusFunction implements Function<SparkJobResponse, SparkJobStatus> {

    /**
     * Spark job result cache
     */
    @Nonnull
    private final SparkJobCacheService cache;

    /**
     * Construct a {@code JobStatusFunction}.
     */
    public JobStatusFunction(@Nonnull final SparkJobCacheService cache) {
        this.cache = cache;
    }

    @Nonnull
    @Override
    public SparkJobStatus apply(@Nonnull final SparkJobResponse response) {
        final String cacheId = cache.putResult(response.getResult());
        return new DefaultSparkJobStatus(cacheId, cache);
    }
}
