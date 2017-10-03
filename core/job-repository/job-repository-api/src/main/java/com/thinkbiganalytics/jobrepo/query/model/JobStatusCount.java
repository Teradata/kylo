package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-api
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

import java.util.Date;

/**
 * Get a count of the jobs for a grouped by status
 * Used in getting counts for all jobs for a given feed that are either Running or have failed
 */
public interface JobStatusCount {

    /**
     * Return the total count
     *
     * @return the count of jobs that match the {@link #getStatus()}
     */
    Long getCount();

    /**
     * Set the job count
     */
    void setCount(Long count);

    String getFeedId();


    void setFeedId(String feedId);

    /**
     * Return the feed name
     *
     * @return the feed name
     */
    String getFeedName();

    /**
     * set the feed name
     */
    void setFeedName(String feedName);

    /**
     * Return the job name
     *
     * @return the name of the job
     */
    String getJobName();

    /**
     * set the job name
     */
    void setJobName(String jobName);

    /**
     * Return the status
     *
     * @return the job status
     */
    String getStatus();

    /**
     * set the job status
     */
    void setStatus(String status);

    /**
     * Return the date of the job, or date indicating the summary for the job status
     *
     * @return the date of the job, or date indicating the summary for the job status
     */
    Date getDate();

    /**
     * set the date of the job or date of the summary data
     */
    void setDate(Date date);
}
