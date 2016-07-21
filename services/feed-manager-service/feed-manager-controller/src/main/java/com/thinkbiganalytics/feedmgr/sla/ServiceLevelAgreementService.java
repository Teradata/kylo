package com.thinkbiganalytics.feedmgr.sla;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.GenericBaseUiPolicyRuleBuilder;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by sr186054 on 7/18/16.
 */
public class ServiceLevelAgreementService {

    @Inject
    private FeedManagerFeedService feedManagerFeedService;

    public List<ServiceLevelAgreementRule> discoverSlaMetrics() {

        List<ServiceLevelAgreementRule> rules = new ArrayList<>();
        Set<Class<?>>
            validators = new Reflections("com.thinkbiganalytics").getTypesAnnotatedWith(ServiceLevelAgreementMetric.class);
        for (Class c : validators) {
            ServiceLevelAgreementMetric policy = (ServiceLevelAgreementMetric) c.getAnnotation(ServiceLevelAgreementMetric.class);
            String desc = policy.description();
            String shortDesc = policy.shortDescription();
            if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(shortDesc)) {
                desc = shortDesc;
            }
            if (StringUtils.isBlank(shortDesc) && StringUtils.isNotBlank(desc)) {
                shortDesc = desc;
            }
            List<FieldRuleProperty> properties = ServiceLevelAgreementMetricTransformer.instance().getUiProperties(c);
            rules.add(
                (ServiceLevelAgreementRule) new GenericBaseUiPolicyRuleBuilder<ServiceLevelAgreementRule>(ServiceLevelAgreementRule.class, policy.name()).description(desc).shortDescription(shortDesc)
                    .addProperties(properties).objectClassType(c).build());
        }

        feedManagerFeedService
            .applyFeedSelectOptions(ServiceLevelAgreementMetricTransformer.instance().findPropertiesForRulesetMatchingRenderTypes(rules, new String[]{PolicyProperty.PROPERTY_TYPE.feedChips.name(),
                                                                                                                                                      PolicyProperty.PROPERTY_TYPE.feedSelect.name()}));

        return rules;
    }

    public List<ServiceLevelAgreementActionUiConfigurationItem> discoverActionConfigurations() {

        List<ServiceLevelAgreementActionUiConfigurationItem> rules = new ArrayList<>();
        Set<Class<?>>
            items = new Reflections("com.thinkbiganalytics").getTypesAnnotatedWith(ServiceLevelAgreementActionConfig.class);
        for (Class c : items) {
            ServiceLevelAgreementActionConfig policy = (ServiceLevelAgreementActionConfig) c.getAnnotation(ServiceLevelAgreementActionConfig.class);
            String desc = policy.description();
            String shortDesc = policy.shortDescription();
            if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(shortDesc)) {
                desc = shortDesc;
            }
            if (StringUtils.isBlank(shortDesc) && StringUtils.isNotBlank(desc)) {
                shortDesc = desc;
            }
            List<FieldRuleProperty> properties = ServiceLevelAgreementMetricTransformer.instance().getUiProperties(c);
            ServiceLevelAgreementActionUiConfigurationItem
                configItem =
                (ServiceLevelAgreementActionUiConfigurationItem) new GenericBaseUiPolicyRuleBuilder<ServiceLevelAgreementActionUiConfigurationItem>(
                    ServiceLevelAgreementActionUiConfigurationItem.class, policy.name()).description(desc).shortDescription(shortDesc)
                    .addProperties(properties).objectClassType(c).build();
            configItem.setActionClasses(Lists.newArrayList(policy.actionClasses()));
            rules.add(configItem);
        }

        return rules;
    }

    public List<ServiceLevelAgreement> saveFeedSla(FeedServiceLevelAgreements serviceLevelAgreements) {
        return feedManagerFeedService.saveFeedSla(serviceLevelAgreements);
    }

    public FeedServiceLevelAgreements getServiceLevelAgreements(String feedId) {
        return feedManagerFeedService.getFeedServiceLevelAgreements(feedId);
    }


}
