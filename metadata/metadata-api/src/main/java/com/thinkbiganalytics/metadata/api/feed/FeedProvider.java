package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public interface FeedProvider{

    FeedSource ensureFeedSource(Feed.ID feedId, Datasource.ID dsId);
    FeedSource ensureFeedSource(Feed.ID feedId, Datasource.ID id, ServiceLevelAgreement.ID slaId);
    FeedDestination ensureFeedDestination(Feed.ID feedId, Datasource.ID dsId);


    Feed ensureFeed(Category.ID categoryId,String feedSystemName);

    Feed ensureFeed(String categorySystemName,String feedSystemName);
    Feed ensureFeed(String categorySystemName,String feedSystemName, String descr);

    Feed ensureFeed(String categorySystemName,String feedSystemName, String descr, Datasource.ID destId);
    Feed ensureFeed(String categorySystemName,String feedSystemName, String descr, Datasource.ID srcId, Datasource.ID destId);
    
    Feed createPrecondition(Feed.ID feedId, String descr, List<Metric> metrics);
    PreconditionBuilder buildPrecondition(Feed.ID feedId);

    Feed findBySystemName(String systemName);
    Feed findBySystemName(String categorySystemName, String systemName);

    FeedCriteria feedCriteria();
    
    Feed getFeed(Feed.ID id);
    List<? extends Feed> getFeeds();
    List<Feed> getFeeds(FeedCriteria criteria);
    
    Feed<?> addDependent(Feed.ID targetId, Feed.ID dependentId);
    Feed<?> removeDependent(Feed.ID feedId, Feed.ID depId);
    
//    FeedSource getFeedSource(FeedSource.ID id);
//    FeedDestination getFeedDestination(FeedDestination.ID id);

    Feed.ID resolveFeed(Serializable fid);
//    FeedSource.ID resolveSource(Serializable sid);
//    FeedDestination.ID resolveDestination(Serializable sid);

    boolean enableFeed(Feed.ID id);
    boolean disableFeed(Feed.ID id);

    /**
     * Deletes the feed with the specified id.
     *
     * @param feedId the feed id to be deleted
     * @throws RuntimeException if the feed cannot be deleted
     */
    void deleteFeed(Feed.ID feedId);

    Feed updateFeedServiceLevelAgreement(Feed.ID feedId, ServiceLevelAgreement sla);

    /**
     * Merge properties and return the newly merged properties
     */
    Map<String, Object> mergeFeedProperties(Feed.ID feedId, Map<String, Object> properties);

    Map<String, Object> replaceProperties(Feed.ID feedId, Map<String, Object> properties);

    // TODO Methods to add policy info to source
}
