package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.feed.datasource.DerivedDatasourceFactory;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.jobrepo.repository.FeedRepository;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedPropertyChangeEvent;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProperties;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.policy.precondition.DependentFeedPrecondition;
import com.thinkbiganalytics.policy.precondition.Precondition;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionPolicyTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

public class DefaultFeedManagerFeedService extends AbstractFeedManagerFeedService implements FeedManagerFeedService {

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private DatasourceProvider datasourceProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    FeedManagerTemplateService templateRestProvider;

    @Inject
    FeedManagerPreconditionService feedPreconditionModelTransform;

    @Inject
    FeedModelTransform feedModelTransform;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    ServiceLevelAgreementProvider slaProvider;

    @Inject
    ServiceLevelAgreementService serviceLevelAgreementService;


    /** Operations manager feed repository */
    @Inject
    FeedRepository feedRepository;


    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    OperationalMetadataAccess operationalMetadataAccess;

    /** Metadata event service */
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

    @Override
    public List<FeedMetadata> getReusableFeeds() {
        return null;
    }

    // I had to use autowired instead of Inject to allow null values.
    @Autowired(required = false)
    @Qualifier("hadoopAuthorizationService")
    private HadoopAuthorizationService hadoopAuthorizationService;

    /** Event listener for precondition events */
    private final MetadataEventListener<FeedPropertyChangeEvent> feedPropertyChangeListener = new FeedPropertyChangeDispatcher();

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
    public FeedMetadata getFeedByName(final String categoryName, final String feedName) {
        FeedMetadata feedMetadata = metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            FeedManagerFeed domainFeed = feedManagerFeedProvider.findBySystemName(categoryName, feedName);
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
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            return getFeedById(id, false);
        });

    }

    @Override
    public FeedMetadata getFeedById(final String id, final boolean refreshTargetTableSchema) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            FeedMetadata feedMetadata = null;
            FeedManagerFeed.ID domainId = feedManagerFeedProvider.resolveId(id);
            FeedManagerFeed domainFeed = feedManagerFeedProvider.findById(domainId);
            if (domainFeed != null) {
                feedMetadata = feedModelTransform.domainToFeedMetadata(domainFeed);
            }
            if (refreshTargetTableSchema && feedMetadata != null) {
                feedModelTransform.refreshTableSchemaFromHive(feedMetadata);
            }
            return feedMetadata;
        });

    }

    @Override
    public Collection<FeedMetadata> getFeeds() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            Collection<FeedMetadata> feeds = null;
            List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findAll();
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
        Collection<FeedMetadata> feeds = getFeeds();
        List<FeedSummary> summaryList = new ArrayList<>();
        if (feeds != null && !feeds.isEmpty()) {
            for (FeedMetadata feed : feeds) {
                summaryList.add(new FeedSummary(feed));
            }
        }
        return summaryList;


    }

    @Override
    public List<FeedSummary> getFeedSummaryForCategory(final String categoryId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            List<FeedSummary> summaryList = new ArrayList<>();
            FeedManagerCategory.ID categoryDomainId = categoryProvider.resolveId(categoryId);
            List<? extends FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findByCategoryId(categoryDomainId);
            if (domainFeeds != null && !domainFeeds.isEmpty()) {
                List<FeedMetadata> feeds = feedModelTransform.domainToFeedMetadata(domainFeeds);
                for (FeedMetadata feed : feeds) {
                    summaryList.add(new FeedSummary(feed));
                }
            }
            return summaryList;
        });

    }

    @Override
    public List<FeedMetadata> getFeedsWithTemplate(final String registeredTemplateId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            List<FeedMetadata> feedMetadatas = null;
            FeedManagerTemplate.ID templateDomainId = templateProvider.resolveId(registeredTemplateId);
            List<? extends FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findByTemplateId(templateDomainId);
            if (domainFeeds != null) {
                feedMetadatas = feedModelTransform.domainToFeedMetadata(domainFeeds);
            }
            return feedMetadatas;
        });
    }

    @Override
    protected RegisteredTemplate getRegisteredTemplateWithAllProperties(final String templateId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            return templateRestProvider.getRegisteredTemplate(templateId);
        });

    }

    @Override
    public Feed.ID resolveFeed(@Nonnull Serializable fid) {
        return metadataAccess.read(() -> feedProvider.resolveFeed(fid));
    }

    // @Transactional(transactionManager = "metadataTransactionManager")
    public NifiFeed createFeed(final FeedMetadata feedMetadata) {
        if (feedMetadata.getState() == null) {
            if (feedMetadata.isActive()) {
                feedMetadata.setState(Feed.State.ENABLED.name());
            } else {
                feedMetadata.setState(Feed.State.DISABLED.name());
            }
        }
        return super.createFeed(feedMetadata);

    }

    @Override
    //@Transactional(transactionManager = "metadataTransactionManager")
    public void saveFeed(final FeedMetadata feed) {
        if (StringUtils.isBlank(feed.getId())) {
            feed.setIsNew(true);
        }
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_FEEDS);

            List<HadoopSecurityGroup> previousSavedSecurityGroups = null;
            // Store the old security groups before saving beccause we need to compare afterward
            if(feed.isNew()) {
                FeedManagerFeed existing = feedManagerFeedProvider.findBySystemName(feed.getCategory().getSystemName(), feed.getSystemFeedName());
                // Since we know this is expected to be new check if the category/feed name combo is already being used.
                if (existing != null) {
                    throw new DuplicateFeedNameException(feed.getCategoryName(), feed.getFeedName());
                }
            } else {
                FeedManagerFeed previousStateBeforeSaving = feedManagerFeedProvider.findById(feedManagerFeedProvider.resolveId(feed.getId()));
                Map<String, String> userProperties = previousStateBeforeSaving.getUserProperties();
                previousSavedSecurityGroups = previousStateBeforeSaving.getSecurityGroups();
            }

            //if this is the first time saving this feed create a new one
            FeedManagerFeed domainFeed = feedModelTransform.feedToDomain(feed);

            if (domainFeed.getState() == null) {
                domainFeed.setState(Feed.State.ENABLED);
            }

            //initially save the feed
            if (feed.isNew()) {
                domainFeed = feedManagerFeedProvider.update(domainFeed);
            }


            final String domainId = domainFeed.getId().toString();
            final String feedName = FeedNameUtil.fullName(domainFeed.getCategory().getName(), domainFeed.getName());

            // Build preconditions
            assignFeedDependencies(feed, domainFeed);

            //Assign the datasources
            assignFeedDatasources(feed, domainFeed);

            //sync the feed information to ops manager
            operationalMetadataAccess.commit(() -> opsManagerFeedProvider.save(opsManagerFeedProvider.resolveId(domainId), feedName));

            // Update hadoop security group polices if the groups changed
            if(!feed.isNew() && !ListUtils.isEqualList(previousSavedSecurityGroups, domainFeed.getSecurityGroups())) {
                List<HadoopSecurityGroup> securityGroups = domainFeed.getSecurityGroups();
                List<String> groupsAsCommaList = securityGroups.stream().map(group -> group.getName()).collect(Collectors.toList());
                hadoopAuthorizationService.updateSecurityGroupsForAllPolicies(feed.getSystemCategoryName(), feed.getSystemFeedName(), groupsAsCommaList, domainFeed.getProperties());
            }
            domainFeed = feedManagerFeedProvider.update(domainFeed);

            // Return result
            return feed;
        }, (e) -> {
            if (feed.isNew() && StringUtils.isNotBlank(feed.getId())) {
                //Rollback ops Manager insert if it is newly created
                operationalMetadataAccess.commit(() -> {
                    opsManagerFeedProvider.delete(opsManagerFeedProvider.resolveId(feed.getId()));
                    return null;
                });
            }
        });


    }

    /**
     * Looks for the Feed Preconditions and assigns the Feed Dependencies
     */
    private void assignFeedDependencies(FeedMetadata feed, FeedManagerFeed domainFeed) {
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

    private void assignFeedDatasources(FeedMetadata feed, FeedManagerFeed domainFeed) {
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

    /**
     * Assigns FeedSource and FeedDestination along with their respective Datasources
     */
    private void assignFeedDatasourcesx(FeedMetadata feed, FeedManagerFeed domainFeed) {
        final Feed.ID domainFeedId = domainFeed.getId();
        Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> sources = new HashSet<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID>();
        Set<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> destinations = new HashSet<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID>();

        String uniqueName = FeedNameUtil.fullName(feed.getCategory().getSystemName(), feed.getSystemFeedName());

        RegisteredTemplate template = feed.getRegisteredTemplate();
        if (template == null) {
            //fetch it for checks
            template = templateRestProvider.getRegisteredTemplateByName(feed.getTemplateName());
        }
        //Sources

        if (feed.getDataTransformation() != null && !feed.getDataTransformation().getTableNamesFromViewModel().isEmpty()) {
            Set<String> hiveTableSources = feed.getDataTransformation().getTableNamesFromViewModel();
            //create hive sources
            hiveTableSources.stream().forEach(hiveTable -> {
                com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource table
                    = datasourceProvider.ensureHiveTableDatasource(hiveTable,
                                                                   feed.getDescription(),
                                                                   StringUtils.trim(StringUtils.substringBefore(hiveTable, ".")),
                                                                   StringUtils.trim(StringUtils.substringAfterLast(hiveTable, ".")));
                sources.add(table.getId());
            });

        } else if (feed.getInputProcessorType().endsWith(".GetFile")) {
            String dir = "/" + uniqueName;
            if (template != null && template.getInputProcessors() != null) {
                RegisteredTemplate.Processor processor = template.getInputProcessors().stream().filter(p -> p.getType().equals(feed.getInputProcessorType())).findFirst().orElse(null);

                if (processor != null) {
                    NifiProperty inputdir = processor.getProperties().stream().filter(property -> property.getKey().equalsIgnoreCase("Input Directory")).findFirst().orElse(null);
                    if (inputdir != null) {
                        dir = inputdir.getValue();
                    }
                }
            }
            //get the value from the feed if possible
            NifiProperty
                feedInput =
                feed.getProperties().stream().filter(property -> property.getProcessorType().equals(feed.getInputProcessorType()) && property.getKey().equals("Input Directory")).findFirst()
                    .orElse(null);
            if (feedInput != null) {
                dir = feedInput.getValue();
            }

            DirectoryDatasource directoryDatasource = datasourceProvider.ensureDirectoryDatasource(dir, feed.getDescription(), Paths.get(dir));
            sources.add(directoryDatasource.getId());
        } else {
            DerivedDatasource defaultDatasource = datasourceProvider.ensureDatasource(uniqueName, feed.getDescription(), DerivedDatasource.class);
            sources.add(defaultDatasource.getId());
        }
        if (template != null && template.isDefineTable() || template.isDataTransformation()) {

            //find the destination for this type
            com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource table
                = datasourceProvider.ensureHiveTableDatasource(uniqueName,
                                                               feed.getDescription(),
                                                               feed.getCategory().getSystemName(),
                                                               feed.getSystemFeedName());
            destinations.add(table.getId());
        }

        sources.stream().forEach(sourceId -> feedProvider.ensureFeedSource(domainFeedId, sourceId));
        destinations.stream().forEach(sourceId -> feedProvider.ensureFeedDestination(domainFeedId, sourceId));
        //TODO deal with inputs changing sources?
    }

    @Override
    public void deleteFeed(@Nonnull final String feedId) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_FEEDS);

            Feed feed = feedProvider.getFeed(feedProvider.resolveFeed(feedId));
            //unschedule any SLAs
            serviceLevelAgreementService.unscheduleServiceLevelAgreement(feed.getId());
            feedRepository.deleteFeed(feed.getCategory().getName(), feed.getName());
            feedManagerFeedProvider.deleteById(feed.getId());
            operationalMetadataAccess.commit(() -> {
                opsManagerFeedProvider.delete(opsManagerFeedProvider.resolveId(feedId));
                return null;
            });
            return true;
        });
    }

    @Override
    public void enableFeedCleanup(@Nonnull String feedId) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_FEEDS);

            final Feed.ID id = feedProvider.resolveFeed(feedId);
            return feedProvider.mergeFeedProperties(id, ImmutableMap.of(FeedProperties.CLEANUP_ENABLED, "true"));
        });
    }

    // @Transactional(transactionManager = "metadataTransactionManager")
    private boolean enableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_FEEDS);
            boolean enabled = feedProvider.enableFeed(feedId);
            FeedManagerFeed domainFeed = feedManagerFeedProvider.findById(feedId);
            if (domainFeed != null) {
                FeedMetadata feedMetadata = feedModelTransform.deserializeFeedMetadata(domainFeed);
                feedMetadata.setState(FeedMetadata.STATE.ENABLED.name());
                domainFeed.setJson(ObjectMapperSerializer.serialize(feedMetadata));
                feedManagerFeedProvider.update(domainFeed);
            }

            return enabled;
        });

    }

    // @Transactional(transactionManager = "metadataTransactionManager")
    private boolean disableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_FEEDS);

            boolean disabled = feedProvider.disableFeed(feedId);
            FeedManagerFeed domainFeed = feedManagerFeedProvider.findById(feedId);
            if (domainFeed != null) {
                FeedMetadata feedMetadata = feedModelTransform.deserializeFeedMetadata(domainFeed);
                feedMetadata.setState(FeedMetadata.STATE.DISABLED.name());
                domainFeed.setJson(ObjectMapperSerializer.serialize(feedMetadata));
                feedManagerFeedProvider.update(domainFeed);
            }

            return disabled;
        });

    }

    public FeedSummary enableFeed(final String feedId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_FEEDS);

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
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_FEEDS);

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
                feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName() + (isDisabled ? " (DISABLED) ": ""), feedSummary.getCategoryAndFeedSystemName(), isDisabled ? "This feed is currently disabled" : ""));
            }
            for (FieldRuleProperty property : properties) {
                property.setSelectableValues(feedSelection);
                if (property.getValues() == null) {
                    property.setValues(new ArrayList<>()); // reset the intial values to be an empty arraylist
                }
            }
        }
    }

    @Override
    public void updateFeedsWithTemplate(String oldTemplateId, String newTemplateId) {
        //not needed
    }

    @Nonnull
    @Override
    public Set<UserField> getUserFields() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            return UserPropertyTransform.toUserFields(feedProvider.getUserFields());
        });
    }

    @Nonnull
    @Override
    public Optional<Set<UserProperty>> getUserFields(@Nonnull final String categoryId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_FEEDS);

            final Optional<Set<UserFieldDescriptor>> categoryUserFields = categoryProvider.getFeedUserFields(categoryProvider.resolveId(categoryId));
            final Set<UserFieldDescriptor> globalUserFields = feedProvider.getUserFields();
            if (categoryUserFields.isPresent()) {
                return Optional.of(UserPropertyTransform.toUserProperties(Collections.emptyMap(), Sets.union(globalUserFields, categoryUserFields.get())));
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserField> userFields) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_FEEDS);

        feedProvider.setUserFields(UserPropertyTransform.toUserFieldDescriptors(userFields));
    }

    private class FeedPropertyChangeDispatcher implements MetadataEventListener<FeedPropertyChangeEvent> {


        @Override
        public void notify(@Nonnull final FeedPropertyChangeEvent metadataEvent) {
            Properties oldProperties = metadataEvent.getNifiPropertiesToDelete();
            metadataAccess.commit(() -> {
                Feed feed = feedProvider.getFeed(feedProvider.resolveFeed(metadataEvent.getFeedId()));
                oldProperties.forEach((k,v) -> {
                    feed.removeProperty((String)k);
                });
            }, MetadataAccess.SERVICE) ;
        }

    }
}
