package com.thinkbiganalytics.metadata.modeshape.datasource;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

/**
 * Created by sr186054 on 6/7/16.
 */
public abstract class JcrFeedConnection extends JcrObject implements FeedConnection {

    private static String DATASOURCE_NAME = "tba:datasource";

    public JcrFeedConnection(Node node) {
        super(node);
    }

    public JcrFeedConnection(Node node, JcrDatasource datasource) {
        this(node);
        this.setProperty(DATASOURCE_NAME, datasource);
    }

    public Datasource getDatasource() {

        try {
            PropertyIterator itr = this.node.getProperties();
            while (itr.hasNext()) {
                Property p = itr.nextProperty();
                Value v = p.getValue();
            }
        } catch (Exception e) {

        }

        return getProperty(DATASOURCE_NAME, JcrDatasource.class);
    }


    @Override
    public Feed getFeed() {
        return null;
    }
}