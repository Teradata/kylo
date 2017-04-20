package com.thinkbiganalytics.feedmgr.service.category;

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

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.security.AccessController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * An implementation of {@link FeedManagerCategoryService} backed by a {@link CategoryProvider}.
 */
public class DefaultFeedManagerCategoryService implements FeedManagerCategoryService {

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    CategoryModelTransform categoryModelTransform;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    private SecurityService securityService;

    @Inject
    private AccessController accessController;

    @Override
    public Collection<FeedCategory> getCategories() {
        return metadataAccess.read((MetadataCommand<Collection<FeedCategory>>) () -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            List<Category> domainCategories = categoryProvider.findAll();
            return categoryModelTransform.domainToFeedCategory(domainCategories);
        });
    }

    @Override
    public FeedCategory getCategoryById(final String id) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            final Category.ID domainId = categoryProvider.resolveId(id);
            final Category domainCategory = categoryProvider.findById(domainId);
            return categoryModelTransform.domainToFeedCategory(domainCategory);
        });
    }

    @Override
    public FeedCategory getCategoryBySystemName(final String name) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            final Category domainCategory = categoryProvider.findBySystemName(name);
            return categoryModelTransform.domainToFeedCategory(domainCategory);
        });
    }

    @Override
    public void saveCategory(final FeedCategory category) {
        final Category.ID domainId = metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_CATEGORIES);

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
            final Category domainCategory = categoryProvider.update(categoryModelTransform.feedCategoryToDomain(category));

            // Repopulate identifier
            category.setId(domainCategory.getId().toString());

            ///update access control
            //TODO only do this when modifying the access control
            if (domainCategory.getAllowedActions().hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                category.toRoleMembershipChangeList().stream().forEach(roleMembershipChange -> securityService.changeCategoryRoleMemberships(category.getId(), roleMembershipChange));
            }

            return domainCategory.getId();
        });

        // Update user-defined fields (must be outside metadataAccess)
        final Set<UserFieldDescriptor> userFields = (category.getUserFields() != null) ? UserPropertyTransform.toUserFieldDescriptors(category.getUserFields()) : Collections.emptySet();
        categoryProvider.setFeedUserFields(domainId, userFields);
    }

    @Override
    public boolean deleteCategory(final String categoryId) throws InvalidOperationException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_CATEGORIES);

        final Category.ID domainId = metadataAccess.read(() -> categoryProvider.resolveId(categoryId));
        categoryProvider.deleteById(domainId);
        return true;
    }

    @Nonnull
    @Override
    public Set<UserField> getUserFields() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            return UserPropertyTransform.toUserFields(categoryProvider.getUserFields());
        });
    }

    @Override
    public void setUserFields(@Nonnull Set<UserField> userFields) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_CATEGORIES);

        categoryProvider.setUserFields(UserPropertyTransform.toUserFieldDescriptors(userFields));
    }

    @Nonnull
    @Override
    public Set<UserProperty> getUserProperties() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            return UserPropertyTransform.toUserProperties(Collections.emptyMap(), categoryProvider.getUserFields());
        });
    }
}
