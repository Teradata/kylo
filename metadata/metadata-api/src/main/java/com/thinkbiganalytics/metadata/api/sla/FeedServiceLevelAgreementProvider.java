package com.thinkbiganalytics.metadata.api.sla;

/*-
 * #%L
 * thinkbig-metadata-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.List;
import java.util.Set;

/**
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

    boolean removeAllRelationships(ServiceLevelAgreement.ID id);
}
