/**
 * 
 */
package com.thinkbiganalytics.security;

import java.util.Set;

import com.thinkbiganalytics.security.action.Action;

/**
 *
 * @author Sean Felten
 */
public interface AccessController {
    
    /** The default services module name */
    static final String SERVICES = "services";

    /**
     * Checks whether permission is granted to perform the given action(s) defined in the named module.
     * @param moduleName the module name
     * @param action the action being checked
     * @param others additional actions that are being checked
     */
    void checkPermission(String moduleName, Action action, Action... others);
    
    /**
     * Checks whether permission is granted to perform the given actions defined in the named module.
     * @param moduleName the module name
     * @param actions the actions being checked
     */
    void checkPermission(String moduleName, Set<Action> actions);
}
