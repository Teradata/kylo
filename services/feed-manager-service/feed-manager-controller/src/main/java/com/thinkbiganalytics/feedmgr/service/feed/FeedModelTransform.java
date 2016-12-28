package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroupProvider;
import com.thinkbiganalytics.metadata.modeshape.security.JcrHadoopSecurityGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Transforms feeds between Feed Manager and Metadata formats.
 */
public class FeedModelTransform {

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private TemplateModelTransform templateModelTransform;

    @Inject
    private CategoryModelTransform categoryModelTransform;

    @Inject
    private HiveService hiveService;

    @Inject
    private HadoopSecurityGroupProvider hadoopSecurityGroupProvider;

    /**
     *
     * @param feedMetadata
     */
    private void simplifyFeedMetadataForSerialization(FeedMetadata feedMetadata){


        if(feedMetadata.getTable() != null){
            feedMetadata.getTable().simplifyFieldPoliciesForSerialization();
        }

    }

    /**
     * Transforms the specified Feed Manager feed to a Metadata feed.
     *
     * @param feedMetadata the Feed Manager feed
     * @return the Metadata feed
     */
    @Nonnull
    public FeedManagerFeed feedToDomain(@Nonnull final FeedMetadata feedMetadata) {
        //resolve the id
        Feed.ID domainId = feedMetadata.getId() != null ? feedManagerFeedProvider.resolveId(feedMetadata.getId()) : null;
        FeedManagerFeed domain = domainId != null ? feedManagerFeedProvider.findById(domainId) : null;

        FeedCategory restCategoryModel = feedMetadata.getCategory();
        Category category = null;

        if (restCategoryModel != null && (domain == null || domain.getCategory() == null)) {
            category = categoryProvider.findById(categoryProvider.resolveId(restCategoryModel.getId()));
        }

        if (domain == null) {
            //ensure the Category exists
            if (category == null) {
                final String categoryId = (restCategoryModel != null) ? restCategoryModel.getId() : "(null)";
                throw new RuntimeException("Category cannot be found while creating feed " + feedMetadata.getSystemFeedName() + ".  Category Id is " + categoryId);
            }
            domain = feedManagerFeedProvider.ensureFeed(category.getId(), feedMetadata.getSystemFeedName());
            domainId = domain.getId();
            Feed.State state = Feed.State.valueOf(feedMetadata.getState());
            domain.setState(state);
            //reassign the domain data back to the ui model....
            feedMetadata.setFeedId(domainId.toString());
            feedMetadata.setState(state.name());

        }
        domain.setDisplayName(feedMetadata.getFeedName());
        domain.setDescription(feedMetadata.getDescription());

        feedMetadata.setId(domain.getId().toString());

        RegisteredTemplate template = feedMetadata.getRegisteredTemplate();
        if (template != null) {
            FeedManagerTemplate domainTemplate = templateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(template);
            domain.setTemplate(domainTemplate);
        }
        if (StringUtils.isNotBlank(feedMetadata.getState())) {
            Feed.State state = Feed.State.valueOf(feedMetadata.getState().toUpperCase());
            domain.setState(state);
        }
        domain.setNifiProcessGroupId(feedMetadata.getNifiProcessGroupId());

        //clear out the state as that
        simplifyFeedMetadataForSerialization(feedMetadata);

        domain.setJson(ObjectMapperSerializer.serialize(feedMetadata));
        // if (feedMetadata.getVersion() == null) {
        //     feedMetadata.setVersion(1L);
        // }

        //Datasource datasource = NifiFeedDatasourceFactory.transformSources(feedMetadata);
        // feedProvider.ensureFeedSource()

        if (domain.getTemplate() == null) {
            FeedManagerTemplate.ID templateId = templateProvider.resolveId(feedMetadata.getTemplateId());
            FeedManagerTemplate domainTemplate = templateProvider.findById(templateId);
            domain.setTemplate(domainTemplate);

        }

        // Set user-defined properties
        if (feedMetadata.getUserProperties() != null) {
            final Set<UserFieldDescriptor> userFields = getUserFields(category);
            domain.setUserProperties(UserPropertyTransform.toMetadataProperties(feedMetadata.getUserProperties()), userFields);
        }

        // Set the hadoop security groups
        final List<HadoopSecurityGroup> securityGroups = new ArrayList<>();
        if (feedMetadata.getSecurityGroups() != null) {

            for (com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup securityGroup : feedMetadata.getSecurityGroups()) {
                JcrHadoopSecurityGroup hadoopSecurityGroup = (JcrHadoopSecurityGroup) hadoopSecurityGroupProvider.ensureSecurityGroup(securityGroup.getName());
                hadoopSecurityGroup.setGroupId(securityGroup.getId());
                hadoopSecurityGroup.setDescription(securityGroup.getDescription());
                securityGroups.add(hadoopSecurityGroup);
            }

        }
        domain.setSecurityGroups(securityGroups);

        domain.setVersionName(feedMetadata.getVersionName());
        return domain;
    }

