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

    // TODO are there other levels of access besides the ability to view or administer operational
    // functions?
    public static final Action ACCESS_OPS = Action.create("accessOperations",
                                                          "Access Operational information",
                                                          "Allows access to operational information like active feeds and execution history, etc.");
    public static final Action ADMIN_OPS = ACCESS_OPS.subAction("adminOperations",
                                                                "Administer Operations",
                                                                "Allows administration of operations, such as stopping and abandoning them.");
}
