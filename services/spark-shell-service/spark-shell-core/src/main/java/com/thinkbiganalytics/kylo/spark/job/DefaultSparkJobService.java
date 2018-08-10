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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.kylo.spark.SparkException;
import com.thinkbiganalytics.kylo.spark.job.tasks.BatchJobSupplier;
import com.thinkbiganalytics.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Executes Spark job requests and returns the response.
 */
@Service
public class DefaultSparkJobService implements SparkJobService {

    /**
     * Spark job cache service
     */
    @Nonnull
    private final SparkJobCacheService cache;

    /**
     * Executor for Spark jobs
     */
    @Nonnull
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("spark-job-pool-%d").build());

    /**
     * Mapping of context identifier to context
     */
    private Cache<String, DefaultSparkJobContext> jobs;

    /**
     * Spark Shell process manager
     */
    @Nonnull
    private final SparkShellProcessManager processManager;

    /**
     * Spark Shell REST client
     */
    @Nonnull
    private final SparkShellRestClient restClient;

    /**
     * Interval to poll the Spark Shell REST client
     */
    private long pollInterval;

    /**
     * Time to retain a reference to the Spark job
     */
    private long timeToLive;

    /**
     * Construct a {@code DefaultSparkJobService}.
     */
    @Autowired
    public DefaultSparkJobService(@Nonnull final SparkShellProcessManager processManager, @Nonnull final SparkShellRestClient restClient, @Nonnull final SparkJobCacheService cache) {
        this.processManager = processManager;
        this.restClient = restClient;
        this.cache = cache;
    }

    @Nonnull
    @Override
    public SparkJobContext create(@Nonnull final SparkJobRequest request) {
        // Replace parent id with Spark's id
        if (request.getParent() != null && request.getParent().getId() != null) {
            final DefaultSparkJobContext parent = jobs.getIfPresent(request.getParent().getId());
            if (parent != null) {
                request.getParent().setId(parent.getSparkJobId());
            } else {
                throw new SparkException("job.parentExpired");
            }
        }

        // Find Spark process
        final SparkShellProcess process;

        try {
            if (request.getMode() == SparkJobRequest.Mode.BATCH) {
                process = processManager.getSystemProcess();
            } else if (request.getMode() == SparkJobRequest.Mode.INTERACTIVE) {
                process = processManager.getProcessForUser(SecurityContextHolder.getContext().getAuthentication().getName());
            } else {
                throw new SparkException("job.invalid-mode");
            }
        } catch (final InterruptedException e) {
            throw new SparkException("job.cancelled", e);
        }

        // Create task
        final BatchJobSupplier task = new BatchJobSupplier(request, process, restClient);
        task.setPollInterval(pollInterval, TimeUnit.MILLISECONDS);

        // Create context
        final DefaultSparkJobContext context = DefaultSparkJobContext.create(task, cache, executor);
        jobs.put(context.getId(), context);
        return context;
    }

    @Nonnull
    @Override
    public Optional<SparkJobContext> findById(@Nonnull final String id) {
        return Optional.ofNullable(jobs.getIfPresent(id));
    }

    @PostConstruct
    public void init() {
        jobs = Caffeine.newBuilder()
            .executor(executor)
            .expireAfterAccess(timeToLive, TimeUnit.MILLISECONDS)
            .<String, DefaultSparkJobContext>removalListener((id, context, cause) -> {
                if (context != null) {
                    context.cancel();
                }
            })
            .build();
    }

    /**
     * Sets the interval to poll the Spark Shell REST client.
     */
    @Value("${spark.job.poll-interval:1000}")  // defaults to 1 second
    public void setPollInterval(final long pollInterval) {
        this.pollInterval = pollInterval;
    }

    /**
     * Sets the time to retain a reference to the Spark job.
     */
    @Value("${spark.job.time-to-live:3600000}")  // defaults to 1 hour
    public void setTimeToLive(final long timeToLive) {
        this.timeToLive = timeToLive;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
