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
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * A JCR provider for {@link Category} objects.
 */
public class JcrCategoryProvider extends BaseJcrProvider<Category, Category.ID> implements CategoryProvider {

    /**
     * JCR node type manager
     */
    @Inject
    ExtensibleTypeProvider extensibleTypeProvider;
    
    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;

    /**
     * Transaction support
     */
    @Inject
    MetadataAccess metadataAccess;

    @Override
    public Category findBySystemName(String systemName) {
        final String query = "SELECT * FROM [" + getNodeType(getJcrEntityClass()) + "] as cat WHERE cat.[" + JcrCategory.SYSTEM_NAME + "] = $systemName";
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
        boolean isNew = ! hasEntityNode(path, systemName);
        JcrCategory category = (JcrCategory) findOrCreateEntity(path, systemName, props);
        
        if (isNew) {
            this.actionsProvider.getAvailableActions(AllowedActions.CATEGORY) 
                    .ifPresent(actions -> category.setupAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser()));
        }
        
        return category;
    }

    @Override
    public Category.ID resolveId(Serializable fid) {
        return new JcrCategory.CategoryId(fid);
    }

    @Override
    public void delete(final Category category) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteById(final Category.ID id) {
        // TODO service?
        metadataAccess.commit(() -> {
            // Get category
            final Category category = findById(id);

            if (category != null) {
                // Delete user type
                final ExtensibleType type = extensibleTypeProvider.getType(ExtensionsConstants.getUserCategoryFeed(category.getName()));
                if (type != null) {
                    extensibleTypeProvider.deleteType(type.getId());
                }

                // Delete category
                super.delete(category);
            }
            return true;
        }, MetadataAccess.SERVICE);
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getUserFields() {
        return JcrPropertyUtil.getUserFields(ExtensionsConstants.USER_CATEGORY, extensibleTypeProvider);
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserFieldDescriptor> userFields) {
        metadataAccess.commit(() -> {
            JcrPropertyUtil.setUserFields(ExtensionsConstants.USER_CATEGORY, userFields, extensibleTypeProvider);
            return userFields;
        }, MetadataAccess.SERVICE);
    }

    @Nonnull
    @Override
    public Optional<Set<UserFieldDescriptor>> getFeedUserFields(@Nonnull final Category.ID categoryId) {
        return Optional.ofNullable(findById(categoryId))
            .map(category -> JcrPropertyUtil.getUserFields(ExtensionsConstants.getUserCategoryFeed(category.getName()), extensibleTypeProvider));
    }

    @Override
    public void setFeedUserFields(@Nonnull final Category.ID categoryId, @Nonnull final Set<UserFieldDescriptor> userFields) {
        metadataAccess.commit(() -> {
            final Category category = findById(categoryId);
            JcrPropertyUtil.setUserFields(ExtensionsConstants.getUserCategoryFeed(category.getName()), userFields, extensibleTypeProvider);
            return userFields;
        }, MetadataAccess.SERVICE);
    }

    @Override
    public void rename(@Nonnull final Category.ID categoryId, @Nonnull final String newName) {
        // Move the node to the new path
        final JcrCategory category = (JcrCategory) findById(categoryId);
        final Node node = category.getNode();

        try {
            final String newPath = JcrUtil.path(node.getParent().getPath(), newName).toString();
            JcrMetadataAccess.getActiveSession().move(node.getPath(), newPath);
        } catch (final RepositoryException e) {
            throw new IllegalStateException("Unable to rename category: " + node, e);
        }

        // Update properties
        category.setProperty(JcrCategory.SYSTEM_NAME, newName);

        // Move user fields
        final Optional<Set<UserFieldDescriptor>> feedUserFields = getFeedUserFields(category.getId());

        if (feedUserFields.isPresent()) {
            final ExtensibleType type = extensibleTypeProvider.getType(ExtensionsConstants.getUserCategoryFeed(category.getName()));
            if (type != null) {
                extensibleTypeProvider.deleteType(type.getId());
            }

            setFeedUserFields(category.getId(), feedUserFields.get());
        }
    }
}
