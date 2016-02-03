/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    public FeedSource ensureFeedSource(Feed.ID feedId, ID dsId) {
        return ensureFeedSource(feedId, dsId, null);
    }

    @Override
    public FeedSource ensureFeedSource(Feed.ID feedId, Dataset.ID dsId, ServiceLevelAgreement.ID slaId) {
        BaseFeed feed = this.feeds.get(feedId);
        Dataset ds = this.datasetProvider.getDataset(dsId);
        
        if (feed == null) {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
        
        if (ds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + dsId);
        }
        
        return ensureFeedSource(feed, ds, slaId);
    }
    
    @Override
    public FeedDestination ensureFeedDestination(Feed.ID feedId, Dataset.ID dsId) {
        BaseFeed feed = this.feeds.get(feedId);
        Dataset ds = this.datasetProvider.getDataset(dsId);
        
        if (feed == null) {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
        
        if (ds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + dsId);
        }
        
        return ensureFeedDestination(feed, ds);
    }
    
    @Override
    public Feed ensureFeed(String name, String descr, ID srcId, ID destId) {
        Dataset sds = this.datasetProvider.getDataset(srcId);
        Dataset dds = this.datasetProvider.getDataset(destId);

        if (sds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + srcId);
        }
        
        if (dds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + destId);
        }
        
        BaseFeed feed = (BaseFeed) ensureFeed(name, descr);
        
        ensureFeedSource(feed, sds, null);
        ensureFeedDestination(feed, dds);
        
        return feed;
    }

    @Override
    public Feed ensureFeed(String name, String descr) {
        synchronized (this.feeds) {
            for (BaseFeed feed : this.feeds.values()) {
                if (feed.getName().equals(name)) {
                    return feed;
                }
            }
        }
            
        BaseFeed newFeed = new BaseFeed(name, descr);
        this.feeds.put(newFeed.getId(), newFeed);
        return newFeed;
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

    
    private FeedSource ensureFeedSource(BaseFeed feed, Dataset ds, ServiceLevelAgreement.ID slaId) {
        Map<Dataset.ID, FeedSource> srcIds = new HashMap<>();
        for (FeedSource src : feed.getSources()) {
            srcIds.put(src.getDataset().getId(), src);
        }
        
        if (srcIds.containsKey(ds.getId())) {
            return srcIds.get(ds.getId());
        } else {
            return feed.addSource(ds, slaId);
        }
    }

    private FeedDestination ensureFeedDestination(BaseFeed feed, Dataset ds) {
        if (feed.getDestination().getDataset().getId().equals(ds.getId())) {
            return feed.getDestination();
        } else {
            return feed.addDestination(ds);
        }
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
