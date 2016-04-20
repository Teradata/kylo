/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.Datasource.ID;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.DestinationId;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.FeedId;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.FeedPreconditionImpl;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.SourceId;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

/**
 *
 * @author Sean Felten
 */
public class InMemoryFeedProvider implements FeedProvider {
    
    private static final Criteria ALL = new Criteria() {
        public boolean apply(BaseFeed input) {
            return true;
        };
    };
    
    @Inject
    private DatasourceProvider datasetProvider;
    
    @Inject
    private ServiceLevelAgreementProvider slaProvider;
    
    @Inject 
    private FeedPreconditionService preconditionService;

    
//    private Map<Feed.ID, BaseFeed> feeds = new ConcurrentHashMap<>();
    private Map<Feed.ID, Feed> feeds = new ConcurrentHashMap<>();
    private Map<FeedSource.ID, BaseFeed> sources = new ConcurrentHashMap<>();
    private Map<FeedDestination.ID, BaseFeed> destinations = new ConcurrentHashMap<>();
    
    
    public InMemoryFeedProvider() {
        super();
    }
    
    public InMemoryFeedProvider(DatasourceProvider datasetProvider) {
        super();
        this.datasetProvider = datasetProvider;
    }
    
    @PostConstruct
    public void startup() {
        for (Feed feed : getFeeds()) {
            if (feed.getPrecondition() != null) {
                this.preconditionService.watchFeed(feed);
            }
        }
    }


    @Inject
    public void setDatasourceProvider(DatasourceProvider datasetProvider) {
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
    public FeedSource ensureFeedSource(Feed.ID feedId, Datasource.ID dsId, ServiceLevelAgreement.ID slaId) {
        BaseFeed feed = (BaseFeed) this.feeds.get(feedId);
        Datasource ds = this.datasetProvider.getDatasource(dsId);
        
        if (feed == null) {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
        
        if (ds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + dsId);
        }
        
        return ensureFeedSource(feed, ds, slaId);
    }
    
    @Override
    public FeedDestination ensureFeedDestination(Feed.ID feedId, Datasource.ID dsId) {
        BaseFeed feed = (BaseFeed) this.feeds.get(feedId);
        Datasource ds = this.datasetProvider.getDatasource(dsId);
        
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
        Datasource dds = this.datasetProvider.getDatasource(destId);
    
        if (dds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + destId);
        }
        
        BaseFeed feed = (BaseFeed) ensureFeed(name, descr);
        
        ensureFeedDestination(feed, dds);
        return feed;
    }

    @Override
    public Feed ensureFeed(String name, String descr, ID srcId, ID destId) {
        Datasource sds = this.datasetProvider.getDatasource(srcId);
        Datasource dds = this.datasetProvider.getDatasource(destId);

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
            for (Feed feed : this.feeds.values()) {
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
    public Feed ensurePrecondition(Feed.ID feedId, String name, String descr, List<List<Metric>> metrics) {
        BaseFeed feed = (BaseFeed) this.feeds.get(feedId);
        
        if (feed != null) {
            // Remove the old one if any
            FeedPreconditionImpl precond = (FeedPreconditionImpl) feed.getPrecondition();
            if (precond != null) {
                this.slaProvider.removeAgreement(precond.getAgreement().getId());
            }
            
            ServiceLevelAgreementBuilder slaBldr = this.slaProvider.builder()
                    .name(name)
                    .description(descr);
            
            for (List<Metric> list : metrics) {
                slaBldr.obligationGroupBuilder(Condition.SUFFICIENT)
                    .obligationBuilder()
                        .metric(list)
                        .add()
                    .build();
            }
            
            this.preconditionService.watchFeed(feed);
            
            feed.setPrecondition(slaBldr.build());
            return feed;
        } else {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
    }
    
    @Override
    public Feed updatePrecondition(Feed.ID feedId, List<List<Metric>> metrics) {
        String name = "Feed " + feedId + " Precondtion";
        StringBuilder descr = new StringBuilder();
        for (List<Metric> list : metrics) {
            for (Metric metric : list) {
                descr.append(metric.getClass().getSimpleName()).append(", ");
            }
            descr.append(" | ");
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
    public List<Feed> getFeeds() {
        return getFeeds(ALL);
    }

    @Override
    public List<Feed> getFeeds(FeedCriteria criteria) {
        Criteria critImpl = (Criteria) criteria;
        Iterator<Feed> filtered = Iterators.filter(this.feeds.values().iterator(), critImpl);
        Iterator<Feed> limited = Iterators.limit(filtered, critImpl.getLimit());
        
        return ImmutableList.copyOf(limited);
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


    
    private FeedSource ensureFeedSource(BaseFeed feed, Datasource ds, ServiceLevelAgreement.ID slaId) {
        Map<Datasource.ID, FeedSource> srcIds = new HashMap<>();
        for (FeedSource src : feed.getSources()) {
            srcIds.put(src.getDatasource().getId(), src);
        }
        
        if (srcIds.containsKey(ds.getId())) {
            return srcIds.get(ds.getId());
        } else {
            ServiceLevelAgreement sla = this.slaProvider.getAgreement(slaId);
            
            if (sla != null) {
                FeedSource src = feed.addSource(ds, sla);
                this.sources.put(src.getId(), feed);
                return src;
            } else {
                throw new FeedCreateException("An SLA with the given ID does not exists: " + slaId);
            }
        }
    }

    private FeedDestination ensureFeedDestination(BaseFeed feed, Datasource ds) {
        FeedDestination dest = feed.getDestination(ds.getId());
        
        if (dest != null) {
            return dest;
        } else {
            dest = feed.addDestination(ds);
            this.destinations.put(dest.getId(), feed);
            return dest;
        }
    }


    private static class Criteria extends AbstractMetadataCriteria<FeedCriteria> implements FeedCriteria, Predicate<Feed> {
        
        private String name;
        private Set<Datasource.ID> sourceIds = new HashSet<>();
        private Set<Datasource.ID> destIds = new HashSet<>();
        
        @Override
        public boolean apply(Feed input) {
            if (this.name != null && ! name.equals(input.getName())) return false;
            
            if (! this.destIds.isEmpty()) {
                for (FeedDestination dest : input.getDestinations()) {
                    if (this.destIds.contains(dest.getDatasource().getId())) {
                        return true;
                    }
                }
                return false;
            }
            
            if (! this.sourceIds.isEmpty()) {
                for (FeedSource src : input.getSources()) {
                    if (this.sourceIds.contains(src.getDatasource().getId())) {
                        return true;
                    }
                }
                return false;
            }
            
            return true;
        }

        @Override
        public FeedCriteria sourceDatasource(ID id, ID... others) {
            this.sourceIds.add(id);
            for (ID other : others) {
                this.sourceIds.add(other);
            }
            return this;
        }

        @Override
        public FeedCriteria destinationDatasource(ID id, ID... others) {
            this.destIds.add(id);
            for (ID other : others) {
                this.destIds.add(other);
            }
            return this;
        }

        @Override
        public FeedCriteria name(String name) {
            this.name = name;
            return this;
        }
        
    }
}
