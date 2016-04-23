package com.thinkbiganalytics.scheduler.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 9/24/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobInfo {

  private ScheduleIdentifier jobIdentifier;
  private List<TriggerInfo> triggers;
  private String description;
  private Class jobClass;
  @JsonIgnore
  private Map<String, Object> jobData;

  public JobInfo() {

  }

  public JobInfo(ScheduleIdentifier jobIdentifier) {
    this.jobIdentifier = jobIdentifier;
  }


  public ScheduleIdentifier getJobIdentifier() {
    return jobIdentifier;
  }


  public void setJobIdentifier(ScheduleIdentifier jobIdentifier) {
    this.jobIdentifier = jobIdentifier;
  }


  public List<TriggerInfo> getTriggers() {
    return triggers;
  }


  public void setTriggers(List<TriggerInfo> triggers) {
    this.triggers = triggers;
  }


  public String getDescription() {
    return description;
  }


  public void setDescription(String description) {
    this.description = description;
  }


  public Class getJobClass() {
    return jobClass;
  }


  public void setJobClass(Class jobClass) {
    this.jobClass = jobClass;
  }


  public Map<String, Object> getJobData() {
    return jobData;
  }


  public void setJobData(Map<String, Object> jobData) {
    this.jobData = jobData;
  }
}
