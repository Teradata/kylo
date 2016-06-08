package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 6/8/16.
 */
public class JcrFeedManagerFeedProvider extends JcrFeedProvider implements FeedManagerFeedProvider {


    @Override
    public Class<JcrFeed> getEntityClass() {
        return JcrFeed.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrFeedManagerFeed.class;
    }

    @Override
    public FeedManagerFeed findBySystemName(String categorySystemName, String systemName) {
        FeedCriteria c = super.feedCriteria();
        c.category(categorySystemName);
        c.name(systemName);
        List<Feed> feeds = super.getFeeds(c);
        if (feeds != null && !feeds.isEmpty()) {
            return (FeedManagerFeed) feeds.get(0);
        }
        return null;
    }

    @Override
    public List<? extends FeedManagerFeed> findByTemplateId(FeedManagerTemplate.ID templateId) {
        String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeed.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedManagerFeed.TEMPLATE) + ".id = $id";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("id", templateId.toString());
        return JcrUtil.find(getSession(), query, JcrFeedManagerFeed.class);
    }

    @Override
    public List<? extends FeedManagerFeed> findByCategoryId(FeedManagerCategory.ID categoryId) {

        String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeed.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedManagerFeed.CATEGORY) + ".id = $id";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("id", categoryId.toString());
        return JcrUtil.find(getSession(), query, JcrFeedManagerFeed.class);
    }


}
