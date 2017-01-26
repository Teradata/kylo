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

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A JCR provider for {@link FeedManagerCategory} objects.
 */
public class JcrFeedManagerCategoryProvider extends BaseJcrProvider<FeedManagerCategory, Category.ID> implements FeedManagerCategoryProvider {

    @Inject
    private CategoryProvider categoryProvider;

    @Override
    public Class<? extends FeedManagerCategory> getEntityClass() {
        return JcrFeedManagerCategory.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {

        return JcrFeedManagerCategory.class;
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        return ((JcrCategoryProvider) categoryProvider).getNodeType(jcrEntityType);
    }

    @Override
    public FeedManagerCategory findBySystemName(String systemName) {
        JcrCategory c = (JcrCategory) categoryProvider.findBySystemName(systemName);
        if (c != null) {
            return new JcrFeedManagerCategory(c);
        }
        return null;
    }

    @Override
    public FeedManagerCategory ensureCategory(String systemName) {
        JcrCategory c = (JcrCategory) categoryProvider.ensureCategory(systemName);
        if (c != null) {
            return new JcrFeedManagerCategory(c);
        }
        return null;
    }

    public Category.ID resolveId(Serializable fid) {
        return new JcrCategory.CategoryId(fid);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void delete(FeedManagerCategory feedManagerCategory) {
        categoryProvider.delete(feedManagerCategory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deleteById(Category.ID id) {
        categoryProvider.deleteById(id);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Set<UserFieldDescriptor> getUserFields() {
        return categoryProvider.getUserFields();
    }

    @Override
    public void setUserFields(@Nonnull Set<UserFieldDescriptor> userFields) {
        categoryProvider.setUserFields(userFields);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Optional<Set<UserFieldDescriptor>> getFeedUserFields(@Nonnull Category.ID categoryId) {
        return categoryProvider.getFeedUserFields(categoryId);
    }

    @Override
    public void setFeedUserFields(@Nonnull Category.ID categoryId, @Nonnull Set<UserFieldDescriptor> userFields) {
        categoryProvider.setFeedUserFields(categoryId, userFields);
    }

    @Override
    public void rename(@Nonnull Category.ID categoryId, @Nonnull String newName) {
        categoryProvider.rename(categoryId, newName);
    }
}
