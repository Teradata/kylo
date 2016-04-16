package com.thinkbiganalytics.jobrepo.query.model;

import java.util.Date;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface FeedHealth {

  Long getHealthyCount();

  void setHealthyCount(Long healthyCount);

  Long getUnhealthyCount();

  void markHealthCountsSet();

  void setUnhealthyCount(Long unhealthyCount);

  String getFeed();

  void setFeed(String feed);

  ExecutedFeed getLastOpFeed();

  void setLastOpFeed(ExecutedFeed lastOpFeed);

  Long getAvgRuntime();

  void setAvgRuntime(Long avgRuntime);

  Date getLastUnhealthyTime();

  void setLastUnhealthyTime(Date lastUnhealthyTime);

  boolean isHealthy();

  String getFeedState(ExecutedFeed feed);

  String getLastOpFeedState();

  public static enum STATE {
    WAITING, RUNNING
  }
}
