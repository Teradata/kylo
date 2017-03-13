/**
 * 
 */
package com.thinkbiganalytics.metadata.api.template.security;

import com.thinkbiganalytics.security.action.Action;

/**
 *
 */
public interface TemplateAccessControl {
    
    Action ACCESS_TEMPLATE = Action.create("accessTemplate",
                                           "Access Template",
                                           "Allows the ability to view the template and see basic summary information about it");
    Action EDIT_TEMPLATE = ACCESS_TEMPLATE.subAction("editTemplate",
                                                  "Edit Template",
                                                  "Allows editing the full details about the template");
    Action DELETE = ACCESS_TEMPLATE.subAction("deleteTemplate",
                                              "Delete",
                                              "Allows deleting the template");
    Action EXPORT = ACCESS_TEMPLATE.subAction("exportTemplate",
                                              "Export",
                                              "Allows exporting the template");
    Action CREATE_FEED = ACCESS_TEMPLATE.subAction("createTemplate",
                                                   "Create Template",
                                                   "Allows creating feeds under this template");
    Action CHANGE_PERMS = ACCESS_TEMPLATE.subAction("changeTemplatePermissions",
                                                    "Change Permissions",
                                                    "Allows editing of the permissions that grant access to the template");

}
