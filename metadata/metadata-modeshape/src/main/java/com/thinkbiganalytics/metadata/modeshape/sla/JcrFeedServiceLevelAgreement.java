package com.thinkbiganalytics.metadata.modeshape.sla;

import java.util.Set;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;

/**
 * Resulting Query object that gets all SLAs and their related Feeds
 *
 * @see JcrFeedServiceLevelAgreementProvider
 */
public class JcrFeedServiceLevelAgreement extends JcrServiceLevelAgreement implements FeedServiceLevelAgreement {

    private Set<JcrFeed> feeds;


    public JcrFeedServiceLevelAgreement(Node node, Set<JcrFeed> feeds) {
        super(node);
        this.feeds = feeds;
    }

    @Override
    public Set<? extends Feed> getFeeds() {
        return feeds;
    }
}
