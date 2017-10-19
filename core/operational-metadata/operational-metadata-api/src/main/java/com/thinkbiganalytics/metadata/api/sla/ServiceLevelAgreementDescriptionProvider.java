package com.thinkbiganalytics.metadata.api.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;

import java.io.Serializable;
import java.util.List;
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
    void updateServiceLevelAgreement(ServiceLevelAgreement.ID slaId, String name, String description, Set<Feed.ID> feeds, Set<VelocityTemplate.ID> velocityTemplates);

    ServiceLevelAgreement.ID resolveId(Serializable ser);

    List< ? extends ServiceLevelAgreementDescription> findForFeed(OpsManagerFeed.ID feedId);

    List<ServiceLevelAgreementDescription> findAll();

    ServiceLevelAgreementDescription findOne(ServiceLevelAgreement.ID id);
}