    /**
     * Transforms the specified Metadata feed to a Feed Manager feed.
     *
     * @param domain the Metadata feed
     * @return the Feed Manager feed
     */
    @Nonnull
    public FeedMetadata domainToFeedMetadata(@Nonnull final FeedManagerFeed domain) {
        return domainToFeedMetadata(domain, null);
    }

    /**
     * Transforms the specified Metadata feeds to Feed Manager feeds.
     *
     * @param domain the Metadata feeds
     * @return the Feed Manager feeds
     */
    @Nonnull
    public List<FeedMetadata> domainToFeedMetadata(@Nonnull final Collection<? extends FeedManagerFeed> domain) {
        final Map<Category, Set<UserFieldDescriptor>> userFieldMap = Maps.newHashMap();
        return domain.stream().map(f -> domainToFeedMetadata(f, userFieldMap)).collect(Collectors.toList());
    }

    public FeedMetadata deserializeFeedMetadata(FeedManagerFeed domain) {
        String json = domain.getJson();
        FeedMetadata feedMetadata = ObjectMapperSerializer.deserialize(json, FeedMetadata.class);
        return feedMetadata;
    }


    /**
     * Transforms the specified Metadata feed to a Feed Manager feed.
     *
     * @param domain       the Metadata feed
     * @param userFieldMap cache map from category to user-defined fields, or {@code null}
     * @return the Feed Manager feed
     */
    @Nonnull
    private FeedMetadata domainToFeedMetadata(@Nonnull final FeedManagerFeed<?> domain, @Nullable final Map<Category, Set<UserFieldDescriptor>> userFieldMap) {

        FeedMetadata feed = deserializeFeedMetadata(domain);
        feed.setId(domain.getId().toString());
        feed.setFeedId(domain.getId().toString());
        feed.setTemplateId(domain.getTemplate().getId().toString());
        if (domain.getCreatedTime() != null) {
            feed.setCreateDate(domain.getCreatedTime().toDate());
        }
        if (domain.getModifiedTime() != null) {
            feed.setUpdateDate(domain.getModifiedTime().toDate());
        }

        FeedManagerTemplate template = domain.getTemplate();
        if (template != null) {
            RegisteredTemplate registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
            feed.setRegisteredTemplate(registeredTemplate);
            feed.setTemplateId(registeredTemplate.getId());
        }
        FeedManagerCategory category = domain.getCategory();
        if (category != null) {
            feed.setCategory(categoryModelTransform.domainToFeedCategorySimple(category));
        }
        feed.setState(domain.getState() != null ? domain.getState().name() : null);
        feed.setVersionName(domain.getVersionName() != null ? domain.getVersionName() : null);

        // Set user-defined properties
        final Set<UserFieldDescriptor> userFields;
        if (userFieldMap == null) {
            userFields = getUserFields(category);
        } else if (userFieldMap.containsKey(category)) {
            userFields = userFieldMap.get(category);
        } else {
            userFields = getUserFields(category);
            userFieldMap.put(category, userFields);
        }

        @SuppressWarnings("unchecked")
        final Set<UserProperty> userProperties = UserPropertyTransform.toUserProperties(domain.getUserProperties(), userFields);
        feed.setUserProperties(userProperties);

        // Convert JCR securitygroup to DTO
        List<com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup> restSecurityGroups = new ArrayList<>();
        if(domain.getSecurityGroups() != null && domain.getSecurityGroups().size() > 0) {
            for(Object group : domain.getSecurityGroups()) {
                HadoopSecurityGroup hadoopSecurityGroup = (HadoopSecurityGroup)group;
                com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup restSecurityGroup = new com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup();
                restSecurityGroup.setDescription(hadoopSecurityGroup.getDescription());
                restSecurityGroup.setId(hadoopSecurityGroup.getGroupId());
                restSecurityGroup.setName(hadoopSecurityGroup.getName());
                restSecurityGroups.add(restSecurityGroup);
            }
        }
        feed.setSecurityGroups(restSecurityGroups);

        if (domain.getUsedByFeeds() != null) {
            final List<FeedSummary> usedByFeeds = domain.getUsedByFeeds().stream()
                    .map(this::domainToFeedSummary)
                    .collect(Collectors.toList());
            feed.setUsedByFeeds(usedByFeeds);
        }

        return feed;
    }

