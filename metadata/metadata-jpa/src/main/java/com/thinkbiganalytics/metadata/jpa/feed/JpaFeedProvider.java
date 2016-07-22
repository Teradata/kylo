/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import com.google.common.base.Predicate;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceNotFoundException;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.PreconditionBuilder;
import com.thinkbiganalytics.metadata.jpa.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.jpa.BaseJpaProvider;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed.JpaFeedPrecondition;
import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.AgreementNotFoundException;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Sean Felten
 */
public class JpaFeedProvider extends BaseJpaProvider<Feed, Feed.ID> implements FeedProvider {

    @PersistenceContext
    @Inject
    @Qualifier("metadataEntityManager")
    private EntityManager entityMgr;
    
    @Inject
    private DatasourceProvider datasourceProvider;
    
    @Inject
    private ServiceLevelAgreementProvider slaProvider;

    @Inject
    private CategoryProvider<Category> categoryProvider;

    @Override
    public Class<? extends Feed> getEntityClass() {
        return JpaFeed.class;
    }

    @Override
    public ID resolveId(Serializable fid) {
        return resolveFeed(fid);
    }

    @Override
    public Feed ensureFeed(Category.ID categoryId, String feedSystemName) {
        Category category = categoryProvider.findById(categoryId);
        if(category == null) {
            throw new CategoryNotFoundException("Unable to ensure feed of name "+feedSystemName+" because the Category does not exist",categoryId);
        }
        else {
            return ensureFeed(category.getName(),feedSystemName);
        }
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName) {
        return ensureFeed(categorySystemName, feedSystemName, null);
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
    public Feed ensureFeed(String categorySystemName,String name, String descr) {
        return ensureFeed(categorySystemName,name, descr, null, null);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Feed ensureFeed(String categorySystemName, String name, String descr, Datasource.ID destId) {
        return ensureFeed(categorySystemName,name, descr, null, destId);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensureFeed(java.lang.String, java.lang.String, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Feed ensureFeed(String categorySystemName, String name, String descr, Datasource.ID srcId, Datasource.ID destId) {
        JpaFeed feed = null;
        List<Feed> feeds = getFeeds(feedCriteria().name(name).category(categorySystemName));
        
        if (feeds.isEmpty()) {
            feed = new JpaFeed(name, descr);
            Category category = categoryProvider.findBySystemName(categorySystemName);
            if(category != null){
                feed.setCategory(category);
            }
            else {
                throw new CategoryNotFoundException("Unable to find Category for "+categorySystemName+" while attempting to create feed "+name,null);
            }
            this.entityMgr.persist(feed);
        } else {
            feed = (JpaFeed) feeds.get(0);
        }
        
        if (srcId != null) {
            ensureFeedSource(feed, srcId, null);
        }
        
        if (destId != null) {
            ensureFeedDestination(feed, destId);
        }
        
        this.entityMgr.merge(feed);
//        this.entityMgr.flush();
        return feed;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#ensurePrecondition(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public Feed createPrecondition(ID feedId, String descr, List<Metric> metrics) {
        JpaFeed feed = (JpaFeed) getFeed(feedId);
        
        if (feed != null) {
            ServiceLevelAgreement sla = this.slaProvider.builder()
                    .name("Precondition for feed " + feed.getName() + " (" + feed.getId() + ")")
                    .description(descr)
                    .obligationBuilder(Condition.SUFFICIENT)
                        .metric(metrics)
                        .build()
                    .build();
            
            feed.setPrecondition((JpaFeedPrecondition) sla);
            this.entityMgr.merge(feed);
            return feed;
        } else {
            throw new FeedNotFoundExcepton(feedId);
        }
    }
    
    @Override
    public PreconditionBuilder buildPrecondition(ID feedId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Feed<?> addDependent(ID targetId, ID dependentId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Feed<?> removeDependent(ID feedId, ID dependentId) {
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
        return this.entityMgr.find(JpaFeed.class, id);
    }



    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeeds()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Feed> getFeeds() {
//        return new ArrayList<Feed>(this.entityMgr.createQuery("select f from JpaFeed f", JpaFeed.class).getResultList());
        return new ArrayList<Feed>(this.entityMgr.createQuery("select f from JpaFeed f").getResultList());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#getFeeds(com.thinkbiganalytics.metadata.api.feed.FeedCriteria)
     */
    @Override
    public List<Feed> getFeeds(FeedCriteria criteria) {
        Criteria critImpl = (Criteria) criteria;
        return new ArrayList<Feed>(critImpl.select(this.entityMgr, JpaFeed.class));
    }

    /*
    @Override
    public FeedSource getFeedSource(FeedSource.ID id) {
        return this.entityMgr.find(JpaFeedSource.class, id);
    }


    @Override
    public FeedDestination getFeedDestination(FeedDestination.ID id) {
        return this.entityMgr.find(JpaFeedDestination.class, id);
    }
*/
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#resolveFeed(java.io.Serializable)
     */
    @Override
    public Feed.ID resolveFeed(Serializable fid) {
        if (fid instanceof JpaFeed.FeedId) {
            return (JpaFeed.FeedId) fid;
        } else {
            return new JpaFeed.FeedId(fid);
        }
    }
    
    @Override
    public Feed findBySystemName(String systemName) {
        return findBySystemName(null, systemName);
    }

    @Override
    public Feed findBySystemName(String categorySystemName, String systemName) {
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

    /*
      @Override
      public FeedSource.ID resolveSource(Serializable sid) {
          if (sid instanceof JpaFeedSource.SourceId) {
              return (JpaFeedSource.SourceId) sid;
          } else {
              return new JpaFeedSource.SourceId(sid);
          }
      }


      @Override
      public FeedDestination.ID resolveDestination(Serializable did) {
          if (did instanceof JpaFeedDestination.DestinationId) {
              return (JpaFeedDestination.DestinationId) did;
          } else {
              return new JpaFeedDestination.DestinationId(did);
          }
      }
  */
    @Override
    public boolean enableFeed(ID id) {
       Feed feed = getFeed(id);
        if(feed != null){
            JpaFeed jpaFeed = (JpaFeed) feed;
            jpaFeed.setState(Feed.State.ENABLED);
            entityMgr.merge(jpaFeed);
            return true;
        }
        return false;
    }

    @Override
    public boolean disableFeed(ID id) {
        Feed feed = getFeed(id);
        if(feed != null){
            JpaFeed jpaFeed = (JpaFeed) feed;
            jpaFeed.setState(Feed.State.DISABLED);
             entityMgr.merge(jpaFeed);
            return true;
        }
        return false;
    }

    private static class Criteria extends AbstractMetadataCriteria<FeedCriteria> implements FeedCriteria, Predicate<Feed> {
        
        private String name;
        private Set<Datasource.ID> sourceIds = new HashSet<>();
        private Set<Datasource.ID> destIds = new HashSet<>();
        private String category;
        
        @Override
        protected void applyFilter(StringBuilder queryStr, HashMap<String, Object> params) {
            StringBuilder cond = new StringBuilder();
            StringBuilder join = new StringBuilder();
            
            if (this.name != null) {
                cond.append("name = :name");
                params.put("name", this.name);
            }
            if (this.category != null) {
                cond.append("category = :category");
                params.put("category", this.category);
            }
            
            applyIdFilter(cond, join, this.sourceIds, "sources", params);
            applyIdFilter(cond, join, this.destIds, "destinations", params);
            
            if (join.length() > 0) {
                queryStr.append(join.toString());
            }
            
            if (cond.length() > 0) {
                queryStr.append(" where ").append(cond.toString());
            }
        }
        
        private void applyIdFilter(StringBuilder cond, StringBuilder join, Set<Datasource.ID> idSet, String relation, HashMap<String, Object> params) {
            if (! idSet.isEmpty()) {
                if (cond.length() > 0) {
                    cond.append("and ");
                }
                
                String alias = relation.substring(0, 1);
                join.append("join e.").append(relation).append(" ").append(alias).append(" ");
                cond.append(alias).append(".datasource.id in :").append(relation).append(" ");
                params.put(relation, idSet);
            }
        }

        @Override
        public boolean apply(Feed input) {
            if (this.name != null && ! name.equals(input.getName())) return false;
            if(this.category != null && input.getCategory() != null && !this.category.equals(input.getCategory().getName())){
                return false;
            }
            if (! this.destIds.isEmpty()) {
                List<FeedDestination> destinations = input.getDestinations();
                for (FeedDestination dest : destinations) {
                    if (this.destIds.contains(dest.getDatasource().getId())) {
                        return true;
                    }
                }
                return false;
            }
            
            if (! this.sourceIds.isEmpty()) {
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

        @Override
        public FeedCriteria category(String category) {
            this.category = category;
            return this;
        }
    }

    @Override
    public Feed updateFeedServiceLevelAgreement(ID feedId, ServiceLevelAgreement sla) {
        return null;
    }

    @Override
    public Map<String, Object> mergeFeedProperties(ID feedId, Map<String, Object> properties) {
        return null;
    }

    @Override
    public Map<String, Object> replaceProperties(ID feedId, Map<String, Object> properties) {
        return null;
    }
}
