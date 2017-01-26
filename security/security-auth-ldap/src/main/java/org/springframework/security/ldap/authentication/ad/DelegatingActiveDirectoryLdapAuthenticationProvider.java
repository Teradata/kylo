/**
 * 
 */
package org.springframework.security.ldap.authentication.ad;

/*-
 * #%L
 * thinkbig-security-auth-ldap
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

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * A decorator provider around a ActiveDirectoryLdapAuthenticationProvider because the latter hads been declared
 * final.  Allows disabling/enabling the loading of user group membership info after successful authentication.
 * 
 * @author Sean Felten
 */
public class DelegatingActiveDirectoryLdapAuthenticationProvider extends AbstractLdapAuthenticationProvider {
    
    private final ActiveDirectoryLdapAuthenticationProvider delegate;
    private final boolean groupsEnabled;
    
    public DelegatingActiveDirectoryLdapAuthenticationProvider(ActiveDirectoryLdapAuthenticationProvider delegate) {
        this(delegate, false);
    }
    
    public DelegatingActiveDirectoryLdapAuthenticationProvider(ActiveDirectoryLdapAuthenticationProvider delegate, boolean groupsEnabled) {
        super();
        this.delegate = delegate;
        this.groupsEnabled = groupsEnabled;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider#doAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken)
     */
    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        return this.delegate.doAuthentication(auth);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider#loadUserAuthorities(org.springframework.ldap.core.DirContextOperations, java.lang.String, java.lang.String)
     */
    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
        if (this.groupsEnabled) {
            return this.delegate.loadUserAuthorities(userData, username, password);
        } else {
            return Collections.emptyList();
        }
    }

}
