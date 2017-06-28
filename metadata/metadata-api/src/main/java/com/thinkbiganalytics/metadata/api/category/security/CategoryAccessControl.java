/**
 *
 */
package com.thinkbiganalytics.metadata.api.category.security;

/*-
 * #%L
 * kylo-metadata-api
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

import com.thinkbiganalytics.security.action.Action;

/**
 *
 */
public interface CategoryAccessControl {

    Action ACCESS_CATEGORY = Action.create("accessCategory",
                                           "Access Category",
                                           "Allows the ability to view the category and see basic summary information about it");
    Action EDIT_SUMMARY = ACCESS_CATEGORY.subAction("editCategorySummary",
                                                    "Edit Summary",
                                                    "Allows editing of the summary information about the category");
    Action ACCESS_DETAILS = ACCESS_CATEGORY.subAction("accessCategoryDetails",
                                                      "Access Details",
                                                      "Allows viewing the full details about the category");
    Action EDIT_DETAILS = ACCESS_DETAILS.subAction("editCategoryDetails",
                                                   "Edit Details",
                                                   "Allows editing of the details about the category");
    Action DELETE = ACCESS_DETAILS.subAction("deleteCategory",
                                             "Delete",
                                             "Allows deleting the category");
    Action CREATE_FEED = ACCESS_CATEGORY.subAction("createFeedUnderCategory",
                                                   "Create Feed under Category",
                                                   "Allows creating feeds under this category");
    Action CHANGE_PERMS = ACCESS_CATEGORY.subAction("changeCategoryPermissions",
                                                    "Change Permissions",
                                                    "Allows editing of the permissions that grant access to the category");

}
