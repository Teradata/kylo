package com.thinkbiganalytics.metadata.jpa.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementActionTemplateProvider;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementDescriptionProvider;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Service
public class JpaServiceLevelAgreementDescriptionProvider implements ServiceLevelAgreementDescriptionProvider {


    private JpaServiceLevelAgreementDescriptionRepository serviceLevelAgreementDescriptionRepository;

    @Inject
    private ServiceLevelAgreementProvider slaProvider;

    @Inject
    private OpsManagerFeedProvider feedProvider;

    @Inject
    private ServiceLevelAgreementActionTemplateProvider serviceLevelAgreementActionTemplateProvider;




    @Autowired
    public JpaServiceLevelAgreementDescriptionProvider(JpaServiceLevelAssessmentRepository serviceLevelAssessmentRepository, JpaServiceLevelAgreementDescriptionRepository serviceLevelAgreementDescriptionRepository) {
         this.serviceLevelAgreementDescriptionRepository = serviceLevelAgreementDescriptionRepository;
    }


    /**
     * Updates the Service Level Agreement (SLA) JPA mapping and its relationship to Feeds
     * @param slaId the SLA id
     * @param name the SLA Name
     * @param description the SLA Description
     * @param feeds a set of Feed Ids related to this SLA
     */
    @Override
    public void updateServiceLevelAgreement(ServiceLevelAgreement.ID slaId, String name, String description, Set<Feed.ID> feeds, Set<VelocityTemplate.ID> velocityTemplates){
        ServiceLevelAgreementDescriptionId id = null;
        if(!(slaId instanceof  ServiceLevelAgreementDescriptionId)){
            id = new ServiceLevelAgreementDescriptionId(slaId.toString());
        }
        else {
            id = (ServiceLevelAgreementDescriptionId) slaId;
        }
        JpaServiceLevelAgreementDescription serviceLevelAgreementDescription = serviceLevelAgreementDescriptionRepository.findOne(id);
        if(serviceLevelAgreementDescription == null){
            serviceLevelAgreementDescription = new JpaServiceLevelAgreementDescription();
            serviceLevelAgreementDescription.setSlaId(id);
        }
        serviceLevelAgreementDescription.setName(name);
        serviceLevelAgreementDescription.setDescription(description);
        List<OpsManagerFeed> jpaFeeds = null;
        if(feeds != null){
            List<OpsManagerFeed.ID> feedIds =feeds.stream().map(f -> feedProvider.resolveId(f.toString())).collect(Collectors.toList());
            jpaFeeds = (List<OpsManagerFeed>) feedProvider.findByFeedIds(feedIds);
        }
        if(jpaFeeds != null) {
            serviceLevelAgreementDescription.setFeeds(new HashSet<>(jpaFeeds));
        }
        else {
            serviceLevelAgreementDescription.setFeeds(null);
        }

        serviceLevelAgreementDescriptionRepository.save(serviceLevelAgreementDescription);
        //save the velocity template relationships
        serviceLevelAgreementActionTemplateProvider.assignTemplateByIds(serviceLevelAgreementDescription,velocityTemplates);



    }

    public ServiceLevelAgreement.ID resolveId(Serializable ser) {
        if (ser instanceof ServiceLevelAgreementDescriptionId) {
            return (ServiceLevelAgreementDescriptionId) ser;
        } else {
            return new ServiceLevelAgreementDescriptionId(ser);
        }
    }

    public List< ? extends  ServiceLevelAgreementDescription> findForFeed(OpsManagerFeed.ID feedId){
        return serviceLevelAgreementDescriptionRepository.findForFeed(feedId);
    }


}
