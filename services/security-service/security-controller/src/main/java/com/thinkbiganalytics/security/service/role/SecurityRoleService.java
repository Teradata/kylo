/**
 * 
 */
package com.thinkbiganalytics.security.service.role;

/*-
 * #%L
 * kylo-security-controller
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public interface SecurityRoleService {

    Optional<SecurityRole> getEntityRole(String entityName, String roleName);
    
    List<SecurityRole> getEntityRoles(String entityName);
    
    Map<String, List<SecurityRole>> getAllEntityRoles();
    
    SecurityRole createRole(String entityName, String systemName, String title, String descr);
    
    
}
