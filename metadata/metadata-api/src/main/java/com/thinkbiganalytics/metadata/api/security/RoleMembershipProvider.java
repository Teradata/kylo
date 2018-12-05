/**
 * 
 */
package com.thinkbiganalytics.metadata.api.security;

import com.thinkbiganalytics.security.role.RoleMembership;

/*-
 * #%L
 * kylo-metadata-api
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.security.role.SecurityRole;

import java.util.Set;

/**
 * Provides access to role memberships of all entities.
 */
public interface RoleMembershipProvider {

    /**
     * @return the set of all role memberships of all entities
     */
    Set<RoleMembership> findAll();
    
    /**
     * @param role the role associated with the memberships
     * @return the set of all role memberships of all entities associates with the role
     */
    Set<RoleMembership> findForRole(SecurityRole role);
}
