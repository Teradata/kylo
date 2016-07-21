package com.thinkbiganalytics.feedmgr.sla;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 7/19/16.
 */
public class ServiceLevelAgreementMetricTransformerHelper {


    public ServiceLevelAgreementMetricTransformerHelper() {
    }

    public void applyFeedNameToCurrentFeedProperties(FeedServiceLevelAgreements serviceLevelAgreements, String category, String feed) {
        if (serviceLevelAgreements != null) {
            List<FieldRuleProperty>
                properties =
                ServiceLevelAgreementMetricTransformer.instance().findPropertiesForRulesetMatchingRenderType(serviceLevelAgreements.getAllRules(), PolicyProperty.PROPERTY_TYPE.currentFeed.name());
            if (properties != null && !properties.isEmpty()) {
                for (FieldRuleProperty property : properties) {
                    property.setValue(category + "." + feed);
                }
            }
        }
    }

    /**
     * Transform UI model to Java Objects
     */
    public List<ServiceLevelAgreement> getServiceLevelAgreements(FeedServiceLevelAgreements serviceLevelAgreements) {
        List<ServiceLevelAgreement> slaList = new ArrayList<>();

        if (serviceLevelAgreements != null) {
            for (ServiceLevelAgreementGroup sla : serviceLevelAgreements.getServiceLevelAgreements()) {
                ServiceLevelAgreement transformedSla = new ServiceLevelAgreement();
                transformedSla.setName(sla.getName());
                transformedSla.setDescription(sla.getDescription());
                slaList.add(transformedSla);
                for (ServiceLevelAgreementRule rule : sla.getRules()) {
                    try {
                        ObligationGroup group = new ObligationGroup();
                        Metric policy = ServiceLevelAgreementMetricTransformer.instance().fromUiModel(rule);
                        Obligation obligation = new Obligation(policy.getDescription());
                        obligation.setMetrics(Lists.newArrayList(policy));
                        group.addObligation(obligation);
                        group.setCondition(rule.getCondition().name());
                        transformedSla.addGroup(group);
                    } catch (PolicyTransformException e) {
                        e.printStackTrace();
                    }
                }

                //transform the responders
                if (sla.getActionConfigurations() != null) {
                    List<ServiceLevelAgreementActionConfiguration> actionConfigurations = new ArrayList<>();
                    for (ServiceLevelAgreementActionUiConfigurationItem agreementActionUiConfigurationItem : sla.getActionConfigurations()) {
                        try {
                            ServiceLevelAgreementActionConfiguration actionConfiguration = ServiceLevelAgreementActionConfigTransformer.instance().fromUiModel(agreementActionUiConfigurationItem);
                            actionConfigurations.add(actionConfiguration);
                        } catch (PolicyTransformException e) {
                            e.printStackTrace();
                        }
                    }
                    transformedSla.setActionConfigurations(actionConfigurations);
                }

            }
        }
        return slaList;
    }

    public FeedServiceLevelAgreements toFeedServiceLevelAgreements(String feedId, List<ServiceLevelAgreement> slas) {
        FeedServiceLevelAgreements feedServiceLevelAgreements = new FeedServiceLevelAgreements();
        for (ServiceLevelAgreement sla : slas) {
            ServiceLevelAgreementGroup group = toServiceLevelAgreementGroup(sla);
            feedServiceLevelAgreements.addServiceLevelAgreement(group);
        }
        feedServiceLevelAgreements.setFeedId(feedId);
        return feedServiceLevelAgreements;

    }

    /**
     * Transform the Rest Model back to the form model
     */
    public ServiceLevelAgreementGroup toServiceLevelAgreementGroup(ServiceLevelAgreement sla) {
        ServiceLevelAgreementGroup slaGroup = new ServiceLevelAgreementGroup();
        slaGroup.setName(sla.getName());
        slaGroup.setDescription(sla.getDescription());
        for (ObligationGroup group : sla.getGroups()) {
            List<Obligation> obligations = group.getObligations();
            for (Obligation obligation : obligations) {
                List<Metric> metrics = obligation.getMetrics();
                for (Metric metric : metrics) {
                    ServiceLevelAgreementRule rule = ServiceLevelAgreementMetricTransformer.instance().toUIModel(metric);
                    slaGroup.addServiceLevelAgreementRule(rule);
                }
            }
        }

        //transform the responders if they exist
        if (sla.getActionConfigurations() != null) {
            List<ServiceLevelAgreementActionUiConfigurationItem> agreementActionUiConfigurationItems = new ArrayList<>();
            for (ServiceLevelAgreementActionConfiguration responderConfig : sla.getActionConfigurations()) {
                ServiceLevelAgreementActionUiConfigurationItem uiModel = ServiceLevelAgreementActionConfigTransformer.instance().toUIModel(responderConfig);
                agreementActionUiConfigurationItems.add(uiModel);
            }
            slaGroup.setActionConfigurations(agreementActionUiConfigurationItems);
        }
        return slaGroup;
    }

}
