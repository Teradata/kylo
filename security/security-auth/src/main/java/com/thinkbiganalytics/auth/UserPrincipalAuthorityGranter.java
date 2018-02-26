/**
 *
 */
package com.thinkbiganalytics.auth;

/*-
 * #%L
 * thinkbig-security-auth
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

import com.google.common.collect.Sets;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import java.security.Principal;
import java.util.Set;

/**
 * A granter that, when presented with a UsernamePrincipal, returns a set containing its name and the "ROLE_USER" role name.
 * <p>
 * Previously, this provider would also constructed an authority with the name "ROLE_USER" (Spring's default role name format) for the user.  
 * This was to support spring's role-based access control such as with annotations.  But since we do not use spring annotations for access 
 * control these are not needed.
 */
public class UserPrincipalAuthorityGranter implements AuthorityGranter {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AuthorityGranter#grant(java.security.Principal)
     */
    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof UsernamePrincipal) {
            String name = principal.getName();

            // If it is ever decided to use spring's role-based access control (unlikely) then we can use the code below instead.
//            return Sets.newHashSet(name, "ROLE_USER");
            return Sets.newHashSet(name);
        } else {
            return null;
        }
    }

}
