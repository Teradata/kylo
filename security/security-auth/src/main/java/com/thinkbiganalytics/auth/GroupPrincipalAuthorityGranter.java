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

import org.springframework.security.authentication.jaas.AuthorityGranter;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * A granter that, when presented with a Group principal, returns a set containing the name of that principal (the group's name)
 * and another name constructed by prefixing "ROLE_" to the upper case principal name (Spring's default role name format.)
 * If the group contains member principals then it will add authorities for those as well; including, recursively, any of the 
 * member's memberships if they are groups themselves.
 */
public class GroupPrincipalAuthorityGranter implements AuthorityGranter {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AuthorityGranter#grant(java.security.Principal)
     */
    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof Group) {
            Set<String> authorities = new HashSet<>();
            
            addAuthorities(principal, authorities);
            return authorities;
        } else {
            return null;
        }
    }

    private void addAuthorities(Principal principal, Set<String> authorities) {
        String name = principal.getName();
        authorities.add(name);
        
        if (principal instanceof Group) {
            String springRole = name.toUpperCase().startsWith("ROLE_") ? name.toUpperCase() : "ROLE_" + name.toUpperCase();
            authorities.add(springRole);
            
            Enumeration<? extends Principal> members = ((Group) principal).members();
            while (members.hasMoreElements()) {
                addAuthorities(members.nextElement(), authorities);
            }
        }
    }
}
