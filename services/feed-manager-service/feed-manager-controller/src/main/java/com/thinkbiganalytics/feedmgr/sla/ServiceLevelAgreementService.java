package com.thinkbiganalytics.feedmgr.sla;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.GenericBaseUiPolicyRuleBuilder;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashSet;
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
    FeedServiceLevelAgreementProvider feedSlaProvider;

    @Inject
    JcrMetadataAccess metadataAccess;

    @Inject
    ServiceLevelAgreementScheduler serviceLevelAgreementScheduler;


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
            .applyFeedSelectOptions(
                ServiceLevelAgreementMetricTransformer.instance().findPropertiesForRulesetMatchingRenderTypes(rules, new String[]{PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name(),
                                                                                                                                  PolicyPropertyTypes.PROPERTY_TYPE.feedSelect.name(),
                                                                                                                                  PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name()}));

        return rules;
    }

    public List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> getServiceLevelAgreements() {
        return metadataAccess.read(() -> {

            List<FeedServiceLevelAgreement> agreements = feedSlaProvider.findAllAgreements();
            if (agreements != null) {
                return Model.transformFeedServiceLevelAgreements(agreements);
            }
            return null;

        });
    }


    public List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> getFeedServiceLevelAgreements(String feedId) {
        return metadataAccess.read(() -> {

            Feed.ID id = feedProvider.resolveFeed(feedId);
            List<FeedServiceLevelAgreement> agreements = feedSlaProvider.findFeedServiceLevelAgreements(id);
            if (agreements != null) {
                return Model.transformFeedServiceLevelAgreements(agreements);
            }
            return null;

        });
    }

    /**
     * get a SLA and convert it to the editable SLA form object
     */
    public ServiceLevelAgreementGroup getServiceLevelAgreementAsFormObject(String slaId) {
        return metadataAccess.read(() -> {

            FeedServiceLevelAgreement agreement = feedSlaProvider.findAgreement(slaProvider.resolve(slaId));
            if (agreement != null) {
                com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement modelSla = Model.toModel(agreement, true);
                ServiceLevelAgreementMetricTransformerHelper transformer = new ServiceLevelAgreementMetricTransformerHelper();
                ServiceLevelAgreementGroup serviceLevelAgreementGroup = transformer.toServiceLevelAgreementGroup(modelSla);
                feedManagerFeedService
                    .applyFeedSelectOptions(
                        ServiceLevelAgreementMetricTransformer.instance()
                            .findPropertiesForRulesetMatchingRenderTypes(serviceLevelAgreementGroup.getRules(), new String[]{PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name(),
                                                                                                                             PolicyPropertyTypes.PROPERTY_TYPE.feedSelect.name(),
                                                                                                                             PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name()}));
                return serviceLevelAgreementGroup;
            }
            return null;

        });
    }

    public boolean removeAgreement(String id) {
        return metadataAccess.commit(() -> slaProvider.removeAgreement(slaProvider.resolve(id)));

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
        return metadataAccess.commit(() -> {

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
                    com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement savedSla = slaBuilder.build();

                    List<ServiceLevelAgreementActionConfiguration> actions = transformer.getActionConfigurations(serviceLevelAgreement);

                    // now assign the sla checks
                    slaProvider.slaCheckBuilder(savedSla.getId()).removeSlaChecks().actionConfigurations(actions).build();

                    //all referencing Feeds
                    List<String> systemCategoryAndFeedNames = transformer.getCategoryFeedNames(serviceLevelAgreement);
                    Set<Feed> slaFeeds = new HashSet<Feed>();
                    Set<Feed.ID> slaFeedIds = new HashSet<Feed.ID>();
                    for (String categoryAndFeed : systemCategoryAndFeedNames) {
                        //fetch and update the reference to the sla
                        String categoryName = StringUtils.trim(StringUtils.substringBefore(categoryAndFeed, "."));
                        String feedName = StringUtils.trim(StringUtils.substringAfterLast(categoryAndFeed, "."));
                        Feed feedEntity = feedProvider.findBySystemName(categoryName, feedName);
                        if (feedEntity != null) {
                            slaFeeds.add(feedEntity);
                            slaFeedIds.add(feedEntity.getId());
                        }
                    }


                    if (feed != null) {
                        Feed.ID feedId = feedProvider.resolveFeed(feed.getFeedId());
                        if (!slaFeedIds.contains(feedId)) {
                            Feed feedEntity = feedProvider.getFeed(feedId);
                            slaFeeds.add(feedEntity);
                        }
                    }
                    //relate them
                    feedSlaProvider.relateFeeds(savedSla, slaFeeds);
                    com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement restModel = Model.toModel(savedSla, slaFeeds, true);



                    return restModel;

                }
                return null;


        });


    }

    public ServiceLevelAgreement saveFeedSla(ServiceLevelAgreementGroup serviceLevelAgreement, String feedId) {

        return metadataAccess.commit(() -> {
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
        });

    }



}
