package com.thinkbiganalytics.scheduler;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 9/24/15.
 */
public interface JobInfo {

  JobIdentifier getJobIdentifier();

  void setJobIdentifier(JobIdentifier jobIdentifier);

  List<TriggerInfo> getTriggers();

  void setTriggers(List<TriggerInfo> triggers);

  String getDescription();

  void setDescription(String description);

  Class getJobClass();

  void setJobClass(Class jobClass);

  Map<String, Object> getJobData();

  void setJobData(Map<String, Object> jobData);
}
