package com.thinkbiganalytics.metadata.modeshape;

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

import org.modeshape.jcr.security.SecurityContext;

/**
 * A security context that is in effect when an administrative operation is being executed under
 * the ModeShaepAdminPrincipal credential
 */
public class TestSecurityContext implements SecurityContext {

    @Override
    public String getUserName() {
        return "test";
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasRole(String roleName) {
        return roleName.equalsIgnoreCase("user") || roleName.equalsIgnoreCase("readwrite");
    }

    @Override
    public void logout() {
        // Ignored
    }
}
