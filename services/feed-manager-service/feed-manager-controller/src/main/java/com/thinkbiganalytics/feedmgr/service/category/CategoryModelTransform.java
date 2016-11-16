package com.thinkbiganalytics.feedmgr.service.category;

import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransform;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroupProvider;
import com.thinkbiganalytics.metadata.modeshape.security.JcrHadoopSecurityGroup;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Transforms categories between Feed Manager and Metadata formats.
 */
public class CategoryModelTransform {

    /** Provider for categories */
    @Inject
    FeedManagerCategoryProvider categoryProvider;

    /** Transform functions for feeds */
    @Inject
    FeedModelTransform feedModelTransform;

    @Inject
    private HadoopSecurityGroupProvider hadoopSecurityGroupProvider;

    /**
     * Transforms the specified Metadata category to a Feed Manager category.
     *
     * @param domainCategory the Metadata category
     * @return the Feed Manager category
     */
    @Nullable
    public FeedCategory domainToFeedCategory(@Nullable final FeedManagerCategory domainCategory) {
        return domainToFeedCategory(domainCategory, categoryProvider.getUserFields());
    }

    /**
     * Transforms the specified Metadata categories into Feed Manager categories.
     *
     * @param domain the Metadata categories
     * @return the Feed Manager categories
     */
    @Nonnull
    public List<FeedCategory> domainToFeedCategory(@Nonnull final Collection<FeedManagerCategory> domain) {
        final Set<UserFieldDescriptor> userFields = categoryProvider.getUserFields();
        return domain.stream().map(c -> domainToFeedCategory(c, userFields)).collect(Collectors.toList());
    }

    /**
     * Transforms the specified Metadata category into a Feed Manager category.
     *
     * @param domainCategory the Metadata category
     * @param userFields the user-defined fields
     * @return the Feed Manager category
     */
    @Nullable
    private FeedCategory domainToFeedCategory(@Nullable final FeedManagerCategory domainCategory, @Nonnull final Set<UserFieldDescriptor> userFields) {
        if (domainCategory != null) {
            FeedCategory category = new FeedCategory();
            category.setId(domainCategory.getId().toString());
            if (domainCategory.getFeeds() != null) {
                List<FeedSummary> summaries = feedModelTransform.domainToFeedSummary(domainCategory.getFeeds());
                category.setFeeds(summaries);
                category.setRelatedFeeds(summaries.size());
            }
            category.setIconColor(domainCategory.getIconColor());
            category.setIcon(domainCategory.getIcon());
            category.setName(domainCategory.getDisplayName());
            category.setSystemName(domainCategory.getName());
            category.setDescription(domainCategory.getDescription());
            category.setCreateDate(domainCategory.getCreatedTime() != null ? domainCategory.getCreatedTime().toDate() : null);
            category.setUpdateDate(domainCategory.getModifiedTime() != null ? domainCategory.getModifiedTime().toDate() : null);

            // Transform user-defined fields and properties
            category.setUserFields(UserPropertyTransform.toUserFields(categoryProvider.getFeedUserFields(domainCategory.getId()).orElse(Collections.emptySet())));
            category.setUserProperties(UserPropertyTransform.toUserProperties(domainCategory.getUserProperties(), userFields));

            // Convert JCR securitygroup to DTO
            List<com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup> restSecurityGroups = new ArrayList<>();
            if(domainCategory.getSecurityGroups() != null && domainCategory.getSecurityGroups().size() > 0) {
                for(Object group : domainCategory.getSecurityGroups()) {
                    HadoopSecurityGroup hadoopSecurityGroup = (HadoopSecurityGroup)group;
                    com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup restSecurityGroup = new com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup();
                    restSecurityGroup.setDescription(hadoopSecurityGroup.getDescription());
                    restSecurityGroup.setId(hadoopSecurityGroup.getGroupId());
                    restSecurityGroup.setName(hadoopSecurityGroup.getName());
                    restSecurityGroups.add(restSecurityGroup);
                }
            }
            category.setSecurityGroups(restSecurityGroups);

            return category;
        } else {
            return null;
        }
    }

