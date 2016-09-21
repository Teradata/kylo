/**
 * 
 */
package com.thinkbiganalytics.jobrepo.security;

import com.thinkbiganalytics.security.action.Action;

/**
 * Describes the permissible actions related to operational services (feed processing management, 
 * job execution history, etc.)
 * 
 * @author Sean Felten
 */
public interface OperationsAccessControl {

    // TODO are there other levels of access besides the ability to view or administer operational functions?
    public static final Action ACCESS_OPS = Action.create("accessOperations");
    public static final Action ADMIN_OPS = ACCESS_OPS.subAction("adminOperations");
}
