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

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import org.apache.spark.SparkContext;
import org.apache.spark.scheduler.StageInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tracks the status of a Spark job and provides the result.
 *
 * @param <T> result type
 */
public abstract class Job<T> extends FutureTask<T> {

    /**
     * Group id.
     */
    @Nonnull
    private final String groupId;

    /**
     * Spark job id.
     */
    @Nullable
    private Integer jobId;

    /**
     * Spark context.
     */
    @Nonnull
    private final SparkContext sparkContext;

    /**
     * List of Spark stages
     */
    @Nonnull
    private List<StageInfo> stages = new ArrayList<>();

    /**
     * Number of completed tasks
     */
    @Nonnull
    private final AtomicInteger tasksCompleted = new AtomicInteger(0);

    /**
     * Total number of tasks
     */
    @Nonnull
    private final AtomicInteger tasksTotal = new AtomicInteger(0);

    /**
     * Constructs a {@code Job} with the specified id and result supplier.
     */
    protected Job(@Nonnull final String groupId, @Nonnull final Supplier<T> supplier, @Nonnull final SparkContext sparkContext) {
        super(new Callable<T>() {
            @Override
            public T call() {
                return supplier.get();
            }
        });
        this.groupId = groupId;
        this.sparkContext = sparkContext;
    }

    /**
     * Gets the group id.
     */
    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the job progress to completed.
     */
    public void onJobEnd() {
        tasksCompleted.set(tasksTotal.get());
    }

    /**
     * Gets the Spark job id.
     */
    @Nonnull
    public Optional<Integer> getJobId() {
        return Optional.fromNullable(jobId);
    }

    public void setJobId(final int jobId) {
        this.jobId = jobId;
    }

    /**
     * Sets the job progress for the specified stage.
     *
     * @param stage          the current stage
     * @param tasksCompleted the number of completed tasks
     */
    public void onStageProgress(@Nonnull final StageInfo stage, final int tasksCompleted) {
        int sum = 0;
        for (final StageInfo previousStage : stages) {
            if (previousStage.stageId() < stage.stageId()) {
                sum += previousStage.numTasks();
            }
        }
        this.tasksCompleted.set(sum + tasksCompleted);
    }

    /**
     * Sets a task progress to completed.
     */
    public void onTaskEnd() {
        tasksCompleted.incrementAndGet();
    }

    /**
     * Gets the progress of this transformation
     *
     * @return the progress from 0.0 to 1.0
     */
    public Double progress() {
        final int total = tasksTotal.get();
        if (total > 0) {
            return ((double) tasksCompleted.get()) / total;
        } else {
            return 0.0;
        }
    }

    @Override
    public void run() {
        sparkContext.setJobGroup(groupId, "Transform Job", false);
        super.run();
        sparkContext.clearJobGroup();
    }

    @Override
    protected boolean runAndReset() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the Spark stages.
     */
    @Nonnull
    public List<StageInfo> getStages() {
        return stages;
    }

    /**
     * Sets the Spark stages.
     */
    public void setStages(@Nonnull final List<StageInfo> stages) {
        this.stages = stages;
        tasksCompleted.set(0);

        int sum = 0;
        for (final StageInfo stage : stages) {
            sum += stage.numTasks();
        }
        tasksTotal.set(sum);
    }
}
