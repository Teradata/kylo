/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.config;

/*-
 * #%L
 * kylo-security-auth
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import org.springframework.security.authentication.jaas.JaasAuthenticationCallbackHandler;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * A replacement for Spring's {@link org.springframework.security.authentication.jaas.JaasNameCallbackHandler} that corrects
 * a faulty implementation of the Spring version.  The Spring implementation assumed the principal from the Authentication
 * is either UserDetails or is something on which toString() should be called to obtain the name to set on the callback.  
 * This implementation simply calls getName() on the Authentication object and lets it make the choice of what name to return.
 */
public class JaasAuthenticationNameCallbackHandler implements JaasAuthenticationCallbackHandler {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.JaasAuthenticationCallbackHandler#handle(javax.security.auth.callback.Callback, org.springframework.security.core.Authentication)
     */
    @Override
    public void handle(Callback callback, Authentication auth) throws IOException, UnsupportedCallbackException {
        if (callback instanceof NameCallback) {
            NameCallback nc = (NameCallback) callback;
            nc.setName(auth.getName());
        }
    }

}
