package com.thinkbiganalytics.jobrepo.query.model;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

/**
 * Created by sr186054 on 8/31/15.
 */
public class TbaJobExecution extends JobExecution {

  private String jobType;
  private boolean isLatest;
  private String feedName;

  public TbaJobExecution(JobInstance job, Long id, JobParameters jobParameters, String jobConfigurationName) {
    super(job, id, jobParameters, jobConfigurationName);
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public boolean isLatest() {
    return isLatest;
  }

  public void setIsLatest(boolean isLatest) {
    this.isLatest = isLatest;
  }

  public String getFeedName() {
    return feedName;
  }

  public void setFeedName(String feedName) {
    this.feedName = feedName;
  }
}
