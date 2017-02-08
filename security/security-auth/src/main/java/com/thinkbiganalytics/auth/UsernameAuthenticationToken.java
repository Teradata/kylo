/**
 *
 */
package com.thinkbiganalytics.auth;

/*-
 * #%L
 * kylo-security-auth
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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * A type of token representing only a username that must be validated.  This implementation
 * simply wraps a UsernamePasswordAuthenticationToken with a null password.
 */
public class UsernameAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3803881659484269062L;

    private UsernamePasswordAuthenticationToken wrappedToken;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>UsernameAuthenticationToken</code>, as the {@link #isAuthenticated()}
     * will return <code>false</code>.
     */
    public UsernameAuthenticationToken(Object principal) {
        super(null);
        this.wrappedToken = new UsernamePasswordAuthenticationToken(principal, null);
    }

    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or
     * <code>AuthenticationProvider</code> implementations that are satisfied with
     * producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     */
    public UsernameAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.wrappedToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getCredentials()
     */
    @Override
    public Object getCredentials() {
        return this.wrappedToken.getCredentials();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getPrincipal()
     */
    @Override
    public Object getPrincipal() {
        return this.wrappedToken.getPrincipal();
    }

    /**
     * @return the wrappedToken the wrapped UsernamePasswordAuthenticationToken
     */
    public UsernamePasswordAuthenticationToken getWrappedToken() {
        return wrappedToken;
    }

}
