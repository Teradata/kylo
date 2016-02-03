package com.thinkbiganalytics.metadata.api.feed;

import java.util.Collection;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

public interface FeedProvider {

    FeedSource ensureFeedSource(Feed.ID feedId, Dataset.ID dsId);
    FeedSource ensureFeedSource(Feed.ID feedId, Dataset.ID id, ServiceLevelAgreement.ID slaId);
    FeedDestination ensureFeedDestination(Feed.ID feedId, Dataset.ID dsId);
    
    Feed ensureFeed(String name, String descr);
    Feed ensureFeed(String name, String descr, Dataset.ID srcId, Dataset.ID destId);
    
    FeedCriteria feedCriteria();
    
    Feed getFeed(Feed.ID id);
    Collection<Feed> getFeeds();
    Collection<Feed> getFeeds(FeedCriteria criteria);
    
 
    // TODO Methods to add policy info to source
}
