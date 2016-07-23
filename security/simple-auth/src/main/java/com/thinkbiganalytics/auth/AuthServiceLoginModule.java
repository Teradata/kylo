/**
 * 
 */
package com.thinkbiganalytics.auth;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sean Felten
 */
public class AuthServiceLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceLoginModule.class);
    
    private AuthenticationService authService;
    
    private Subject subject;
    private CallbackHandler callbackHandler;
    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;
    private UsernamePrincipal user;

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        log.debug("Initialize - subject: {}, callback handler: {}, options: {}", subject, callbackHandler, options);
        
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        
        if (options.containsKey("authService")) {
            this.authService = (AuthenticationService) options.get("authService");
        } else {
            log.error("This login module requires an \"authService\" option");
            throw new IllegalArgumentException("This login module requires an \"authService\" option");
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#login()
     */
    @Override
    public boolean login() throws LoginException {
        try {
            Callback[] callbacks = new Callback[2];
            callbacks[0] = new NameCallback("username");
            callbacks[1] = new PasswordCallback("password", false);
            
            this.callbackHandler.handle(callbacks);
            
            String name = ((NameCallback) callbacks[0]).getName();
            char[] password = ((PasswordCallback) callbacks[1]).getPassword();
            
            this.loginSucceeded = this.authService.authenticate(name, new String(password));
            
            if (loginSucceeded) {
                log.debug("Login success for: {}", name);
                
                this.user = new UsernamePrincipal(name);
            } else {
                log.debug("Login failure for: {}", name);
            }
            
            return this.loginSucceeded;
        } catch (IOException | UnsupportedCallbackException e) {
            log.error("Login failure attempting to retrieve username/password", e);
            throw new LoginException("Login failure attempting to retrieve username/password: " + e.getMessage());
        } catch (Exception e) {
            log.error("Login failure", e);
            throw new LoginException("Login failure: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException {
        if (this.loginSucceeded) {
            this.subject.getPrincipals().add(this.user);
            this.commitSucceeded = true;
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    @Override
    public boolean abort() throws LoginException {
        if (this.commitSucceeded) {
            this.subject.getPrincipals().remove(this.user);
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    @Override
    public boolean logout() throws LoginException {
        if (this.commitSucceeded) {
            this.subject.getPrincipals().remove(this.user);
        }
        
        return true;
    }

}
