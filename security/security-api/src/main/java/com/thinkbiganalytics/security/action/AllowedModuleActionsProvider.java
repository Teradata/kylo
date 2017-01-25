/**
 * 
 */
package com.thinkbiganalytics.security.action;

/*-
 * #%L
 * thinkbig-security-api
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
