/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public class JcrFeedSource extends JcrFeedConnection implements FeedSource {

    public static final String NODE_TYPE = "tba:feedSource";

    public JcrFeedSource(Node node) {
        super(node);
    }
    
    public JcrFeedSource(Node node, JcrDatasource datasource) {
        super(node, datasource);
        datasource.addSourceNode(this.node);
    }
    
    

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedSource#getAgreement()
     */
    @Override
    public ServiceLevelAgreement getAgreement() {
        return null;
    }
}
