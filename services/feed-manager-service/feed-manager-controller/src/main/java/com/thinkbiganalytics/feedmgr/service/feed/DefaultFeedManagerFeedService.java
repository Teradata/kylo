package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.jobrepo.repository.FeedRepository;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProperties;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionPolicyTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DefaultFeedManagerFeedService extends AbstractFeedManagerFeedService implements FeedManagerFeedService {

    @Inject
    private FeedProvider feedProvider;

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

    @Override
    public List<FeedMetadata> getReusableFeeds() {
        return null;
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
            feedMetadata.setState(Feed.State.ENABLED.name());
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

            //if this is the first time saving this feed create a new one
            FeedManagerFeed domainFeed = feedModelTransform.feedToDomain(feed);
            if (domainFeed.getState() == null) {
                domainFeed.setState(Feed.State.ENABLED);
            }
            domainFeed = feedManagerFeedProvider.update(domainFeed);

            //call out to operations to make the connection to modeshape for the feeds
            final String domainId = domainFeed.getId().toString();
            final String feedName = FeedNameUtil.fullName(domainFeed.getCategory().getName(), domainFeed.getName());


            // Build preconditions
            List<PreconditionRule> preconditions = feed.getSchedule().getPreconditions();
            if (preconditions != null) {
                PreconditionPolicyTransformer transformer = new PreconditionPolicyTransformer(preconditions);
                transformer.applyFeedNameToCurrentFeedProperties(feed.getCategory().getSystemName(), feed.getSystemFeedName());
                List<com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup> transformedPreconditions = transformer.getPreconditions();
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

            }
            //sync the feed information to ops manager
            operationalMetadataAccess.commit(() -> opsManagerFeedProvider.save(opsManagerFeedProvider.resolveId(domainId), feedName));


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

    @Override
    public void deleteFeed(@Nonnull final String feedId) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_FEEDS);

            Feed feed = feedProvider.getFeed(feedProvider.resolveFeed(feedId));
            feedRepository.deleteFeed(feed.getCategory().getName(), feed.getName());
            feedProvider.deleteFeed(feed.getId());
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

            return feedProvider.enableFeed(feedId);
        });

    }

    // @Transactional(transactionManager = "metadataTransactionManager")
    private boolean disableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_FEEDS);

            return feedProvider.disableFeed(feedId);
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
                }
                FeedSummary feedSummary = new FeedSummary(feedMetadata);
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
                boolean enabled = disableFeed(domainId);
                //re fetch it
                if (enabled) {
                    feedMetadata.setState(Feed.State.DISABLED.name());
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
                feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName(), feedSummary.getCategoryAndFeedSystemName()));
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
}
