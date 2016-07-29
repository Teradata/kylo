/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.security.Principal;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.google.common.collect.Sets;

/**
 * A granter that, when presented with a RolePrincipal, returns a set containing the name of that principal (the role's name.)
 * @author Sean Felten
 */
public class RolePrincipalAuthorityGranter implements AuthorityGranter {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AuthorityGranter#grant(java.security.Principal)
     */
    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof RolePrincipal) {
            String name = principal.getName();
            String springRole = name.toUpperCase().startsWith("ROLE_") ? name.toUpperCase() : "ROLE_" + name.toUpperCase();
            
            return Sets.newHashSet(name, springRole);
        } else {
            return null;
        }
    }

}
