package com.thinkbiganalytics.feedmgr.service.feed;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;
import com.thinkbiganalytics.feedmgr.nifi.CreateFeedBuilder;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.feed.datasource.DerivedDatasourceFactory;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChange;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.event.feed.FeedPropertyChangeEvent;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProperties;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.nifi.feedmgr.FeedRollbackException;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.policy.precondition.DependentFeedPrecondition;
import com.thinkbiganalytics.policy.precondition.Precondition;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionPolicyTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

public class DefaultFeedManagerFeedService implements FeedManagerFeedService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFeedManagerFeedService.class);
    /**
     * Event listener for precondition events
     */
    private final MetadataEventListener<FeedPropertyChangeEvent> feedPropertyChangeListener = new FeedPropertyChangeDispatcher();

    @Inject
    FeedManagerTemplateProvider templateProvider;
    @Inject
    FeedManagerTemplateService templateRestProvider;
    @Inject
    FeedManagerPreconditionService feedPreconditionModelTransform;
    @Inject
    FeedModelTransform feedModelTransform;
    @Inject
    ServiceLevelAgreementProvider slaProvider;
    @Inject
    ServiceLevelAgreementService serviceLevelAgreementService;
    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;
    @Inject
    private DatasourceProvider datasourceProvider;
    /**
     * Metadata event service
     */
    @Inject
    private MetadataEventService eventService;
    @Inject
    private AccessController accessController;
    @Inject
    private MetadataEventService metadataEventService;
    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;
    @Inject
    private DerivedDatasourceFactory derivedDatasourceFactory;
    // use autowired instead of Inject to allow null values.
    @Autowired(required = false)
    @Qualifier("hadoopAuthorizationService")
    private HadoopAuthorizationService hadoopAuthorizationService;

    @Inject
    private SecurityService securityService;

    @Inject
    protected CategoryProvider categoryProvider;

    @Inject
    protected FeedProvider feedProvider;

    @Inject
    protected MetadataAccess metadataAccess;

    @Inject
    private FeedManagerTemplateService feedManagerTemplateService;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    PropertyExpressionResolver propertyExpressionResolver;
    @Inject
    NifiFlowCache nifiFlowCache;
    @Inject
    private LegacyNifiRestClient nifiRestClient;



    @Value("${nifi.remove.inactive.versioned.feeds:true}")
    private boolean removeInactiveNifiVersionedFeedFlows;

    /**
     * Adds listeners for transferring events.
     */
    @PostConstruct
    public void addEventListener() {
        metadataEventService.addListener(feedPropertyChangeListener);
    }

    /**
     * Removes listeners and stops transferring events.
     */
    @PreDestroy
    public void removeEventListener() {
        metadataEventService.removeListener(feedPropertyChangeListener);
    }

    @Override
    public boolean checkFeedPermission(String id, Action action, Action... more) {
        if (accessController.isEntityAccessControlled()) {
            return metadataAccess.read(() -> {
                Feed.ID domainId = feedProvider.resolveId(id);
                Feed domainFeed = feedProvider.findById(domainId);

                if (domainFeed != null) {
                    domainFeed.getAllowedActions().checkPermission(action, more);
                    return true;
                } else {
                    return false;
                }
            });
        } else {
            return true;
        }
    }

    @Override
    public FeedMetadata getFeedByName(final String categoryName, final String feedName) {
        FeedMetadata feedMetadata = metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            Feed domainFeed = feedProvider.findBySystemName(categoryName, feedName);
            if (domainFeed != null) {
                return feedModelTransform.domainToFeedMetadata(domainFeed);
            }
            return null;
        });
        return feedMetadata;
    }

    @Override
    public FeedMetadata getFeedById(final String id) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            return getFeedById(id, false);
        });

    }

    @Override
    public FeedMetadata getFeedById(final String id, final boolean refreshTargetTableSchema) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            FeedMetadata feedMetadata = null;
            Feed.ID domainId = feedProvider.resolveId(id);
            Feed domainFeed = feedProvider.findById(domainId);
            if (domainFeed != null) {
                feedMetadata = feedModelTransform.domainToFeedMetadata(domainFeed);
            }
            if (refreshTargetTableSchema && feedMetadata != null) {
                //commented out for now as some issues were found with feeds with TEXTFILE as their output
                //this will attempt to sync the schema stored in modeshape with that in Hive
                // feedModelTransform.refreshTableSchemaFromHive(feedMetadata);
            }
            return feedMetadata;
        });

    }

    @Override
    public Collection<FeedMetadata> getFeeds() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            Collection<FeedMetadata> feeds = null;
            List<Feed> domainFeeds = feedProvider.findAll();
            if (domainFeeds != null) {
                feeds = feedModelTransform.domainToFeedMetadata(domainFeeds);
            }
            return feeds;
        });

    }

    @Override
    public Collection<? extends UIFeed> getFeeds(boolean verbose) {
        if (verbose) {
            return getFeeds();
        } else {
            return getFeedSummaryData();
        }

    }

    @Override
    public List<FeedSummary> getFeedSummaryData() {

        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            List<FeedSummary> feeds = null;
            Collection<? extends Feed> domainFeeds = feedProvider.findAll();
            if (domainFeeds != null) {
                feeds = feedModelTransform.domainToFeedSummary(domainFeeds);
            }
            return feeds;
        });

    }

    @Override
    public List<FeedSummary> getFeedSummaryForCategory(final String categoryId) {
        return metadataAccess.read(() -> {
            List<FeedSummary> summaryList = new ArrayList<>();
            boolean hasPermission = this.accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);
            if(hasPermission) {

                Category.ID categoryDomainId = categoryProvider.resolveId(categoryId);
                List<? extends Feed> domainFeeds = feedProvider.findByCategoryId(categoryDomainId);
                if (domainFeeds != null && !domainFeeds.isEmpty()) {
                    List<FeedMetadata> feeds = feedModelTransform.domainToFeedMetadata(domainFeeds);
                    for (FeedMetadata feed : feeds) {
                        summaryList.add(new FeedSummary(feed));
                    }
                }
            }
            return summaryList;
        });

    }

    @Override
    public List<FeedMetadata> getFeedsWithTemplate(final String registeredTemplateId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            List<FeedMetadata> feedMetadatas = null;
            FeedManagerTemplate.ID templateDomainId = templateProvider.resolveId(registeredTemplateId);
            List<? extends Feed> domainFeeds = feedProvider.findByTemplateId(templateDomainId);
            if (domainFeeds != null) {
                feedMetadatas = feedModelTransform.domainToFeedMetadata(domainFeeds);
            }
            return feedMetadatas;
        });
    }


    @Override
    public Feed.ID resolveFeed(@Nonnull Serializable fid) {
        return metadataAccess.read(() -> feedProvider.resolveFeed(fid));
    }


    /**
     * Create/Update a Feed in NiFi
     * Save the metadata to Kylo meta store
     *
     * @param feedMetadata the feed metadata
     * @return an object indicating if the feed creation was successful or not
     */
    public NifiFeed createFeed(final FeedMetadata feedMetadata) {

        //functional access to be able to create a feed
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

        if (feedMetadata.getState() == null) {
            if (feedMetadata.isActive()) {
                feedMetadata.setState(Feed.State.ENABLED.name());
            } else {
                feedMetadata.setState(Feed.State.DISABLED.name());
            }
        }
        NifiFeed feed = createAndSaveFeed(feedMetadata);
        //register the audit for the update event
        if (feed.isSuccess() && !feedMetadata.isNew()) {
            Feed.State state = Feed.State.valueOf(feedMetadata.getState());
            Feed.ID id = feedProvider.resolveId(feedMetadata.getId());
            notifyFeedStateChange(feedMetadata, id, state, MetadataChange.ChangeType.UPDATE);
        } else if (feed.isSuccess() && feedMetadata.isNew()) {
            //update the access control
            feedMetadata.toRoleMembershipChangeList().stream().forEach(roleMembershipChange -> securityService.changeFeedRoleMemberships(feed.getFeedMetadata().getId(), roleMembershipChange));
        }
        return feed;

    }


    /**
     * Create/Update a Feed in NiFi
     * Save the metadata to Kylo meta store
     *
     * @param feedMetadata the feed metadata
     * @return an object indicating if the feed creation was successful or not
     */
    private NifiFeed createAndSaveFeed(FeedMetadata feedMetadata) {

        NifiFeed feed = null;
        if (StringUtils.isBlank(feedMetadata.getId())) {
            feedMetadata.setIsNew(true);

            //If the feed is New we need to ensure the user has CREATE_FEED entity permission under the feeds category
            if (accessController.isEntityAccessControlled()) {
                //ensure the user has rights to create feeds under this category
                metadataAccess.read(() -> {
                    Category domainCategory = categoryProvider.findById(categoryProvider.resolveId(feedMetadata.getCategory().getId()));
                    if (domainCategory == null) {
                        //throw exception
                        throw new MetadataRepositoryException("Unable to find the category " + feedMetadata.getCategory().getSystemName());
                    }
                    //Query for Category and ensure the user has access to create feeds on that category
                    domainCategory.getAllowedActions().checkPermission(CategoryAccessControl.CREATE_FEED);

                });
            }

        } else if (accessController.isEntityAccessControlled()) {
            metadataAccess.read(() -> {
                //perform explict entity access check here as we dont want to modify the NiFi flow unless user has access to edit the feed
                Feed.ID domainId = feedProvider.resolveId(feedMetadata.getId());
                Feed domainFeed = feedProvider.findById(domainId);
                if (domainFeed != null) {
                    domainFeed.getAllowedActions().checkPermission(FeedAccessControl.EDIT_DETAILS);
                } else {
                    throw new NotFoundException("Feed not found for id " + feedMetadata.getId());
                }
            });
        }



        //replace expressions with values
        if (feedMetadata.getTable() != null) {
            feedMetadata.getTable().updateMetadataFieldValues();
        }

        if (feedMetadata.getProperties() == null) {
            feedMetadata.setProperties(new ArrayList<NifiProperty>());
        }

        //store ref to the originalFeedProperties before resolving and merging with the template
        List<NifiProperty> orignialFeedProperties = feedMetadata.getProperties();

        //get all the properties for the metadata
        RegisteredTemplate
            registeredTemplate =
            registeredTemplateService.findRegisteredTemplate(
                new RegisteredTemplateRequest.Builder().templateId(feedMetadata.getTemplateId()).templateName(feedMetadata.getTemplateName()).isFeedEdit(true).includeSensitiveProperties(true)
                    .build());

        //update the template properties with the feedMetadata properties
         List<NifiProperty> matchedProperties =
                NifiPropertyUtil
                    .matchAndSetPropertyByProcessorName(registeredTemplate.getProperties(), feedMetadata.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);

        feedMetadata.setProperties(registeredTemplate.getProperties());
        feedMetadata.setRegisteredTemplate(registeredTemplate);


        //resolve any ${metadata.} properties
        List<NifiProperty> resolvedProperties = propertyExpressionResolver.resolvePropertyExpressions(feedMetadata);

        /*
        //store all input related properties as well
        List<NifiProperty> inputProperties = NifiPropertyUtil
            .findInputProperties(registeredTemplate.getProperties());

        ///store only those matched and resolved in the final metadata store
        Set<NifiProperty> updatedProperties = new HashSet<>();
        //first get all those selected properties where the value differs from the template value

        List<NifiProperty> modifiedProperties = registeredTemplate.findModifiedDefaultProperties();
        if (modifiedProperties != null) {
             propertyExpressionResolver.resolvePropertyExpressions(modifiedProperties,feedMetadata);
            updatedProperties.addAll(modifiedProperties);
        }
        updatedProperties.addAll(matchedProperties);
        updatedProperties.addAll(resolvedProperties);
        updatedProperties.addAll(inputProperties);
        feedMetadata.setProperties(new ArrayList<NifiProperty>(updatedProperties));

        */

        //decrypt the metadata
        feedModelTransform.decryptSensitivePropertyValues(feedMetadata);

        FeedMetadata.STATE state = FeedMetadata.STATE.NEW;
        try {
            state = FeedMetadata.STATE.valueOf(feedMetadata.getState());
        } catch (Exception e) {
            //if the string isnt valid, disregard as it will end up disabling the feed.
        }

        boolean enabled = (FeedMetadata.STATE.NEW.equals(state) && feedMetadata.isActive()) || FeedMetadata.STATE.ENABLED.equals(state);

        // flag to indicate to enable the feed later
        //if this is the first time for this feed and it is set to be enabled, mark it to be enabled after we commit to the JCR store
        boolean enableLater = false;
        if (enabled && feedMetadata.isNew()) {
            enableLater = true;
            enabled = false;
            feedMetadata.setState(FeedMetadata.STATE.DISABLED.name());
        }

        CreateFeedBuilder
            feedBuilder =
            CreateFeedBuilder.newFeed(nifiRestClient, nifiFlowCache, feedMetadata, registeredTemplate.getNifiTemplateId(), propertyExpressionResolver, propertyDescriptorTransform).enabled(enabled)
                .removeInactiveVersionedProcessGroup(removeInactiveNifiVersionedFeedFlows);

        if (registeredTemplate.isReusableTemplate()) {
            feedBuilder.setReusableTemplate(true);
            feedMetadata.setIsReusableFeed(true);
        } else {
            feedBuilder.inputProcessorType(feedMetadata.getInputProcessorType())
                .feedSchedule(feedMetadata.getSchedule()).properties(feedMetadata.getProperties());
            if (registeredTemplate.usesReusableTemplate()) {
                for (ReusableTemplateConnectionInfo connection : registeredTemplate.getReusableTemplateConnections()) {
                    feedBuilder.addInputOutputPort(new InputOutputPort(connection.getReusableTemplateInputPortName(), connection.getFeedOutputPortName()));
                }
            }
        }
        NifiProcessGroup
            entity = feedBuilder.build();

        feed = new NifiFeed(feedMetadata, entity);
        //set the original feedProperties back to the feed
        feedMetadata.setProperties(orignialFeedProperties);
        //encrypt the metadata properties
        feedModelTransform.encryptSensitivePropertyValues(feedMetadata);
        if (entity.isSuccess()) {
            feedMetadata.setNifiProcessGroupId(entity.getProcessGroupEntity().getId());

            try {

                saveFeed(feedMetadata);
                feed.setEnableAfterSave(enableLater);
                feed.setSuccess(true);
                feedBuilder.checkAndRemoveVersionedProcessGroup();

            } catch (Exception e) {
                feed.setSuccess(false);
                feed.addErrorMessage(e);
            }

        } else {
            feed.setSuccess(false);
        }
        if (!feed.isSuccess()) {
            if (!entity.isRolledBack()) {
                try {
                    feedBuilder.rollback();
                } catch (FeedRollbackException rollbackException) {
                    log.error("Error rolling back feed {}. {} ", feedMetadata.getCategoryAndFeedName(), rollbackException.getMessage());
                    feed.addErrorMessage("Error occurred in rolling back the Feed.");
                }
                entity.setRolledBack(true);
            }
        }
        return feed;
    }


    private void saveFeed(final FeedMetadata feed) {
        if (StringUtils.isBlank(feed.getId())) {
            feed.setIsNew(true);
        }
        metadataAccess.commit(() -> {
            List<? extends HadoopSecurityGroup> previousSavedSecurityGroups = null;
            // Store the old security groups before saving beccause we need to compare afterward
            if (feed.isNew()) {
                Feed existing = feedProvider.findBySystemName(feed.getCategory().getSystemName(), feed.getSystemFeedName());
                // Since we know this is expected to be new check if the category/feed name combo is already being used.
                if (existing != null) {
                    throw new DuplicateFeedNameException(feed.getCategoryName(), feed.getFeedName());
                }
            } else {
                Feed previousStateBeforeSaving = feedProvider.findById(feedProvider.resolveId(feed.getId()));
                Map<String, String> userProperties = previousStateBeforeSaving.getUserProperties();
                previousSavedSecurityGroups = previousStateBeforeSaving.getSecurityGroups();
            }

            //if this is the first time saving this feed create a new one
            Feed domainFeed = feedModelTransform.feedToDomain(feed);

            if (domainFeed.getState() == null) {
                domainFeed.setState(Feed.State.ENABLED);
            }

            //initially save the feed
            if (feed.isNew()) {
                domainFeed = feedProvider.update(domainFeed);
            }

            final String domainId = domainFeed.getId().toString();
            final String feedName = FeedNameUtil.fullName(domainFeed.getCategory().getName(), domainFeed.getName());

            // Build preconditions
            assignFeedDependencies(feed, domainFeed);

            //Assign the datasources
            assignFeedDatasources(feed, domainFeed);

            //sync the feed information to ops manager
            metadataAccess.commit(() -> opsManagerFeedProvider.save(opsManagerFeedProvider.resolveId(domainId), feedName));

            // Update hadoop security group polices if the groups changed
            if (!feed.isNew() && !ListUtils.isEqualList(previousSavedSecurityGroups, domainFeed.getSecurityGroups())) {
                List<? extends HadoopSecurityGroup> securityGroups = domainFeed.getSecurityGroups();
                List<String> groupsAsCommaList = securityGroups.stream().map(group -> group.getName()).collect(Collectors.toList());
                hadoopAuthorizationService.updateSecurityGroupsForAllPolicies(feed.getSystemCategoryName(), feed.getSystemFeedName(), groupsAsCommaList, domainFeed.getProperties());
            }
            domainFeed = feedProvider.update(domainFeed);

            // Return result
            return feed;
        }, (e) -> {
            if (feed.isNew() && StringUtils.isNotBlank(feed.getId())) {
                //Rollback ops Manager insert if it is newly created
                metadataAccess.commit(() -> {
                    opsManagerFeedProvider.delete(opsManagerFeedProvider.resolveId(feed.getId()));
                    return null;
                });
            }
        });


    }

    /**
     * Looks for the Feed Preconditions and assigns the Feed Dependencies
     */
    private void assignFeedDependencies(FeedMetadata feed, Feed domainFeed) {
        final Feed.ID domainFeedId = domainFeed.getId();
        List<PreconditionRule> preconditions = feed.getSchedule().getPreconditions();
        if (preconditions != null) {
            PreconditionPolicyTransformer transformer = new PreconditionPolicyTransformer(preconditions);
            transformer.applyFeedNameToCurrentFeedProperties(feed.getCategory().getSystemName(), feed.getSystemFeedName());
            List<com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup> transformedPreconditions = transformer.getPreconditionObligationGroups();
            ServiceLevelAgreementBuilder
                preconditionBuilder =
                feedProvider.buildPrecondition(domainFeed.getId()).name("Precondition for feed " + feed.getCategoryAndFeedName() + "  (" + domainFeed.getId() + ")");
            for (com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup precondition : transformedPreconditions) {
                for (Obligation group : precondition.getObligations()) {
                    preconditionBuilder.obligationGroupBuilder(ObligationGroup.Condition.valueOf(precondition.getCondition())).obligationBuilder().metric(group.getMetrics()).build();
                }
            }
            preconditionBuilder.build();

            //add in the lineage dependency relationships
            //will the feed exist in the jcr store here if it is new??

            //store the existing list of dependent feeds to track and delete those that dont match
            Set<Feed.ID> oldDependentFeedIds = new HashSet<Feed.ID>();
            Set<Feed.ID> newDependentFeedIds = new HashSet<Feed.ID>();

            List<Feed> dependentFeeds = domainFeed.getDependentFeeds();
            if (dependentFeeds != null && !dependentFeeds.isEmpty()) {
                dependentFeeds.stream().forEach(dependentFeed -> {
                    oldDependentFeedIds.add(dependentFeed.getId());
                });
            }
            //find those preconditions that are marked as dependent feed types
            List<Precondition> preconditionPolicies = transformer.getPreconditionPolicies();
            preconditionPolicies.stream().filter(precondition -> precondition instanceof DependentFeedPrecondition).forEach(dependentFeedPrecondition -> {
                DependentFeedPrecondition feedPrecondition = (DependentFeedPrecondition) dependentFeedPrecondition;
                List<String> dependentFeedNames = feedPrecondition.getDependentFeedNames();
                if (dependentFeedNames != null && !dependentFeedNames.isEmpty()) {
                    //find the feed
                    for (String dependentFeedName : dependentFeedNames) {
                        Feed dependentFeed = feedProvider.findBySystemName(dependentFeedName);
                        if (dependentFeed != null) {
                            Feed.ID newDependentFeedId = dependentFeed.getId();
                            newDependentFeedIds.add(newDependentFeedId);
                            //add and persist it if it doesnt already exist
                            if (!oldDependentFeedIds.contains(newDependentFeedId)) {
                                feedProvider.addDependent(domainFeedId, dependentFeed.getId());
                            }
                        }
                    }
                }

            });
            //delete any of those dependent feed ids from the oldDependentFeeds that are not part of the newDependentFeedIds
            oldDependentFeedIds.stream().filter(oldFeedId -> !newDependentFeedIds.contains(oldFeedId))
                .forEach(dependentFeedToDelete -> feedProvider.removeDependent(domainFeedId, dependentFeedToDelete));

        }
    }

    private void assignFeedDatasources(FeedMetadata feed, Feed domainFeed) {
        final Feed.ID domainFeedId = domainFeed.getId();
        Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> sources = new HashSet<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID>();
        Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> destinations = new HashSet<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID>();

        String uniqueName = FeedNameUtil.fullName(feed.getCategory().getSystemName(), feed.getSystemFeedName());

        RegisteredTemplate template = feed.getRegisteredTemplate();
        if (template == null) {
            //fetch it for checks
            template = templateRestProvider.getRegisteredTemplate(feed.getTemplateId());

        }
        //find Definition registration

        derivedDatasourceFactory.populateDatasources(feed, template, sources, destinations);
        //remove the older sources only if they have changed

        if (domainFeed.getSources() != null) {
            Set<Datasource.ID>
                existingSourceIds =
                ((List<FeedSource>) domainFeed.getSources()).stream().filter(source -> source.getDatasource() != null).map(source1 -> source1.getDatasource().getId()).collect(Collectors.toSet());
            if (!sources.containsAll(existingSourceIds) || (sources.size() != existingSourceIds.size())) {
                //remove older sources
                //cant do it here for some reason.. need to do it in a separate transaction
                feedProvider.removeFeedSources(domainFeedId);
            }
        }
        sources.stream().forEach(sourceId -> feedProvider.ensureFeedSource(domainFeedId, sourceId));
        destinations.stream().forEach(sourceId -> feedProvider.ensureFeedDestination(domainFeedId, sourceId));
        //TODO deal with inputs changing sources?

    }

    @Override
    public void deleteFeed(@Nonnull final String feedId) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

            Feed feed = feedProvider.getFeed(feedProvider.resolveFeed(feedId));
            //unschedule any SLAs
            serviceLevelAgreementService.unscheduleServiceLevelAgreement(feed.getId());
            feedProvider.deleteFeed(feed.getId());
            opsManagerFeedProvider.delete(opsManagerFeedProvider.resolveId(feedId));
            return true;
        });
    }

    @Override
    public void enableFeedCleanup(@Nonnull String feedId) {
        metadataAccess.commit(() -> {
            final Feed.ID id = feedProvider.resolveFeed(feedId);
            return feedProvider.mergeFeedProperties(id, ImmutableMap.of(FeedProperties.CLEANUP_ENABLED, "true"));
        });
    }

    private boolean enableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(() -> {
            boolean enabled = feedProvider.enableFeed(feedId);
            Feed domainFeed = feedProvider.findById(feedId);
            FeedMetadata feedMetadata = null;
            if (domainFeed != null) {
                feedMetadata = feedModelTransform.deserializeFeedMetadata(domainFeed, true);
                feedMetadata.setState(FeedMetadata.STATE.ENABLED.name());
                domainFeed.setJson(ObjectMapperSerializer.serialize(feedMetadata));
                feedProvider.update(domainFeed);
            }
            if (enabled) {
                notifyFeedStateChange(feedMetadata, feedId, Feed.State.ENABLED, MetadataChange.ChangeType.UPDATE);
            }

            return enabled;
        });

    }


    // @Transactional(transactionManager = "metadataTransactionManager")
    private boolean disableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(() -> {

            boolean disabled = feedProvider.disableFeed(feedId);
            Feed domainFeed = feedProvider.findById(feedId);
            FeedMetadata feedMetadata = null;
            if (domainFeed != null) {
                feedMetadata = feedModelTransform.deserializeFeedMetadata(domainFeed, false);
                feedMetadata.setState(FeedMetadata.STATE.DISABLED.name());
                domainFeed.setJson(ObjectMapperSerializer.serialize(feedMetadata));
                feedProvider.update(domainFeed);
            }
            if (disabled) {
                notifyFeedStateChange(feedMetadata, feedId, Feed.State.DISABLED, MetadataChange.ChangeType.UPDATE);
            }

            return disabled;
        });

    }

    public FeedSummary enableFeed(final String feedId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            if (StringUtils.isNotBlank(feedId)) {
                FeedMetadata feedMetadata = getFeedById(feedId);
                Feed.ID domainId = feedProvider.resolveFeed(feedId);
                boolean enabled = enableFeed(domainId);
                //re fetch it
                if (enabled) {
                    feedMetadata.setState(Feed.State.ENABLED.name());
                    serviceLevelAgreementService.enableServiceLevelAgreementSchedule(domainId);

                }
                FeedSummary feedSummary = new FeedSummary(feedMetadata);
                //start any Slas

                return feedSummary;
            }
            return null;
        });


    }

    public FeedSummary disableFeed(final String feedId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            if (StringUtils.isNotBlank(feedId)) {
                FeedMetadata feedMetadata = getFeedById(feedId);
                Feed.ID domainId = feedProvider.resolveFeed(feedId);
                boolean disabled = disableFeed(domainId);
                //re fetch it
                if (disabled) {
                    feedMetadata.setState(Feed.State.DISABLED.name());
                    serviceLevelAgreementService.disableServiceLevelAgreementSchedule(domainId);
                }
                FeedSummary feedSummary = new FeedSummary(feedMetadata);
                return feedSummary;
            }
            return null;
        });

    }

    @Override
    /**
     * Applies new LableValue array to the FieldProperty.selectableValues {label = Category.Display Feed Name, value=category.system_feed_name}
     */
    public void applyFeedSelectOptions(List<FieldRuleProperty> properties) {
        if (properties != null && !properties.isEmpty()) {
            List<FeedSummary> feedSummaries = getFeedSummaryData();
            List<LabelValue> feedSelection = new ArrayList<>();
            for (FeedSummary feedSummary : feedSummaries) {
                boolean isDisabled = feedSummary.getState() == Feed.State.DISABLED.name();
                boolean canEditDetails = feedSummary.hasAction(FeedAccessControl.EDIT_DETAILS.getSystemName());
                Map<String, Object> labelValueProperties = new HashMap<>();
                labelValueProperties.put("feed:disabled", isDisabled);
                labelValueProperties.put("feed:editDetails", canEditDetails);
                feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName() + (isDisabled ? " (DISABLED) " : ""), feedSummary.getCategoryAndFeedSystemName(),
                                                 isDisabled ? "This feed is currently disabled" : "", labelValueProperties));
            }
            for (FieldRuleProperty property : properties) {
                property.setSelectableValues(feedSelection);
                if (property.getValues() == null) {
                    property.setValues(new ArrayList<>()); // reset the intial values to be an empty arraylist
                }
            }
        }
    }

    @Nonnull
    @Override
    public Set<UserField> getUserFields() {
        return metadataAccess.read(() -> {
            boolean hasPermission = this.accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);
            return  hasPermission ? UserPropertyTransform.toUserFields(feedProvider.getUserFields()) : Collections.emptySet();
        });
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserField> userFields) {
        boolean hasPermission = this.accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);
        if(hasPermission) {
            feedProvider.setUserFields(UserPropertyTransform.toUserFieldDescriptors(userFields));
        }
    }

    @Nonnull
    @Override
    public Optional<Set<UserProperty>> getUserFields(@Nonnull final String categoryId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            final Optional<Set<UserFieldDescriptor>> categoryUserFields = categoryProvider.getFeedUserFields(categoryProvider.resolveId(categoryId));
            final Set<UserFieldDescriptor> globalUserFields = feedProvider.getUserFields();
            if (categoryUserFields.isPresent()) {
                return Optional.of(UserPropertyTransform.toUserProperties(Collections.emptyMap(), Sets.union(globalUserFields, categoryUserFields.get())));
            } else {
                return Optional.empty();
            }
        });
    }

    private class FeedPropertyChangeDispatcher implements MetadataEventListener<FeedPropertyChangeEvent> {


        @Override
        public void notify(@Nonnull final FeedPropertyChangeEvent metadataEvent) {
            Properties oldProperties = metadataEvent.getData().getNifiPropertiesToDelete();
            metadataAccess.commit(() -> {
                Feed feed = feedProvider.getFeed(feedProvider.resolveFeed(metadataEvent.getData().getFeedId()));
                oldProperties.forEach((k, v) -> {
                    feed.removeProperty((String) k);
                });
            }, MetadataAccess.SERVICE);
        }

    }


    /**
     * update the audit information for feed state changes
     *
     * @param feedId     the feed id
     * @param state      the new state
     * @param changeType the event type
     */
    private void notifyFeedStateChange(FeedMetadata feedMetadata, Feed.ID feedId, Feed.State state, MetadataChange.ChangeType changeType) {
        final Principal principal = SecurityContextHolder.getContext().getAuthentication() != null
                                    ? SecurityContextHolder.getContext().getAuthentication()
                                    : null;
        FeedChange change = new FeedChange(changeType, feedMetadata != null ? feedMetadata.getCategoryAndFeedName() : "", feedId, state);
        FeedChangeEvent event = new FeedChangeEvent(change, DateTime.now(), principal);
        metadataEventService.notify(event);
    }
}
