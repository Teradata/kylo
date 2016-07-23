/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

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
        return principal instanceof RolePrincipal ? Collections.singleton(principal.getName()) : null;
    }

}
