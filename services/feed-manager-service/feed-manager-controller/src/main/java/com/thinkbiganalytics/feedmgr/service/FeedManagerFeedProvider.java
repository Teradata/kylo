package com.thinkbiganalytics.feedmgr.service;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.rest.JerseyClientException;

import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 5/1/16.
 */
public interface FeedManagerFeedProvider {

  List<FeedMetadata> getReusableFeeds();

  FeedMetadata getFeedByName(String feedName);

  FeedMetadata getFeedById(String id);

  Collection<FeedMetadata> getFeeds();
  public Collection<? extends UIFeed> getFeeds(boolean verbose);

  public List<FeedSummary> getFeedSummaryData();

  public List<FeedSummary> getFeedSummaryForCategory(String categoryId);

  List<FeedMetadata> getFeedsWithTemplate(String registeredTemplateId);

  NifiFeed createFeed(FeedMetadata feedMetadata) throws JerseyClientException;

  void saveFeed(FeedMetadata feed);

  public void updateFeedsWithTemplate(String oldTemplateId, String newTemplateId);


}
