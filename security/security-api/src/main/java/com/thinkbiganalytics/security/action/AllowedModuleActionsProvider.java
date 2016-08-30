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
     * @return an optional set of allowed actions if they exist for the given group name
     */
    Optional<AllowedActions> getAvailableActions(String moduleName);

    /**
     * Retrieves all of the actions allowed by the current user which are organized under the given module name.
     * @param moduleName the name of the module
     * @return an optional set of allowed actions if they exist for the given group name
     */
    Optional<AllowedActions> getAllowedActions(String moduleName);
    
    /**
     * This is a convenience method to check whether the current user has permission to perform
     * the specified action for the module name.
     * @param moduleName the name of the module
     * @param action
     */
    void checkPermission(String moduleName, AllowableAction action);
}
