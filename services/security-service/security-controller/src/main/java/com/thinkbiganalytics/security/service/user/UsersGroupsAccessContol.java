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

    static final Action USERS_GROUPS_SUPPORT = Action.create("accessUsersGroupsSupport");
    
    static final Action ACCESS_USERS = USERS_GROUPS_SUPPORT.subAction("accessUsers");
    static final Action ADMIN_USERS = ACCESS_USERS.subAction("adminUsers");
    
    static final Action ACCESS_GROUPS = USERS_GROUPS_SUPPORT.subAction("accessGroups");
    static final Action ADMIN_GROUPS = ACCESS_GROUPS.subAction("adminGroups");
}
