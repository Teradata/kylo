package com.thinkbiganalytics.metadata.modeshape.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
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
        if (category != null) {
            // Delete user type
            final ExtensibleType type = extensibleTypeProvider.getType(getUserTypeName(category));
            extensibleTypeProvider.deleteType(type.getId());

            // Delete category
            super.delete(category);
        }
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getUserFields() {
        return JcrPropertyUtil.getUserFields("usr:category", extensibleTypeProvider);
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserFieldDescriptor> userFields) {
        JcrPropertyUtil.setUserFields("usr:category", userFields, extensibleTypeProvider);
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getFeedUserFields(@Nonnull final Category.ID categoryId) {
        final Category category = findById(categoryId);
        return JcrPropertyUtil.getUserFields(getUserTypeName(category), extensibleTypeProvider);
    }

    @Override
    public void setFeedUserFields(@Nonnull final Category.ID categoryId, @Nonnull final Set<UserFieldDescriptor> userFields) {
        final Category category = findById(categoryId);
        JcrPropertyUtil.setUserFields(getUserTypeName(category), userFields, extensibleTypeProvider);
    }

    /**
     * Gets the name of the type containing user fields for feeds within the specified category.
     *
     * @param category the category
     * @return the type name
     */
    @Nonnull
    private String getUserTypeName(@Nonnull final Category category) {
        return "usr:category:" + category.getName() + ":feed";
    }
}
