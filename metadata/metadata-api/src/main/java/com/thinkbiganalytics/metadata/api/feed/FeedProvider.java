package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;
import java.util.List;

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

public interface FeedProvider  {

    FeedSource ensureFeedSource(Feed.ID feedId, Datasource.ID dsId);
    FeedSource ensureFeedSource(Feed.ID feedId, Datasource.ID id, ServiceLevelAgreement.ID slaId);
    FeedDestination ensureFeedDestination(Feed.ID feedId, Datasource.ID dsId);
    
    Feed ensureFeed(String name, String descr);
    Feed ensureFeed(String name, String descr, Datasource.ID destId);
    Feed ensureFeed(String name, String descr, Datasource.ID srcId, Datasource.ID destId);
    
    Feed ensurePrecondition(Feed.ID feedId, String name, String descr, List<List<Metric>> metrics);
    Feed updatePrecondition(Feed.ID feedId, List<List<Metric>> metrics);
    
    FeedCriteria feedCriteria();
    
    Feed getFeed(Feed.ID id);
    List<Feed> getFeeds();
    List<Feed> getFeeds(FeedCriteria criteria);
    
    FeedSource getFeedSource(FeedSource.ID id);
    FeedDestination getFeedDestination(FeedDestination.ID id);

    Feed.ID resolveFeed(Serializable fid);
    FeedSource.ID resolveSource(Serializable sid);
    FeedDestination.ID resolveDestination(Serializable sid);

    boolean enableFeed(Feed.ID id);
    boolean disableFeed(Feed.ID id);


    
    // TODO Methods to add policy info to source
}
