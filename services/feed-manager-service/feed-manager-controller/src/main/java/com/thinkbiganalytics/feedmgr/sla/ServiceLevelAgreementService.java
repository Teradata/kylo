package com.thinkbiganalytics.feedmgr.sla;

import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by sr186054 on 7/18/16.
 */
public class ServiceLevelAgreementService implements ServicesApplicationStartupListener{

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


    private List<ServiceLevelAgreementRule> serviceLevelAgreementRules;

    @Override
    public void onStartup(DateTime startTime) {
        discoverServiceLevelAgreementRules();
    }
    private  List<ServiceLevelAgreementRule>  discoverServiceLevelAgreementRules(){
        List<ServiceLevelAgreementRule> rules = ServiceLevelAgreementMetricTransformer.instance().discoverSlaMetrics();
        serviceLevelAgreementRules = rules;
        return serviceLevelAgreementRules;
    }

    public List<ServiceLevelAgreementRule> discoverSlaMetrics() {
        List<ServiceLevelAgreementRule> rules = serviceLevelAgreementRules;
        if(rules == null){
          rules = discoverServiceLevelAgreementRules();
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
                return ServiceLevelAgreementModelTransform.transformFeedServiceLevelAgreements(agreements);
            }
            return null;

        });
    }

    public void enableServiceLevelAgreementSchedule(Feed.ID feedId) {
        metadataAccess.commit(() -> {
            List<FeedServiceLevelAgreement> agreements = feedSlaProvider.findFeedServiceLevelAgreements(feedId);
            if (agreements != null) {
                for (com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement sla : agreements) {
                    serviceLevelAgreementScheduler.enableServiceLevelAgreement(sla);
                }
            }
        }, MetadataAccess.SERVICE);
    }

    public void unscheduleServiceLevelAgreement(Feed.ID feedId) {
        metadataAccess.read(() -> {
            List<FeedServiceLevelAgreement> agreements = feedSlaProvider.findFeedServiceLevelAgreements(feedId);
            if (agreements != null) {
                for (com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement sla : agreements) {
                    serviceLevelAgreementScheduler.unscheduleServiceLevelAgreement(sla.getId());
                }
            }
        });
    }



    public void disableServiceLevelAgreementSchedule(Feed.ID feedId) {
        metadataAccess.commit(() -> {
            List<FeedServiceLevelAgreement> agreements = feedSlaProvider.findFeedServiceLevelAgreements(feedId);
            if (agreements != null) {
                for (com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement sla : agreements) {
                    serviceLevelAgreementScheduler.disableServiceLevelAgreement(sla);
                }
            }
        }, MetadataAccess.SERVICE);
    }


    public List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> getFeedServiceLevelAgreements(String feedId) {
        return metadataAccess.read(() -> {

            Feed.ID id = feedProvider.resolveFeed(feedId);
            List<FeedServiceLevelAgreement> agreements = feedSlaProvider.findFeedServiceLevelAgreements(id);
            if (agreements != null) {
                return ServiceLevelAgreementModelTransform.transformFeedServiceLevelAgreements(agreements);
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
                com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement modelSla = ServiceLevelAgreementModelTransform.toModel(agreement, true);
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

    public boolean removeAndUnscheduleAgreement(String id) {
        return metadataAccess.commit(() -> {
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID slaId = slaProvider.resolve(id);
            slaProvider.removeAgreement(slaId);
            serviceLevelAgreementScheduler.unscheduleServiceLevelAgreement(slaId);
            return true;
        });

    }

    public boolean removeAllAgreements() {
        return metadataAccess.commit(() -> {
            List<com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement> agreements = slaProvider.getAgreements();
            if (agreements != null) {
                for (com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement agreement : agreements) {
                    slaProvider.removeAgreement(agreement.getId());
                }
            }
            return true;
        });

    }

    public List<ServiceLevelAgreementActionUiConfigurationItem> discoverActionConfigurations() {

      return ServiceLevelAgreementActionConfigTransformer.instance().discoverActionConfigurations();
    }


    public List<ServiceLevelAgreementActionValidation> validateAction(String actionConfigurationClassName ) {
        return ServiceLevelAgreementActionConfigTransformer.instance().validateAction(actionConfigurationClassName);
    }


    public ServiceLevelAgreement saveAndScheduleSla(ServiceLevelAgreementGroup serviceLevelAgreement) {
        return saveAndScheduleSla(serviceLevelAgreement, null);
    }


    private ServiceLevelAgreement saveAndScheduleSla(ServiceLevelAgreementGroup serviceLevelAgreement, FeedMetadata feed) {
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
                    com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement restModel = ServiceLevelAgreementModelTransform.toModel(savedSla, slaFeeds, true);
                    //schedule it
                    serviceLevelAgreementScheduler.scheduleServiceLevelAgreement(savedSla);
                    return restModel;

                }
                return null;


        });


    }

    public ServiceLevelAgreement saveAndScheduleFeedSla(ServiceLevelAgreementGroup serviceLevelAgreement, String feedId) {

        return metadataAccess.commit(() -> {
            FeedMetadata feed = null;
            if (StringUtils.isNotBlank(feedId)) {
                feed = feedManagerFeedService.getFeedById(feedId);
            }
            if (feed != null) {

                ServiceLevelAgreement sla = saveAndScheduleSla(serviceLevelAgreement, feed);

                return sla;
            } else {
                //TODO LOG ERROR CANNOT GET FEED
                throw new FeedNotFoundExcepton("Unable to create SLA for Feed " + feedId, feedProvider.resolveFeed(feedId));
            }
        });

    }



}
