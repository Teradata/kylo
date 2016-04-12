/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.common.base.Predicate;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.jpa.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.sla.api.Metric;
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
        Feed feed = getFeed(feedId);
        FeedSource src = feed.getSource(dsId);
        
        if (src != null) {
            return src;
        } else {
            JpaFeed feedImpl = (JpaFeed) feed;
            JpaDatasource dsImpl = (JpaDatasource) this.datasourceProvider.getDatasource(dsId);
            return feedImpl.addSource(dsImpl);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeedSource(com.thinkbiganalytics.metadata.api.feed.Feed.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID)
     */
    @Override
    public FeedSource ensureFeedSource(
            ID feedId,
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id,
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID slaId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeedDestination(com.thinkbiganalytics.metadata.api.feed.Feed.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public FeedDestination ensureFeedDestination(
            ID feedId,
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String)
     */
    @Override
    public Feed ensureFeed(String name, String descr) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Feed ensureFeed(
            String name,
            String descr,
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID destId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Feed ensureFeed(
            String name,
            String descr,
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID srcId,
            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID destId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensurePrecondition(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public Feed ensurePrecondition(ID feedId, String name, String descr, List<List<Metric>> metrics) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#updatePrecondition(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.util.List)
     */
    @Override
    public Feed updatePrecondition(ID feedId, List<List<Metric>> metrics) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeeds()
     */
    @Override
    public Collection<Feed> getFeeds() {
        return this.entityMgr.createQuery("select f from JpaFeed f").getResultList();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeeds(com.thinkbiganalytics.metadata.api.feed.FeedCriteria)
     */
    @Override
    public Collection<Feed> getFeeds(FeedCriteria criteria) {
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
