package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 8/5/16.
 */
public interface FeedServiceLevelAgreementProvider {

    /**
     * Find all SLAs and if they have any Feed Relationships also add those into the resulting list
     */
    List<FeedServiceLevelAgreement> findAllAgreements();

    /**
     * Find the SLA and get the list of Feeds it is related to
     */
    FeedServiceLevelAgreement findAgreement(ServiceLevelAgreement.ID slaId);

    /**
     * Find all agreements associated to a given Feed
     */
    List<FeedServiceLevelAgreement> findFeedServiceLevelAgreements(Feed.ID feedId);

    /**
     * relate an SLA to a set of Feeds
     */
    FeedServiceLevelAgreementRelationship relate(ServiceLevelAgreement sla, Set<Feed.ID> feedIds);

    /**
     * relate an SLA to a set of Feeds
     */
    FeedServiceLevelAgreementRelationship relateFeeds(ServiceLevelAgreement sla, Set<Feed> feeds);

    /**
     * Cleanup and remove Feed Relationships on an SLA
     */
    boolean removeFeedRelationships(ServiceLevelAgreement.ID id);
}
