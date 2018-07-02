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
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * A replacement for Spring's {@link org.springframework.security.authentication.jaas.JaasPasswordCallbackHandler} that corrects
 * some faulty assumptions that the Spring implementation makes, such as assuming the credential is always a string.
 */
public class JaasAuthenticationPasswordCallbackHandler implements JaasAuthenticationCallbackHandler {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.JaasAuthenticationCallbackHandler#handle(javax.security.auth.callback.Callback, org.springframework.security.core.Authentication)
     */
    @Override
    public void handle(Callback callback, Authentication auth) throws IOException, UnsupportedCallbackException {
        if (callback instanceof PasswordCallback) {
            PasswordCallback pc = (PasswordCallback) callback;
            Object credential = auth.getCredentials();
            
            if (credential instanceof String) {
                pc.setPassword(((String) credential).toCharArray());
            } else if (credential instanceof char[]) {
                pc.setPassword((char[]) credential);
            } else {
                pc.setPassword(credential.toString().toCharArray());
            }
        }
    }
}
