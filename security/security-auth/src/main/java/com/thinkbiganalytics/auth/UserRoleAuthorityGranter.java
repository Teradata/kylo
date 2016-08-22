/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.security.Principal;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.google.common.collect.Sets;
import com.thinkbiganalytics.security.UsernamePrincipal;

/**
 * A granter that, when presented with a UsernamePrincipal, returns a set containing the "ROLE_USER" role name.
 * @author Sean Felten
 */
public class UserRoleAuthorityGranter implements AuthorityGranter {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AuthorityGranter#grant(java.security.Principal)
     */
    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof UsernamePrincipal) {
            String name = principal.getName();
            String springRole = name.toUpperCase().startsWith("ROLE_") ? name.toUpperCase() : "ROLE_" + name.toUpperCase();
            
            return Sets.newHashSet(name, springRole);
        } else {
            return null;
        }
    }

}
