/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

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
        return principal instanceof UsernamePrincipal ? Collections.singleton("ROLE_USER") : null;
    }

}
