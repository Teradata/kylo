/**
 *
 */
package com.thinkbiganalytics.auth.jaas;

/*-
 * #%L
 * thinkbig-security-auth
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

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 *
 */
public abstract class AbstractLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(AbstractLoginModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;
    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;
    private Principal userPrincipal;
    private Set<Principal> principals = new HashSet<>();

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        log.debug("Initialize - subject: {}, callback handler: {}, options: {}", subject, callbackHandler, options);

        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#login()
     */
    @Override
    public boolean login() throws LoginException {
        try {
            boolean active = doLogin();

            setLoginSucceeded(true);
            return active && isLoginSucceeded();
        } catch (LoginException e) {
            log.debug("Login exception", e);
            setLoginSucceeded(false);
            throw e;
        } catch (Exception e) {
            log.debug("Login exception", e);
            setLoginSucceeded(false);
            throw new LoginException("Login failure: " + (e.getMessage() == null ? e.toString() : e.getMessage()));
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException {
        if (isLoginSucceeded()) {
            try {
                boolean active = doCommit();

                setCommitSucceeded(true);
                return active && isCommitSucceeded();
            } catch (LoginException e) {
                log.debug("Login commit exception", e);
                setCommitSucceeded(false);
                throw e;
            } catch (Exception e) {
                log.debug("Login commit exception", e);
                setCommitSucceeded(false);
                throw new LoginException("Login commit failure: " + e.getMessage());
            }
        } else {
            return isCommitSucceeded();
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    @Override
    public boolean abort() throws LoginException {
        if (!isLoginSucceeded()) {
            return false;
        } else if (isCommitSucceeded()) {
            try {
                boolean active = doAbort();

                return active && isCommitSucceeded();
            } catch (LoginException e) {
                log.debug("Login abort exception", e);
                setLoginSucceeded(false);
                throw e;
            } catch (Exception e) {
                log.debug("Login abort exception", e);
                setLoginSucceeded(false);
                throw new LoginException("Login abort failure: " + e.getMessage());
            }
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    @Override
    public boolean logout() throws LoginException {
        try {
            boolean active = doLogout();

            return active;
        } catch (LoginException e) {
            log.debug("Logout exception", e);
            setLoginSucceeded(false);
            throw e;
        } catch (Exception e) {
            log.debug("Logout exception", e);
            setLoginSucceeded(false);
            throw new LoginException("Login logout failure: " + e.getMessage());
        } finally {
            this.loginSucceeded = false;
            this.commitSucceeded = false;
            this.sharedState = null;
            this.options = null;
        }
    }

    protected abstract boolean doLogin() throws Exception;

    protected abstract boolean doCommit() throws Exception;

    protected abstract boolean doAbort() throws Exception;

    protected abstract boolean doLogout() throws Exception;


    public Map<String, ?> getSharedState() {
        return sharedState;
    }

    public Map<String, ?> getOptions() {
        return options;
    }

    protected boolean isLoginSucceeded() {
        return loginSucceeded;
    }

    protected void setLoginSucceeded(boolean loginSucceeded) {
        this.loginSucceeded = loginSucceeded;
    }

    protected boolean isCommitSucceeded() {
        return commitSucceeded;
    }

    protected void setCommitSucceeded(boolean commitSucceeded) {
        this.commitSucceeded = commitSucceeded;
    }

    protected Set<Principal> getPrincipals() {
        return principals;
    }

    /**
     * @return the user principal (if any) and all ather princials in a combined set.
     */
    protected Set<Principal> getAllPrincipals() {
        return getUserPrincipal() == null ? getPrincipals() : Stream.concat(Stream.of(getUserPrincipal()),
                                                                            getPrincipals().stream()).collect(Collectors.toSet());
    }

    protected Principal getUserPrincipal() {
        return userPrincipal;
    }

    protected void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
        addPrincipal(this.userPrincipal);
    }

    protected UsernamePrincipal addNewUserPrincipal(String username) {
        UsernamePrincipal user = new UsernamePrincipal(username);
        setUserPrincipal(user);
        return user;
    }

    protected boolean clearUserPrincipal() {
        Principal principal = this.userPrincipal;
        this.userPrincipal = null;
        return principal != null ? removePrincipal(principal) : false;
    }

    protected GroupPrincipal addNewGroupPrincipal(String name) {
        GroupPrincipal group = new GroupPrincipal(name);
        addPrincipal(group);
        return group;
    }

    protected boolean addPrincipal(Principal principal) {
        return this.principals.add(principal);
    }

    protected boolean addAllPrincipals(Collection<? extends Principal> principals) {
        return this.principals.addAll(principals);
    }

    protected boolean removePrincipal(Principal principal) {
        return this.principals.remove(principal);
    }

    protected void clearAllPrincipals() {
        this.userPrincipal = null;
        this.principals.clear();
    }

    protected Subject getSubject() {
        return subject;
    }

    protected CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    protected void handle(Callback... callbacks) throws LoginException {
        try {
            getCallbackHandler().handle(callbacks);
        } catch (IOException e) {
            log.warn("I/O failure attempting to handle callback among: {}", e);
            throw new LoginException("Login failure attempting to retrieve required login information: " + e.getMessage());
        } catch (UnsupportedCallbackException e) {
            log.error("Unsupported callback among: {}", e);
            throw new LoginException("Login failure attempting to retrieve required login information: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    protected Optional<Object> getOption(String name) {
        return Optional.ofNullable(this.options.get(name));
    }
}
