package com.thinkbiganalytics.metadata.api.jobrepo.step;

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchStepExecution {

    Long getStepExecutionId();

    Long getVersion();

    String getStepName();

    DateTime getStartTime();

    DateTime getEndTime();

    StepStatus getStatus();

    void setStatus(StepStatus status);

    BatchJobExecution getJobExecution();

    Set<BatchStepExecutionContextValue> getStepExecutionContext();

    Map<String, String> getStepExecutionContextAsMap();

    ExecutionConstants.ExitCode getExitCode();

    void setExitCode(ExecutionConstants.ExitCode exitCode);

    String getExitMessage();

    DateTime getLastUpdated();

    boolean isFinished();

    NifiEventStepExecution getNifiEventStepExecution();

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
