package com.thinkbiganalytics.metadata.modeshape.feed;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 * @author Sean Felten
 */
public abstract class JcrFeedConnection extends JcrObject implements FeedConnection {

    public static String DATASOURCE = "tba:datasource";

    public JcrFeedConnection(Node node) {
        super(node);
    }

    public JcrFeedConnection(Node node, JcrDatasource datasource) {
        this(node);
        this.setProperty(DATASOURCE, datasource);
    }

    public Datasource getDatasource() {
        return JcrUtil.getReferencedObject(this.node, DATASOURCE, JcrDatasourceProvider.TYPE_RESOLVER);
    }


    @Override
    public Feed getFeed() {
        try {
            return JcrUtil.createJcrObject(this.node.getParent(), JcrFeed.class);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access feed", e);
        }
    }
}