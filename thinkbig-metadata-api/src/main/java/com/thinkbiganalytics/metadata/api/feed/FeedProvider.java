package com.thinkbiganalytics.metadata.api.feed;

import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

public interface FeedProvider {

    FeedSource createFeedSource(Dataset.ID id);
    FeedSource createFeedSource(Dataset.ID id, ServiceLevelAgreement.ID slaId);
    FeedDestination createFeedDestination(Dataset.ID dsId);
    Feed createFeed(String name, String descr, Dataset.ID srcId, Dataset.ID destId);
    Feed createFeed(String name, String descr, FeedSource src, FeedDestination dest);
    
    FeedCriteria feedCriteria();
    
    Feed getFeed(Feed.ID id);
    List<Feed> getFeeds();
    List<Feed> getFeeds(FeedCriteria criteria);
    
 
    // TODO Methods to add policy info to source
}
