/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import com.google.common.base.Predicate;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceNotFoundException;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.event.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.jpa.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed.JpaFeedPrecondition;
import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.AgreementNotFoundException;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

/**
 *
 * @author Sean Felten
 */
public class FeedProviderImpl implements FeedProvider {

    @Inject
    private EntityManager entityMgr;
    
    @Inject
    private DatasourceProvider datasourceProvider;
    
    @Inject
    private ServiceLevelAgreementProvider slaProvider;
    
//    @Inject 
//    private FeedPreconditionService preconditionService;


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#asFeedId(java.lang.String)
     */
    @Override
    public ID asFeedId(String feedIdStr) {
        return new JpaFeed.FeedId(feedIdStr);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeedSource(com.thinkbiganalytics.metadata.api.feed.Feed.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public FeedSource ensureFeedSource(ID feedId, Datasource.ID dsId) {
        return ensureFeedSource(feedId, dsId, null);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeedSource(com.thinkbiganalytics.metadata.api.feed.Feed.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID)
     */
    @Override
    public FeedSource ensureFeedSource(ID feedId, Datasource.ID dsId, ServiceLevelAgreement.ID slaId) {
        JpaFeed feed = (JpaFeed) getFeed(feedId);
        JpaFeedSource src = null;
        
        if (feed != null) {
            return ensureFeedSource(feed, dsId, slaId);
        } else {
            throw new FeedNotFoundExcepton(feedId);
        }
    }

    private FeedSource ensureFeedSource(JpaFeed feed, Datasource.ID dsId, ServiceLevelAgreement.ID slaId) {
        JpaFeedSource src = (JpaFeedSource) feed.getSource(dsId);
            
        if (src != null) {
            return src;
        } else {
            JpaServiceLevelAgreement sla = null;
            
            if (slaId != null) {
                sla = (JpaServiceLevelAgreement) this.slaProvider.getAgreement(slaId);
                
                if (sla == null) {
                    throw new AgreementNotFoundException(slaId);
                }
            }
            
            JpaDatasource dsImpl = (JpaDatasource) this.datasourceProvider.getDatasource(dsId);
            
            if (dsImpl != null) {
                if (sla != null) {
                    src = feed.addSource(dsImpl, sla);
                } else {
                    src = feed.addSource(dsImpl);
                }
                
                this.entityMgr.merge(feed);
                return src;
            } else {
                throw new DatasourceNotFoundException("Could not create the feed source because the datasource does not exist", dsId);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeedDestination(com.thinkbiganalytics.metadata.api.feed.Feed.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public FeedDestination ensureFeedDestination(ID feedId, Datasource.ID dsId) {
        JpaFeed feed = (JpaFeed) getFeed(feedId);
        JpaFeedDestination dest = null;
        
        if (feed != null) {
            return ensureFeedDestination(feed, dsId);
        } else {
            throw new FeedNotFoundExcepton(feedId);
        }
    }

    private FeedDestination ensureFeedDestination(JpaFeed feed, Datasource.ID dsId) {
        JpaFeedDestination dest = (JpaFeedDestination) feed.getDestination(dsId);
            
        if (dest != null) {
            return dest;
        } else {
            JpaDatasource dsImpl = (JpaDatasource) this.datasourceProvider.getDatasource(dsId);
            
            if (dsImpl != null) {
                dest = feed.addDestination(dsImpl);
                
                this.entityMgr.merge(feed);
                return dest;
            } else {
                throw new DatasourceNotFoundException("Could not create the feed destination because the datasource does not exist", dsId);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String)
     */
    @Override
    public Feed ensureFeed(String name, String descr) {
        return ensureFeed(name, descr, null, null);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Feed ensureFeed( String name, String descr, Datasource.ID destId) {
        return ensureFeed(name, descr, null, destId);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Feed ensureFeed(String name, String descr, Datasource.ID srcId, Datasource.ID destId) {
        JpaFeed feed = null;
        List<Feed> feeds = getFeeds(feedCriteria().name(name));
        
        if (feeds.isEmpty()) {
            feed = new JpaFeed(name, descr);
        } else {
            feed = (JpaFeed) feeds.get(0);
        }
        
        if (srcId != null) {
            ensureFeedSource(feed, srcId, null);
        }
        
        if (destId != null) {
            ensureFeedDestination(feed, destId);
        }
        
        this.entityMgr.persist(feed);
        return feed;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensurePrecondition(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public Feed ensurePrecondition(ID feedId, String name, String descr, List<List<Metric>> metrics) {
        JpaFeed feed = (JpaFeed) getFeed(feedId);
        
        if (feed != null) {
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
            
            JpaServiceLevelAgreement sla = (JpaServiceLevelAgreement) slaBldr.build();
            
            // TODO create interface for service in API project or somewhere
//            this.preconditionService.watchFeed(feed);
            
            feed.setPrecondition(sla);
            this.entityMgr.merge(feed);
            return feed;
        } else {
            throw new FeedNotFoundExcepton(feedId);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#updatePrecondition(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.util.List)
     */
    @Override
    public Feed updatePrecondition(ID feedId, List<List<Metric>> metrics) {
        JpaFeed feed = (JpaFeed) getFeed(feedId);
        
        if (feed != null) {
            JpaFeed.JpaFeedPrecondition precond = (JpaFeedPrecondition) feed.getPrecondition();
            ServiceLevelAgreement.ID slaId = precond.getAgreement().getId();
            ServiceLevelAgreementBuilder slaBldr = this.slaProvider.builder(slaId);
            
            for (List<Metric> list : metrics) {
                slaBldr.obligationGroupBuilder(Condition.SUFFICIENT)
                    .obligationBuilder()
                        .metric(list)
                        .add()
                    .build();
            }
            
            JpaServiceLevelAgreement sla = (JpaServiceLevelAgreement) slaBldr.build();
            
            feed.setPrecondition(sla);
            this.entityMgr.merge(feed);
            return feed;
        } else {
            throw new FeedNotFoundExcepton(feedId);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#feedCriteria()
     */
    @Override
    public FeedCriteria feedCriteria() {
        return new Criteria();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeed(com.thinkbiganalytics.metadata.api.feed.Feed.ID)
     */
    @Override
    public Feed getFeed(ID id) {
        return this.entityMgr.find(JpaFeed.class, id);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeeds()
     */
    @Override
    public List<Feed> getFeeds() {
//        return new ArrayList<Feed>(this.entityMgr.createQuery("select f from JpaFeed f", JpaFeed.class).getResultList());
        return new ArrayList<Feed>(this.entityMgr.createQuery("select f from JpaFeed f").getResultList());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeeds(com.thinkbiganalytics.metadata.api.feed.FeedCriteria)
     */
    @Override
    public List<Feed> getFeeds(FeedCriteria criteria) {
        // TODO Auto-generated method stub
//        return null;
        return getFeeds();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeedSource(com.thinkbiganalytics.metadata.api.feed.FeedSource.ID)
     */
    @Override
    public FeedSource getFeedSource(com.thinkbiganalytics.metadata.api.feed.FeedSource.ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeedDestination(com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID)
     */
    @Override
    public FeedDestination getFeedDestination(com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#resolveFeed(java.io.Serializable)
     */
    @Override
    public ID resolveFeed(Serializable fid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#resolveSource(java.io.Serializable)
     */
    @Override
    public com.thinkbiganalytics.metadata.api.feed.FeedSource.ID resolveSource(Serializable sid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#resolveDestination(java.io.Serializable)
     */
    @Override
    public com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID resolveDestination(Serializable sid) {
        // TODO Auto-generated method stub
        return null;
    }

    
    private static class Criteria extends AbstractMetadataCriteria<FeedCriteria> implements FeedCriteria, Predicate<Feed> {
        
        private String name;
        private Set<Datasource.ID> sourceIds = new HashSet<>();
        private Set<Datasource.ID> destIds = new HashSet<>();
        
        protected List<JpaFeed> select(EntityManager emgr) {
            CriteriaBuilder builder = emgr.getCriteriaBuilder();
            CriteriaQuery<JpaFeed> query = builder.createQuery( JpaFeed.class );
            Root<JpaFeed> root = query.from( JpaFeed.class );
//            root.fetch( "projects", JoinType.LEFT);
//            query.select(root).where(
//                builder.and(
//                    builder.equal(root.get("username"), username),
//                    builder.equal(root.get("password"), password)
//                )
//            );
            return emgr.createQuery( query ).getResultList();
        }
        
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
        public FeedCriteria sourceDatasource(Datasource.ID id, Datasource.ID... others) {
            this.sourceIds.add(id);
            for (Datasource.ID other : others) {
                this.sourceIds.add(other);
            }
            return this;
        }

        @Override
        public FeedCriteria destinationDatasource(Datasource.ID id, Datasource.ID... others) {
            this.destIds.add(id);
            for (Datasource.ID other : others) {
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
