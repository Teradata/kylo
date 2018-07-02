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

import com.thinkbiganalytics.cluster.ClusterService;
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
import com.thinkbiganalytics.metadata.api.event.category.CategoryChange;
import com.thinkbiganalytics.metadata.api.event.category.CategoryChangeEvent;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;

import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
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
    private CategoryProvider categoryProvider;

    @Inject
    private CategoryModelTransform categoryModelTransform;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private SecurityService securityService;

    @Inject
    private AccessController accessController;

    @Inject
    private MetadataEventService metadataEventService;

    @Inject
    private ClusterService clusterService;


    @Override
    public boolean checkCategoryPermission(final String id, final Action action, final Action... more) {
        if (accessController.isEntityAccessControlled()) {
            return metadataAccess.read(() -> {
                final Category.ID domainId = categoryProvider.resolveId(id);
                final Category domainCategory = categoryProvider.findById(domainId);

                if (domainCategory != null) {
                    domainCategory.getAllowedActions().checkPermission(action, more);
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
    public Collection<FeedCategory> getCategories() {
        return getCategories(false);
    }

    public Collection<FeedCategory> getCategories(boolean includeFeedDetails) {
        return metadataAccess.read((MetadataCommand<Collection<FeedCategory>>) () -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            List<Category> domainCategories = categoryProvider.findAll();
            return categoryModelTransform.domainToFeedCategory(domainCategories, includeFeedDetails);
        });
    }

    @Override
    public FeedCategory getCategoryById(final String id) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            final Category.ID domainId = categoryProvider.resolveId(id);
            final Category domainCategory = categoryProvider.findById(domainId);
            return categoryModelTransform.domainToFeedCategory(domainCategory, true);
        });
    }

    @Override
    public FeedCategory getCategoryBySystemName(final String name) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            final Category domainCategory = categoryProvider.findBySystemName(name);
            return categoryModelTransform.domainToFeedCategory(domainCategory, true);
        });
    }

    @Override
    public void saveCategory(final FeedCategory feedCategory) {

        boolean isNew = feedCategory.getId() == null;

        // Perform the rest of the updates as the current user.
        Category.ID updateId = metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_CATEGORIES);
            
            if (feedCategory.getId() != null) {
                final Category.ID domainId = feedCategory.getId() != null ? categoryProvider.resolveId(feedCategory.getId()) : null;
                final FeedCategory oldCategory = getCategoryById(feedCategory.getId());
                
                if (oldCategory != null && !oldCategory.getSystemName().equalsIgnoreCase(feedCategory.getSystemName())) {
                    //system names have changed, only regenerate the system name if there are no related feeds
                    if (oldCategory.getRelatedFeeds() == 0) {
                        categoryProvider.renameSystemName(domainId, feedCategory.getSystemName());
                    }
                }
            }

            // Update the domain entity
            final Category domainCategory = categoryProvider.update(categoryModelTransform.feedCategoryToDomain(feedCategory));

            // Repopulate identifier
            feedCategory.setId(domainCategory.getId().toString());

            ///update access control
            //TODO only do this when modifying the access control
            if (domainCategory.getAllowedActions().hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                feedCategory.toRoleMembershipChangeList().stream().forEach(roleMembershipChange -> securityService.changeCategoryRoleMemberships(feedCategory.getId(), roleMembershipChange));
                feedCategory.toFeedRoleMembershipChangeList().stream()
                    .forEach(roleMembershipChange -> securityService.changeCategoryFeedRoleMemberships(feedCategory.getId(), roleMembershipChange));
            }
            
            // Update user-defined fields
            final Set<UserFieldDescriptor> userFields = (feedCategory.getUserFields() != null) ? UserPropertyTransform.toUserFieldDescriptors(feedCategory.getUserFields()) : Collections.emptySet();
            categoryProvider.setFeedUserFields(domainCategory.getId(), userFields);

            return domainCategory.getId();
        });
        
        notifyCategoryChange(updateId, feedCategory.getSystemName(), isNew ? MetadataChange.ChangeType.CREATE : MetadataChange.ChangeType.UPDATE);
    }

    @Override
    public boolean deleteCategory(final String categoryId) throws InvalidOperationException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_CATEGORIES);

        SimpleCategory simpleCategory = metadataAccess.read(() -> {
            Category.ID id = categoryProvider.resolveId(categoryId);
            final Category category = categoryProvider.findById(id);

            if (category != null) {

                if (accessController.isEntityAccessControlled()) {
                    //this check should throw a runtime exception
                    category.getAllowedActions().checkPermission(CategoryAccessControl.DELETE);
                }
                return new SimpleCategory(id, category.getSystemName());
            } else {
                //unable to read the category
                return null;
            }


        });
        if (simpleCategory != null) {
            metadataAccess.commit(() -> {
                categoryProvider.deleteById(simpleCategory.getCategoryId());
            }, MetadataAccess.SERVICE);
            notifyCategoryChange(simpleCategory.getCategoryId(), simpleCategory.getCategoryName(), MetadataChange.ChangeType.DELETE);
            return true;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public Set<UserField> getUserFields() {
        return metadataAccess.read(() -> {
            boolean hasPermission = this.accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);
            return hasPermission ? UserPropertyTransform.toUserFields(categoryProvider.getUserFields()) : Collections.emptySet();
        });
    }

    @Override
    public void setUserFields(@Nonnull Set<UserField> userFields) {
         metadataAccess.commit(() -> {
            boolean hasPermission = this.accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_CATEGORIES);
            if (hasPermission) {
                categoryProvider.setUserFields(UserPropertyTransform.toUserFieldDescriptors(userFields));
            }
        });
    }

    @Nonnull
    @Override
    public Set<UserProperty> getUserProperties() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);

            return UserPropertyTransform.toUserProperties(Collections.emptyMap(), categoryProvider.getUserFields());
        });
    }

    private class SimpleCategory {

        private Category.ID categoryId;
        private String categoryName;

        public SimpleCategory(Category.ID categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        public Category.ID getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }
    }

    /**
     * update the audit information for feed state changes
     *
     * @param categoryId   the category id
     * @param categoryName the categoryName
     * @param changeType   the event type
     */
    private void notifyCategoryChange(Category.ID categoryId, String categoryName, MetadataChange.ChangeType changeType) {
        final Principal principal = SecurityContextHolder.getContext().getAuthentication() != null
                                    ? SecurityContextHolder.getContext().getAuthentication()
                                    : null;
        CategoryChange change = new CategoryChange(changeType, categoryName, categoryId);
        CategoryChangeEvent event = new CategoryChangeEvent(change, DateTime.now(), principal);
        metadataEventService.notify(event);
        clusterService.sendMessageToOthers(CategoryChange.CLUSTER_EVENT_TYPE, change);
    }
}
