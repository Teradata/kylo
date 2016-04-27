package com.thinkbiganalytics.jobrepo.query.model;

import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ExecutedStep {

  String getStepName();

  void setStepName(String stepName);

  ExecutionStatus getStatus();

  void setStatus(ExecutionStatus status);

  long getReadCount();

  void setReadCount(long readCount);

  long getWriteCount();

  void setWriteCount(long writeCount);

  long getCommitCount();

  void setCommitCount(long commitCount);

  long getRollbackCount();

  void setRollbackCount(long rollbackCount);

  long getReadSkipCount();

  void setReadSkipCount(long readSkipCount);

  long getProcessSkipCount();

  void setProcessSkipCount(long processSkipCount);

  long getWriteSkipCount();

  void setWriteSkipCount(long writeSkipCount);

  DateTime getStartTime();

  void setStartTime(DateTime startTime);

  DateTime getEndTime();

  void setEndTime(DateTime endTime);

  DateTime getLastUpdateTime();

  void setLastUpdateTime(DateTime lastUpdateTime);

  Map<String, Object> getExecutionContext();

  void setExecutionContext(Map<String, Object> executionContext);

  String getExitCode();

  void setExitCode(String exitCode);

  String getExitDescription();

  void setExitDescription(String exitDescription);

  long getId();

  void setId(long id);

  int getVersion();

  void setVersion(int version);

  Long getRunTime();
  void setRunTime(Long runtime);
  Long getTimeSinceEndTime();
  void setTimeSinceEndTime(Long timeSinceEndTime);

  public boolean isRunning();
  public void setRunning(boolean running);


}
