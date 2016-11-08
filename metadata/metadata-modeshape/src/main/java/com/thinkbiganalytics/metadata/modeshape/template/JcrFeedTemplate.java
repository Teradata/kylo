package com.thinkbiganalytics.metadata.modeshape.template;

import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedManagerFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/8/16.
 */
public class JcrFeedTemplate extends AbstractJcrAuditableSystemEntity implements FeedManagerTemplate {

    public static String NODE_TYPE = "tba:feedTemplate";

    public static String STATE = "tba:state";
    public static String DEFINE_TABLE = "tba:defineTable";
    public static String DATA_TRANSFORMATION = "tba:dataTransformation";
    public static String ALLOW_PRECONDITIONS = "tba:allowPreconditions";
    public static String ICON = "tba:icon";
    public static String ICON_COLOR = "tba:iconColor";
    public static String NIFI_TEMPLATE_ID = "tba:nifiTemplateId";
    public static String FEEDS = "tba:feeds";

    public static String JSON = "tba:json";

    public JcrFeedTemplate(Node node) {
        super(node);
    }

    @Override
    public FeedTemplateId getId() {
        try {
            return new JcrFeedTemplate.FeedTemplateId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    public static class FeedTemplateId extends JcrEntity.EntityId implements FeedManagerTemplate.ID {

        public FeedTemplateId(Serializable ser) {
            super(ser);
        }
    }

    @Override
    public String getName() {
        return getTitle();
    }

    @Override
    public String getNifiTemplateId() {

        return getProperty(NIFI_TEMPLATE_ID, String.class);
    }

    @Override
    public boolean isDefineTable() {
        return getProperty(DEFINE_TABLE, Boolean.class);
    }

    @Override
    public boolean isDataTransformation() {
        return getProperty(DATA_TRANSFORMATION, Boolean.class);
    }

    @Override
    public boolean isAllowPreconditions() {
        return getProperty(ALLOW_PRECONDITIONS, Boolean.class);
    }

    @Override
    public String getIcon() {
        return getProperty(ICON, String.class);
    }

    @Override
    public String getIconColor() {
        return getProperty(ICON_COLOR, String.class);
    }

    @Override
    public String getJson() {
        return getProperty(JSON, String.class);
    }


    @Override
    public void setNifiTemplateId(String nifiTemplateId) {
        setProperty(NIFI_TEMPLATE_ID, nifiTemplateId);
    }

    @Override
    public void setAllowPreconditions(boolean allowedPreconditions) {
        setProperty(ALLOW_PRECONDITIONS, allowedPreconditions);
    }

    @Override
    public void setDefineTable(boolean defineTable) {
        setProperty(DEFINE_TABLE, defineTable);
    }

    @Override
    public void setDataTransformation(boolean dataTransformation) {
        setProperty(DATA_TRANSFORMATION, dataTransformation);
    }

    @Override
    public void setName(String name) {
        setTitle(name);
    }

    @Override
    public void setIcon(String icon) {
        setProperty(ICON, icon);
    }

    @Override
    public void setIconColor(String iconColor) {
        setProperty(ICON_COLOR, iconColor);
    }

    @Override
    public void setJson(String json) {
        setProperty(JSON, json);
    }

    public State getState() {
        return getProperty(STATE, FeedManagerTemplate.State.ENABLED);
    }

    @Override
    public void setState(State state) {
        setProperty(STATE, state);
    }

    public boolean isEnabled() {
        return State.ENABLED.equals(getState());
    }

    public void enable() {
        setProperty(STATE, FeedManagerTemplate.State.ENABLED);
    }

    public void disable() {
        setProperty(STATE, State.DISABLED);
    }

    public List<FeedManagerFeed> getFeeds() {
        List<FeedManagerFeed> feeds = new ArrayList<>();
        Set<Node> feedNodes = JcrPropertyUtil.getSetProperty(this.node, FEEDS);

        for (Node depNode : feedNodes) {
            feeds.add(new JcrFeedManagerFeed(depNode));
        }

        return feeds;
    }

    public boolean addFeed(FeedManagerFeed<?> feed) {
        JcrFeedManagerFeed<?> jcrFeed = (JcrFeedManagerFeed<?>) feed;
        Node feedNode = jcrFeed.getNode();

        return JcrPropertyUtil.addToSetProperty(this.node, FEEDS, feedNode);
    }

    public boolean removeFeed(FeedManagerFeed<?> feed) {
        JcrFeedManagerFeed<?> jcrFeed = (JcrFeedManagerFeed<?>) feed;
        Node feedNode = jcrFeed.getNode();

        return JcrPropertyUtil.removeFromSetProperty(this.node, FEEDS, feedNode);
    }
}
