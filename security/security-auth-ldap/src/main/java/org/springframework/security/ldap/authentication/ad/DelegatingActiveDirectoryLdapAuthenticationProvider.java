/**
 * 
 */
package org.springframework.security.ldap.authentication.ad;

import java.util.Collection;
import java.util.Collections;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;

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
