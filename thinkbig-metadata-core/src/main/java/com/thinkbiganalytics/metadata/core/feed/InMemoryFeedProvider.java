/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.DestinationId;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.FeedId;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.FeedPreconditionImpl;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.SourceId;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

/**
 *
 * @author Sean Felten
 */
public class InMemoryFeedProvider implements FeedProvider {
    
    @Inject
    private DatasetProvider datasetProvider;
    
    @Inject
    private ServiceLevelAgreementProvider slaProvider;
    
    @Inject 
    private FeedPreconditionService preconditionService;

    
    private Map<Feed.ID, BaseFeed> feeds = new ConcurrentHashMap<>();
    private Map<FeedSource.ID, BaseFeed> sources = new ConcurrentHashMap<>();
    private Map<FeedDestination.ID, BaseFeed> destinations = new ConcurrentHashMap<>();
    
    
    public InMemoryFeedProvider() {
        super();
    }
    
    public InMemoryFeedProvider(DatasetProvider datasetProvider) {
        super();
        this.datasetProvider = datasetProvider;
    }


    @Inject
    public void setDatasetProvider(DatasetProvider datasetProvider) {
        this.datasetProvider = datasetProvider;
    }
    
    @Override
    public Feed.ID asFeedId(String feedIdStr) {
        return new FeedId(feedIdStr);
    }

    @Override
    public Feed.ID resolveFeed(Serializable fid) {
        if (fid instanceof FeedId) {
            return (FeedId) fid;
        } else {
            return new FeedId(fid);
        }
    }

    @Override
    public FeedSource.ID resolveSource(Serializable sid) {
        if (sid instanceof SourceId) {
            return (SourceId) sid;
        } else {
            return new SourceId(sid);
        }
    }

    @Override
    public FeedDestination.ID resolveDestination(Serializable did) {
        if (did instanceof DestinationId) {
            return (DestinationId) did;
        } else {
            return new DestinationId(did);
        }
    }

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
    public Feed ensureFeed(String name, String descr, ID destId) {
        Dataset dds = this.datasetProvider.getDataset(destId);
    
        if (dds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + destId);
        }
        
        BaseFeed feed = (BaseFeed) ensureFeed(name, descr);
        
        ensureFeedDestination(feed, dds);
        return feed;
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
    public Feed ensurePrecondition(Feed.ID feedId, String name, String descr, Set<Metric> metrics) {
        BaseFeed feed = this.feeds.get(feedId);
        
        if (feed != null) {
            // Remove the old one if any
            FeedPreconditionImpl precond = (FeedPreconditionImpl) feed.getPrecondition();
            if (precond != null) {
                this.slaProvider.removeAgreement(precond.getAgreement().getId());
            }
            
            ServiceLevelAgreement sla = this.slaProvider.builder()
                    .name(name)
                    .description(descr)
                    .obligationBuilder()
                        .metric(metrics)
                        .add()
                    .build();
            
            this.preconditionService.watchFeed(feed.getId());
            
            feed.setPrecondition(sla);
            return feed;
        } else {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
    }
    
    @Override
    public Feed updatePrecondition(Feed.ID feedId, Set<Metric> metrics) {
        String name = "Feed " + feedId + " Precondtion";
        StringBuilder descr = new StringBuilder();
        for (Metric metric : metrics) {
            descr.append(metric.getClass().getSimpleName()).append(", ");
        }
        
        return ensurePrecondition(feedId, name, descr.toString(), metrics);
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
    
    @Override
    public FeedSource getFeedSource(FeedSource.ID id) {
        Feed feed = this.sources.get(id);
        
        if (feed != null) {
            return feed.getSource(id);
        } else {
            return null;
        }
    }
    
    @Override
    public FeedDestination getFeedDestination(FeedDestination.ID id) {
        Feed feed = this.destinations.get(id);
        
        if (feed != null) {
            return feed.getDestination(id);
        } else {
            return null;
        }
    }


    
    private FeedSource ensureFeedSource(BaseFeed feed, Dataset ds, ServiceLevelAgreement.ID slaId) {
        Map<Dataset.ID, FeedSource> srcIds = new HashMap<>();
        for (FeedSource src : feed.getSources()) {
            srcIds.put(src.getDataset().getId(), src);
        }
        
        if (srcIds.containsKey(ds.getId())) {
            return srcIds.get(ds.getId());
        } else {
            FeedSource src = feed.addSource(ds, slaId);
            this.sources.put(src.getId(), feed);
            return src;
        }
    }

    private FeedDestination ensureFeedDestination(BaseFeed feed, Dataset ds) {
        FeedDestination dest = feed.getDestination(ds.getId());
        
        if (dest != null) {
            return dest;
        } else {
            dest = feed.addDestination(ds);
            this.destinations.put(dest.getId(), feed);
            return dest;
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
                for (FeedDestination dest : input.getDestinations()) {
                    if (this.destId.equals(dest.getDataset().getId())) {
                        return true;
                    }
                }
                return false;
            }
            
            if (this.sourceId != null) {
                for (FeedSource src : input.getSources()) {
                    if (this.sourceId.equals(src.getDataset().getId())) {
                        return true;
                    }
                }
                return false;
            }
            
            return true;
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
