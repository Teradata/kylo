package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

public interface FeedProvider {

    ID asFeedId(String feedIdStr);

    FeedSource ensureFeedSource(Feed.ID feedId, Dataset.ID dsId);
    FeedSource ensureFeedSource(Feed.ID feedId, Dataset.ID id, ServiceLevelAgreement.ID slaId);
    FeedDestination ensureFeedDestination(Feed.ID feedId, Dataset.ID dsId);
    
    Feed ensureFeed(String name, String descr);
    Feed ensureFeed(String name, String descr, Dataset.ID destId);
    Feed ensureFeed(String name, String descr, Dataset.ID srcId, Dataset.ID destId);
    
    Feed ensurePrecondition(Feed.ID feedId, String name, String descr, Set<Metric> metrics);
    Feed updatePrecondition(Feed.ID feedId, Set<Metric> metrics);
    
    FeedCriteria feedCriteria();
    
    Feed getFeed(Feed.ID id);
    Collection<Feed> getFeeds();
    Collection<Feed> getFeeds(FeedCriteria criteria);
    
    FeedSource getFeedSource(FeedSource.ID id);
    FeedDestination getFeedDestination(FeedDestination.ID id);

    Feed.ID resolveFeed(Serializable fid);
    FeedSource.ID resolveSource(Serializable sid);
    FeedDestination.ID resolveDestination(Serializable sid);


    
    // TODO Methods to add policy info to source
}
