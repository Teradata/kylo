package com.thinkbiganalytics.metadata.modeshape.feed;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/4/16.
 */
public class JcrFeed extends AbstractJcrSystemEntity implements Feed {

    public static final String NODE_TYPE = "tba:feed";
    public static final String SOURCE_NAME = "tba:sources";
    public static final String DESTINATION_NAME = "tba:destinations";
    public static final String CATEGORY = "tba:category";


    public JcrFeed(Node node) {
        super(node);
    }

    public JcrFeed(Node node, JcrCategory category) {
        super(node);
        setProperty(CATEGORY, category);

        Object o = getProperty(CATEGORY);
        int i = 0;

    }

    @Override
    public FeedId getId() {
        try {
            return new JcrFeed.FeedId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
    public static class FeedId extends JcrEntity.EntityId implements Feed.ID {

        public FeedId(Serializable ser) {
            super(ser);
        }
    }

    public JcrCategory getCategory() {

        return JcrUtil.getNode(this.node, JcrFeed.CATEGORY, JcrCategory.class);
        /*
        try {
            return new JcrCategory(node.getParent());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Category for feed " + getTitle(), e);
        }
        */
    }


    public List<JcrSource> getSources() {
        return JcrUtil.getNodes(this.node, SOURCE_NAME, JcrSource.class);
    }

    public List<JcrDestination> getDestinations() {
        return JcrUtil.getNodes(this.node, DESTINATION_NAME, JcrDestination.class);
    }


    @Override
    public DateTime getModifiedTime() {
        return null;
    }

    @Override
    public String getName() {
        return getSystemName();
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public FeedPrecondition getPrecondition() {
        return null;
    }

    @Override
    public FeedSource getSource(final Datasource.ID id) {
        FeedSource source = null;
        List<JcrSource> sources = getSources();
        if (sources != null && !sources.isEmpty()) {
            source = Iterables.tryFind(sources, new Predicate<JcrSource>() {
                @Override
                public boolean apply(JcrSource jcrSource) {
                    return jcrSource.getDatasource().getId().equals(id);
                }
            }).orNull();
        }
        return source;
    }

    @Override
    public FeedSource getSource(final FeedSource.ID id) {
        FeedSource source = null;
        List<JcrSource> sources = getSources();
        if (sources != null && !sources.isEmpty()) {
            source = Iterables.tryFind(sources, new Predicate<JcrSource>() {
                @Override
                public boolean apply(JcrSource jcrSource) {
                    return jcrSource.getId().equals(id);
                }
            }).orNull();
        }
        return source;

    }

    @Override
    public FeedDestination getDestination(final Datasource.ID id) {
        FeedDestination destination = null;
        List<JcrDestination> destinations = getDestinations();
        if (destinations != null && !destinations.isEmpty()) {
            destination = Iterables.tryFind(destinations, new Predicate<JcrDestination>() {
                @Override
                public boolean apply(JcrDestination jcrDestination) {
                    return jcrDestination.getDatasource().getId().equals(id);
                }
            }).orNull();
        }
        return destination;
    }

    @Override
    public FeedDestination getDestination(final FeedDestination.ID id) {

        FeedDestination destination = null;
        List<JcrDestination> destinations = getDestinations();
        if (destinations != null && !destinations.isEmpty()) {
            destination = Iterables.tryFind(destinations, new Predicate<JcrDestination>() {
                @Override
                public boolean apply(JcrDestination jcrDestination) {
                    return jcrDestination.getId().equals(id);
                }
            }).orNull();
        }
        return destination;
    }

    @Override
    public void setInitialized(boolean flag) {

    }

    @Override
    public void setDisplayName(String name) {
        setTitle(name);
    }

    @Override
    public void setState(State state) {

    }

    @Override
    public Integer getVersion() {
        return null;
    }

    @Override
    public DateTime getCreatedTime() {
        return null;
    }


}
