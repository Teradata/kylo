package com.thinkbiganalytics.metadata.core.feed;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.Datasource.ID;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.PreconditionBuilder;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.FeedId;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed.FeedPreconditionImpl;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A provider of {@link Feed} objects that stores everything in memory.
 */
public class InMemoryFeedProvider implements FeedProvider {

    private static final Criteria ALL = new Criteria() {
        public boolean apply(BaseFeed input) {
            return true;
        }

        ;
    };

    @Inject
    private DatasourceProvider datasetProvider;

    @Inject
    private ServiceLevelAgreementProvider slaProvider;

    private Map<Feed.ID, Feed> feeds = new ConcurrentHashMap<>();

    public InMemoryFeedProvider() {
        super();
    }

    public InMemoryFeedProvider(DatasourceProvider datasetProvider) {
        super();
        this.datasetProvider = datasetProvider;
    }

    @Inject
    public void setDatasourceProvider(DatasourceProvider datasetProvider) {
        this.datasetProvider = datasetProvider;
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
    public Feed<?> ensureFeed(Category.ID categoryId, String feedSystemName) {
        throw new UnsupportedOperationException("Unable to ensure feed by categoryId with InMemoryProvider");
    }

    @Override
    public Feed<?> ensureFeed(String categorySystemName, String feedSystemName) {
        return ensureFeed(categorySystemName, feedSystemName, null);
    }

    @Override
    public Feed<?> ensureFeed(String categorySystemName, String name, String descr, ID destId) {
        Datasource dds = this.datasetProvider.getDatasource(destId);

        if (dds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + destId);
        }

        BaseFeed feed = (BaseFeed) ensureFeed(categorySystemName, name, descr);

        ensureFeedDestination(feed, dds);
        return feed;
    }

    @Override
    public Feed<?> ensureFeed(String categorySystemName, String name, String descr, ID srcId, ID destId) {
        Datasource sds = this.datasetProvider.getDatasource(srcId);
        Datasource dds = this.datasetProvider.getDatasource(destId);

        if (sds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + srcId);
        }

        if (dds == null) {
            throw new FeedCreateException("A dataset with the given ID does not exists: " + destId);
        }

        BaseFeed feed = (BaseFeed) ensureFeed(categorySystemName, name, descr);

        ensureFeedSource(feed, sds, null);
        ensureFeedDestination(feed, dds);

