package com.thinkbiganalytics.scheduler;


import java.util.Date;

public interface TriggerInfo {

  Date getNextFireTime();

  Date getPreviousFireTime();

  Date getStartTime();

  Date getEndTime();

  String getCronExpression();

  String getCronExpressionSummary();

  String getDescription();

  boolean isSimpleTrigger();

  void setSimpleTrigger(boolean isSimpleTrigger);

  boolean isScheduled();

  void setScheduled(boolean scheduled);

  TriggerInfo.TriggerState getState();

  TriggerIdentifier getTriggerIdentifier();

  JobIdentifier getJobIdentifier();

  void setTriggerIdentifier(TriggerIdentifier triggerIdentifier);

  void setJobIdentifier(JobIdentifier jobIdentifier);

  void setNextFireTime(Date nextFireTime);

  void setPreviousFireTime(Date previousFireTime);

  void setStartTime(Date startTime);

  void setEndTime(Date endTime);

  void setCronExpression(String cronExpression);

  void setCronExpressionSummary(String summary);

  void setDescription(String description);

  void setState(TriggerInfo.TriggerState state);

  void setTriggerClass(Class triggerClass);

  Class getTriggerClass();

  public static enum TriggerState {
    NONE,
    NORMAL,
    PAUSED,
    COMPLETE,
    ERROR,
    BLOCKED;
  }
}
