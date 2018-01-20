package com.thinkbiganalytics.auth.file;

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
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

/**
 * FailFastUsersRolesLoginModule will fail fast if user and group configuration files are not found.
 * This class extends original UsersRolesLoginModule so that it can re-use its configuration file loading strategy.
 */
public class FailFastUsersRolesLoginModule extends UsersRolesLoginModule {


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
}
