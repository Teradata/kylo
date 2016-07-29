/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.auth;

import java.security.Principal;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.google.common.collect.Sets;

/**
 * A granter that, when presented with a UsernamePrincipal, returns a set containing the "ROLE_USER" role name.
 * @author Sean Felten
 */
public class ModeShapeAuthorityGranter implements AuthorityGranter {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AuthorityGranter#grant(java.security.Principal)
     */
    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof ModeShapePrincipal) {
            return Sets.newHashSet(principal.getName());
        } else {
            return null;
        }
    }

}
