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
    
    Optional<AllowedActions> getAvailavleActions(String moduleName);

    Optional<AllowedActions> getAllowedActions(String moduleName);
    
    void checkPermission(String moduleName, AllowableAction action);
}
