/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.config;

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
