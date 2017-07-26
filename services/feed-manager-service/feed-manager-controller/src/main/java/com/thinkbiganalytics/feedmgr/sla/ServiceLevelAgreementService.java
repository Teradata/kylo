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

import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
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
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Service for interacting with SLA's
 */
public class ServiceLevelAgreementService implements ServicesApplicationStartupListener {

    private static final Logger log = LoggerFactory.getLogger(ServiceLevelAgreementService.class);

    @Inject
    ServiceLevelAgreementProvider slaProvider;
    @Inject
    FeedServiceLevelAgreementProvider feedSlaProvider;
    @Inject
    JcrMetadataAccess metadataAccess;
    @Inject
    ServiceLevelAgreementScheduler serviceLevelAgreementScheduler;
    @Inject
    private FeedManagerFeedService feedManagerFeedService;
    @Inject
    private FeedProvider feedProvider;
    @Inject
    private ServiceLevelAgreementModelTransform serviceLevelAgreementTransform;

    @Inject
    private AccessController accessController;


    private List<ServiceLevelAgreementRule> serviceLevelAgreementRules;

    @Override
    public void onStartup(DateTime startTime) {
        discoverServiceLevelAgreementRules();
    }

    private List<ServiceLevelAgreementRule> discoverServiceLevelAgreementRules() {
        List<ServiceLevelAgreementRule> rules = ServiceLevelAgreementMetricTransformer.instance().discoverSlaMetrics();
        serviceLevelAgreementRules = rules;
        return serviceLevelAgreementRules;
    }

