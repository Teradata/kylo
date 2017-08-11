package com.thinkbiganalytics.metadata.api.feed;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;

import org.joda.time.DateTime;

/**
 * Represents overall feed health and job execution summary data for a given feed.
 */
public interface FeedHealth {

    /**
     * Return the feed identifier
     *
     * @return the feed id
     */
    OpsManagerFeed.ID getFeedId();

    /**
     * Return the name of the feed.
     */
    String getFeedName();

    /**
     * Return the latest batch job execution id {@link BatchJobExecution#getJobExecutionId()}
     *
     * @return the latest batch job execution
     */
    Long getJobExecutionId();

    /**
     * Return the latest job instance id with respect to the {@link #getJobInstanceId()}
     *
     * @return the latest job instance id
     * @see BatchJobInstance#getJobInstanceId()
     */
    Long getJobInstanceId();

    /**
     * Return the latest batch job execution start time.
     * The job is referenced via the {@link #getJobExecutionId()}
     *
     * @return the latest batch job execution start time
     */
    DateTime getStartTime();

    /**
     * Return the latest batch job execution end time.
     * The job is referenced via the {@link #getJobExecutionId()}
     *
     * @return the latest batch job execution end time.
     */
    DateTime getEndTime();

    /**
     * Return the status for the latest job execution.
     * The job is referenced via the {@link #getJobExecutionId()}
     *
     * @return the status for the latest job execution.
     */
    BatchJobExecution.JobStatus getStatus();

    /**
     * Return the exit code for the latest job execution.
     * The job is referenced via the {@link #getJobExecutionId()}
     *
     * @return the exit code for the latest job execution
     */
    ExecutionConstants.ExitCode getExitCode();

    /**
     * Return a message indicating information about what happened during the latest job execution.
     * The job is referenced via the {@link #getJobExecutionId()}
     *
     * @return a message indicating information about what happened during the latest job execution
     */
    String getExitMessage();

    /**
     * Return the number of batch job executions that have been run for this feed
     *
     * @return the number of batch job executions that have been run for this feed
     */
    Long getAllCount();

    /**
     * Return the number of batch job executions that have a status {@link BatchJobExecution#getStatus()} of Failed
     *
     * @return the number of batch job executions that have Failed
     */
    Long getFailedCount();

    /**
     * Return the number of batch job executions that are not Abandoned and have a status, {@link BatchJobExecution#getStatus()}, or exit code {@link BatchJobExecution#getExitCode()} of Completed
     * indicating the Job is complete and has not failed.
     *
     * @return the number of batch job executions that have Completed.
     */
    Long getCompletedCount();

    /**
     * Return the number of batch job executions that have a status {@link BatchJobExecution#getStatus()} of Abandoned
     *
     * @return the number of batch job executions that have Abandoned.
     */
    Long getAbandonedCount();

    /**
     * Return the number of batch job executions that are running
     *
     * @return the number of batch job executions that are running
     */
    Long getRunningCount();

    /**
     *
     * @return true if streaming feed
     */
    boolean isStream();
}
