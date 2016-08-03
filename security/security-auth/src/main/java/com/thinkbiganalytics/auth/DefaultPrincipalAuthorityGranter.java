/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.security.Principal;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.google.common.collect.Sets;

/**
 * The default granter that will simply return a set containing the name of the principal.
 * @author Sean Felten
 */
public class DefaultPrincipalAuthorityGranter implements AuthorityGranter {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AuthorityGranter#grant(java.security.Principal)
     */
    @Override
    public Set<String> grant(Principal principal) {
        return Sets.newHashSet(principal.getName());
    }

}
