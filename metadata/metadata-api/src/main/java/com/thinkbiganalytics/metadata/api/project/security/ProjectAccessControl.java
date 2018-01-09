/**
 *
 */
package com.thinkbiganalytics.metadata.api.project.security;

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
public interface ProjectAccessControl {

    Action ACCESS_PROJECT = Action.create("accessProject",
                                           "Access Project",
                                           "Allows the ability to view the project and see basic summary information about it");
    Action EDIT_PROJECT = ACCESS_PROJECT.subAction("editProject",
                                                     "Edit Project",
                                                     "Allows editing the full details about the project");
    Action DELETE_PROJECT = ACCESS_PROJECT.subAction("deleteProject",
                                              "Delete Project",
                                              "Allows deleting the project");

    Action CHANGE_PERMS = ACCESS_PROJECT.subAction("changeProjectPermissions",
                                                    "Change Permissions",
                                                    "Allows editing of the permissions that grant access to the project");

    String ROLE_EDITOR = "editor";
    String ROLE_READER = "readOnly";
    String ROLE_ADMIN = "admin";

}
