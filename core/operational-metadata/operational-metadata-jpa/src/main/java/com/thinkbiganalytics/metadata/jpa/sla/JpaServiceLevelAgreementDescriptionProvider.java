package com.thinkbiganalytics.metadata.jpa.sla;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementDescriptionProvider;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
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
    public void updateServiceLevelAgreement(ServiceLevelAgreement.ID slaId, String name, String description, Set<Feed.ID> feeds){
        JpaServiceLevelAgreementDescription.ServiceLevelAgreementId id = null;
        if(!(slaId instanceof  JpaServiceLevelAgreementDescription.ServiceLevelAgreementId)){
            id = new JpaServiceLevelAgreementDescription.ServiceLevelAgreementId(slaId.toString());
        }
        else {
            id = (JpaServiceLevelAgreementDescription.ServiceLevelAgreementId) slaId;
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

    }

    public ServiceLevelAgreement.ID resolveId(Serializable ser) {
        if (ser instanceof JpaServiceLevelAgreementDescription.ServiceLevelAgreementId) {
            return (JpaServiceLevelAgreementDescription.ServiceLevelAgreementId) ser;
        } else {
            return new JpaServiceLevelAgreementDescription.ServiceLevelAgreementId(ser);
        }
    }


}
