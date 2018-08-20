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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

/**
 * Caches Spark job results for a limited time.
 *
 * <p>NOTE: An improvement would be to not cache the results but only send it to the current subscribers of {@link SparkJobContext}. If the results are needed again after the processor has completed,
 * they can be retrieved again from Spark.</p>
 */
@Service
public class SparkJobCacheService {

    /**
     * Executes cache maintenance tasks
     */
    @Nonnull
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("spark-job-cache-%d").build());

    /**
     * Spark job result cache
     */
    @Nonnull
    private final Map<String, SparkJobResult> results = new ConcurrentHashMap<>();

    /**
     * Time to retain Spark job results
     */
    private long timeToLive;

    /**
     * Removes the specified Spark job result from this cache.
     */
    public void clearResult(@Nonnull final String id) {
        results.remove(id);
    }

    /**
     * Gets the specified Spark job result and removes it from this cache.
     */
    @Nullable
    public SparkJobResult getResult(@Nonnull final String id) {
        final SparkJobResult result = results.get(id);
        clearResult(id);
        return result;
    }

    /**
     * Adds the specified Spark job result to this cache and returns a cache identifier.
     */
    @Nonnull
    public String putResult(@Nonnull final SparkJobResult result) {
        final String id = UUID.randomUUID().toString();
        results.put(id, result);
        executor.schedule(() -> clearResult(id), timeToLive, TimeUnit.MILLISECONDS);
        return id;
    }

    /**
     * Sets the time to retain Spark job results.
     */
    @Value("${spark.job.result.time-to-live:300000}")  // defaults to 5 minutes
    public void setResultTimeToLive(final long timeToLive) {
        if (timeToLive > 0) {
            this.timeToLive = timeToLive;
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
