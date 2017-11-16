package com.thinkbiganalytics.spark.service;

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

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;

import org.apache.spark.scheduler.StageInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Tracks the progress of executing and recently completed jobs.
 *
 * <p>Ensures that each job is executed in a separate thread. This is required to track Spark job statistics.</p>
 */
public abstract class TransformJobTracker {

    /**
     * Key for the job group ID property
     */
    public static final String SPARK_JOB_GROUP_ID = "spark.jobGroup.id";

    private final ClassLoader contextClassLoader;

    /**
     * Executes jobs in separate threads
     */
    private final ScheduledExecutorService executor;

    /**
     * Map of group id to job
     */
    protected final Cache<String, TransformJob> groups;

    /**
     * Map of job id to job
     */
    protected final Map<Integer, TransformJob> jobs = new ConcurrentHashMap<>();

    /**
     * Map of stage id to job
     */
    protected final Map<Integer, TransformJob> stages = new ConcurrentHashMap<>();

    public TransformJobTracker(@Nonnull final ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
        executor = Executors.newScheduledThreadPool(2, createThreadFactory());
        groups = createCache();
    }

    /**
     * Adds a listener to the Spark context of the specified script engine.
     *
     * @param engine the Spark script engine
     */
    public abstract void addSparkListener(@Nonnull SparkScriptEngine engine);

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Gets the job with the specified group id.
     *
     * @param groupId the group id
     * @return the job
     * @throws IllegalArgumentException if the job does not exist
     */
    @Nonnull
    public Optional<TransformJob> getJob(@Nonnull final String groupId) {
        return Optional.fromNullable(groups.getIfPresent(groupId));
    }

    /**
     * Removes the specified job from this tracker.
     *
     * @param groupId the group id
     */
    public void removeJob(@Nonnull final String groupId) {
        groups.invalidate(groupId);
    }

    /**
     * Submits a job to be executed.
     *
     * @param job the transform job
     */
    public void submitJob(@Nonnull final TransformJob job) {
        groups.put(job.getGroupId(), job);
        executor.execute(job);
    }

    /**
     * Creates a job cache.
     */
    @Nonnull
    private Cache<String, TransformJob> createCache() {
        final Cache<String, TransformJob> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .removalListener(new RemovalListener<String, TransformJob>() {
                @Override
                public void onRemoval(@Nonnull final RemovalNotification<String, TransformJob> notification) {
                    final Optional<Integer> jobId = notification.getValue().getJobId();
                    if (jobId.isPresent()) {
                        jobs.remove(jobId.get());
                    }

                    for (final StageInfo stage : notification.getValue().getStages()) {
                        stages.remove(stage.stageId());
                    }
                }
            })
            .build();

        // Schedule clean-up of groups
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                groups.cleanUp();
            }
        }, 1, 1, TimeUnit.HOURS);

        return cache;
    }

    /**
     * Creates a thread factory for running transform jobs.
     *
     * @return the thread factory
     */
    @Nonnull
    private ThreadFactory createThreadFactory() {
        final ThreadFactory parentThreadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setContextClassLoader(contextClassLoader);
                return thread;
            }
        };

        return new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("transform-job-%d")
            .setThreadFactory(parentThreadFactory)
            .build();
    }
}
