package com.thinkbiganalytics.metadata.api.jobrepo.step;

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
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

/**
 * Represents a Step Execution
 */
public interface BatchStepExecution {

    /**
     * Return the unique id for a step execution
     *
     * @return the step execution id
     */
    Long getStepExecutionId();

    /**
     * Return the version number of this step
     *
     * @return the version number
     */
    Long getVersion();

    /**
     * Return the name of the step.
     *
     * @return the name of the step
     */
    String getStepName();

    /**
     * Return the start time of this step
     *
     * @return the start time of the step
     */
    DateTime getStartTime();

    /**
     * Return the end time of this step
     *
     * @return the end time of the step
     */
    DateTime getEndTime();

    /**
     * Return the status for this step execution
     *
     * @return the status for this step exeuction
     */
    StepStatus getStatus();

    /**
     * set the status for this step
     */
    void setStatus(StepStatus status);

    /**
     * Return the job execution
     *
     * @return the job execution
     */
    BatchJobExecution getJobExecution();

    /**
     * Return the execution context obtained while this step executed
     *
     * @return the execution context obtained during this step execution
     */
    Set<BatchStepExecutionContextValue> getStepExecutionContext();

    /**
     * Return the {@link #getStepExecutionContext()} as a key,value map
     *
     * @return the execution context values as a key,value map
     */
    Map<String, String> getStepExecutionContextAsMap();

    /**
     * Return the exit code for this step indicating if it successful or not
     *
     * @return the exit code for this step
     */
    ExecutionConstants.ExitCode getExitCode();

    /**
     * set the exit code
     */
    void setExitCode(ExecutionConstants.ExitCode exitCode);

    /**
     * Return a message describing this step execution
     *
     * @return a message describing this step execution
     */
    String getExitMessage();

    /**
     * set the message describing the step exeuction
     */
    void setExitMessage(String msg);

    /**
     * Return the date the step was updated
     */
    DateTime getLastUpdated();

    /**
     * Return true if finished, false if still running
     *
     * @return true if finished, false if still running
     */
    boolean isFinished();

    /**
     * Return the reference to the NiFi step execution
     *
     * @return the nifi step execution
     */
    NifiEventStepExecution getNifiEventStepExecution();

    /**
     * Step status
     */
    public enum StepStatus {
        COMPLETED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
        FAILED,
        ABANDONED,
        UNKNOWN;
    }
}
