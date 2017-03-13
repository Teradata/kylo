/**
 * 
 */
package com.thinkbiganalytics.metadata.api.category.security;

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
   Action EXPORT = ACCESS_CATEGORY.subAction("exportCategory",
                                             "Export",
                                             "Allows exporting the category");
   Action CREATE_FEED = ACCESS_CATEGORY.subAction("createFeed",
                                            "Create Feed",
                                            "Allows creating feeds under this category");
   Action CHANGE_PERMS = ACCESS_CATEGORY.subAction("changeCategoryPermissions",
                                                   "Change Permissions",
                                                   "Allows editing of the permissions that grant access to the category");

}
