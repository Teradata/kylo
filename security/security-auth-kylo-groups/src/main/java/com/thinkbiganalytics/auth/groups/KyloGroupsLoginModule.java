package com.thinkbiganalytics.auth.groups;

/*-
 * #%L
 * kylo-security-auth-kylo-groups
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
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.rest.model.UserGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.spi.LoginModule;
import javax.ws.rs.core.GenericType;

/**
 * Login module which gets Kylo groups. This is used to limit user groups to those available in Kylo.
 */
public class KyloGroupsLoginModule extends AbstractLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(KyloGroupsLoginModule.class);
    /**
     * Option for the URL of the REST API endpoint
     */
    static final String LOGIN_USER_FIELD = "loginUser";
    /**
     * Option for the URL of the REST API endpoint
     */
    static final String LOGIN_PASSWORD_FIELD = "loginPassword";

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
    private String loginPassword = null;

    @Override
    public void initialize(@Nonnull final Subject subject, @Nonnull final CallbackHandler callbackHandler, @Nonnull final Map<String, ?> sharedState, @Nonnull final Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        try {
            config = (LoginJerseyClientConfig) options.get(REST_CLIENT_CONFIG);
            loginUser = (String) getOption(LOGIN_USER_FIELD).orElse(null);
            loginPassword = loginUser == null ? null : (String) getOption(LOGIN_PASSWORD_FIELD)
                .orElseThrow(() -> new IllegalArgumentException("A REST login password is required if a login username was provided"));
        } catch (RuntimeException e) {
            log.error("Unhandled exception during initialization", e);
            throw e;
        }
    }

    @Override
    protected boolean doLogin() throws Exception {
        // Get username and password
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        final String username;
        final String password;

        if (loginUser == null) {
            // Use groups's own username and password to access the REST API if a loginUser was not provided.
            handle(nameCallback, passwordCallback);
            username = nameCallback.getName();
            password = new String(passwordCallback.getPassword());
        } else {
            // Using the loginUser to access API so only need the authenticating groups's name.
            handle(nameCallback);
            username = loginUser;
            password = loginPassword;
        }

        final LoginJerseyClientConfig userConfig = new LoginJerseyClientConfig(config);
        userConfig.setUsername(username);
        userConfig.setPassword(password);

        final List<UserGroup> groups;
        try {
            groups = retrieveGroups(userConfig);
        } catch (Exception e) {
            log.error("An error occurred while getting Kylo Groups username: {}", username, e);
            throw new RuntimeException("An error occurred while getting Kylo Groups", e);
        }

        // Parse response
        if (groups == null) {
            log.debug("Received null response from Groups API for username: {}", username);
            throw new RuntimeException("Received null response from Groups API for username: {} " + username);
        }

        groups.forEach(group -> addNewGroupPrincipal(group.getSystemName()));
        return true;
    }

    @Override
    protected boolean doCommit() throws Exception {
        Set<Principal> otherPrincipals = getSubject().getPrincipals(); //e.g. AD principals, e.g. B, C, D
        Set<Principal> kyloGroups = getPrincipals(); //kylo groups, e.g. A, B, C
        otherPrincipals.removeIf(principal -> principal instanceof GroupPrincipal && !kyloGroups.contains(principal)); //intersection result, e.g. B, C
        return true;
    }

    private List<UserGroup> retrieveGroups(final LoginJerseyClientConfig userConfig) {
        String endpoint = "/v1/security/groups";
        return getClient(userConfig).get(endpoint, null, new GenericType<List<UserGroup>>() {});
    }

    @Override
    protected boolean doAbort() throws Exception {
        return doLogout();
    }

    @Override
    protected boolean doLogout() throws Exception {
        getSubject().getPrincipals().removeAll(getAllPrincipals());
        return true;
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
