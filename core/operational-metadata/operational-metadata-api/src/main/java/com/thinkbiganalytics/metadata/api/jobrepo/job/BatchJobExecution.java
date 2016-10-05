package com.thinkbiganalytics.metadata.api.jobrepo.job;

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobExecution {

    BatchJobInstance getJobInstance();

    Long getJobExecutionId();

    Long getVersion();

    DateTime getCreateTime();

    void setCreateTime(DateTime createTime);

    DateTime getStartTime();

    void setStartTime(DateTime startTime);

    DateTime getEndTime();

    void setEndTime(DateTime endTime);

    JobStatus getStatus();

    void setStatus(JobStatus status);

    ExecutionConstants.ExitCode getExitCode();

    String getExitMessage();

    void setExitMessage(String exitMessage);

    DateTime getLastUpdated();

    Set<? extends BatchJobExecutionParameter> getJobParameters();

    Set<BatchStepExecution> getStepExecutions();

    Set<BatchJobExecutionContextValue> getJobExecutionContext();

    Map<String, String> getJobExecutionContextAsMap();

    NifiEventJobExecution getNifiEventJobExecution();

    void setNifiEventJobExecution(NifiEventJobExecution nifiEventJobExecution);

    boolean isFailed();

    boolean isSuccess();

    boolean isFinished();

    public static enum JobStatus {
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
