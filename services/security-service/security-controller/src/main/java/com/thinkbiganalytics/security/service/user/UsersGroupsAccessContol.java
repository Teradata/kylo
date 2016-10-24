/**
 * 
 */
package com.thinkbiganalytics.security.service.user;

import com.thinkbiganalytics.security.action.Action;

/**
 *
 * @author Sean Felten
 */
public interface UsersGroupsAccessContol {

    static final Action USERS_GROUPS_SUPPORT = Action.create("accessUsersGroupsSupport",
                                                             "Access Users and Groups support",
                                                             "Allows access to user and group-related functions");

    static final Action ACCESS_USERS = USERS_GROUPS_SUPPORT.subAction("accessUsers",
                                                                      "Access Users",
                                                                      "Allows the ability to view existing users");
    static final Action ADMIN_USERS = ACCESS_USERS.subAction("adminUsers",
                                                             "Administer Users",
                                                             "Allows the ability to create and manage users");

    static final Action ACCESS_GROUPS = USERS_GROUPS_SUPPORT.subAction("accessGroups",
                                                                       "Access Groups",
                                                                       "Allows the ability to view existing groups");
    static final Action ADMIN_GROUPS = ACCESS_GROUPS.subAction("adminGroups",
                                                               "Administer Groups",
                                                               "Allows the ability to create and manage groups");
}
