/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.config;

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
