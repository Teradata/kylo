/**
 * 
 */
package com.thinkbiganalytics.auth.jaas;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sean Felten
 */
public abstract class AbstractLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(AbstractLoginModule.class);
    
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;
    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        log.debug("Initialize - subject: {}, callback handler: {}, options: {}", subject, callbackHandler, options);
        
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#login()
     */
    @Override
    public boolean login() throws LoginException {
        try {
            boolean active = doLogin();
            
            setLoginSucceeded(true);
            return active && isLoginSucceeded();
        } catch (LoginException e) {
            setLoginSucceeded(false);
            throw e;
        }catch (Exception e) {
            setLoginSucceeded(false);
            throw new LoginException("Login failure: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException {
        if (isLoginSucceeded()) {
            try {
                boolean active = doCommit();
                
                setCommitSucceeded(true);
                return active && isCommitSucceeded();
            } catch (LoginException e) {
                setCommitSucceeded(false);
                throw e;
            }catch (Exception e) {
                setCommitSucceeded(false);
                throw new LoginException("Login commit failure: " + e.getMessage());
            }
        } else {
            return isCommitSucceeded();
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    @Override
    public boolean abort() throws LoginException {
        if (! isLoginSucceeded()) {
            return false;
        } else if (isCommitSucceeded()) {
            try {
                boolean active = doAbort();
                
                return active && isCommitSucceeded();
            } catch (LoginException e) {
                setLoginSucceeded(false);
                throw e;
            }catch (Exception e) {
                setLoginSucceeded(false);
                throw new LoginException("Login abort failure: " + e.getMessage());
            }
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    @Override
    public boolean logout() throws LoginException {
        try {
            boolean active = doLogout();
            
            return active;
        } catch (LoginException e) {
            setLoginSucceeded(false);
            throw e;
        } catch (Exception e) {
            setLoginSucceeded(false);
            throw new LoginException("Login logout failure: " + e.getMessage());
        } finally {
            this.loginSucceeded = false;
            this.commitSucceeded = false;
            this.sharedState = null;
            this.options = null;
        }
    }
    
    protected abstract boolean doLogin() throws Exception;
    
    protected abstract boolean doCommit() throws Exception;
    
    protected abstract boolean doAbort() throws Exception;
    
    protected abstract boolean doLogout() throws Exception;

    
    protected Subject getSubject() {
        return subject;
    }

    protected CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }
    
    public Map<String, ?> getSharedState() {
        return sharedState;
    }
    
    public Map<String, ?> getOptions() {
        return options;
    }

    protected boolean isLoginSucceeded() {
        return loginSucceeded;
    }

    protected void setLoginSucceeded(boolean loginSucceeded) {
        this.loginSucceeded = loginSucceeded;
    }

    protected boolean isCommitSucceeded() {
        return commitSucceeded;
    }

    protected void setCommitSucceeded(boolean commitSucceeded) {
        this.commitSucceeded = commitSucceeded;
    }

    protected void handle(Callback... callbacks) throws LoginException {
        try {
            getCallbackHandler().handle(callbacks);
        } catch (IOException e) {
            log.warn("I/O failure attempting to handle callback among: {}", e);
            throw new LoginException("Login failure attempting to retrieve required login information: " + e.getMessage());
        } catch (UnsupportedCallbackException e) {
            log.error("Unsupported callback among: {}", e);
            throw new LoginException("Login failure attempting to retrieve required login information: " + e.getMessage());
        }
    }
    
    protected void failLogin(String message) throws LoginException {
        failLogin(message, null);
    }
    
    protected void failLogin(String message, Throwable throwable) throws LoginException {
        if (throwable != null) {
            log.error("Login failure: " + message, throwable);
            throw new LoginException("Login failure: " + message + " - " + throwable.getMessage());
        } else {
            log.error("Login failure: " + message);
            throw new LoginException("Login failure: " + message);
        }
        
    }
}
