/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryPovider;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 *
 * @author Sean Felten
 */
public class JcrFeedProvider extends BaseJcrProvider<JcrFeed,Feed.ID> implements FeedProvider {

    @Inject
    JcrCategoryPovider categoryPovider;

    @Override
    public String getNodeType() {
        return JcrFeed.FEED_TYPE;
    }

    @Override
    public Class<? extends JcrFeed> getEntityClass() {
        return JcrFeed.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrFeed.class;
    }

    public JcrFeed createFeed(Category.ID categoryId, Map<String,Object> props) {
        Category category = ((JcrCategoryPovider)categoryPovider).findById(categoryId);
        String systemName = (String)props.get(AbstractJcrSystemEntity.SYSTEM_NAME);
        String categoryPath = EntityUtil.pathForCategory(category.getName());
        Node feedNode = createEntityNode(categoryPath,systemName);
        JcrFeed feed = new JcrFeed(feedNode);
        feed.setSystemName(systemName);
        return feed;
    }


    /**
     * 
     */
    public JcrFeedProvider() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public FeedSource ensureFeedSource(ID feedId, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId) {
        // TODO Auto-generated method stub
        return null;
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
    public Feed ensureFeed(String name, String descr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Feed ensureFeed(String name, String descr, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID destId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Feed ensureFeed(String name, String descr, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID srcId, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID destId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Feed ensurePrecondition(ID feedId, String name, String descr, List<List<Metric>> metrics) {
        // TODO Auto-generated method stub
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
