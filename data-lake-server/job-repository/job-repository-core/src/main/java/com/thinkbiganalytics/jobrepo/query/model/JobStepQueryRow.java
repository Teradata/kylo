/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.model;

/**
 * Created by sr186054 on 12/10/15.
 */
public class JobStepQueryRow {

  private Long jobExecutionId;
  private String stepName;
  private Long stepExecutionId;
  private Long jobInstanceId;


  public Long getJobExecutionId() {
    return jobExecutionId;
  }

  public void setJobExecutionId(Long jobExecutionId) {
    this.jobExecutionId = jobExecutionId;
  }

  public String getStepName() {
    return stepName;
  }

  public void setStepName(String stepName) {
    this.stepName = stepName;
  }

  public Long getStepExecutionId() {
    return stepExecutionId;
  }

  public void setStepExecutionId(Long stepExecutionId) {
    this.stepExecutionId = stepExecutionId;
  }

  public Long getJobInstanceId() {
    return jobInstanceId;
  }

  public void setJobInstanceId(Long jobInstanceId) {
    this.jobInstanceId = jobInstanceId;
  }
}
