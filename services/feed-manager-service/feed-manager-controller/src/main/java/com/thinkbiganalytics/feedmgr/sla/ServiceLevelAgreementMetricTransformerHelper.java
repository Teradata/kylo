package com.thinkbiganalytics.feedmgr.sla;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms SLA UI objects (to / from the Rest/Model)
 */
public class ServiceLevelAgreementMetricTransformerHelper {


    public ServiceLevelAgreementMetricTransformerHelper() {
    }


    /**
     * gets all SystemCategory.SystemFeedName values
     */
    public List<String> getCategoryFeedNames(ServiceLevelAgreementGroup serviceLevelAgreement) {
        List<String> names = new ArrayList<>();

        if (serviceLevelAgreement != null) {
            List<FieldRuleProperty>
                properties =
                ServiceLevelAgreementMetricTransformer.instance().findPropertiesForRulesetMatchingRenderTypes(serviceLevelAgreement.getRules(),
                                                                                                              new String[]{PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name(),
                                                                                                                           PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name(),
                                                                                                                           PolicyPropertyTypes.PROPERTY_TYPE.feedSelect.name()});
            if (properties != null && !properties.isEmpty()) {
                //get the Value or List of values.
                for (FieldRuleProperty property : properties) {
                    String value = property.getValue();
                    if (StringUtils.isNotBlank(value)) {
                        names.add(value);
                    } else if (property.getValues() != null && !property.getValues().isEmpty()) {
                        names.addAll(property.getValues().stream().map(LabelValue::getValue).collect(Collectors.toList()));
                    }
                }
            }
        }
        return names;

    }

    public void applyFeedNameToCurrentFeedProperties(ServiceLevelAgreementGroup serviceLevelAgreement, String categoryAndFeedName) {
        if (serviceLevelAgreement != null) {
            List<FieldRuleProperty>
                properties = getPropertiesMatchingFeedType(serviceLevelAgreement);
            applyFeedNameToCurrentFeedProperties(properties, categoryAndFeedName);
        }

    }

    private void applyFeedNameToCurrentFeedProperties(List<FieldRuleProperty>
                                                          properties, String categoryAndFeedName) {
        if (!properties.isEmpty()) {
            for (FieldRuleProperty property : properties) {
                if (StringUtils.isBlank(property.getValue())) {
                    property.setValue(categoryAndFeedName);
                }
            }
        }
    }

    private List<FieldRuleProperty> getPropertiesMatchingFeedType(ServiceLevelAgreementGroup serviceLevelAgreement) {
        List<FieldRuleProperty>
            properties = new ArrayList<>();
        if (serviceLevelAgreement != null) {

            List<FieldRuleProperty>
                currentFeedProperties =
                ServiceLevelAgreementMetricTransformer.instance().findPropertiesForRulesetMatchingRenderType(serviceLevelAgreement.getRules(), PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name());

            if (currentFeedProperties != null && !currentFeedProperties.isEmpty()) {
                properties.addAll(currentFeedProperties);
            }

            List<FieldRuleProperty>
                defaultValueProperties = ServiceLevelAgreementMetricTransformer.instance().findPropertiesForRulesMatchingDefaultValue(serviceLevelAgreement.getRules(),
                                                                                                                                      PolicyPropertyTypes.CURRENT_FEED_VALUE);

            if (defaultValueProperties != null && !defaultValueProperties.isEmpty()) {
                properties.addAll(defaultValueProperties);
            }

        }
        return properties;
    }

    public void applyFeedNameToCurrentFeedProperties(ServiceLevelAgreementGroup serviceLevelAgreement, String category, String feed) {
        if (serviceLevelAgreement != null) {
            List<FieldRuleProperty>
                properties = getPropertiesMatchingFeedType(serviceLevelAgreement);

            applyFeedNameToCurrentFeedProperties(properties, FeedNameUtil.fullName(category, feed));
        }
    }


    public ServiceLevelAgreement getServiceLevelAgreement(ServiceLevelAgreementGroup serviceLevelAgreement) {
        ServiceLevelAgreement transformedSla = new ServiceLevelAgreement();
        transformedSla.setId(serviceLevelAgreement.getId());
        transformedSla.setName(serviceLevelAgreement.getName());
        transformedSla.setDescription(serviceLevelAgreement.getDescription());
        for (ServiceLevelAgreementRule rule : serviceLevelAgreement.getRules()) {
            try {
                ObligationGroup group = new ObligationGroup();
                Metric policy = ServiceLevelAgreementMetricTransformer.instance().fromUiModel(rule);
                Obligation obligation = new Obligation(policy.getDescription());
                obligation.setMetrics(Lists.newArrayList(policy));
                group.addObligation(obligation);
                group.setCondition(rule.getCondition().name());
                transformedSla.addGroup(group);
            } catch (PolicyTransformException e) {
                throw new RuntimeException(e);
            }
        }

        return transformedSla;
    }


    public List<ServiceLevelAgreementActionConfiguration> getActionConfigurations(ServiceLevelAgreementGroup serviceLevelAgreement) {
        List<ServiceLevelAgreementActionConfiguration> actionConfigurations = new ArrayList<>();
        if (serviceLevelAgreement.getActionConfigurations() != null) {
            for (ServiceLevelAgreementActionUiConfigurationItem agreementActionUiConfigurationItem : serviceLevelAgreement.getActionConfigurations()) {
                try {
                    ServiceLevelAgreementActionConfiguration actionConfiguration = ServiceLevelAgreementActionConfigTransformer.instance().fromUiModel(agreementActionUiConfigurationItem);
                    actionConfigurations.add(actionConfiguration);
                } catch (PolicyTransformException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return actionConfigurations;
    }


    /**
     * Transform the Rest Model back to the form model
     */
    public ServiceLevelAgreementGroup toServiceLevelAgreementGroup(ServiceLevelAgreement sla) {
        ServiceLevelAgreementGroup slaGroup = new ServiceLevelAgreementGroup();
        slaGroup.setId(sla.getId());
        slaGroup.setName(sla.getName());
        slaGroup.setDescription(sla.getDescription());
        slaGroup.setActionErrors(sla.getSlaCheckErrors());
        slaGroup.setRuleErrors(sla.getObligationErrors());
        for (ObligationGroup group : sla.getGroups()) {
            List<Obligation> obligations = group.getObligations();
            for (Obligation obligation : obligations) {
                List<Metric> metrics = obligation.getMetrics();
                for (Metric metric : metrics) {
                    ServiceLevelAgreementRule rule = ServiceLevelAgreementMetricTransformer.instance().toUIModel(metric);
                    rule.setCondition(com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition.valueOf(group.getCondition()));
                    slaGroup.addServiceLevelAgreementRule(rule);

                }
            }
        }

        //transform the Actions if they exist.
        if (sla.getSlaChecks() != null) {
            List<ServiceLevelAgreementActionUiConfigurationItem> agreementActionUiConfigurationItems = new ArrayList<>();
            for (ServiceLevelAgreementCheck check : sla.getSlaChecks()) {
                for (ServiceLevelAgreementActionConfiguration actionConfig : check.getActionConfigurations()) {
                    if (actionConfig != null) {
                        ServiceLevelAgreementActionUiConfigurationItem uiModel = ServiceLevelAgreementActionConfigTransformer.instance().toUIModel(actionConfig);
                        agreementActionUiConfigurationItems.add(uiModel);
                    }
                }
            }
            slaGroup.setActionConfigurations(agreementActionUiConfigurationItems);
        }
        slaGroup.setCanEdit(sla.isCanEdit());
        return slaGroup;
    }

}
