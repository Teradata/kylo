/**
 *
 */
package com.thinkbiganalytics.security.service.user;

/*-
 * #%L
 * thinkbig-security-controller
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
public interface UsersGroupsAccessContol {

    static final Action USERS_GROUPS_SUPPORT = Action.create("accessUsersGroupsSupport",
                                                             "Access Users and Groups Support",
                                                             "Allows access to user and group-related functions");

    static final Action ACCESS_USERS = USERS_GROUPS_SUPPORT.subAction("accessUsers",
                                                                      "Access Users",
                                                                      "Allows the ability to view existing users");

    static final Action ADMIN_USERS = ACCESS_USERS.subAction("adminUsers",
                                                             "Administer Users",
                                                             "Allows the ability to create, edit and delete users");

    static final Action ACCESS_GROUPS = USERS_GROUPS_SUPPORT.subAction("accessGroups",
                                                                       "Access Groups",
                                                                       "Allows the ability to view existing groups");

    static final Action ADMIN_GROUPS = ACCESS_GROUPS.subAction("adminGroups",
                                                               "Administer Groups",
                                                               "Allows the ability to create, edit and delete groups");
}
