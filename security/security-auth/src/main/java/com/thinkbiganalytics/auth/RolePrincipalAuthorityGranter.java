/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.security.Principal;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.google.common.collect.Sets;
import com.thinkbiganalytics.security.GroupPrincipal;

/**
 * A granter that, when presented with a GroupPrincipal, returns a set containing the name of that principal (the role's name)
 * and another name constructed by prefixing "ROLE_" to the upper case principal name (Spring's default role name format.)
 * @author Sean Felten
 */
public class RolePrincipalAuthorityGranter implements AuthorityGranter {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AuthorityGranter#grant(java.security.Principal)
     */
    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof GroupPrincipal) {
            String name = principal.getName();
            String springRole = name.toUpperCase().startsWith("ROLE_") ? name.toUpperCase() : "ROLE_" + name.toUpperCase();
            
            return Sets.newHashSet(name, springRole);
        } else {
            return null;
        }
    }

}
