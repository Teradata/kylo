/**
 *
 */
package com.thinkbiganalytics.security;

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

import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.util.Set;

/**
 * Defines the component responsible for making access control checks.  Access checks
 * should be inserted into places before attempting a particular action.  Each access check
 * is made against the context of the current authenticated user executing the code.
 */
public interface AccessController {

    /**
     * The default services module name
     */
    String SERVICES = AllowedActions.SERVICES;

    /**
     * Checks whether permission is granted to perform the given action(s) defined in the named module.
     *
     * @param moduleName the module name
     * @param action     the action being checked
     * @param others     additional actions that are being checked
     */
    void checkPermission(String moduleName, Action action, Action... others);

    /**
     * Checks whether permission is granted to perform the given actions defined in the named module.
     *
     * @param moduleName the module name
     * @param actions    the actions being checked
     */
    void checkPermission(String moduleName, Set<Action> actions);

    /**
     * Check to see if the user has an service permission for a given module
     * @param moduleName the service module to check
     * @param action the permission to check
     * @param others additional permissions
     * @return true if valid, false if not
     */
    boolean hasPermission(String moduleName, Action action, Action... others);


    /**
     * Returns true if setup to use Entity access control, false if not
     * @return true if using entity access control, false if not
     */
    boolean isEntityAccessControlled();
}
