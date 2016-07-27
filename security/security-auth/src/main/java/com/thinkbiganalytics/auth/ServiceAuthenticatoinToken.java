/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.util.Arrays;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;

/**
 * An authentication representing a general service account token.  Used internally by service threads.
 * @author Sean Felten
 */
public class ServiceAuthenticatoinToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;
    
    private static final UsernamePrincipal USER = new UsernamePrincipal("service");
    
    public ServiceAuthenticatoinToken() {
        super(Arrays.asList(new JaasGrantedAuthority("ROLE_SERVICE", new ServiceRolePrincipal())));
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getCredentials()
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getPrincipal()
     */
    @Override
    public Object getPrincipal() {
        return USER;
    }

}
