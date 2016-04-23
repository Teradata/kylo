package com.thinkbiganalytics.jobrepo.query.model;

import java.util.Date;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface RelatedJobExecution {

  Long getJobExecutionId();

  void setJobExecutionId(Long jobExecutionId);

  String getJobName();

  void setJobName(String jobName);

  Date getStartTime();

  void setStartTime(Date startTime);

  Date getEndTime();

  void setEndTime(Date endTime);
}
