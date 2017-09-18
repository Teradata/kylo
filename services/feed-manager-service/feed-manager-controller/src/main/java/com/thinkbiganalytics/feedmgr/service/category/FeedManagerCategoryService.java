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
import com.thinkbiganalytics.security.action.Action;

import java.security.AccessControlException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Manages Feed Manager categories.
 */
public interface FeedManagerCategoryService {

    /**
     * Checks the current security context has been granted permission to perform the specified action(s)
     * on the category with the specified feed ID.  If the category does not exist then no check is made.
     *
     * @param id     the category ID
     * @param action an action to check
     * @param more   any additional actions to check
     * @return true if the category existed, otherwise false
     * @throws AccessControlException thrown if the category exists and the action(s) checked are not permitted
     */
    boolean checkCategoryPermission(String id, Action action, Action... more);

    Collection<FeedCategory> getCategories();

    Collection<FeedCategory> getCategories(boolean includeFeedDetails);

    FeedCategory getCategoryById(String id);

    FeedCategory getCategoryBySystemName(String name);

    void saveCategory(FeedCategory category);

    boolean deleteCategory(String categoryId) throws InvalidOperationException;

    /**
     * Gets the user-defined fields for all categories.
     *
     * @return the user-defined fields
     */
    @Nonnull
    Set<UserField> getUserFields();

    /**
     * Sets the user-defined fields for all categories.
     *
     * @param userFields the new set of user-defined fields
     */
    void setUserFields(@Nonnull Set<UserField> userFields);

    /**
     * Gets the user-defined fields for all categories.
     *
     * @return the user-defined fields
     */
    @Nonnull
    Set<UserProperty> getUserProperties();
}
