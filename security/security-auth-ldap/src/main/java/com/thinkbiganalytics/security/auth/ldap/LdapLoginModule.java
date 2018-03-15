/**
 *
 */
package com.thinkbiganalytics.security.auth.ldap;

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

import com.thinkbiganalytics.auth.jaas.AbstractLoginModule;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.CredentialException;

/**
 *
 */
public class LdapLoginModule extends AbstractLoginModule {

    /**
     * Option for the {@link LdapAuthenticator} used to authenticate via LDAP
     */
    public static final String AUTHENTICATOR = "authenticator";
    /**
     * Option for the {@link LdapAuthoritiesPopulator} used to retrieve any groups associated with the authenticated user
     */
    public static final String AUTHORITIES_POPULATOR = "authoritiesPopulator";
    private static final Logger log = LoggerFactory.getLogger(LdapLoginModule.class);
    private LdapAuthenticator authenticator;
    private LdapAuthoritiesPopulator authoritiesPopulator;


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        this.authenticator = (LdapAuthenticator) getOption(AUTHENTICATOR).orElseThrow(() -> new IllegalArgumentException("The \"" + AUTHENTICATOR + "\" option is required"));
        this.authoritiesPopulator = (LdapAuthoritiesPopulator) getOption(AUTHORITIES_POPULATOR).orElse(null);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doLogin()
     */
    @Override
    protected boolean doLogin() throws Exception {
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);

        handle(nameCallback, passwordCallback);

        if (nameCallback.getName() == null) {
            throw new AccountException("No username provided for authentication");
        }

        Principal userPrincipal = new UsernamePrincipal(nameCallback.getName());
        String password = new String(passwordCallback.getPassword());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrincipal, password);

        try {
            log.debug("Authenticating: {}", userPrincipal);
            DirContextOperations dirContext = this.authenticator.authenticate(authentication);
            log.debug("Successfully Authenticated: {}", userPrincipal);

            setUserPrincipal(userPrincipal);

            for (GrantedAuthority grant : this.authoritiesPopulator.getGrantedAuthorities(dirContext, nameCallback.getName())) {
                String groupName = grant.getAuthority();

                log.debug("Found group for {}: {}", userPrincipal, groupName);

                if (groupName != null) {
                    addNewGroupPrincipal(groupName);
                }
            }

            return true;
        } catch (BadCredentialsException e) {
            throw new CredentialException(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doCommit()
     */
    @Override
    protected boolean doCommit() throws Exception {
        getSubject().getPrincipals().addAll(getPrincipals());
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doAbort()
     */
    @Override
    protected boolean doAbort() throws Exception {
        return doLogout();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doLogout()
     */
    @Override
    protected boolean doLogout() throws Exception {
        getSubject().getPrincipals().removeAll(getPrincipals());
        return true;
    }

}
