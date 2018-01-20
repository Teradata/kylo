package com.thinkbiganalytics.feedmgr.sla;

/*-
 * #%L
 * thinkbig-feed-manager-service-api
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
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementEmailTemplate;

import java.util.List;

/**
 * Created by sr186054 on 7/23/17.
 */
public interface ServiceLevelAgreementService {


    List<ServiceLevelAgreementRule> discoverSlaMetrics();

    List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> getServiceLevelAgreements();

    void enableServiceLevelAgreementSchedule(Feed.ID feedId);

    void unscheduleServiceLevelAgreement(Feed.ID feedId,String categoryAndFeedName);

    void disableServiceLevelAgreementSchedule(Feed.ID feedId);

    boolean hasServiceLevelAgreements(Feed.ID id );

    List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> getFeedServiceLevelAgreements(String feedId);

    boolean canEditServiceLevelAgreement(String slaId);

    boolean canAccessServiceLevelAgreement(String slaId);

    ServiceLevelAgreement getServiceLevelAgreement(String slaId);

    ServiceLevelAgreementGroup getServiceLevelAgreementAsFormObject(String slaId);

    /**
     * Remove and unschedule a given sla by its id
     * @param id
     * @return
     */
    boolean removeAndUnscheduleAgreement(String id);

    /**
     * Remove and Unschedule all SLA's for a feedId
     * @param feedId the feed id
     */
    void removeAndUnscheduleAgreementsForFeed(Feed.ID feedId, String categoryAndFeedName);

    boolean removeAllAgreements();

    List<ServiceLevelAgreementActionUiConfigurationItem> discoverActionConfigurations();

    List<ServiceLevelAgreementActionValidation> validateAction(String actionConfigurationClassName);

    ServiceLevelAgreement saveAndScheduleSla(ServiceLevelAgreementGroup serviceLevelAgreement);

    ServiceLevelAgreement saveAndScheduleFeedSla(ServiceLevelAgreementGroup serviceLevelAgreement, String feedId);

    List<SimpleServiceLevelAgreementDescription> getSlaReferencesForVelocityTemplate(String velocityTemplateId);

    ServiceLevelAgreementEmailTemplate saveEmailTemplate(ServiceLevelAgreementEmailTemplate emailTemplate);

    List<ServiceLevelAgreementEmailTemplate> getServiceLevelAgreementEmailTemplates();
}