    /**
     * Transforms the specified Metadata category to a simple Feed Manager category.
     *
     * @param domainCategory the Metadata category
     * @return the Feed Manager category
     */
    @Nullable
    public FeedCategory domainToFeedCategorySimple(@Nullable final FeedManagerCategory domainCategory) {
        if (domainCategory != null) {
            FeedCategory category = new FeedCategory();
            category.setId(domainCategory.getId().toString());
            category.setIconColor(domainCategory.getIconColor());
            category.setIcon(domainCategory.getIcon());
            category.setName(domainCategory.getDisplayName());
            category.setSystemName(domainCategory.getName());
            category.setDescription(domainCategory.getDescription());
            category.setCreateDate(domainCategory.getCreatedTime() != null ? domainCategory.getCreatedTime().toDate() : null);
            category.setUpdateDate(domainCategory.getModifiedTime() != null ? domainCategory.getModifiedTime().toDate() : null);
            return category;
        } else {
            return null;
        }
    }

    /**
     * Transforms the specified Metadata categories to simple Feed Manager categories.
     *
     * @param domain the Metadata categories
     * @return the Feed Manager categories
     */
    @Nonnull
    public List<FeedCategory> domainToFeedCategorySimple(@Nonnull final Collection<FeedManagerCategory> domain) {
        return domain.stream().map(this::domainToFeedCategorySimple).collect(Collectors.toList());
    }

    /**
     * Transforms the specified Feed Manager category to a Metadata category.
     *
     * @param feedCategory the Feed Manager category
     * @return the Metadata category
     */
    @Nonnull
    public FeedManagerCategory feedCategoryToDomain(@Nonnull final FeedCategory feedCategory) {
        final Set<UserFieldDescriptor> userFields = categoryProvider.getUserFields();
        return feedCategoryToDomain(feedCategory, userFields);
    }

    /**
     * Transforms the specified Feed Manager categories to Metadata categories.
     *
     * @param feedCategories the Feed Manager categories
     * @return the Metadata categories
     */
    public List<FeedManagerCategory> feedCategoryToDomain(Collection<FeedCategory> feedCategories) {
        final Set<UserFieldDescriptor> userFields = categoryProvider.getUserFields();
        return feedCategories.stream().map(c -> feedCategoryToDomain(c, userFields)).collect(Collectors.toList());
    }

    /**
     * Transforms the specified Feed Manager category to a Metadata category.
     *
     * @param feedCategory the Feed Manager category
     * @param userFields the user-defined fields
     * @return the Metadata category
     */
    @Nonnull
    private FeedManagerCategory feedCategoryToDomain(@Nonnull final FeedCategory feedCategory, @Nonnull final Set<UserFieldDescriptor> userFields) {
        Category.ID domainId = feedCategory.getId() != null ? categoryProvider.resolveId(feedCategory.getId()) : null;
        FeedManagerCategory category = null;
        if (domainId != null) {
            category = categoryProvider.findById(domainId);
            if (!feedCategory.getSystemName().equals(category.getName())) {
                categoryProvider.rename(domainId, feedCategory.getSystemName());
            }
        }

        if (category == null) {
            category = categoryProvider.ensureCategory(feedCategory.getSystemName());
        }
        if (category == null) {
            throw new CategoryNotFoundException("Unable to find Category ", domainId);
        }
        domainId = category.getId();
        feedCategory.setId(domainId.toString());
        category.setDisplayName(feedCategory.getName());
        category.setDescription(feedCategory.getDescription());
        category.setIcon(feedCategory.getIcon());
        category.setIconColor(feedCategory.getIconColor());
        category.setCreatedTime(new DateTime(feedCategory.getCreateDate()));
        category.setModifiedTime(new DateTime(feedCategory.getUpdateDate()));

        // Transforms the Feed Manager user-defined properties to domain user-defined properties
        if (feedCategory.getUserProperties() != null) {
            category.setUserProperties(UserPropertyTransform.toMetadataProperties(feedCategory.getUserProperties()), userFields);
        }

        // Set the hadoop security groups
        final List<HadoopSecurityGroup> securityGroups = new ArrayList<>();
        if (feedCategory.getSecurityGroups() != null) {

            for (com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup securityGroup : feedCategory.getSecurityGroups()) {
                JcrHadoopSecurityGroup hadoopSecurityGroup = (JcrHadoopSecurityGroup) hadoopSecurityGroupProvider.ensureSecurityGroup(securityGroup.getName());
                hadoopSecurityGroup.setGroupId(securityGroup.getId());
                hadoopSecurityGroup.setDescription(securityGroup.getDescription());
                securityGroups.add(hadoopSecurityGroup);
            }

        }
        category.setSecurityGroups(securityGroups);

        return category;
    }
}
