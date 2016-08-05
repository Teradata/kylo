package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.Set;

/**
 * Created by sr186054 on 8/5/16.
 */
public interface FeedServiceLevelAgreementRelationship extends ExtensibleEntity {

    ServiceLevelAgreement getAgreement();

    Set<? extends Feed> getFeeds();

    boolean removeFeedRelationships(ServiceLevelAgreement.ID id);
}
