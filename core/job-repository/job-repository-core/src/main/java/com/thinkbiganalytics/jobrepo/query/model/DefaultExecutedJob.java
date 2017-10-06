package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-core
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents a job execution in a JSON friendly format.
 *
 * @see com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform
 */
@SuppressWarnings("UnusedDeclaration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultExecutedJob implements Serializable, ExecutedJob {

    private static final long serialVersionUID = 3327858118326404823L;
    private long instanceId;
    private long executionId;
    private String jobName;
    private DateTime createTime;
    private DateTime endTime;
    private Map<String, Object> executionContext;
    private String exitCode;
    private String exitStatus;
    private Map<String, Object> jobParameters;
    private DateTime lastUpdated;
    private DateTime startTime;
    private boolean isLatest;
    private ExecutionStatus status;
    private List<ExecutedStep> executedSteps;
    private Long runTime;
    private Long timeSinceEndTime;
    private String jobType;
    private String feedName;
    private String feedId;
    private boolean isStream;

    public DefaultExecutedJob() {

    }

    public DefaultExecutedJob(ExecutedJob job) {
        this.instanceId = job.getInstanceId();
        this.executionId = job.getExecutionId();
        this.jobName = job.getJobName();
        this.createTime = job.getCreateTime();
        this.endTime = job.getEndTime();
        this.executionContext = job.getExecutionContext();
        this.exitCode = job.getExitCode();
        this.exitStatus = job.getExitStatus();
        this.jobParameters = job.getJobParameters();
        this.lastUpdated = job.getLastUpdated();
        this.startTime = job.getStartTime();
        this.status = job.getStatus();
        this.executedSteps = job.getExecutedSteps();
        this.runTime = job.getRunTime();
        this.timeSinceEndTime = job.getTimeSinceEndTime();
        this.jobType = job.getJobType();
        this.feedName = job.getFeedName();
        this.feedId = job.getFeedId();
    }

    @Override
    public long getInstanceId() {
        return instanceId;
    }

    @Override
    public void setInstanceId(final long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public long getExecutionId() {
        return executionId;
    }

    @Override
    public void setExecutionId(final long executionId) {
        this.executionId = executionId;
    }


    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public DateTime getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(final DateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public DateTime getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(final DateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Map<String, Object> getExecutionContext() {
        return executionContext;
    }

    @Override
    public void setExecutionContext(final Map<String, Object> executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public String getExitCode() {
        return exitCode;
    }

    @Override
    public void setExitCode(final String exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String getExitStatus() {
        return exitStatus;
    }

    @Override
    public void setExitStatus(String exitStatus) {
        this.exitStatus = exitStatus;
    }

    @Override
    public Map<String, Object> getJobParameters() {
        return jobParameters;
    }

    @Override
    public void setJobParameters(final Map<String, Object> jobParameters) {
        this.jobParameters = jobParameters;
    }

    @Override
    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(final DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public DateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(final DateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public ExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(final ExecutionStatus status) {
        this.status = status;
    }

    @Override
    public List<ExecutedStep> getExecutedSteps() {
        return executedSteps;
    }

    @Override
    public void setExecutedSteps(final List<ExecutedStep> executedSteps) {
        this.executedSteps = executedSteps;
    }

    @Override
    public Long getRunTime() {
        return runTime;
    }

    @Override
    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    @Override
    public Long getTimeSinceEndTime() {
        return timeSinceEndTime;
    }

    @Override
    public void setTimeSinceEndTime(Long timeSinceEndTime) {
        this.timeSinceEndTime = timeSinceEndTime;
    }


    @Override
    public String getJobType() {
        return jobType;
    }

    @Override
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    @Override
    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    @Override
    public String getDisplayStatus() {
        String displayStatus = this.status.name();
        if (ExecutionStatus.FAILED.equals(this.status) || (ExecutionStatus.COMPLETED.equals(this.status) && "FAILED"
            .equalsIgnoreCase(this.exitCode))) {
            displayStatus = "FAILED";
        }

        if (displayStatus.equalsIgnoreCase("FAILED") && "STOPPED".equalsIgnoreCase(this.exitCode)) {
            displayStatus = "STOPPED";
        }
        return displayStatus;
    }

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
    }
}
