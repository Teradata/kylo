/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.model;

import java.util.Date;

/**
 * Created by sr186054 on 12/10/15.
 */
public class DefaultRelatedJobExecution implements RelatedJobExecution {

  private Long jobExecutionId;
  private String jobName;
  private Date startTime;
  private Date endTime;


  @Override
  public Long getJobExecutionId() {
    return jobExecutionId;
  }

  @Override
  public void setJobExecutionId(Long jobExecutionId) {
    this.jobExecutionId = jobExecutionId;
  }

  @Override
  public String getJobName() {
    return jobName;
  }

  @Override
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  @Override
  public Date getStartTime() {
    return startTime;
  }

  @Override
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  @Override
  public Date getEndTime() {
    return endTime;
  }

  @Override
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }
}
