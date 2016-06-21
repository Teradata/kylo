package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrDestination extends JcrFeedConnection implements FeedDestination {

    public static String NODE_TYPE = "tba:feedDestination";

    public JcrDestination(Node node) {
        super(node);
    }

    public JcrDestination(Node node, JcrDatasource datasource) {
        super(node, datasource);
    }

    @Override
    public FeedDestinationId getId() {
        try {
            return new JcrDestination.FeedDestinationId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    public static class FeedDestinationId extends JcrEntity.EntityId implements FeedDestination.ID {

        public FeedDestinationId(Serializable ser) {
            super(ser);
        }
    }


}
