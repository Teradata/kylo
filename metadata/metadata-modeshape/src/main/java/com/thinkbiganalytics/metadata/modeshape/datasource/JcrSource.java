package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrSource extends JcrFeedConnection implements FeedSource {

    public static String NODE_TYPE = "tba:feedSource";

    public JcrSource(Node node) {
        super(node);
    }

    public JcrSource(Node node, JcrDatasource datasource) {
        super(node, datasource);
    }

    @Override
    public FeedSourceId getId() {
        try {
            return new JcrSource.FeedSourceId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    public static class FeedSourceId extends JcrEntity.EntityId implements FeedSource.ID {

        public FeedSourceId(Serializable ser) {
            super(ser);
        }
    }


    @Override
    public ServiceLevelAgreement getAgreement() {
        return null;
    }

}
