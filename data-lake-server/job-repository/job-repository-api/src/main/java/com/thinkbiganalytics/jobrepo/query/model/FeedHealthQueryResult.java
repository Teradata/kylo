package com.thinkbiganalytics.jobrepo.query.model;

import java.util.Date;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface FeedHealthQueryResult {

  String getFeed();

  void setFeed(String feed);

  Long getCount();

  void setCount(Long count);

  String getHealth();

  void setHealth(String health);

  Date getEndTime();

  void setEndTime(Date endTime);
}
