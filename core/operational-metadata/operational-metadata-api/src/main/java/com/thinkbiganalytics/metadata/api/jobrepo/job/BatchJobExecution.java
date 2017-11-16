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

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;

import org.joda.time.DateTime;
import org.omg.CORBA.UNKNOWN;

import java.util.Map;
import java.util.Set;

/**
 * Represents a Batch Job Execution entry
 */
public interface BatchJobExecution {

    /**
     * @return the the job instance this execution is from
     */
    BatchJobInstance getJobInstance();

    /**
     * Return the unique identifier for this job
     *
     * @return the job execution id.
     */
    Long getJobExecutionId();

    /**
     * Return the update version for this job starting with 0.
     * Each time a job is updated its version should increment
     *
     * @return the update version for this job.
     */
    Long getVersion();

    /**
     * Return the create time for this job
     *
     * @return the create time for this job
     */
    DateTime getCreateTime();

    /**
     * set the create time for this job
     */
    void setCreateTime(DateTime createTime);

    /**
     * Return the time this job was started
     *
     * @return the time this job was started
     */
    DateTime getStartTime();

    /**
     *
     * @param startTime
     */
    void setStartTime(DateTime startTime);

    /**
     * Return  the time this job completed
     *
     * @return the time this job completed
     */
    DateTime getEndTime();

    /**
     * set the end time for the job
     */
    void setEndTime(DateTime endTime);

    /**
     * Return the job status
     *
     * @return the job status
     */
    JobStatus getStatus();

    /**
     * set the job status
     */
    void setStatus(JobStatus status);

    /**
     * return the exit code for a job.
     * A job may complete (as indicated by its {@link #getStatus()}), but it may result in an overall failure as indicated in this exit code
     *
     * @return the exit code for a job.
     */
    ExecutionConstants.ExitCode getExitCode();

    void setExitCode(ExecutionConstants.ExitCode exitCode);

    /**
     * Return the exit message with more descriptions about the result of the job
     *
     * @return the exit message with more descriptions about the result of the job
     */
    String getExitMessage();

    /**
     * Set the exit message describing the job
     */
    void setExitMessage(String exitMessage);

    /**
     * Return the date this job record was last updated
     *
     * @return the date this job record was last updated
     */
    DateTime getLastUpdated();

    /**
     * Return any job parameters used to setup this job.
     *
     * @return any job parameters used to setup this job.
     */
    Set<? extends BatchJobExecutionParameter> getJobParameters();

    /**
     * Return the job parameters as a key,value map
     *
     * @return the job parameters as a key,value map
     */
    Map<String, String> getJobParametersAsMap();

    /**
     * Return all step exeucitons for this job
     *
     * @return all step exeucitons for this job
     */
    Set<BatchStepExecution> getStepExecutions();

    /**
     * Return all job execution context values retrieved during this execution
     *
     * @return all job execution context values retrieved during this execution
     */
    Set<BatchJobExecutionContextValue> getJobExecutionContext();

    /**
     * Return the execution context values as a key,value map
     *
     * @return the execution context values as a key,value map
     */
    Map<String, String> getJobExecutionContextAsMap();

    /**
     * Return the NiFi event object used to start this job.
     *
     * @return the NiFi event object used to start this job.
     */
    NifiEventJobExecution getNifiEventJobExecution();

    /**
     * set  the nifi event object used to start this job
     */
    void setNifiEventJobExecution(NifiEventJobExecution nifiEventJobExecution);

    /**
     * Check to see if this job is a failure
     *
     * @return true if failed, false if not
     */
    boolean isFailed();

    /**
     * Check to see if this job is successful
     *
     * @return true if successful, false if not
     */
    boolean isSuccess();

    /**
     * Check to see if this job is finished
     *
     * @return true if finished, false if still running
     */
    boolean isFinished();


    boolean isStream();

    static String RUNNING_DISPLAY_STATUS = "RUNNING";

    /**
     * Various statuses for a Job
     */
    public static enum JobStatus {
        COMPLETED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
        ABANDONED,
        UNKNOWN,
        FAILED;
    }

}
