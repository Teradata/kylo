package com.thinkbiganalytics.jobrepo.query.model;

import java.util.Date;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface JobStatusCount {

  Long getCount();

  void setCount(Long count);

  String getFeedName();

  void setFeedName(String feedName);

  String getJobName();

  void setJobName(String jobName);

  String getStatus();

  void setStatus(String status);

  Date getDate();

  void setDate(Date date);
}
