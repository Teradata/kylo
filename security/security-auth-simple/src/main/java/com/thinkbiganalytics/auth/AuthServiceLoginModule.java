/**
 *
 */
package com.thinkbiganalytics.auth;

/*-
 * #%L
 * thinkbig-security-auth-simple
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
import com.thinkbiganalytics.security.ServiceAdminPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

/**
 * A LoginModule that delegates to an AuthenticationService to authenticate a user name and password.
 */
public class AuthServiceLoginModule extends AbstractLoginModule {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceLoginModule.class);

    private static final String AUTH_SERVICE_OPTION = "authService";

    private AuthenticationService authService;
    private UsernamePrincipal user;

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        if (options.containsKey(AUTH_SERVICE_OPTION)) {
            this.authService = (AuthenticationService) options.get(AUTH_SERVICE_OPTION);
        } else {
            log.error("This login module requires an \"authService\" option");
            throw new IllegalArgumentException("This login module requires an \"authService\" option");
        }
    }

    @Override
    public boolean doLogin() throws Exception {
        NameCallback nameCb = new NameCallback("username");
        PasswordCallback passwordCb = new PasswordCallback("password", false);

        handle(nameCb, passwordCb);

        if (this.authService.authenticate(nameCb.getName(), passwordCb.getPassword())) {
            log.debug("Login success for: {}", nameCb.getName());

            this.user = new UsernamePrincipal(nameCb.getName());
        } else {
            log.debug("Login failure for: {}", nameCb.getName());
        }
        passwordCb.clearPassword();

        return true;
    }

    @Override
    public boolean doCommit() throws Exception {
        getSubject().getPrincipals().add(this.user);
        // For now assume everyone who authenticates in this simple implementation are administrators.
        getSubject().getPrincipals().add(new ServiceAdminPrincipal());
        return true;
    }

    @Override
    public boolean doAbort() throws Exception {
        return logout();
    }

    @Override
    public boolean doLogout() throws Exception {
        getSubject().getPrincipals().remove(this.user);
        getSubject().getPrincipals().remove(new ServiceAdminPrincipal());
        return true;
    }
}
