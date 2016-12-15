/**
 * 
 */
package com.example.kylo.plugin;

import java.util.Arrays;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.CredentialException;

import com.thinkbiganalytics.auth.jaas.AbstractLoginModule;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

/**
 * This is a simplistic example of a {@link LoginModule} that only tests whether the authenticating 
 * user's username and password match what has been configured via the modules options.  This
 * LoginModule extends {@link AbstractLoginModule}, which simplifies some of the boiler plate
 * behavior that all login modules must follow.
 * 
 * @author Sean Felten
 */
public class ExampleLoginModule extends AbstractLoginModule {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    
    private String username;
    private char[] password;
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        
        // Retrieve the configured username and optional password that must match the incoming values entered by the user.
        this.username = (String) getOption(USERNAME)
                        .orElseThrow(() -> new IllegalArgumentException("The \"" + USERNAME + "\" option is required"));
        this.password = (char[]) getOption(PASSWORD).orElse(new char[0]);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doLogin()
     */
    @Override
    protected boolean doLogin() throws Exception {
        // This method performs authentication and returns true if successful
        // or throws a LoginException (or subclass) on failure.  Returning false 
        // means that this module should not participate in this login attempt.
        
        // In this example we'll just use whatever username and password (if present) were provided as
        // configuration options to match the credentials of the current authenticating user.
        
        // Ask the system for the username/password to be authenticated using callbacks.
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);

        // Have the system fill in the requested values (username/password) by setting them in each callback.
        handle(nameCallback, passwordCallback);

        if (! this.username.equals(nameCallback.getName())) {
            throw new CredentialException("The username and/or password are invalid");
        }
        
        if (this.password.length > 0 && ! Arrays.equals(this.password, passwordCallback.getPassword())) {
            throw new CredentialException("The username and/or password are invalid");
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doCommit()
     */
    @Override
    protected boolean doCommit() throws Exception {
        // Associate the username and the admin group with the subject.
        getSubject().getPrincipals().add(new UsernamePrincipal(this.username));
        getSubject().getPrincipals().add(new GroupPrincipal("admin"));
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doAbort()
     */
    @Override
    protected boolean doAbort() throws Exception {
        // Since it is possible for login to still be aborted even after this module was told to commit, 
        // remove the principals we may have added to the subject.
        getSubject().getPrincipals().remove(new UsernamePrincipal(this.username));
        getSubject().getPrincipals().remove(new GroupPrincipal("admin"));
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.AbstractLoginModule#doLogout()
     */
    @Override
    protected boolean doLogout() throws Exception {
        // Nothing to do upon logout.
        return true;
    }

}
