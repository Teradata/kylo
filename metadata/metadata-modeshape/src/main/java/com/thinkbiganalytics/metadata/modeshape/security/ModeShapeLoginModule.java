/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import com.thinkbiganalytics.auth.jaas.AbstractLoginModule;

/**
 * A module that (for now) simply adds ModeShape's course-grained "readwrite" role principal to the logged in user's subject.
 * This module does not attempt to authenticate the user but simply updates the subject upon successful login.
 * @author Sean Felten
 */
public class ModeShapeLoginModule extends AbstractLoginModule {
    
    private ModeShapePrincipal principal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
    }

    @Override
    protected boolean doLogin() throws Exception {
        // No login behavior required; assume login success
        return true;
    }

    @Override
    protected boolean doCommit() throws Exception {
        // For now assume everyone has read/write.
        this.principal = new ModeShapeReadWritePrincipal();
        getSubject().getPrincipals().add(this.principal);
        return true;
    }

    @Override
    protected boolean doAbort() throws Exception {
        return logout();
    }

    @Override
    protected boolean doLogout() throws Exception {
        getSubject().getPrincipals().remove(this.principal);
        return true;
    }
}