        return feed;
    }

    @Override
    public Feed<?> ensureFeed(String categorySystemName, String name, String descr) {
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
    public Feed<?> createPrecondition(Feed.ID feedId, String descr, List<Metric> metrics) {
        BaseFeed feed = (BaseFeed) this.feeds.get(feedId);

        if (feed != null) {
            // Remove the old one if any
            FeedPreconditionImpl precond = (FeedPreconditionImpl) feed.getPrecondition();
            if (precond != null) {
                this.slaProvider.removeAgreement(precond.getAgreement().getId());
            }

            ServiceLevelAgreement sla = this.slaProvider.builder()
                    .name("Precondition for feed " + feed.getName() + " (" + feed.getId() + ")")
                    .description(descr)
                    .obligationBuilder(Condition.REQUIRED)
                    .metric(metrics)
                    .build()
                    .build();

            return setupPrecondition(feed, sla);
        } else {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }
    }

    @Override
    public PreconditionBuilder buildPrecondition(final Feed.ID feedId) {
        BaseFeed feed = (BaseFeed) this.feeds.get(feedId);

        if (feed != null) {
            ServiceLevelAgreementBuilder slaBldr = this.slaProvider.builder();
            return new PreconditionbuilderImpl(slaBldr, feed);
        } else {
            throw new FeedCreateException("A feed with the given ID does not exists: " + feedId);
        }

    }

    @Override
    public Feed<?> addDependent(com.thinkbiganalytics.metadata.api.feed.Feed.ID targetId, com.thinkbiganalytics.metadata.api.feed.Feed.ID dependentId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Feed<?> removeDependent(com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId, com.thinkbiganalytics.metadata.api.feed.Feed.ID depId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeedCriteria feedCriteria() {
        return new Criteria();
    }

    @Override
    public Feed<?> getFeed(Feed.ID id) {
        return this.feeds.get(id);
    }

    @Override
    public List<Feed> getFeeds() {
        return getFeeds(ALL);
    }

    @Override
    public Feed<?> findBySystemName(String systemName) {
        return findBySystemName(null, systemName);
    }

    @Override
    public Feed<?> findBySystemName(String categorySystemName, String systemName) {
        FeedCriteria c = feedCriteria();
        if (categorySystemName != null) {
            c.category(categorySystemName);
        }
        c.name(systemName);
        List<Feed> feeds = getFeeds(c);
        if (feeds != null && !feeds.isEmpty()) {
            return feeds.get(0);
        }
        return null;
    }

    @Override
    public List<Feed> getFeeds(FeedCriteria criteria) {
        Criteria critImpl = (Criteria) criteria;
        Iterator<Feed> filtered = Iterators.filter(this.feeds.values().iterator(), critImpl);
        Iterator<Feed> limited = Iterators.limit(filtered, critImpl.getLimit());

        return ImmutableList.copyOf(limited);
    }


    @Override
    public boolean enableFeed(Feed.ID id) {
        BaseFeed feed = (BaseFeed) getFeed(id);
        if (feed != null) {
            feed.setState(Feed.State.ENABLED);
            return true;
        }
        return false;

    }

    @Override
    public boolean disableFeed(Feed.ID id) {
        BaseFeed feed = (BaseFeed) getFeed(id);
        if (feed != null) {
            feed.setState(Feed.State.DISABLED);
            return true;
        }
        return false;
    }

    @Override
    public void deleteFeed(@Nonnull final Feed.ID feedId) {
        feeds.remove(feedId);
    }

    private FeedSource ensureFeedSource(BaseFeed<?> feed, Datasource ds, ServiceLevelAgreement.ID slaId) {
        Map<Datasource.ID, FeedSource> srcIds = new HashMap<>();
        for (FeedSource src : feed.getSources()) {
            srcIds.put(src.getDatasource().getId(), src);
        }

        if (srcIds.containsKey(ds.getId())) {
            return srcIds.get(ds.getId());
        } else {
            ServiceLevelAgreement sla = this.slaProvider.getAgreement(slaId);
            FeedSource src = feed.addSource(ds, sla);
            return src;
        }
    }

    private FeedDestination ensureFeedDestination(BaseFeed feed, Datasource ds) {
        FeedDestination dest = feed.getDestination(ds.getId());

        if (dest != null) {
            return dest;
        } else {
            dest = feed.addDestination(ds);
            return dest;
        }
    }

    private Feed<?> setupPrecondition(BaseFeed feed, ServiceLevelAgreement sla) {
        feed.setPrecondition(sla);
        return feed;
    }

    private static class Criteria extends AbstractMetadataCriteria<FeedCriteria> implements FeedCriteria, Predicate<Feed> {

        private String name;
        private Set<Datasource.ID> sourceIds = new HashSet<>();
        private Set<Datasource.ID> destIds = new HashSet<>();
        private String category;

        @Override
        public boolean apply(Feed input) {
            if (this.name != null && !name.equals(input.getName())) {
                return false;
            }

            if (this.category != null && input.getCategory() != null && !this.category.equals(input.getCategory().getName())) {
                return false;
            }

            if (!this.destIds.isEmpty()) {
                List<FeedDestination> destinations = input.getDestinations();
                for (FeedDestination dest : destinations) {
                    if (this.destIds.contains(dest.getDatasource().getId())) {
                        return true;
                    }
                }
                return false;
            }

            if (!this.sourceIds.isEmpty()) {
                List<FeedSource> sources = input.getSources();
                for (FeedSource src : sources) {
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

        @Override
        public FeedCriteria category(String category) {
            this.category = category;
            return this;
        }

    }


    private class PreconditionbuilderImpl implements PreconditionBuilder {

        private final ServiceLevelAgreementBuilder slaBuilder;
        private final BaseFeed feed;

        public PreconditionbuilderImpl(ServiceLevelAgreementBuilder slaBuilder, BaseFeed feed) {
            super();
            this.slaBuilder = slaBuilder;
            this.feed = feed;
        }

        public ServiceLevelAgreementBuilder name(String name) {
            return slaBuilder.name(name);
        }

        public ServiceLevelAgreementBuilder description(String description) {
            return slaBuilder.description(description);
        }

        public ServiceLevelAgreementBuilder obligation(Obligation obligation) {
            return slaBuilder.obligation(obligation);
        }

        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder() {
            return slaBuilder.obligationBuilder();
        }

        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder(Condition condition) {
            return slaBuilder.obligationBuilder(condition);
        }

        public ObligationGroupBuilder obligationGroupBuilder(Condition condition) {
            return slaBuilder.obligationGroupBuilder(condition);
        }

        public ServiceLevelAgreement build() {
            ServiceLevelAgreement sla = slaBuilder.build();

            setupPrecondition(feed, sla);
            return sla;
        }

        @Override
        public ServiceLevelAgreementBuilder actionConfigurations(List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations) {
            return null;
        }
    }


    @Override
    public Feed updateFeedServiceLevelAgreement(Feed.ID feedId, ServiceLevelAgreement sla) {
        return null;
    }

    @Override
    public Map<String, Object> mergeFeedProperties(Feed.ID feedId, Map<String, Object> properties) {
        return null;
    }

    @Override
    public Map<String, Object> replaceProperties(Feed.ID feedId, Map<String, Object> properties) {
        return null;
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getUserFields() {
        return Collections.emptySet();
    }

    @Override
    public void setUserFields(@Nonnull Set<UserFieldDescriptor> userFields) {}
}
