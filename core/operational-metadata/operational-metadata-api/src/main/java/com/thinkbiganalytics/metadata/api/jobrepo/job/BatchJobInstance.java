package com.thinkbiganalytics.metadata.api.jobrepo.job;

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

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import java.util.List;

/**
 * Represents the Batch job instance.
 * A job instance can hold multiple references to job executions if a job is replayed/rerun.
 */
public interface BatchJobInstance {

    /**
     * Return the job instance id
     *
     * @return the job instance id
     */
    Long getJobInstanceId();

    /**
     * Return the version of the job instance
     *
     * @return the version for this job instance
     */
    Long getVersion();

    /**
     * Return the name of the job.
     *
     * @return the name of the job
     */
    String getJobName();

    /**
     * Return a unique key identifying this job instance
     *
     * @return a unique key identifying this job instance
     */
    String getJobKey();

    /**
     * Return a list of all job executions attached to this job instance
     *
     * @return a list of all job executions attached to this job instance
     */
    List<BatchJobExecution> getJobExecutions();

    /**
     * Return the feed for which this job instance belongs to
     *
     * @return the feed attached to this job instance
     */
    OpsManagerFeed getFeed();
}
