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
import com.thinkbiganalytics.spark.metadata.Job;
import com.thinkbiganalytics.spark.metadata.SaveJob;
import com.thinkbiganalytics.spark.metadata.StandardSparkListener;
import com.thinkbiganalytics.spark.metadata.TransformJob;

import org.apache.spark.Success$;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.SparkListenerStageCompleted;
import org.apache.spark.scheduler.SparkListenerStageSubmitted;
import org.apache.spark.scheduler.SparkListenerTaskEnd;
import org.apache.spark.scheduler.StageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import scala.collection.JavaConverters;

/**
 * Tracks the progress of executing and recently completed jobs.
 *
 * <p>Ensures that each job is executed in a separate thread. This is required to track Spark job statistics.</p>
 */
public class JobTrackerService extends StandardSparkListener {

    private static final Logger log = LoggerFactory.getLogger(JobTrackerService.class);

    /**
     * Key for the job group ID property
     */
    private static final String SPARK_JOB_GROUP_ID = "spark.jobGroup.id";

    /**
     * Class loader for job threads
     */
    private final ClassLoader contextClassLoader;

    /**
     * Executes jobs in separate threads
     */
    private final ScheduledExecutorService executor;

    /**
     * Map of group id to job
     */
    private final Cache<String, Job<?>> groups;

    /**
     * Map of job id to job
     */
    private final Map<Integer, Job<?>> jobs = new ConcurrentHashMap<>();

    /**
     * Map of stage id to job
     */
    private final Map<Integer, Job<?>> stages = new ConcurrentHashMap<>();

    /**
     * Constructs a {@code JobTrackerService} that uses the specified class loader for job threads.
     */
    public JobTrackerService(@Nonnull final ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
        executor = Executors.newScheduledThreadPool(2, createThreadFactory());
        groups = createCache();
    }

    /**
     * Gets the job with the specified group id.
     *
     * @param groupId the group id
     * @return the job
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <T> Optional<Job<T>> getJob(@Nonnull final String groupId) {
        return (Optional) Optional.fromNullable(groups.getIfPresent(groupId));
    }

    /**
     * Gets the SaveJob with the specified group id.
     *
     * @param groupId the group id
     * @return the save job
     */
    @Nonnull
    public Optional<SaveJob> getSaveJob(@Nonnull final String groupId) {
        final Job<?> job = getJob(groupId).orNull();
        return (job != null && job instanceof SaveJob) ? Optional.of((SaveJob) job) : Optional.<SaveJob>absent();
    }

    /**
     * Gets the TransformJob with the specified group id.
     *
     * @param groupId the group id
     * @return the transform job
     */
    @Nonnull
    public Optional<TransformJob> getTransformJob(@Nonnull final String groupId) {
        final Job<?> job = getJob(groupId).orNull();
        return (job != null && job instanceof TransformJob) ? Optional.of((TransformJob) job) : Optional.<TransformJob>absent();
    }

    @Override
    public void onStageCompleted(@Nonnull final SparkListenerStageCompleted event) {
        log.trace("Stage {} completed with failure {}", event.stageInfo().stageId(), event.stageInfo().failureReason());
        final int completed = (event.stageInfo().failureReason().isEmpty()) ? event.stageInfo().numTasks() : 0;

        final Job<?> job = stages.get(event.stageInfo().stageId());
        if (job != null) {
            job.onStageProgress(event.stageInfo(), completed);
        }
    }

    @Override
    public void onStageSubmitted(@Nonnull final SparkListenerStageSubmitted event) {
        log.trace("Stage {} submitted with {} tasks", event.stageInfo().stageId(), event.stageInfo().numTasks());
        final Job<?> job = stages.get(event.stageInfo().stageId());
        if (job != null) {
            job.onStageProgress(event.stageInfo(), 0);
        }
    }

    @Override
    public void onTaskEnd(@Nonnull final SparkListenerTaskEnd event) {
        log.trace("Task {} completed for stage {} with {}", event.taskInfo().taskId(), event.stageId(), event.reason());
        if (event.reason() == Success$.MODULE$) {
            final Job<?> job = stages.get(event.stageId());
            if (job != null) {
                job.onTaskEnd();
            }
        }
    }

    @Override
    public void onJobStart(@Nonnull final SparkListenerJobStart event) {
        log.trace("Job {} started with stages {}", event.jobId(), event.stageIds());

        // Find transform job ID
        final String groupId = event.properties().getProperty(SPARK_JOB_GROUP_ID);
        if (groupId == null) {
            return;
        }

        // Find transform job
        final Optional<Job<Object>> job = getJob(groupId);
        if (!job.isPresent()) {
            log.debug("Missing job {}", groupId);
            return;
        }

        // Update job info
        job.get().setJobId(event.jobId());
        job.get().setStages(JavaConverters.seqAsJavaListConverter(event.stageInfos()).asJava());
        jobs.put(event.jobId(), job.get());

        for (final Object stageId : JavaConverters.seqAsJavaListConverter(event.stageIds()).asJava()) {
            stages.put((Integer) stageId, job.get());
        }
    }

    @Override
    public void onJobEnd(@Nonnull final SparkListenerJobEnd event) {
        log.trace("Job {} ended", event.jobId());
        final Job<?> job = jobs.remove(event.jobId());
        if (job != null) {
            job.onJobEnd();
            for (final StageInfo stage : job.getStages()) {
                stages.remove(stage.stageId());
            }
        }
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
    public <T> void submitJob(@Nonnull final Job<T> job) {
        groups.put(job.getGroupId(), job);
        executor.execute(job);
    }

    /**
     * Creates a job cache.
     */
    @Nonnull
    private Cache<String, Job<?>> createCache() {
        final Cache<String, Job<?>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .removalListener(new RemovalListener<String, Job<?>>() {
                @Override
                public void onRemoval(@Nonnull final RemovalNotification<String, Job<?>> notification) {
                    //noinspection ConstantConditions
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
            public Thread newThread(@Nonnull final Runnable r) {
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
