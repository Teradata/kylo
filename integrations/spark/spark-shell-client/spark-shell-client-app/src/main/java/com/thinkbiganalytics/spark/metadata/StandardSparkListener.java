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

import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.SparkListenerStageCompleted;
import org.apache.spark.scheduler.SparkListenerStageSubmitted;
import org.apache.spark.scheduler.SparkListenerTaskEnd;

import javax.annotation.Nonnull;

/**
 * Base class for Spark listeners.
 */
public abstract class StandardSparkListener {

    /**
     * Called when a stage completes successfully or fails, with information on the completed stage.
     */
    public void onStageCompleted(@Nonnull SparkListenerStageCompleted stageCompleted) {
    }

    /**
     * Called when a stage is submitted
     */
    public void onStageSubmitted(@Nonnull SparkListenerStageSubmitted stageSubmitted) {
    }

    /**
     * Called when a task ends
     */
    public void onTaskEnd(@Nonnull SparkListenerTaskEnd taskEnd) {
    }

    /**
     * Called when a job starts
     */
    public void onJobStart(@Nonnull SparkListenerJobStart jobStart) {
    }

    /**
     * Called when a job ends
     */
    public void onJobEnd(@Nonnull SparkListenerJobEnd jobEnd) {
    }
}
