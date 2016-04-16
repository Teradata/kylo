package com.thinkbiganalytics.scheduler;

import java.util.Date;
import java.util.List;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface ScheduledJob {

  String getJobStatus();

  void setJobStatus(String jobStatus);

  String getJobGroup();

  void setJobGroup(String jobGroup);

  String getNextFireTimeString();

  void setNextFireTimeString(String nextFireTimeString);

  Date getNextFireTime();

  void setNextFireTime(Date nextFireTime);

  void setCronExpression(String cronExpression);

  String getCronExpression();

  String getJobName();

  void setJobName(String jobName);

  List<TriggerInfo> getTriggers();

  void setTriggers(List<TriggerInfo> triggers);

  JobIdentifier getJobIdentifier();

  void setJobIdentifier(JobIdentifier jobIdentifier);

  void setState();

  String getState();

  String getCronExpressionSummary();

  void setCronExpressionData();

  boolean isRunning();

  boolean isPaused();

  boolean isScheduled();
}
