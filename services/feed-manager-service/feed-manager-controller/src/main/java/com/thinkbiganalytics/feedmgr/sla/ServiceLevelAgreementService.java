package com.thinkbiganalytics.feedmgr.sla;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
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

    @Inject
    ServiceLevelAgreementProvider slaProvider;

    @Inject
    MetadataAccess metadataAccess;


    @Inject
    private FeedProvider feedProvider;

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

    public boolean removeAgreement(String id) {
        return metadataAccess.commit(new Command<Boolean>() {
            @Override
            public Boolean execute() {
                return slaProvider.removeAgreement(slaProvider.resolve(id));
            }
        });

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


    public ServiceLevelAgreement saveSla(ServiceLevelAgreementGroup serviceLevelAgreement) {
        return saveSla(serviceLevelAgreement, null);
    }


    private ServiceLevelAgreement saveSla(ServiceLevelAgreementGroup serviceLevelAgreement, FeedMetadata feed) {

        return metadataAccess.commit(new Command<ServiceLevelAgreement>() {
            @Override
            public ServiceLevelAgreement execute() {

                if (serviceLevelAgreement != null) {
                    ServiceLevelAgreementMetricTransformerHelper transformer = new ServiceLevelAgreementMetricTransformerHelper();
                    if (feed != null) {
                        transformer.applyFeedNameToCurrentFeedProperties(serviceLevelAgreement, feed.getCategory().getSystemName(), feed.getSystemFeedName());
                    }
                    ServiceLevelAgreement sla = transformer.getServiceLevelAgreement(serviceLevelAgreement);

                    ServiceLevelAgreementBuilder slaBuilder = null;
                    com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID existingId = null;
                    if (StringUtils.isNotBlank(sla.getId())) {
                        existingId = slaProvider.resolve(sla.getId());
                    }
                    if (existingId != null) {
                        slaBuilder = slaProvider.builder(existingId);
                    } else {
                        slaBuilder = slaProvider.builder();
                    }

                    slaBuilder.name(sla.getName()).description(sla.getDescription());
                    for (com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup group : sla.getGroups()) {
                        ObligationGroupBuilder groupBuilder = slaBuilder.obligationGroupBuilder(ObligationGroup.Condition.valueOf(group.getCondition()));
                        for (Obligation o : group.getObligations()) {
                            groupBuilder.obligationBuilder().metric(o.getMetrics()).description(o.getDescription()).build();
                        }
                        groupBuilder.build();
                    }
                    slaBuilder.actionConfigurations(sla.getActionConfigurations());
                    com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement savedSla = slaBuilder.build();
                    if (feed != null) {
                        feedProvider.updateFeedServiceLevelAgreement(feedProvider.resolveFeed(feed.getFeedId()), savedSla);
                    }
                    ServiceLevelAgreement restModel = Model.toModel(savedSla, true);

                    return restModel;

                }
                return null;

            }
        });


    }

    public ServiceLevelAgreement saveFeedSla(ServiceLevelAgreementGroup serviceLevelAgreement, String feedId) {
        return metadataAccess.commit(new Command<ServiceLevelAgreement>() {
            @Override
            public ServiceLevelAgreement execute() {
                FeedMetadata feed = null;
                if (StringUtils.isNotBlank(feedId)) {
                    feed = feedManagerFeedService.getFeedById(feedId);
                }
                if (feed != null) {

                    ServiceLevelAgreement sla = saveSla(serviceLevelAgreement, feed);

                    return sla;
                } else {
                    //TODO LOG ERROR CANNOT GET FEED
                    throw new FeedNotFoundExcepton("Unable to create SLA for Feed " + feedId, feedProvider.resolveFeed(feedId));
                }
            }

        });

    }

    public FeedServiceLevelAgreements getServiceLevelAgreements(String feedId) {
        return feedManagerFeedService.getFeedServiceLevelAgreements(feedId);
    }


}
