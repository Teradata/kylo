/**
 * 
 */
package com.thinkbiganalytics.metadata.api.security;

/*-
 * #%L
 * kylo-metadata-api
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

import java.security.Principal;
import java.util.Set;

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public interface RoleAssignments {

    Set<SecurityRole> getAssignedRoles();
    
    Set<Principal> getMembers(SecurityRole role);
    
    void addMember(SecurityRole role, UsernamePrincipal principal);
    
    void addMember(SecurityRole role, GroupPrincipal principal);
    
    void removeMember(SecurityRole role, UsernamePrincipal principal);
    
    void removeMember(SecurityRole role, GroupPrincipal principal);
}
