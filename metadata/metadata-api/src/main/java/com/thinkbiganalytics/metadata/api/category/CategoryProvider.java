package com.thinkbiganalytics.metadata.api.category;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Manages the repository of category objects.
 *
 * @param <Category> the type of provided category
 */
public interface CategoryProvider extends BaseProvider<Category, Category.ID> {

    Category findBySystemName(String systemName);

    Category ensureCategory(String systemName);

    /**
     * Gets the user fields for all categories.
     *
     * @return user field descriptors
     * @since 0.4.0
     */
    @Nonnull
    Set<UserFieldDescriptor> getUserFields();

    /**
     * Sets the user fields for all categories.
     *
     * @param userFields user field descriptors
     * @since 0.4.0
     */
    void setUserFields(@Nonnull Set<UserFieldDescriptor> userFields);

    /**
     * Gets the user fields for all feeds within the specified category.
     *
     * @param categoryId the category id
     * @return user field descriptors, if the category exists
     * @since 0.4.0
     */
    @Nonnull
    Optional<Set<UserFieldDescriptor>> getFeedUserFields(@Nonnull Category.ID categoryId);

    /**
     * Sets the user fields for all feeds within the specified category.
     *
     * @param categoryId the category id
     * @param userFields user field descriptors
     * @since 0.4.0
     */
    void setFeedUserFields(@Nonnull Category.ID categoryId, @Nonnull Set<UserFieldDescriptor> userFields);

    /**
     * Renames system name for the specified category.
     *
     * @param categoryId the category id
     * @param newSystemName    the new system name
     */
    void renameSystemName(@Nonnull Category.ID categoryId, @Nonnull String newSystemName);
}
