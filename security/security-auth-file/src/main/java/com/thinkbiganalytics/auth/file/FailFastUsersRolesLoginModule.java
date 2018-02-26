package com.thinkbiganalytics.auth.file;

import org.jboss.security.SecurityConstants;
import org.jboss.security.SimpleGroup;

/*-
 * #%L
 * kylo-security-auth-file
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

import org.jboss.security.auth.spi.UsersRolesLoginModule;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

/**
 * FailFastUsersRolesLoginModule will fail fast if user and group configuration files are not found.
 * This class extends original UsersRolesLoginModule so that it can re-use its configuration file loading strategy.
 */
public class FailFastUsersRolesLoginModule extends UsersRolesLoginModule {

    private static final Group DEFAULT_CALLER_PRINCIPAL_GROUP = new SimpleGroup(SecurityConstants.CALLER_PRINCIPAL_GROUP);

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        checkConfig(options.get("usersProperties"));
    }

    void checkConfig(Object usersResource) {
        try {
            this.loadUsers();
        } catch (IOException e) {
            String msg = String.format("auth-file is configured but no users resource is found "
                         + "- please verify the config property security.auth.file.users=%s", usersResource);
            throw new IllegalStateException(msg);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getCallerPrincipalGroup(java.util.Set)
     */
    @Override
    protected Group getCallerPrincipalGroup(Set<Principal> principals) {
        // We don't want to use a caller principal group so return a bogus one so that the superclass
        // won't create one for us.
        return DEFAULT_CALLER_PRINCIPAL_GROUP;
    }
}
