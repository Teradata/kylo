package com.thinkbiganalytics.feedmgr.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 7/19/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedServiceLevelAgreements {

    private String feedId;


    private List<ServiceLevelAgreementGroup> serviceLevelAgreements;

    public List<ServiceLevelAgreementGroup> getServiceLevelAgreements() {
        if (serviceLevelAgreements == null) {
            serviceLevelAgreements = new ArrayList<>();
        }
        return serviceLevelAgreements;
    }

    public void setServiceLevelAgreements(List<ServiceLevelAgreementGroup> serviceLevelAgreements) {
        this.serviceLevelAgreements = serviceLevelAgreements;
    }


    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public List<ServiceLevelAgreementRule> getAllRules() {
        List<ServiceLevelAgreementRule> list = new ArrayList<ServiceLevelAgreementRule>();

        for (ServiceLevelAgreementGroup group : getServiceLevelAgreements()) {
            if (group != null && group.getRules() != null && !group.getRules().isEmpty()) {
                list.addAll(group.getRules());
            }
        }
        return list;
    }

    public void addServiceLevelAgreement(ServiceLevelAgreementGroup slaGroup) {
        getServiceLevelAgreements().add(slaGroup);
    }
}
