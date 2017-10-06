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

import org.joda.time.DateTime;

/**
 * Represents the latest {@link BatchJobExecution} for a feed
 * This object holds most of the data related to the latest job execution id for the feed.
 * If needed there is a pointer to the actual {@link BatchJobExecution} object which can be accessed to get more detailed information
 */
public interface LatestFeedJobExecution {

    /**
     * Return the feed
     *
     * @return the feed
     */
    OpsManagerFeed getFeed();


    /**
     * Return the feed name
     *
     * @return the feed name
     */
    String getFeedId();

    /**
     * Return the feed name
     *
     * @return the feed name
     */
    String getFeedName();

    /**
     * Return the latest job execution.  This can be accessed to get additional information about the feed.
     *
     * @return the latest job execution
     */
    BatchJobExecution getJobExecution();

    /**
     * Return the latest job execution start time
     *
     * @return the latest job execution start time
     */
    DateTime getStartTime();

    /**
     * Return the latest job execution end time
     *
     * @return the latest job execution end time
     */
    DateTime getEndTime();

    /**
     * Return the latest job execution status
     *
     * @return the latest job execution status
     */
    BatchJobExecution.JobStatus getStatus();

    /**
     * Return the latest job execution exitCode
     *
     * @return the latest job execution exitCode
     */
    ExecutionConstants.ExitCode getExitCode();

    /**
     * Return the latest job execution exit message
     *
     * @return the latest job execution exit message
     */
    String getExitMessage();

    /**
     * Return the latest job execution id
     *
     * @return the latest job execution id
     */
    Long getJobExecutionId();

    /**
     * Return the latest job instance id
     *
     * @return the latest job instance id
     */
    Long getJobInstanceId();

    /**
     *
     * @return true if Stream, false if not
     */
    boolean isStream();
}
