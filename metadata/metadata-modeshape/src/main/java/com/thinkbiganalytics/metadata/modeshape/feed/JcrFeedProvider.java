/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

import java.io.Serializable;
import java.util.List;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.modeshape.jcr.api.JcrTools;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrFeedProvider extends BaseJcrProvider<JcrFeed,Feed.ID> implements FeedProvider {

    @Inject
    CategoryProvider categoryPovider;

    @Inject
    DatasourceProvider datasourceProvider;

    @Override
    public String getNodeType() {
        return JcrFeed.NODE_TYPE;
    }

    @Override
    public Class<? extends JcrFeed> getEntityClass() {
        return JcrFeed.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrFeed.class;
    }


    /**
     * 
     */
    public JcrFeedProvider() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public FeedSource ensureFeedSource(ID feedId, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId) {
        JcrFeed feed = findById(feedId);
        FeedSource source = feed.getSource(dsId);
        if (source == null) {
            JcrDatasource datasource = (JcrDatasource) datasourceProvider.getDatasource(dsId);
            try {
                if (datasource != null) {
                    Node feedNode = getNodeByIdentifier(feedId);
                    //    JcrUtil.getOrCreateNode(feedNode,JcrFeed.SOURCE_NAME,JcrSource.NODE_TYPE,JcrSource.class,new Object[] {datasource});
                    Node feedSourceNode = feedNode.addNode(JcrFeed.SOURCE_NAME, JcrSource.NODE_TYPE);
                    JcrSource jcrSource = new JcrSource(feedSourceNode, datasource);
                    return jcrSource;
                }
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Unable to create feedSource for dataSource " + dsId + " with Feed Id of " + feedId, e);
            }
        }
        return source;
    }

    @Override
    public FeedSource ensureFeedSource(ID feedId, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id, com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID slaId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeedDestination ensureFeedDestination(ID feedId, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Feed ensureFeed(Category.ID categoryId, String feedSystemName) {
        Category category = categoryPovider.findById(categoryId);
        return ensureFeed(category.getName(),feedSystemName);
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName) {
        String categoryPath = EntityUtil.pathForCategory(categorySystemName);
        Node feedNode = findOrCreateEntityNode(categoryPath, feedSystemName);
        JcrFeed feed = new JcrFeed(feedNode);
        feed.setSystemName(feedSystemName);
        return feed;
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName, String descr) {
        Feed feed = ensureFeed(categorySystemName,feedSystemName);
        feed.setDescription(descr);
        return feed;
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName, String descr, Datasource.ID destId) {
        Feed feed = ensureFeed(categorySystemName,feedSystemName,descr);
        //TODO add/find datasources
        return feed;
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName, String descr, Datasource.ID srcId, Datasource.ID destId) {
        Feed feed = ensureFeed(categorySystemName,feedSystemName,descr);
        if (srcId != null) {
            ensureFeedSource(feed.getId(), srcId);
        }
        return feed;
    }

    @Override
    public Feed ensurePrecondition(ID feedId,  String name, String descr, List<List<Metric>> metrics) {
        return null;
    }

    @Override
    public Feed updatePrecondition(ID feedId, List<List<Metric>> metrics) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeedCriteria feedCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Feed getFeed(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Feed> getFeeds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Feed> getFeeds(FeedCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeedSource getFeedSource(com.thinkbiganalytics.metadata.api.feed.FeedSource.ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeedDestination getFeedDestination(com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ID resolveFeed(Serializable fid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public com.thinkbiganalytics.metadata.api.feed.FeedSource.ID resolveSource(Serializable sid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID resolveDestination(Serializable sid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean enableFeed(ID id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean disableFeed(ID id) {
        // TODO Auto-generated method stub
        return false;
    }

}
