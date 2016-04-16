package com.thinkbiganalytics.jobrepo.query.model;

import java.util.List;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface FeedStatus {

  void populate();

  List<FeedHealth> getFeeds();

  void setFeeds(List<FeedHealth> feeds);

  Integer getHealthyCount();

  void setHealthyCount(Integer healthyCount);

  Integer getFailedCount();

  void setFailedCount(Integer failedCount);

  Float getPercent();

  void setPercent(Integer percent);

  List<FeedHealth> getHealthyFeeds();

  void setHealthyFeeds(List<FeedHealth> healthyFeeds);

  List<FeedHealth> getFailedFeeds();

  void setFailedFeeds(List<FeedHealth> failedFeeds);

  List<FeedSummary> getFeedSummary();

  void setFeedSummary(List<FeedSummary> feedSummary);
}
