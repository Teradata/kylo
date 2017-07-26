/**
 *
 */
package com.thinkbiganalytics.security.auth.ad;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryAuthenticationProvider;

import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AccountException;

/**
 *
 */
public class ActiveDirectoryLoginModule extends AbstractLoginModule {

    private static final Logger log = LoggerFactory.getLogger(ActiveDirectoryLoginModule.class);
    
    public static final String AUTH_PROVIDER = "authProvider";
    
    private ActiveDirectoryAuthenticationProvider authProvider;


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        this.authProvider = (ActiveDirectoryAuthenticationProvider) getOption(AUTH_PROVIDER)
            .orElseThrow(() -> new IllegalArgumentException("The \"" + AUTH_PROVIDER + "\" option is required"));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doLogin()
     */
    @Override
    protected boolean doLogin() throws Exception {
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        
        if (this.authProvider.isUsingServiceCredentials()) {
            handle(nameCallback);
            passwordCallback.setPassword("".toCharArray());
        } else {
            handle(nameCallback, passwordCallback);
        }

        if (nameCallback.getName() == null) {
            throw new AccountException("No username provided for authentication");
        }

        Principal userPrincipal = new UsernamePrincipal(nameCallback.getName());
        String password = new String(passwordCallback.getPassword());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrincipal, password);

        log.debug("Authenticating: {}", userPrincipal);
        Authentication authenticated = this.authProvider.authenticate(authentication);
        log.debug("Successfully Authenticated: {}", userPrincipal);

        setUserPrincipal(userPrincipal);

        for (GrantedAuthority grant : authenticated.getAuthorities()) {
            String groupName = grant.getAuthority();

            log.debug("Found group for {}: {}", userPrincipal, groupName);

            if (groupName != null) {
                addNewGroupPrincipal(groupName);
            }
        }

        return true;
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
