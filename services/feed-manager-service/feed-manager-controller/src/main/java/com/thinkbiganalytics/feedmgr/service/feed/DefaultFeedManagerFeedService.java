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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;
import com.thinkbiganalytics.feedmgr.nifi.CreateFeedBuilder;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.model.EntityVersion;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.FeedVersions;
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
import com.thinkbiganalytics.feedmgr.service.template.NiFiTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChange;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.event.feed.FeedPropertyChangeEvent;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
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
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
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
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

public class DefaultFeedManagerFeedService implements FeedManagerFeedService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFeedManagerFeedService.class);

    private static final Pageable PAGE_ALL = new PageRequest(0, Integer.MAX_VALUE);

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
    private NiFiTemplateCache niFiTemplateCache;

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    @Inject
    private FeedHiveTableService feedHiveTableService;

    @Inject
    private TemplateConnectionUtil templateConnectionUtil;


    @Value("${nifi.remove.inactive.versioned.feeds:true}")
    private boolean removeInactiveNifiVersionedFeedFlows;

    @Value("${nifi.auto.align:true}")
    private boolean nifiAutoFeedsAlignAfterSave;

    @Inject
    private NiFiObjectCache niFiObjectCache;

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
    public FeedVersions getFeedVersions(String feedId, boolean includeContent) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            Feed.ID domainId = feedProvider.resolveId(feedId);

            return feedProvider.findVersions(domainId, includeContent)
                .map(list -> feedModelTransform.domainToFeedVersions(list, domainId))
                .orElse((FeedVersions) null);
        });
    }

    @Override
    public Optional<EntityVersion> getFeedVersion(String feedId, String versionId, boolean includeContent) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            Feed.ID domainFeedId = feedProvider.resolveId(feedId);
            com.thinkbiganalytics.metadata.api.versioning.EntityVersion.ID domainVersionId = feedProvider.resolveVersion(versionId);

            return feedProvider.findVersion(domainFeedId, domainVersionId, includeContent)
                .map(version -> feedModelTransform.domainToFeedVersion(version));
        });
    }

    @Override
    public Collection<FeedMetadata> getFeeds() {
        return getFeeds(PAGE_ALL, null).getContent();
    }

    public Page<FeedMetadata> getFeeds(Pageable pageable, String filter) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            Page<Feed> domainFeeds = feedProvider.findPage(pageable, filter);
            return domainFeeds.map(d -> feedModelTransform.domainToFeedMetadata(d));
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
    public Page<UIFeed> getFeeds(boolean verbose, Pageable pageable, String filter) {
        if (verbose) {
            return getFeeds(pageable, filter).map(UIFeed.class::cast);
        } else {
            return getFeedSummaryData(pageable, filter).map(UIFeed.class::cast);
        }

    }

    @Override
    public List<FeedSummary> getFeedSummaryData() {
        return getFeedSummaryData(PAGE_ALL, null).getContent().stream()
            .map(FeedSummary.class::cast)
            .collect(Collectors.toList());
    }

    public Page<FeedSummary> getFeedSummaryData(Pageable pageable, String filter) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            Page<Feed> domainFeeds = feedProvider.findPage(pageable, filter);
            return domainFeeds.map(d -> feedModelTransform.domainToFeedSummary(d));
        });
    }

    @Override
    public List<FeedSummary> getFeedSummaryForCategory(final String categoryId) {
        return metadataAccess.read(() -> {
            List<FeedSummary> summaryList = new ArrayList<>();
            boolean hasPermission = this.accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);
            if (hasPermission) {

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
     * Create/Update a Feed in NiFi. Save the metadata to Kylo meta store.
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

        if (StringUtils.isBlank(feedMetadata.getId())) {
            feedMetadata.setIsNew(true);
        }

        //Read all the feeds as System Service account to ensure the feed name is unique
        if (feedMetadata.isNew()) {
            metadataAccess.read(() -> {
                Feed existing = feedProvider.findBySystemName(feedMetadata.getCategory().getSystemName(), feedMetadata.getSystemFeedName());
                if (existing != null) {
                    throw new DuplicateFeedNameException(feedMetadata.getCategoryName(), feedMetadata.getFeedName());
                }
            }, MetadataAccess.SERVICE);
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
     * Create/Update a Feed in NiFi. Save the metadata to Kylo meta store.
     *
     * @param feedMetadata the feed metadata
     * @return an object indicating if the feed creation was successful or not
     */
    private NifiFeed createAndSaveFeed(FeedMetadata feedMetadata) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        NifiFeed feed = null;
        if (StringUtils.isBlank(feedMetadata.getId())) {
            feedMetadata.setIsNew(true);

            //If the feed is New we need to ensure the user has CREATE_FEED entity permission
            if (accessController.isEntityAccessControlled()) {
                metadataAccess.read(() -> {
                    //ensure the user has rights to create feeds under the category
                    Category domainCategory = categoryProvider.findById(categoryProvider.resolveId(feedMetadata.getCategory().getId()));
                    if (domainCategory == null) {
                        //throw exception
                        throw new MetadataRepositoryException("Unable to find the category " + feedMetadata.getCategory().getSystemName());
                    }
                    domainCategory.getAllowedActions().checkPermission(CategoryAccessControl.CREATE_FEED);

                    //ensure the user has rights to create feeds using the template
                    FeedManagerTemplate domainTemplate = templateProvider.findById(templateProvider.resolveId(feedMetadata.getTemplateId()));
                    if (domainTemplate == null) {
                        throw new MetadataRepositoryException("Unable to find the template " + feedMetadata.getTemplateId());
                    }
                    //  domainTemplate.getAllowedActions().checkPermission(TemplateAccessControl.CREATE_FEED);
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
        List<NifiProperty> originalFeedProperties = feedMetadata.getProperties();

        //get all the properties for the metadata
        RegisteredTemplate
            registeredTemplate =
            registeredTemplateService.findRegisteredTemplate(
                new RegisteredTemplateRequest.Builder().templateId(feedMetadata.getTemplateId()).templateName(feedMetadata.getTemplateName()).isFeedEdit(true).includeSensitiveProperties(true)
                    .build());

        //copy the registered template properties it a new list so it doest get updated
        List<NifiProperty> templateProperties =registeredTemplate.getProperties().stream().map(nifiProperty -> new NifiProperty(nifiProperty)).collect(Collectors.toList());
        //update the template properties with the feedMetadata properties
        List<NifiProperty> matchedProperties =
            NifiPropertyUtil
                .matchAndSetPropertyByProcessorName(templateProperties, feedMetadata.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);

        registeredTemplate.setProperties(templateProperties);
        feedMetadata.setProperties(registeredTemplate.getProperties());
        feedMetadata.setRegisteredTemplate(registeredTemplate);

        //resolve any ${metadata.} properties
        List<NifiProperty> resolvedProperties = propertyExpressionResolver.resolvePropertyExpressions(feedMetadata);

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
            CreateFeedBuilder
                .newFeed(nifiRestClient, nifiFlowCache, feedMetadata, registeredTemplate.getNifiTemplateId(), propertyExpressionResolver, propertyDescriptorTransform, niFiObjectCache, templateConnectionUtil)
                .enabled(enabled)
                .removeInactiveVersionedProcessGroup(removeInactiveNifiVersionedFeedFlows)
                .autoAlign(nifiAutoFeedsAlignAfterSave)
                .withNiFiTemplateCache(niFiTemplateCache);

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
        stopwatch.stop();
        log.debug("Time to prepare data for saving feed in NiFi: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        stopwatch.reset();
        stopwatch.start();
        NifiProcessGroup
            entity = feedBuilder.build();

        stopwatch.stop();
        log.debug("Time to save feed in NiFi: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        stopwatch.reset();

        feed = new NifiFeed(feedMetadata, entity);

        //set the original feedProperties back to the feed
        feedMetadata.setProperties(originalFeedProperties);
        //encrypt the metadata properties
        feedModelTransform.encryptSensitivePropertyValues(feedMetadata);

        if (entity.isSuccess()) {
            feedMetadata.setNifiProcessGroupId(entity.getProcessGroupEntity().getId());

            try {
                stopwatch.start();
                saveFeed(feedMetadata);
                feed.setEnableAfterSave(enableLater);
                feed.setSuccess(true);
                stopwatch.stop();
                log.debug("Time to saveFeed in Kylo: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                stopwatch.reset();
                stopwatch.start();
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


        metadataAccess.commit(() -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<? extends HadoopSecurityGroup> previousSavedSecurityGroups = null;
            // Store the old security groups before saving because we need to compare afterward
            if (!feed.isNew()) {
                Feed previousStateBeforeSaving = feedProvider.findById(feedProvider.resolveId(feed.getId()));
                Map<String, String> userProperties = previousStateBeforeSaving.getUserProperties();
                previousSavedSecurityGroups = previousStateBeforeSaving.getSecurityGroups();
            }

            //if this is the first time saving this feed create a new one
            Feed domainFeed = feedModelTransform.feedToDomain(feed);

            if (domainFeed.getState() == null) {
                domainFeed.setState(Feed.State.ENABLED);
            }
            stopwatch.stop();
            log.debug("Time to transform the feed to a domain object for saving: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();

            //initially save the feed
            if (feed.isNew()) {
                stopwatch.start();
                domainFeed = feedProvider.update(domainFeed);
                stopwatch.stop();
                log.debug("Time to save the New feed: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                stopwatch.reset();
            }

            final String domainId = domainFeed.getId().toString();
            final String feedName = FeedNameUtil.fullName(domainFeed.getCategory().getSystemName(), domainFeed.getName());

            // Build preconditions
            stopwatch.start();
            assignFeedDependencies(feed, domainFeed);
            stopwatch.stop();
            log.debug("Time to assignFeedDependencies: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();

            //Assign the datasources
            stopwatch.start();
            assignFeedDatasources(feed, domainFeed);
            stopwatch.stop();
            log.debug("Time to assignFeedDatasources: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();

            stopwatch.start();
            boolean isStream = feed.getRegisteredTemplate() != null ? feed.getRegisteredTemplate().isStream() : false;
            Long timeBetweenBatchJobs = feed.getRegisteredTemplate() != null ? feed.getRegisteredTemplate().getTimeBetweenStartingBatchJobs() : 0L;
            //sync the feed information to ops manager
            metadataAccess.commit(() -> opsManagerFeedProvider.save(opsManagerFeedProvider.resolveId(domainId), feedName, isStream, timeBetweenBatchJobs));

            stopwatch.stop();
            log.debug("Time to sync feed data with Operations Manager: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();

            // Update hadoop security group polices if the groups changed
            if (!feed.isNew() && !ListUtils.isEqualList(previousSavedSecurityGroups, domainFeed.getSecurityGroups())) {
                stopwatch.start();
                List<? extends HadoopSecurityGroup> securityGroups = domainFeed.getSecurityGroups();
                List<String> groupsAsCommaList = securityGroups.stream().map(group -> group.getName()).collect(Collectors.toList());
                hadoopAuthorizationService.updateSecurityGroupsForAllPolicies(feed.getSystemCategoryName(), feed.getSystemFeedName(), groupsAsCommaList, domainFeed.getProperties());
                stopwatch.stop();
                log.debug("Time to update hadoop security groups: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                stopwatch.reset();
            }

            // Update Hive metastore
            stopwatch.start();
            final boolean hasHiveDestination = domainFeed.getDestinations().stream()
                .map(FeedDestination::getDatasource)
                .filter(DerivedDatasource.class::isInstance)
                .map(DerivedDatasource.class::cast)
                .anyMatch(datasource -> "HiveDatasource".equals(datasource.getDatasourceType()));
            if (hasHiveDestination) {
                try {
                    feedHiveTableService.updateColumnDescriptions(feed);
                } catch (final DataAccessException e) {
                    log.warn("Failed to update column descriptions for feed: {}", feed.getCategoryAndFeedDisplayName(), e);
                }
            }
            stopwatch.stop();
            log.debug("Time to update hive metastore: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();

            // Update Kylo metastore
            stopwatch.start();
            domainFeed = feedProvider.update(domainFeed);
            stopwatch.stop();
            log.debug("Time to call feedProvider.update: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();
        }, (e) -> {
            if (feed.isNew() && StringUtils.isNotBlank(feed.getId())) {
                //Rollback ops Manager insert if it is newly created
                metadataAccess.commit(() -> {
                    opsManagerFeedProvider.delete(opsManagerFeedProvider.resolveId(feed.getId()));
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

    /**
     * Update a given feeds datasources clearing its sources/destinations before revaluating the data
     *
     * @param feedId the id of the feed rest model to update
     */
    public void updateFeedDatasources(String feedId) {
        metadataAccess.commit(() -> {
            feedProvider.removeFeedDestinations(feedProvider.resolveId(feedId));
            feedProvider.removeFeedSources(feedProvider.resolveId(feedId));
        });

        metadataAccess.commit(() -> {
            Feed domainFeed = feedProvider.findById(feedProvider.resolveId(feedId));
            FeedMetadata feed = feedModelTransform.domainToFeedMetadata(domainFeed);
            assignFeedDatasources(feed, domainFeed);
        });
    }

    /**
     * Iterate all of the feeds, clear all sources/destinations and reassign
     * Note this will be an expensive call
     */
    public void updateAllFeedsDatasources() {
        metadataAccess.commit(() -> {
            feedProvider.findAll().stream().forEach(domainFeed -> {
                domainFeed.clearSourcesAndDestinations();
            });
        });

        metadataAccess.commit(() -> {
            feedProvider.findAll().stream().forEach(domainFeed -> {
                FeedMetadata feed = feedModelTransform.domainToFeedMetadata(domainFeed);
                assignFeedDatasources(feed, domainFeed);
            });
        });
    }

    /**
     * Assign the feed sources/destinations
     *
     * @param feed       the feed rest model
     * @param domainFeed the domain feed
     */
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

    }

    @Override
    public void deleteFeed(@Nonnull final String feedId) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);
            Feed.ID feedIdentifier = feedProvider.resolveFeed(feedId);
            Feed feed = feedProvider.getFeed(feedIdentifier);
            //unschedule any SLAs
            serviceLevelAgreementService.removeAndUnscheduleAgreementsForFeed(feedIdentifier,feed.getQualifiedName());
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

            if (domainFeed != null) {
                domainFeed.setState(Feed.State.ENABLED);
                feedProvider.update(domainFeed);

                if (enabled) {
                    FeedMetadata feedMetadata = feedModelTransform.domainToFeedMetadata(domainFeed);
                    notifyFeedStateChange(feedMetadata, feedId, Feed.State.ENABLED, MetadataChange.ChangeType.UPDATE);
                }
            }

            return enabled;
        });

    }


    // @Transactional(transactionManager = "metadataTransactionManager")
    private boolean disableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(() -> {
            boolean disabled = feedProvider.disableFeed(feedId);
            Feed domainFeed = feedProvider.findById(feedId);

            if (domainFeed != null) {
                domainFeed.setState(Feed.State.DISABLED);
                feedProvider.update(domainFeed);

                if (disabled) {
                    FeedMetadata feedMetadata = feedModelTransform.domainToFeedMetadata(domainFeed);
                    notifyFeedStateChange(feedMetadata, feedId, Feed.State.DISABLED, MetadataChange.ChangeType.UPDATE);
                }
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
                boolean
                    canEditDetails =
                    accessController.isEntityAccessControlled() ? feedSummary.hasAction(FeedAccessControl.EDIT_DETAILS.getSystemName())
                                                                : accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);
                Map<String, Object> labelValueProperties = new HashMap<>();
                labelValueProperties.put("feed:disabled", isDisabled);
                labelValueProperties.put("feed:editDetails", canEditDetails);
                feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName() + (isDisabled ? " (DISABLED) " : ""), feedSummary.getCategoryAndFeedSystemName(),
                                                 isDisabled ? "This feed is currently disabled" : "", labelValueProperties));
            }

            feedSelection.sort(Comparator.comparing(LabelValue::getLabel, String.CASE_INSENSITIVE_ORDER));
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
            return hasPermission ? UserPropertyTransform.toUserFields(feedProvider.getUserFields()) : Collections.emptySet();
        });
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserField> userFields) {
        boolean hasPermission = this.accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);
        if (hasPermission) {
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
        String feedName = feedMetadata != null ? feedMetadata.getCategoryAndFeedName() : "";
        FeedChange change = new FeedChange(changeType, feedName, feedName, feedId, state);
        FeedChangeEvent event = new FeedChangeEvent(change, DateTime.now(), principal);
        metadataEventService.notify(event);
    }
}