    public List<ServiceLevelAgreementRule> discoverSlaMetrics() {
        List<ServiceLevelAgreementRule> rules = serviceLevelAgreementRules;
        if (rules == null) {
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

        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS);
        //find all as a Service account
        List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> agreementsList = this.metadataAccess.read(() -> {
            List<com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement> agreements = feedSlaProvider.findAllAgreements();
            if (agreements != null) {
                return serviceLevelAgreementTransform.transformFeedServiceLevelAgreements(agreements);
            }

            return new ArrayList<>(0);
        }, MetadataAccess.SERVICE);

        if (accessController.isEntityAccessControlled()) {

            Map<String, com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement>
                serviceLevelAgreementMap = agreementsList.stream().collect(Collectors.toMap(com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement::getId, Function.identity()));
            //filter out those feeds user doesnt have access to
            List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> entityAccessControlledSlas = this.metadataAccess.read(() -> {
                List<com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement> agreements = feedSlaProvider.findAllAgreements();
                if (agreements != null) {
                    List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement>
                        serviceLevelAgreements =
                        serviceLevelAgreementTransform.transformFeedServiceLevelAgreements(agreements);
                    return serviceLevelAgreements.stream().filter(agreement -> serviceLevelAgreementMap.get(agreement.getId()).getFeedsCount() == agreement.getFeedsCount())
                        .collect(Collectors.toList());
                }
                return new ArrayList<>(0);
            });

            return entityAccessControlledSlas;
        } else {
            return agreementsList;
        }


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

            boolean
                canAccess =
                accessController.isEntityAccessControlled() ? feedManagerFeedService.checkFeedPermission(id.toString(), FeedAccessControl.ACCESS_FEED)
                                                            : accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS);
            if (canAccess) {
                List<FeedServiceLevelAgreement> agreements = feedSlaProvider.findFeedServiceLevelAgreements(id);
                if (agreements != null) {
                    return serviceLevelAgreementTransform.transformFeedServiceLevelAgreements(agreements);
                }
            }
            return null;

        });
    }


    private com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement findFeedServiceLevelAgreementAsAdmin(String slaId, boolean deep) {
        com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement systemSla = metadataAccess.read(() -> {

            FeedServiceLevelAgreement agreement = feedSlaProvider.findAgreement(slaProvider.resolve(slaId));
            if (agreement != null) {
                return serviceLevelAgreementTransform.toModel(agreement, deep);
            }
            return null;
        }, MetadataAccess.SERVICE);
        return systemSla;
    }

    /**
     * Check to see if the user can edit
     *
     * @param slaId an sla to check
     * @return true if user can edit the SLA, false if not
     */
    public boolean canEditServiceLevelAgreement(String slaId) {
        com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement systemSla = findFeedServiceLevelAgreementAsAdmin(slaId, false);

        if (systemSla != null) {
            if (systemSla.getFeeds() != null && accessController.isEntityAccessControlled()) {
                return systemSla.getFeeds().stream().allMatch(feed -> feedManagerFeedService.checkFeedPermission(feed.getId(), FeedAccessControl.EDIT_DETAILS));
            } else {
                accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS);
                return true;
            }
        }
        return false;
    }

    /**
     * check to see if the current user can read/view the SLA
     *
     * @param slaId an sla to check
     * @return true if user can read the SLA, false if not
     */
    public boolean canAccessServiceLevelAgreement(String slaId) {
        com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement systemSla = findFeedServiceLevelAgreementAsAdmin(slaId, false);

        if (systemSla != null) {
            if (systemSla.getFeeds() != null && accessController.isEntityAccessControlled()) {
                return systemSla.getFeeds().stream().allMatch(feed -> feedManagerFeedService.checkFeedPermission(feed.getId(), FeedAccessControl.ACCESS_FEED));
            } else {
                accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS);
                return true;
            }
        }
        return false;

    }

    public ServiceLevelAgreement getServiceLevelAgreement(String slaId) {

        com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement systemSla = findFeedServiceLevelAgreementAsAdmin(slaId, false);

        //filter out if this SLA has feeds which the current user cannot access
        ServiceLevelAgreement serviceLevelAgreement = metadataAccess.read(() -> {

            FeedServiceLevelAgreement agreement = feedSlaProvider.findAgreement(slaProvider.resolve(slaId));
            if (agreement != null) {
                com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement entityAccessControlledSla = serviceLevelAgreementTransform.toModel(agreement, false);
                if (systemSla.getFeedsCount() == entityAccessControlledSla.getFeedsCount()) {
                    return entityAccessControlledSla;
                }
            }
            return null;
        });

        return serviceLevelAgreement;


    }


    /**
     * get a SLA and convert it to the editable SLA form object
     */
    public ServiceLevelAgreementGroup getServiceLevelAgreementAsFormObject(String slaId) {

        com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement systemSla = findFeedServiceLevelAgreementAsAdmin(slaId, true);

        if (systemSla != null) {

            return metadataAccess.read(() -> {
                //read it in as the current user
                FeedServiceLevelAgreement agreement = feedSlaProvider.findAgreement(slaProvider.resolve(slaId));
                //ensure the feed count match
                if (agreement.getFeeds().size() != systemSla.getFeeds().size()) {
                    throw new AccessControlException("Unable to access the SLA " + agreement.getName() + ".  You dont have proper access to one or more of the feeds associated with this SLA");
                }
                if (agreement != null) {
                    com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement modelSla = serviceLevelAgreementTransform.toModel(agreement, true);
                    ServiceLevelAgreementMetricTransformerHelper transformer = new ServiceLevelAgreementMetricTransformerHelper();
                    ServiceLevelAgreementGroup serviceLevelAgreementGroup = transformer.toServiceLevelAgreementGroup(modelSla);
                    feedManagerFeedService
                        .applyFeedSelectOptions(
                            ServiceLevelAgreementMetricTransformer.instance()
                                .findPropertiesForRulesetMatchingRenderTypes(serviceLevelAgreementGroup.getRules(), new String[]{PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name(),
                                                                                                                                 PolicyPropertyTypes.PROPERTY_TYPE.feedSelect.name(),
                                                                                                                                 PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name()}));
                    serviceLevelAgreementGroup.setCanEdit(modelSla.isCanEdit());
                    return serviceLevelAgreementGroup;
                }
                return null;

            });
        } else {
            return null;
        }
    }

    public boolean removeAndUnscheduleAgreement(String id) {
        boolean canEdit = canEditServiceLevelAgreement(id);

        if (canEdit) {
            return metadataAccess.commit(() -> {
                com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID slaId = slaProvider.resolve(id);
                //attempt to find it
                slaProvider.removeAgreement(slaId);
                serviceLevelAgreementScheduler.unscheduleServiceLevelAgreement(slaId);
                return true;
            });
        } else {
            return false;
        }

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


    public List<ServiceLevelAgreementActionValidation> validateAction(String actionConfigurationClassName) {
        return ServiceLevelAgreementActionConfigTransformer.instance().validateAction(actionConfigurationClassName);
    }


    public ServiceLevelAgreement saveAndScheduleSla(ServiceLevelAgreementGroup serviceLevelAgreement) {
        return saveAndScheduleSla(serviceLevelAgreement, null);
    }


    /**
     * In order to Save an SLA if it is related to a Feed(s) the user needs to have EDIT_DETAILS permission on the Feed(s)
     *
     * @param serviceLevelAgreement the sla to save
     * @param feed                  an option Feed to relate to this SLA.  If this is not present the related feeds are also embedded in the SLA policies.  The Feed is a pointer access to the current
     *                              feed the user is editing if they are creating an SLA from the Feed Details page. If creating an SLA from the main SLA page the feed property will not be populated.
     */
    private ServiceLevelAgreement saveAndScheduleSla(ServiceLevelAgreementGroup serviceLevelAgreement, FeedMetadata feed) {

        //ensure user has permissions to edit the SLA
        if (serviceLevelAgreement != null) {

            ServiceLevelAgreementMetricTransformerHelper transformer = new ServiceLevelAgreementMetricTransformerHelper();

            //Read the feeds on the SLA as a Service. Then verify the current user has access to edit these feeds
            List<String> feedsOnSla = metadataAccess.read(() -> {
                List<String> feedIds = new ArrayList<>();
                //all referencing Feeds
                List<String> systemCategoryAndFeedNames = transformer.getCategoryFeedNames(serviceLevelAgreement);

                for (String categoryAndFeed : systemCategoryAndFeedNames) {
                    //fetch and update the reference to the sla
                    String categoryName = StringUtils.trim(StringUtils.substringBefore(categoryAndFeed, "."));
                    String feedName = StringUtils.trim(StringUtils.substringAfterLast(categoryAndFeed, "."));
                    Feed feedEntity = feedProvider.findBySystemName(categoryName, feedName);
                    if (feedEntity != null) {
                        feedIds.add(feedEntity.getId().toString());
                    }
                }
                return feedIds;
            }, MetadataAccess.SERVICE);

            boolean allowedToEdit = feedsOnSla.isEmpty() ? true : feedsOnSla.stream().allMatch(feedId -> feedManagerFeedService.checkFeedPermission(feedId, FeedAccessControl.EDIT_DETAILS));

            if (allowedToEdit) {

                return metadataAccess.commit(() -> {

                    //Re read back in the Feeds for this session
                    Set<Feed> slaFeeds = new HashSet<Feed>();
                    Set<Feed.ID> slaFeedIds = new HashSet<Feed.ID>();
                    feedsOnSla.stream().forEach(feedId -> {
                        Feed feedEntity = feedProvider.findById(feedProvider.resolveId(feedId));
                        if (feedEntity != null) {
                            slaFeeds.add(feedEntity);
                            slaFeedIds.add(feedEntity.getId());
                        }
                    });

                    if (feed != null) {
                        feedManagerFeedService.checkFeedPermission(feed.getId(), FeedAccessControl.EDIT_DETAILS);
                    }

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

                    if (feed != null) {
                        Feed.ID feedId = feedProvider.resolveFeed(feed.getFeedId());
                        if (!slaFeedIds.contains(feedId)) {
                            Feed feedEntity = feedProvider.getFeed(feedId);
                            slaFeeds.add(feedEntity);
                        }
                    }
                    //relate them
                    feedSlaProvider.relateFeeds(savedSla, slaFeeds);
                    com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement restModel = serviceLevelAgreementTransform.toModel(savedSla, slaFeeds, true);
                    //schedule it
                    serviceLevelAgreementScheduler.scheduleServiceLevelAgreement(savedSla);
                    return restModel;

                });
            }
        }
        return null;
    }

    public ServiceLevelAgreement saveAndScheduleFeedSla(ServiceLevelAgreementGroup serviceLevelAgreement, String feedId) {

        FeedMetadata feed = null;
        if (StringUtils.isNotBlank(feedId)) {
            feed = feedManagerFeedService.getFeedById(feedId);

        }
        if (feed != null && feedManagerFeedService.checkFeedPermission(feed.getId(), FeedAccessControl.EDIT_DETAILS)) {
            ServiceLevelAgreement sla = saveAndScheduleSla(serviceLevelAgreement, feed);
            return sla;
        } else {
            log.error("Error attempting to save and Schedule the Feed SLA {} ({}) ", feed != null ? feed.getCategoryAndFeedName() : " NULL Feed ", feedId);
            throw new FeedNotFoundExcepton("Unable to create SLA for Feed " + feedId, feedProvider.resolveFeed(feedId));
        }


    }


}