    /**
     * Transforms the specified Metadata feed to a Feed Manager feed summary.
     *
     * @param feedManagerFeed the Metadata feed
     * @return the Feed Manager feed summary
     */
    @Nonnull
    public FeedSummary domainToFeedSummary(@Nonnull final Feed feedManagerFeed) {
        FeedSummary feedSummary = new FeedSummary();
        feedSummary.setId(feedManagerFeed.getId().toString());
        feedSummary.setFeedId(feedManagerFeed.getId().toString());
        feedSummary.setCategoryId(feedManagerFeed.getCategory().getId().toString());
        if (feedManagerFeed.getCategory() instanceof FeedManagerCategory) {
            feedSummary.setCategoryIcon(((FeedManagerCategory) feedManagerFeed.getCategory()).getIcon());
            feedSummary.setCategoryIconColor(((FeedManagerCategory) feedManagerFeed.getCategory()).getIconColor());
        }
        feedSummary.setCategoryName(feedManagerFeed.getCategory().getDisplayName());
        feedSummary.setSystemCategoryName(feedManagerFeed.getCategory().getName());
        feedSummary.setUpdateDate(feedManagerFeed.getModifiedTime() != null ? feedManagerFeed.getModifiedTime().toDate() : null);
        feedSummary.setFeedName(feedManagerFeed.getDisplayName());
        feedSummary.setSystemFeedName(feedManagerFeed.getName());
        feedSummary.setActive(feedManagerFeed.getState() != null && feedManagerFeed.getState().equals(Feed.State.ENABLED));

        feedSummary.setState(feedManagerFeed.getState() != null ? feedManagerFeed.getState().name() : null);

        if (feedManagerFeed instanceof FeedManagerFeed) {

            FeedManagerFeed fmf = (FeedManagerFeed) feedManagerFeed;
            if (fmf.getTemplate() != null) {
                feedSummary.setTemplateId(fmf.getTemplate().getId().toString());
                feedSummary.setTemplateName(fmf.getTemplate().getName());
            }
        }
        return feedSummary;
    }

    /**
     * Transforms the specified Metadata feeds to Feed Manager feed summaries.
     *
     * @param domain the Metadata feed
     * @return the Feed Manager feed summaries
     */
    @Nonnull
    public List<FeedSummary> domainToFeedSummary(@Nonnull final Collection<? extends Feed> domain) {
        return domain.stream().map(this::domainToFeedSummary).collect(Collectors.toList());
    }

    /**
     * Gets the user-defined fields including those for the specified category.
     *
     * @param category the domain category
     * @return the user-defined fields
     */
    @Nonnull
    private Set<UserFieldDescriptor> getUserFields(@Nullable final Category category) {
        final Set<UserFieldDescriptor> userFields = feedProvider.getUserFields();
        return (category != null) ? Sets.union(userFields, categoryProvider.getFeedUserFields(category.getId()).orElse(Collections.emptySet())) : userFields;
    }
}
