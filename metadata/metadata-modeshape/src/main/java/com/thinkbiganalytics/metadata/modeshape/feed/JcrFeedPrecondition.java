/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 *
 * @author Sean Felten
 */
public class JcrFeedPrecondition extends JcrObject implements FeedPrecondition {

    private JcrFeed feed;
    
    /**
     * 
     */
    public JcrFeedPrecondition(Node node, JcrFeed feed) {
        super(node);
        this.feed = feed;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedPrecondition#getFeed()
     */
    @Override
    public Feed<?> getFeed() {
        return this.feed;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedPrecondition#getAgreement()
     */
    @Override
    public ServiceLevelAgreement getAgreement() {
        try {
            if (this.node.hasNode("tba:sla")) {
                return new JcrServiceLevelAgreement(this.node.getNode("tba:sla"));
            } else if (this.node.hasProperty("tba:slaRef")) {
                return new JcrServiceLevelAgreement(this.node.getProperty("tba:slaRef").getNode());
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the precondition SLA", e);
        }
    }

    @Override
    public ServiceLevelAssessment getLastAssessment() {
        try {
            if (this.node.hasNode("")) {
                // TODO: create assessment
//            return new JcrServiceLevelAssessment(node);
                return null;
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the last precondition assessment", e);
        }
    }

    @Override
    public void setLastAssessment(ServiceLevelAssessment assmnt) {
        // TODO create assessment
    }

}
