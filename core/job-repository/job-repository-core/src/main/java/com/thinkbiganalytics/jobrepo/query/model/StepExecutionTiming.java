package com.thinkbiganalytics.jobrepo.query.model;

import org.springframework.batch.core.StepExecution;

/**
 * Created by sr186054 on 4/26/16.
 */
public class StepExecutionTiming {


  private Long stepExecutionId;
  private Long runTime;
  private Long timeSinceEndTime;

  public StepExecutionTiming(Long stepExecutionId) {
    this.stepExecutionId = stepExecutionId;
  }

  public Long getStepExecutionId() {
    return stepExecutionId;
  }

  public void setStepExecutionId(Long stepExecutionId) {
    this.stepExecutionId = stepExecutionId;
  }

  public Long getRunTime() {
    return runTime;
  }

  public void setRunTime(Long runTime) {
    this.runTime = runTime;
  }

  public Long getTimeSinceEndTime() {
    return timeSinceEndTime;
  }

  public void setTimeSinceEndTime(Long timeSinceEndTime) {
    this.timeSinceEndTime = timeSinceEndTime;
  }
}
