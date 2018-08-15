package com.thinkbiganalytics.kylo.spark.job;

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

import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The status of a Spark job.
 */
public class DefaultSparkJobStatus implements SparkJobStatus {

    /**
     * Cached result identifier
     */
    @Nonnull
    private final String cacheId;

    /**
     * Result cache service
     */
    @Nonnull
    private final SparkJobCacheService cacheService;

    /**
     * Constructs a {@code DefaultSparkJobStatus}.
     */
    public DefaultSparkJobStatus(@Nonnull final String cacheId, @Nonnull final SparkJobCacheService cacheService) {
        this.cacheId = cacheId;
        this.cacheService = cacheService;
    }

    @Nullable
    @Override
    public SparkJobResult getResult() {
        return cacheService.getResult(cacheId);
    }
}
