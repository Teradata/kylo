/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.Dataset.ID;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public class InMemoryFeedProvider implements FeedProvider {
    
    @Inject
    private DatasetProvider datasetProvider;
    
    private Map<Feed.ID, BaseFeed> feeds;

    @Override
    public FeedSource createFeedSource(Feed.ID feedId, ID dsId) {
        return createFeedSource(feedId, dsId, null);
    }

    @Override
    public FeedSource createFeedSource(Feed.ID feedId, Dataset.ID dsId, ServiceLevelAgreement.ID slaId) {
        BaseFeed feed = this.feeds.get(feedId);
        Dataset ds = this.datasetProvider.getDataset(dsId);
        
        if (feed == null) {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
        
        if (ds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + dsId);
        }
        
        return feed.addSource(ds, slaId);
    }

    @Override
    public FeedDestination createFeedDestination(Feed.ID feedId, Dataset.ID dsId) {
        BaseFeed feed = this.feeds.get(feedId);
        Dataset ds = this.datasetProvider.getDataset(dsId);
        
        if (feed == null) {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
        
        if (ds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + dsId);
        }
        
        return feed.addDestination(ds);
    }

    @Override
    public Feed createFeed(String name, String descr, ID srcId, ID destId) {
        Dataset sds = this.datasetProvider.getDataset(srcId);
        Dataset dds = this.datasetProvider.getDataset(destId);

        if (sds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + srcId);
        }
        
        if (dds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + destId);
        }
        
        BaseFeed feed = new BaseFeed(name, descr);
        feed.addSource(sds);
        feed.addDestination(dds);
        this.feeds.put(feed.getId(), feed);
        return feed;
    }

    @Override
    public Feed createFeed(String name, String descr) {
        BaseFeed feed = new BaseFeed(name, descr);
        this.feeds.put(feed.getId(), feed);
        return feed;
    }

    @Override
    public FeedCriteria feedCriteria() {
        return new Criteria();
    }

    @Override
    public Feed getFeed(Feed.ID id) {
        return this.feeds.get(id);
    }

    @Override
    public Collection<Feed> getFeeds() {
        return new ArrayList<Feed>(this.feeds.values());
    }

    @Override
    public Collection<Feed> getFeeds(FeedCriteria criteria) {
        Criteria critImpl = (Criteria) criteria;
        return new ArrayList<Feed>(Collections2.filter(this.feeds.values(), critImpl));
    }

    
    private static class Criteria implements FeedCriteria, Predicate<BaseFeed> {
        
        private String name;
        private Dataset.ID sourceId;
        private Dataset.ID destId;
        
        @Override
        public boolean apply(BaseFeed input) {
            if (this.name != null && ! name.equals(input.getName())) return false;
            if (this.destId != null) {
                if (! this.destId.equals(input.getDestination().getDataset().getId())) {
                    return false;
                }
            }
            
            if (this.sourceId != null) {
                for (FeedSource src : input.getSources()) {
                    if (this.sourceId.equals(src.getDataset().getId())) {
                        return true;
                    }
                }
            }
            
            return false;
        }

        @Override
        public FeedCriteria sourceDataset(ID id) {
            this.sourceId = id;
            return this;
        }

        @Override
        public FeedCriteria destinationDataset(ID id) {
            this.destId = id;
            return this;
        }

        @Override
        public FeedCriteria name(String name) {
            this.name = name;
            return this;
        }
        
    }
}
