/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.util.Optional;

/**
 *
 * @author Sean Felten
 */
public interface AllowedModuleActionsProvider {
    
    /**
     * Retrieves all of the available actions organized under the given module name.
     * @param moduleName the name of the module
     * @return an optional of allowed actions if they exist for the given group name
     */
    Optional<AllowedActions> getAvailableActions(String moduleName);

    /**
     * Retrieves all of the actions allowed by the current user organized under the given module name.
     * @param moduleName the name of the module
     * @return an optional of allowed actions if they exist for the given group name
     */
    Optional<AllowedActions> getAllowedActions(String moduleName);
    
    /**
     * This is a convenience method to check whether the current user has permission to perform
     * the specified action for the module name.  It is equivalent to retrieving the allowed
     * actions for the module and then performing a permission check on the given action.
     * 
     * @param moduleName the name of the module
     * @param action
     */
    void checkPermission(String moduleName, Action action);
}
