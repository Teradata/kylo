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

import org.joda.time.DateTime;

/**
 * Represents a Batch Job Execution in the context of a Feed
 */
public interface ExecutedFeed {

    /**
     * Return the feed name
     *
     * @return the feed name
     */
    String getName();

    /**
     * set the feed name
     */
    void setName(String name);

    /**
     * Return the feed job instance id
     *
     * @return the job instance id for this feed/job execution
     */
    Long getFeedInstanceId();

    /**
     * set the feed job instance id
     */
    void setFeedInstanceId(Long feedInstanceId);

    /**
     * Return the feed job execution id
     *
     * @return the job execution id
     */
    Long getFeedExecutionId();

    /**
     * set the feed job execution id
     */
    void setFeedExecutionId(Long feedExecutionId);


    /**
     * Return the end time of the job
     *
     * @return return the end time of the job
     */
    DateTime getEndTime();

    /**
     * set the end time of the job
     */
    void setEndTime(DateTime endTime);

    /**
     * Return a String representing the status of this job
     *
     * @see com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants.ExitCode
     */
    String getExitCode();

    /**
     * set the exit code for the job
     */
    void setExitCode(String exitCode);

    /**
     * Return a message indicating details of what happened during the job execution.  This may contain success/failure messages summarizing the job execution as a whole
     *
     * @return a message indicating the details of this job execution
     */
    String getExitStatus();

    /**
     * set the exit message indicating details of what happened during the job execution
     */
    void setExitStatus(String exitStatus);

    /**
     * Return the DateTime the job was started
     *
     * @return the DateTime the job was started
     */
    DateTime getStartTime();

    /**
     * set the start time for the job
     *
     * @param startTime the start time for the job
     */
    void setStartTime(DateTime startTime);

    /**
     * Return the job status indicating overall success or failure of the job
     *
     * @return the status of the job
     */
    ExecutionStatus getStatus();

    /**
     * set the status for this job
     */
    void setStatus(ExecutionStatus status);

    /**
     * Return the run time in millis for this job.  if the job is currently executing it will take the difference from the current time against the jobs start time
     *
     * @return return the run time in millis for this job.
     */
    Long getRunTime();

    /**
     * set the runtime for this job
     */
    void setRunTime(Long runTime);

    /**
     * Return the time in millis since the last time this job ran
     *
     * @return return the time, in millis, since the last time this job ran (i.e Now() - {@link #getEndTime()})
     */
    Long getTimeSinceEndTime();

    /**
     * set the time in millis of the time since this job last ran.  (i.e Now() - {@link #getEndTime()})
     */
    void setTimeSinceEndTime(Long timeSinceEndTime);

}
