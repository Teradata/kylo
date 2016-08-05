package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementRelationship;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.Set;

import javax.jcr.Node;

/**
 * Created by sr186054 on 8/5/16. a Join Object that Relates Feeds to SLAs
 *
 * @see JcrFeedServiceLevelAgreementProvider#relate(ServiceLevelAgreement, Set)
 */
public class JcrFeedServiceLevelAgreementRelationship extends JcrExtensibleEntity implements FeedServiceLevelAgreementRelationship {


    public static String TYPE_NAME = "feedSla";
    public static String NODE_TYPE = "tba:" + TYPE_NAME;
    public static String FEEDS = "feeds"; /// list of feed references on the SLA
    public static String SLA = "sla"; // a ref to the SLA


    public JcrFeedServiceLevelAgreementRelationship(Node node) {
        super(node);
    }

    public JcrFeedServiceLevelAgreementRelationship(JcrExtensibleEntity extensibleEntity) {
        super(extensibleEntity.getNode());
    }

    @Override
    public ServiceLevelAgreement getAgreement() {
        Node node = (Node) this.getProperty(SLA);
        return new JcrServiceLevelAgreement(node);
    }

    @Override
    public Set<? extends Feed> getFeeds() {
        return getPropertyAsSet(FEEDS, JcrFeed.class);
    }

    @Override
    public boolean removeFeedRelationships(ServiceLevelAgreement.ID id) {

        Set<JcrFeed> feeds = (Set<JcrFeed>) getFeeds();
        if (feeds != null && !feeds.isEmpty()) {
            for (JcrFeed feed : feeds) {
                feed.removeServiceLevelAgreement(id);
            }
        }
        setProperty(FEEDS, null);
        return true;


    }


}
