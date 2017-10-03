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

import java.util.List;
import java.util.Map;

/**
 * Represents a Batch Job Execution along with some additional information that is transformed from the domain model {@link com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution}
 */
public interface ExecutedJob {

    /**
     * Return the related job instance id for this job execution
     *
     * @return the job instance id for this job execution
     */
    long getInstanceId();

    /**
     * Set the job instance id
     */
    void setInstanceId(long instanceId);

    /**
     * Return the job execution id for this job.  This is the PrimaryKey for this given Job.
     *
     * @return the job execution id for this job
     */
    long getExecutionId();

    /**
     * set the job execution id
     */
    void setExecutionId(long executionId);

    /**
     * Return the display name for this job.
     *
     * @return the name of the job
     */
    String getJobName();

    /**
     * set the name of the job
     */
    void setJobName(String jobName);

    /**
     * Return the DateTime the job was created
     *
     * @return the time the job was created
     */
    DateTime getCreateTime();

    /**
     * set the create DateTime for the job
     */
    void setCreateTime(DateTime createTime);

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
     * Return a map of key,value pairs with job context level data captured during this job execution
     *
     * @return a map of values captured during the job execution
     */
    Map<String, Object> getExecutionContext();

    /**
     * set the map of execution data to this job
     */
    void setExecutionContext(Map<String, Object> executionContext);

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
     * Return a map of the job parameters used to setup/start this job
     *
     * @return a map of the job parameters used to setup/start this job
     */
    Map<String, Object> getJobParameters();

    /**
     * set the map of parameters used to setup/start this job
     */
    void setJobParameters(Map<String, Object> jobParameters);

    /**
     * Return the DateTime this job was last updated
     *
     * @return the DateTime the job was last updated
     */
    DateTime getLastUpdated();

    /**
     * set the last updated date
     */
    void setLastUpdated(DateTime lastUpdated);

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
     * Return all steps that have been executed during this job execution
     *
     * @return the steps that have been executed during this job execution
     */
    List<ExecutedStep> getExecutedSteps();

    /**
     * set the step executions
     */
    void setExecutedSteps(List<ExecutedStep> executedSteps);

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

    /**
     * Return the job type. 2 supported types (CHECK, or FEED).
     *
     * @return the type of job
     * @see com.thinkbiganalytics.jobrepo.common.constants.FeedConstants  for more information on the types of jobs
     */
    String getJobType();

    /**
     * set the job type
     */
    void setJobType(String jobType);

    /**
     * Return the feed name associated with this job
     *
     * @return the feed associated with this job
     */
    String getFeedName();

    /**
     * set the feed name associated with this job
     */
    void setFeedName(String feedName);



    /**
     * Return the feed id associated with this job
     *
     * @return the feedId associated with this job
     */
    String getFeedId();

    /**
     * set the feed id associated with this job
     */
    void setFeedId(String feedId);

    /**
     * Return a user friendly status of this job
     *
     * @return a user friendly status of this job
     */
    String getDisplayStatus();

    /**
     *
     * @return true if this is a stream job, false if not
     */
    boolean isStream();
}
