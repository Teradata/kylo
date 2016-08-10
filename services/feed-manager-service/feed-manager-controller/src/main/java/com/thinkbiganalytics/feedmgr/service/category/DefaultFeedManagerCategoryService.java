package com.thinkbiganalytics.feedmgr.service.category;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * An implementation of {@link FeedManagerCategoryService} backed by a {@link FeedManagerCategoryProvider}.
 */
public class DefaultFeedManagerCategoryService implements FeedManagerCategoryService {

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Inject
    CategoryModelTransform categoryModelTransform;

    @Inject
    MetadataAccess metadataAccess;

    @Override
    public Collection<FeedCategory> getCategories() {
        return metadataAccess.read((Command<Collection<FeedCategory>>) () -> {
            List<FeedManagerCategory> domainCategories = categoryProvider.findAll();
            return categoryModelTransform.domainToFeedCategory(domainCategories);
        });
    }

    @Override
    public FeedCategory getCategoryById(final String id) {
        return metadataAccess.read(() -> {
            final FeedManagerCategory.ID domainId = categoryProvider.resolveId(id);
            final FeedManagerCategory domainCategory = categoryProvider.findById(domainId);
            return categoryModelTransform.DOMAIN_TO_FEED_CATEGORY.apply(domainCategory);
        });
    }

    @Override
    public FeedCategory getCategoryBySystemName(final String name) {
        return metadataAccess.read(() -> {
            final FeedManagerCategory domainCategory = categoryProvider.findBySystemName(name);
            return categoryModelTransform.DOMAIN_TO_FEED_CATEGORY.apply(domainCategory);
        });
    }

    @Override
    public void saveCategory(final FeedCategory category) {
        metadataAccess.commit(() -> {
            // Determine the system name
            if (category.getId() == null) {
                category.generateSystemName();
            } else {
                final FeedCategory oldCategory = getCategoryById(category.getId());
                if (oldCategory != null && !oldCategory.getName().equalsIgnoreCase(category.getName())) {
                    //names have changed
                    //only regenerate the system name if there are no related feeds
                    if (oldCategory.getRelatedFeeds() == 0) {
                        category.generateSystemName();
                    }
                }
            }

            // Update the domain entity
            FeedManagerCategory domainCategory = categoryModelTransform.FEED_CATEGORY_TO_DOMAIN.apply(category);
            domainCategory = categoryProvider.update(domainCategory);

            final Set<UserFieldDescriptor> userFields = (category.getUserFields() != null) ? UserPropertyTransform.toUserFieldDescriptors(category.getUserFields()) : Collections.emptySet();
            categoryProvider.setFeedUserFields(domainCategory.getId(), userFields);

            // Repopulate identifier
            category.setId(domainCategory.getId().toString());
            return category;
        });
    }

    @Override
    public boolean deleteCategory(final String categoryId) throws InvalidOperationException {
        return metadataAccess.commit(() -> {
            final FeedManagerCategory.ID domainId = categoryProvider.resolveId(categoryId);
            categoryProvider.deleteById(domainId);
            return true;
        });
    }

    @Nonnull
    @Override
    public Set<UserField> getUserFields() {
        return metadataAccess.read(() -> UserPropertyTransform.toUserFields(categoryProvider.getUserFields()));
    }

    @Override
    public void setUserFields(@Nonnull Set<UserField> userFields) {
        metadataAccess.commit(() -> {
            categoryProvider.setUserFields(UserPropertyTransform.toUserFieldDescriptors(userFields));
            return userFields;
        });
    }
}
