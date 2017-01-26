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

import java.util.Map;

/**
 * Represents a Step Execution that is part of a {@link ExecutedJob}, usually transformed from the domain {@link com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution}
 */
public interface ExecutedStep {

    /**
     * Return the display name for this step
     *
     * @return the name for this step
     */
    String getStepName();

    /**
     * set the name of this step
     * @param stepName
     */
    void setStepName(String stepName);

    /**
     * Return the status of this step
     * @return the step status
     */
    ExecutionStatus getStatus();

    /**
     * set the status of this step
     * @param status
     */
    void setStatus(ExecutionStatus status);

    /**
     * Return the time this step was started
     * @return the time the step was started
   */
  DateTime getStartTime();

    /**
     * set the starttime
     * @param startTime
     */
    void setStartTime(DateTime startTime);

    /**
     * Return the time this step was completed
     * @return the end time of this step
     */
    DateTime getEndTime();

    /**
     * set the endtime for this step
     * @param endTime
     */
    void setEndTime(DateTime endTime);

    /**
     * Return the time this step was last updated
     * @return the last update time for this step
     */
    DateTime getLastUpdateTime();

    /**
     * set the time this step was last updated
     * @param lastUpdateTime
     */
    void setLastUpdateTime(DateTime lastUpdateTime);

    /**
     * Return a map holding any values saved during this step execution
     * @return a map indicating values/what happened during this step execution
     */
    Map<String, Object> getExecutionContext();

    /**
     * set the step execution context
     * @param executionContext
     */
    void setExecutionContext(Map<String, Object> executionContext);

    /**
     * Return the ExitCode for this step
     * @return
     */
    String getExitCode();

    /**
     * set the exit code for this step
     * @param exitCode
     */
    void setExitCode(String exitCode);

    /**
     * Return a message describing what happened during this step execution
     * @return a message describing this step execution
     */
    String getExitDescription();

    /**
     * set the step exit description message
     * @param exitDescription
     */
    void setExitDescription(String exitDescription);

    /**
     * Return the id representing this step.  This is the Primary key for this step
     * @return
     */
    long getId();

    /**
     * set the id for this step
     * @param id
     */
    void setId(long id);

    /**
     * Return the version number for this step indicating if any updates were done on the step and how many.
     * @return
     */
    int getVersion();

    /**
     * set the current version of this step
     * @param version
     */
    void setVersion(int version);

    /**
     * Return the runtime, in millis, for this step execution
     * @return
     */
    Long getRunTime();

    /**
     * set the runtime, in millis, for this step execution
     * @param runtime
     */
    void setRunTime(Long runtime);

    /**
     * return the time, in millis, since the time step ended
     * @return
     */
    Long getTimeSinceEndTime();

    /**
     * set the time, in millis, since the time this step ended
     * @param timeSinceEndTime
     */
    void setTimeSinceEndTime(Long timeSinceEndTime);

    /**
     * Return a flag indicating if this step is executing or not
     * @return true if the step is still running, false if not
     */
    public boolean isRunning();

    /**
     * set the flag to indicate if this step is running
     * @param running
     */
    public void setRunning(boolean running);

    /**
     * Return the reference to the respective NiFi event that produced this step
     * @return the id of the NiFi event that produced this step
     */
    Long getNifiEventId();

    /**
     * set the reference to the respective NiFi event that produced this step
     */
    void setNifiEventId(Long nifiEventId);
}
