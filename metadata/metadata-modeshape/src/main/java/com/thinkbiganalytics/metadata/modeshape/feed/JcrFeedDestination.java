/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;

/**
 *
 * @author Sean Felten
 */
public class JcrFeedDestination extends JcrFeedConnection implements FeedDestination {

    public static final String NODE_TYPE = "tba:feedDestination";

    /**
     * @param node
     */
    public JcrFeedDestination(Node node) {
        super(node);
    }

    /**
     * @param node
     * @param datasource
     */
    public JcrFeedDestination(Node node, JcrDatasource datasource) {
        super(node, datasource);
        datasource.addDestinationNode(this.node);
    }
}
