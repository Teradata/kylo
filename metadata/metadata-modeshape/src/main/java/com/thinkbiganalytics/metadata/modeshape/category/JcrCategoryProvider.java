package com.thinkbiganalytics.metadata.modeshape.category;

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
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A JCR provider for {@link Category} objects.
 */
public class JcrCategoryProvider extends BaseJcrProvider<Category, Category.ID> implements CategoryProvider<Category> {

    /** JCR node type manager */
    @Inject
    ExtensibleTypeProvider extensibleTypeProvider;

    /** Transaction support */
    @Inject
    JcrMetadataAccess metadataAccess;

    @Override
    public Category findBySystemName(String systemName) {

        String query = "SELECT * FROM [" + getNodeType() + "] as cat WHERE cat.[" + JcrCategory.SYSTEM_NAME + "] = '" + systemName + "'";
        return findFirst(query);
    }

    @Override
    public String getNodeType() {
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
        return findOrCreateEntity(path, systemName, props);
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
        metadataAccess.commit(new AdminCredentials(), () -> {
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
        });
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getUserFields() {
        return JcrPropertyUtil.getUserFields(ExtensionsConstants.USER_CATEGORY, extensibleTypeProvider);
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserFieldDescriptor> userFields) {
        metadataAccess.commit(new AdminCredentials(), () -> {
            JcrPropertyUtil.setUserFields(ExtensionsConstants.USER_CATEGORY, userFields, extensibleTypeProvider);
            return userFields;
        });
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getFeedUserFields(@Nonnull final Category.ID categoryId) {
        final Category category = findById(categoryId);
        return JcrPropertyUtil.getUserFields(ExtensionsConstants.getUserCategoryFeed(category.getName()), extensibleTypeProvider);
    }

    @Override
    public void setFeedUserFields(@Nonnull final Category.ID categoryId, @Nonnull final Set<UserFieldDescriptor> userFields) {
        metadataAccess.commit(new AdminCredentials(), () -> {
            final Category category = findById(categoryId);
            JcrPropertyUtil.setUserFields(ExtensionsConstants.getUserCategoryFeed(category.getName()), userFields, extensibleTypeProvider);
            return userFields;
        });
    }
}
