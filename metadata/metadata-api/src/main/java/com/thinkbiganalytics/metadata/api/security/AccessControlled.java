/**
 *
 */
package com.thinkbiganalytics.metadata.api.security;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * Defines the ability to access an entity.  Top-level entities may implement this interface to advertise
 * the possible permission that may be applied to it and to apply those permissions.
 */
public interface AccessControlled {
    
    Set<RoleMembership> getRoleMemberships();
    
    Optional<RoleMembership> getRoleMembership(String roleName);
    
    Set<RoleMembership> getInheritedRoleMemberships();
    
    Optional<RoleMembership> getInheritedRoleMembership(String roleName);

    AllowedActions getAllowedActions();

    Principal getOwner();


}
