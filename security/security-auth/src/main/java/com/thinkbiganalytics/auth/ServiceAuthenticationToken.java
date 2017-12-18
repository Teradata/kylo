/**
 *
 */
package com.thinkbiganalytics.auth;

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

import com.thinkbiganalytics.security.ServiceAdminPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;

import java.util.Arrays;

/**
 * An authentication representing a general service account token.  Used internally by service threads.
 */
public class ServiceAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private static final UsernamePrincipal USER = new UsernamePrincipal("service");

    public ServiceAuthenticationToken() {
        super(Arrays.asList(new JaasGrantedAuthority("ROLE_SERVICE", new ServiceAdminPrincipal()),
                            new JaasGrantedAuthority("admin", new ServiceAdminPrincipal()))); // ModeShape role
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getCredentials()
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getPrincipal()
     */
    @Override
    public Object getPrincipal() {
        return USER;
    }

}
