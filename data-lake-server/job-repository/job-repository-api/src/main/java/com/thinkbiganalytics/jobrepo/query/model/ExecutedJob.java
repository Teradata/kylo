package com.thinkbiganalytics.jobrepo.query.model;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ExecutedJob {

  long getInstanceId();

  void setInstanceId(long instanceId);

  long getExecutionId();

  void setExecutionId(long executionId);

  void setJobId(Long jobId);

  String getJobName();

  void setJobName(String jobName);

  List<Throwable> getExceptions();

  void setExceptions(List<Throwable> exceptions);

  DateTime getCreateTime();

  void setCreateTime(DateTime createTime);

  DateTime getEndTime();

  void setEndTime(DateTime endTime);

  Map<String, Object> getExecutionContext();

  void setExecutionContext(Map<String, Object> executionContext);

  String getExitCode();

  void setExitCode(String exitCode);

  String getExitStatus();

  void setExitStatus(String exitStatus);

  String getJobConfigurationName();

  void setJobConfigurationName(String jobConfigurationName);

  Long getJobId();

  Map<String, Object> getJobParameters();

  void setJobParameters(Map<String, Object> jobParameters);

  DateTime getLastUpdated();

  void setLastUpdated(DateTime lastUpdated);

  DateTime getStartTime();

  void setStartTime(DateTime startTime);

  ExecutionStatus getStatus();

  void setStatus(ExecutionStatus status);

  List<ExecutedStep> getExecutedSteps();

  void setExecutedSteps(List<ExecutedStep> executedSteps);

  Long getRunTime();

  void setRunTime(Long runTime);

  Long getTimeSinceEndTime();

  void setTimeSinceEndTime(Long timeSinceEndTime);

  String getJobType();

  void setJobType(String jobType);

  boolean isLatest();

  void setIsLatest(boolean isLatest);

  String getFeedName();

  void setFeedName(String feedName);

  String getDisplayStatus();
}
