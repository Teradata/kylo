package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by sr186054 on 7/24/17.
 */
public interface ServiceLevelAgreementDescriptionProvider {

    /**
     * Updates the Service Level Agreement (SLA) JPA mapping and its relationship to Feeds
     * @param slaId the SLA id
     * @param name the SLA Name
     * @param description the SLA Description
     * @param feeds a set of Feed Ids related to this SLA
     */
    void updateServiceLevelAgreement(ServiceLevelAgreement.ID slaId, String name, String description, Set<Feed.ID> feeds);

    ServiceLevelAgreement.ID resolveId(Serializable ser);
}
