package com.thinkbiganalytics.auth.rest;

/*-
 * #%L
 * REST API Authentication
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
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.security.rest.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.servlet.http.Cookie;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;

/**
 * Authenticates users by querying an external REST API. This allows the UI module to get a user's groups from the Services module.
 */
public class KyloRestLoginModule extends AbstractLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(KyloRestLoginModule.class);
    /**
     * Option for the URL of the REST API endpoint
     */
    static final String LOGIN_USER = "loginUser";
    /**
     * Option for the URL of the REST API endpoint
     */
    static final String LOGIN_PASSWORD = "loginPassword";
    /**
     * Option for the flag indicating whether Kylo services should be signaled on logout
     */
    static final String SERVICES_LOGOUT = "servicesLogout";

    /**
     * Option for REST client configuration
     */
    static final String REST_CLIENT_CONFIG = "restClientConfig";

    /**
     * REST API client configuration
     */
    private LoginJerseyClientConfig config;

    /**
     * Alternate username (such as service account) to use when making the REST call
     */
    private String loginUser = null;

    /**
     * The password to use when a loginUser property is set
     */
    private char[] loginPassword = null;
    
    /**
     * Indicates whether Kylo services should be notified of logout
     */
    private boolean servicesLogout = true;
    

    @Override
    public void initialize(@Nonnull final Subject subject, @Nonnull final CallbackHandler callbackHandler, @Nonnull final Map<String, ?> sharedState, @Nonnull final Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        try {
            config = (LoginJerseyClientConfig) options.get(REST_CLIENT_CONFIG);
            servicesLogout = (boolean) getOption(SERVICES_LOGOUT).orElse(false);
            loginUser = (String) getOption(LOGIN_USER).orElse(null);
            
            if (loginUser != null) {
                String passwordObject = (String)getOption(LOGIN_PASSWORD)
                    .orElseThrow(() -> new IllegalArgumentException("A REST login password is required if a login username was provided"));
                loginPassword = passwordObject.toCharArray();
            }
        } catch (RuntimeException e) {
            log.error("Unhandled exception during initialization", e);
            throw e;
        }
    }

    @Override
    protected boolean doLogin() throws Exception {
        final LoginJerseyClientConfig userConfig = createClientConfig(true);

        final User user;
        try {
            user = retrieveUser(userConfig);
        } catch (final NotAuthorizedException e) {
            log.debug("Received unauthorized response from Login API for user: {}", userConfig.getUsername());
            throw new CredentialException("The username and password combination do not match.");
        } catch (final ProcessingException e) {
            log.error("Failed to process response from Login API for user: {}", userConfig.getUsername(), e);
            throw new FailedLoginException("The login service is unavailable.");
        } catch (final WebApplicationException e) {
            log.error("Received unexpected response from Login API for user: {}", userConfig.getUsername(), e);
            throw new FailedLoginException("The login service is unavailable.");
        }

        // Parse response
        if (user == null) {
            log.debug("No account exists with the name: {}", userConfig.getUsername());
            throw new AccountNotFoundException("No account exists with the name: " + userConfig.getUsername());
        } else if (!user.isEnabled()) {
            log.debug("User from Login API is disabled: {}", userConfig.getUsername());
            throw new AccountLockedException("The account \"" + userConfig.getUsername() + "\" is currently disabled");
        }

        addNewUserPrincipal(user.getSystemName());
        user.getGroups().forEach(this::addNewGroupPrincipal);
        return true;
    }
    
    @Override
    protected boolean doCommit() throws Exception {
        getSubject().getPrincipals().addAll(getAllPrincipals());
        return true;
    }

    @Override
    protected boolean doAbort() throws Exception {
        return doLogout();
    }

    @Override
    protected boolean doLogout() throws Exception {
        final LoginJerseyClientConfig userConfig = createClientConfig(false);

        logoutUser(userConfig);
        getSubject().getPrincipals().removeAll(getAllPrincipals());
        
        return true;
    }

    private LoginJerseyClientConfig createClientConfig(boolean usePasswords) throws LoginException {
        if (usePasswords) {
            final LoginJerseyClientConfig userConfig = new LoginJerseyClientConfig(config);
            
            // Get username and password
            final NameCallback nameCallback = new NameCallback("Username: ");
            final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
            final String authUser;
            final String username;
            final char[] password;
            
            if (loginUser == null) {
                // Use user's own username and password to access the REST API if a loginUser was not provided.
                handle(nameCallback, passwordCallback);
                authUser = null;
                username = nameCallback.getName();
                password = passwordCallback.getPassword();
            } else {
                // Using the loginUser to access API so only need the authenticating user's name.
                handle(nameCallback);
                authUser = nameCallback.getName();
                username = loginUser;
                password = loginPassword;
            } 
            
            userConfig.setUsername(username);
            userConfig.setPassword(password);
            userConfig.setAuthenticatingUser(authUser);
            
            return userConfig;
        } else {
            return config;
        }
    }
    
    private Invocation.Builder createBuilder(@Nonnull JerseyRestClient client, @Nonnull String endpoint, @Nonnull Set<Cookie> cookies) throws LoginException {
        Invocation.Builder builder = client.request(endpoint, null);
        
        if (cookies.size() > 0) {
            for (Cookie cookie : cookies) {
                builder.cookie(cookie.getName(), cookie.getValue());
            }
        }
        
        return builder;
    }

    private User retrieveUser(@Nonnull final LoginJerseyClientConfig userConfig) throws LoginException {
        String endpoint = userConfig.isAlternateUser() ? "/v1/security/users/" + userConfig.getAuthenticatingUser() : "/v1/about/me";
        JerseyRestClient client = getClient(userConfig);
        
        return client.get(createBuilder(client, endpoint, Collections.emptySet()), User.class);
    }

    private void logoutUser(@Nonnull final LoginJerseyClientConfig userConfig) throws LoginException {
        if (servicesLogout) {
            String endpoint = "/v1/logout";
            JerseyRestClient client = getClient(userConfig);
            Invocation.Builder builder = createBuilder(client, endpoint, getSubject().getPrivateCredentials(Cookie.class));
            
            client.get(builder, String.class);
        }
    }

    /**
     * Gets a Jersey client using the specified configuration.
     *
     * @param config the login configuration
     * @return the Jersey client
     */
    @Nonnull
    JerseyRestClient getClient(@Nonnull final LoginJerseyClientConfig config) {
        return new JerseyRestClient(config);
    }
}
