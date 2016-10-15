package com.thinkbiganalytics.metadata.modeshape.feed;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.category.JcrFeedManagerCategory;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;

/**
 * Created by sr186054 on 6/8/16.
 */
public class JcrFeedManagerFeed<C extends JcrFeedManagerCategory> extends JcrFeed<C> implements FeedManagerFeed<C> {

    public static String FEED_JSON = "tba:json";
    public static String PROCESS_GROUP_ID = "tba:processGroupId";
    public static String FEED_TEMPLATE = "tba:feedTemplate";

    public JcrFeedManagerFeed(Node node) throws RepositoryException {
        super(node);
    }

    public JcrFeedManagerFeed(Node node, JcrCategory category) {
        super(node, category);
    }

    @Override
    public void setTemplate(FeedManagerTemplate template) {
        setProperty(FEED_TEMPLATE, template);
    }

    @Override
    public String getJson() {
        return getProperty(FEED_JSON, String.class);
    }

    @Override
    public void setJson(String json) {
        setProperty(FEED_JSON, json);
    }

    @Override
    public FeedManagerTemplate getTemplate() {
        return getProperty(FEED_TEMPLATE, JcrFeedTemplate.class, true);
    }

    @Override
    public String getNifiProcessGroupId() {
        return getProperty(PROCESS_GROUP_ID, String.class);
    }

    public void setNifiProcessGroupId(String id) {
        setProperty(PROCESS_GROUP_ID, id);
    }

    public void setVersion(Integer version) {

    }

    public C getCategory() {

        return (C) getCategory(JcrFeedManagerCategory.class);
    }
}
