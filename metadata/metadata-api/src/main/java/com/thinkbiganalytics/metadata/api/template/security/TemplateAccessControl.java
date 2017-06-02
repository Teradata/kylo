/**
 *
 */
package com.thinkbiganalytics.metadata.api.template.security;

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
    Action CREATE_FEED = ACCESS_TEMPLATE.subAction("createFeedFromTemplate",
                                                   "Create Feed",
                                                   "Allows creating feeds under this template");
    Action CHANGE_PERMS = ACCESS_TEMPLATE.subAction("changeTemplatePermissions",
                                                    "Change Permissions",
                                                    "Allows editing of the permissions that grant access to the template");

}
