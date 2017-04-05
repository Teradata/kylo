/**
 * 
 */
package com.thinkbiganalytics.security.role;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.thinkbiganalytics.security.action.Action;

/*-
 * #%L
 * kylo-security-api
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

/**
 * Used to manage security roles for entities.
 */
public interface SecurityRoleProvider {

    SecurityRole createRole(String entityName, String roleName, String title, String descr);
    
    Map<String, List<SecurityRole>> getRoles();
    
    List<SecurityRole> getEntityRoles(String entityName);
    
    Optional<SecurityRole> getRole(String entityName, String roleName);
    
    boolean removeRole(String entityName, String roleName);
    
    Optional<SecurityRole> setPermissions(String entityName, String roleName, Action... actions);
    
    Optional<SecurityRole> setPermissions(String entityName, String roleName, Collection<Action> actions);
}
