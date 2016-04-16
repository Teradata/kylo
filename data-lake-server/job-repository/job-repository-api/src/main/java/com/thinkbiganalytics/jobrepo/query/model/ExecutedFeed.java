package com.thinkbiganalytics.jobrepo.query.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ExecutedFeed {
    String getName();

    void setName(String name);

    long getFeedInstanceId();

    void setFeedInstanceId(long feedInstanceId);

    long getFeedExecutionId();

    void setFeedExecutionId(long feedExecutionId);

    List<Throwable> getExceptions();

    void setExceptions(List<Throwable> exceptions);

    DateTime getEndTime();

    void setEndTime(DateTime endTime);

    String getExitCode();

    void setExitCode(String exitCode);

    String getExitStatus();

    void setExitStatus(String exitStatus);

    DateTime getStartTime();

    void setStartTime(DateTime startTime);

    ExecutionStatus getStatus();

    void setStatus(ExecutionStatus status);

    List<ExecutedJob> getExecutedJobs();

    void setExecutedJobs(List<ExecutedJob> executedJobs);

    Long getRunTime();

    void setRunTime(Long runTime);

    Long getTimeSinceEndTime();

    void setTimeSinceEndTime(Long timeSinceEndTime);

    boolean isLatest();

    void setIsLatest(boolean isLatest);
}
