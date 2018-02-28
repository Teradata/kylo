package com.thinkbiganalytics.metadata.modeshape.category;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.UserFieldDescriptors;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * A JCR provider for {@link Category} objects.
 */
public class JcrCategoryProvider extends BaseJcrProvider<Category, Category.ID> implements CategoryProvider {
    
    @Inject
    private SecurityRoleProvider roleProvider;

    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;

    @Inject
    private AccessController accessController;

    @Inject
    private FeedOpsAccessControlProvider opsAccessProvider;

    /**
     * Transaction support
     */
    @Inject
    MetadataAccess metadataAccess;

    @Override
    protected <T extends JcrObject> T constructEntity(Node node, Class<T> entityClass) {
        return JcrUtil.createJcrObject(node, entityClass, this.opsAccessProvider);
    }


    @Override
    protected String getEntityQueryStartingPath() {
        return  EntityUtil.pathForCategory();
    }

    @Override
    public Category update(Category category) {
        if (accessController.isEntityAccessControlled()) {
            category.getAllowedActions().checkPermission(CategoryAccessControl.EDIT_DETAILS);
        }
        return super.update(category);
    }

    @Override
    public Category findBySystemName(String systemName) {
        String query = "SELECT * FROM [" + getNodeType(getJcrEntityClass()) + "] as cat WHERE cat.[" + JcrCategory.SYSTEM_NAME + "] = $systemName ";
        query = applyFindAllFilter(query, EntityUtil.pathForCategory());
        return JcrQueryUtil.findFirst(getSession(), query, ImmutableMap.of("systemName", systemName), getEntityClass());
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        return JcrCategory.NODE_TYPE;
    }

    @Override
    public Class<? extends Category> getEntityClass() {
        return JcrCategory.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrCategory.class;
    }

    @Override
    public Category ensureCategory(String systemName) {
        String path = EntityUtil.pathForCategory();
        Map<String, Object> props = new HashMap<>();
        props.put(JcrCategory.SYSTEM_NAME, systemName);
        boolean isNew = !hasEntityNode(path, systemName);
        JcrCategory category = (JcrCategory) findOrCreateEntity(path, systemName, props);

        if (isNew) {
            if (this.accessController.isEntityAccessControlled()) {
                List<SecurityRole> catRoles = this.roleProvider.getEntityRoles(SecurityRole.CATEGORY);
                List<SecurityRole> feedRoles = this.roleProvider.getEntityRoles(SecurityRole.FEED);

                this.actionsProvider.getAvailableActions(AllowedActions.CATEGORY)
                    .ifPresent(actions -> category.enableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), catRoles, feedRoles));
            } else {
                this.actionsProvider.getAvailableActions(AllowedActions.CATEGORY)
                    .ifPresent(actions -> category.disableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser()));
            }
        }

        return category;
    }

    @Override
    public Category.ID resolveId(Serializable fid) {
        return new JcrCategory.CategoryId(fid);
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getUserFields() {
        UserFieldDescriptors descriptors = JcrUtil.getJcrObject(JcrUtil.getNode(getSession(), EntityUtil.pathForGlobalCategoryUserFields()),
                                                                UserFieldDescriptors.class);
        return descriptors != null ? descriptors.getFields() : Collections.emptySet();
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserFieldDescriptor> userFields) {
        UserFieldDescriptors descriptors = JcrUtil.getOrCreateNode(JcrUtil.getNode(getSession(), EntityUtil.pathForCategory()),
                                                                   EntityUtil.CATEGORY_USER_FIELDS,
                                                                   UserFieldDescriptors.NODE_TYPE, 
                                                                   UserFieldDescriptors.class);
        descriptors.setFields(userFields);
    }

    @Nonnull
    @Override
    public Optional<Set<UserFieldDescriptor>> getFeedUserFields(@Nonnull final Category.ID categoryId) {
        JcrCategory category = (JcrCategory) findById(categoryId);
        
        if (category != null) {
            return category.getDetails().map(details ->  getFeedUserFields(details));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setFeedUserFields(@Nonnull final Category.ID categoryId, @Nonnull final Set<UserFieldDescriptor> userFields) {
        final JcrCategory category = (JcrCategory) findById(categoryId);
        
        if (category != null) {
            setUserFieldDesriptors(category, userFields);
        } else {
            throw new CategoryNotFoundException(categoryId);
        }
    }
    
    @Override
    public void renameSystemName(@Nonnull final Category.ID categoryId, @Nonnull final String newSystemName) {
        // Move the node to the new path
        JcrCategory category = (JcrCategory) findById(categoryId);
        final Node node = category.getNode();

        // Update properties
        category.setSystemName(newSystemName);

        try {
            final String newPath = JcrUtil.path(node.getParent().getPath(), newSystemName).toString();
            JcrMetadataAccess.getActiveSession().move(node.getPath(), newPath);
        } catch (final RepositoryException e) {
            throw new IllegalStateException("Unable to rename system name for category: " + node, e);
        }
    }

    private Set<UserFieldDescriptor> getFeedUserFields(CategoryDetails details) {
        UserFieldDescriptors descriptors = JcrUtil.getJcrObject(details.getNode(), CategoryDetails.FEED_USER_FIELDS, UserFieldDescriptors.class);
        return descriptors != null ? descriptors.getFields() : Collections.emptySet();
    }
    
    private void setUserFieldDesriptors(JcrCategory category, Set<UserFieldDescriptor> fieldDescrs) {
        if (category.getDetails().isPresent()) {
            UserFieldDescriptors descriptors = JcrUtil.getOrCreateNode(category.getDetails().get().getNode(), 
                                                                       CategoryDetails.FEED_USER_FIELDS, 
                                                                       UserFieldDescriptors.NODE_TYPE, 
                                                                       UserFieldDescriptors.class);
            descriptors.setFields(fieldDescrs);
        } else {
            throw new AccessControlException("Permission denied adding feed fields to category: " + category.getTitle());
        }
    }
}
