package com.thinkbiganalytics.jobrepo.service;

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


/**
 * High level services associated with jobs
 */
public interface JobService {

    /**
     * @param executionId The job execution to start again
     * @return The new Job Execution Id
     */
    Long restartJobExecution(final Long executionId) throws JobExecutionException;

    /**
     * Take the given job execution and stop the job.
     *
     * @param executionId The job execution to start again
     * @return true/false
     */
    boolean stopJobExecution(final Long executionId) throws JobExecutionException;

    /**
     * Take the given job execution and abandon the job.
     *
     * @param executionId The job execution to start again
     */
    void abandonJobExecution(final Long executionId) throws JobExecutionException;

    /**
     * Take the given job execution and fail the job.
     *
     * @param executionId The job execution to start again
     */
    void failJobExecution(final Long executionId);


}
