package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.sla.FeedServiceLevelAgreements;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementMetricTransformer;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementMetricTransformerHelper;
import com.thinkbiganalytics.jobrepo.repository.FeedRepository;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProperties;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionPolicyTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.LabelValue;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Created by sr186054 on 5/4/16.
 */
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

    @Inject
    FeedServiceLevelAgreementProvider feedServiceLevelAgreementProvider;


    /** Operations manager feed repository */
    @Inject
    FeedRepository feedRepository;

    /** Metadata event service */
    @Inject
    private MetadataEventService eventService;

    @Override
    public List<FeedMetadata> getReusableFeeds() {
        return null;
    }

    @Override
    public FeedMetadata getFeedByName(final String categoryName, final String feedName) {
        FeedMetadata feedMetadata = metadataAccess.read(new Command<FeedMetadata>() {
            @Override
            public FeedMetadata execute() {
                FeedManagerFeed domainFeed = feedManagerFeedProvider.findBySystemName(categoryName, feedName);
                if (domainFeed != null) {
                    return feedModelTransform.DOMAIN_TO_FEED.apply(domainFeed);
                }
                return null;
            }
        });
        return feedMetadata;
    }
    /*

     return metadataAccess.read(new Command<FeedMetadata>() {
            @Override
            public FeedMetadata execute() {
                return null;
            }
        });

     */

    @Override
    public FeedMetadata getFeedById(final String id) {
        return metadataAccess.read(new Command<FeedMetadata>() {
            @Override
            public FeedMetadata execute() {
                return getFeedById(id, false);
            }
        });

    }

    @Override
    public FeedMetadata getFeedById(final String id, final boolean refreshTargetTableSchema) {
        return metadataAccess.read(new Command<FeedMetadata>() {
            @Override
            public FeedMetadata execute() {

                FeedMetadata feedMetadata = null;
                FeedManagerFeed.ID domainId = feedManagerFeedProvider.resolveId(id);
                FeedManagerFeed domainFeed = feedManagerFeedProvider.findById(domainId);
                if (domainFeed != null) {
                    feedMetadata = feedModelTransform.DOMAIN_TO_FEED.apply(domainFeed);
                }
                if (refreshTargetTableSchema && feedMetadata != null) {
                    feedModelTransform.refreshTableSchemaFromHive(feedMetadata);
                }
                return feedMetadata;
            }
        });

    }

    @Override
    public Collection<FeedMetadata> getFeeds() {
        return metadataAccess.read(new Command<Collection<FeedMetadata>>() {
            @Override
            public Collection<FeedMetadata> execute() {
                Collection<FeedMetadata> feeds = null;
                List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findAll();
                if (domainFeeds != null) {
                    feeds = feedModelTransform.domainToFeedMetadata(domainFeeds);
                }
                return feeds;
            }
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
        return metadataAccess.read(new Command<List<FeedSummary>>() {
            @Override
            public List<FeedSummary> execute() {
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
            }
        });

    }

    @Override
    public List<FeedMetadata> getFeedsWithTemplate(final String registeredTemplateId) {
        return metadataAccess.read(new Command<List<FeedMetadata>>() {
            @Override
            public List<FeedMetadata> execute() {
                List<FeedMetadata> feedMetadatas = null;
                FeedManagerTemplate.ID templateDomainId = templateProvider.resolveId(registeredTemplateId);
                List<? extends FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findByTemplateId(templateDomainId);
                if (domainFeeds != null) {
                    feedMetadatas = feedModelTransform.domainToFeedMetadata(domainFeeds);
                }
                return feedMetadatas;
            }
        });
    }

    @Override
    protected RegisteredTemplate getRegisteredTemplateWithAllProperties(final String templateId)  {
        return metadataAccess.read(new Command<RegisteredTemplate>() {
            @Override
            public RegisteredTemplate execute() {
                return templateRestProvider.getRegisteredTemplate(templateId);
            }
        });

    }

    @Override
    public Feed.ID resolveFeed(@Nonnull Serializable fid) {
        return metadataAccess.read(() -> feedProvider.resolveFeed(fid));
    }

    // @Transactional(transactionManager = "metadataTransactionManager")
    public NifiFeed createFeed(final FeedMetadata feedMetadata)  {
        if (feedMetadata.getState() == null) {
            feedMetadata.setState(Feed.State.ENABLED.name());
        }
        return super.createFeed(feedMetadata);

    }


    @Override
    //@Transactional(transactionManager = "metadataTransactionManager")
    public void saveFeed(final FeedMetadata feed) {
        metadataAccess.commit(() -> {
            //if this is the first time saving this feed create a new one
            FeedManagerFeed domainFeed = feedModelTransform.feedToDomain(feed);
            if (domainFeed.getState() == null) {
                domainFeed.setState(Feed.State.ENABLED);
            }
            domainFeed = feedManagerFeedProvider.update(domainFeed);

            // Replace properties
            if (feed.getUserProperties() != null) {
                feedProvider.replaceProperties(domainFeed.getId(), feed.getUserProperties());
            }

            // Build preconditions
            List<PreconditionRule> preconditions = feed.getSchedule().getPreconditions();
            if(preconditions != null) {
                PreconditionPolicyTransformer transformer = new PreconditionPolicyTransformer(preconditions);
                transformer.applyFeedNameToCurrentFeedProperties(feed.getCategory().getSystemName(), feed.getSystemFeedName());
                List<com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup> transformedPreconditions = transformer.getPreconditions();
                ServiceLevelAgreementBuilder preconditionBuilder = feedProvider.buildPrecondition(domainFeed.getId()).name("Precondition for feed " + domainFeed.getId());
                for (com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup precondition : transformedPreconditions) {
                    for (Obligation group : precondition.getObligations()) {
                        preconditionBuilder.obligationGroupBuilder(ObligationGroup.Condition.valueOf(precondition.getCondition())).obligationBuilder().metric(group.getMetrics()).build();
                    }
                }
                preconditionBuilder.build();
            }

            // Return result
            return feed;
        });


    }

    @Override
    public void deleteFeed(@Nonnull final String feedId) {
        metadataAccess.commit(() -> {
            Feed feed = feedProvider.getFeed(feedProvider.resolveFeed(feedId));
            feedRepository.deleteFeed(feed.getCategory().getName(), feed.getName());
            feedProvider.deleteFeed(feed.getId());
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

    // @Transactional(transactionManager = "metadataTransactionManager")
    private boolean enableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(new Command<Boolean>() {
            @Override
            public Boolean execute() {
                return feedProvider.enableFeed(feedId);
            }
        });

    }

    // @Transactional(transactionManager = "metadataTransactionManager")
    private boolean disableFeed(final Feed.ID feedId) {
        return metadataAccess.commit(new Command<Boolean>() {
            @Override
            public Boolean execute() {
                return feedProvider.disableFeed(feedId);
            }
        });

    }

    public FeedSummary enableFeed(final String feedId) {
        return metadataAccess.commit(new Command<FeedSummary>() {
            @Override
            public FeedSummary execute() {
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
            }
        });


    }

    public FeedSummary disableFeed(final String feedId) {
        return metadataAccess.commit(new Command<FeedSummary>() {
            @Override
            public FeedSummary execute() {
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
            }
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


    public FeedServiceLevelAgreements getFeedServiceLevelAgreements(String feedId) {
        return metadataAccess.read(new Command<FeedServiceLevelAgreements>() {
            @Override
            public FeedServiceLevelAgreements execute() {
                FeedManagerFeed.ID domainId = feedManagerFeedProvider.resolveId(feedId);


                List<FeedServiceLevelAgreement> feedServiceLevelAgreements = feedServiceLevelAgreementProvider.findFeedServiceLevelAgreements(domainId);
                List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> restModels = new ArrayList<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement>();
                if(feedServiceLevelAgreements != null && !feedServiceLevelAgreements.isEmpty()){
                    for(FeedServiceLevelAgreement feedServiceLevelAgreement : feedServiceLevelAgreements) {
                        com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement restModel = Model.toModel(feedServiceLevelAgreement,true);
                        restModels.add(restModel);
                    }
                }


                    ServiceLevelAgreementMetricTransformerHelper helper = new ServiceLevelAgreementMetricTransformerHelper();

                    FeedServiceLevelAgreements feedServiceLevelAgreementHolder = helper.toFeedServiceLevelAgreements(feedId, restModels);

                    applyFeedSelectOptions(ServiceLevelAgreementMetricTransformer.instance()
                                               .findPropertiesForRulesetMatchingRenderTypes(feedServiceLevelAgreementHolder.getAllRules(), new String[]{PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name(),
                                                                                                                                                        PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name(),
                                                                                                                                                   PolicyPropertyTypes.PROPERTY_TYPE.feedSelect.name()}));

                return feedServiceLevelAgreementHolder;
            }
        });

    }





}
