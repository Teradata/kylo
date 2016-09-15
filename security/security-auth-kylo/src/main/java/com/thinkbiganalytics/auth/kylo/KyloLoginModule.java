package com.thinkbiganalytics.auth.kylo;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.thinkbiganalytics.auth.jaas.AbstractLoginModule;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserProvider;

/**
 * A login module that authenticates users using the Kylo user store.  By default
 * a user is authenticated only if a user with the provided username exits in
 * the store.  It may also be configured to validate a provided password against the
 * one associated with the username in the store as well.
 */
public class KyloLoginModule extends AbstractLoginModule implements LoginModule {

    /** Option for the {@link MetadataAccess} object */
    public static final String METADATA_ACCESS = "metadataAccess";

    /** Option for the {@link PasswordEncoder} object */
    public static final String PASSWORD_ENCODER = "passwordEncoder";

    /** Option for the {@link UserProvider} object */
    public static final String USER_PROVIDER = "userProvider";
    
    /** Option that indicates whether password authentication is required */
    public static final String REQUIRE_PASSWORD = "requirePassword";

    /** Metadata store */
    private MetadataAccess metadata;

    /** Password encoder */
    private PasswordEncoder passwordEncoder;
    
    /** Whether to required password validation for authentication */
    private boolean requirePassword = false;

    /** Metadata user provider */
    private UserProvider userProvider;
    
    private Set<Principal> principals = new HashSet<>();
    

    @Override
    public void initialize(@Nonnull final Subject subject, @Nonnull final CallbackHandler callbackHandler, @Nonnull final Map<String, ?> sharedState, @Nonnull final Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        metadata = (MetadataAccess) getOption(METADATA_ACCESS).orElseThrow(() -> new IllegalArgumentException("The \"" + METADATA_ACCESS + "\" option is required"));
        passwordEncoder = (PasswordEncoder) getOption(PASSWORD_ENCODER).orElseThrow(() -> new IllegalArgumentException("The \"" + PASSWORD_ENCODER + "\" option is required"));
        userProvider = (UserProvider) getOption(USER_PROVIDER).orElseThrow(() -> new IllegalArgumentException("The \"" + USER_PROVIDER + "\" option is required"));
        requirePassword = (Boolean) getOption(REQUIRE_PASSWORD).orElse(false);
    }

    @Override
    protected boolean doLogin() throws Exception {
        // Get username and password
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        
        if (requirePassword) {
            handle(nameCallback, passwordCallback);
        } else {
            handle(nameCallback);
        }

        // Authenticate user
        metadata.read(() -> {
            Optional<User> user = userProvider.findUserBySystemName(nameCallback.getName());
            
            if (user.isPresent()) {
                if (requirePassword && passwordEncoder.matches(new String(passwordCallback.getPassword()), user.get().getPassword())) {
                    throw new CredentialException("The username and/or password do not match any account");
                }
                
                this.principals.add(user.get().getPrincipal());
                this.principals.addAll(user.get().getAllGroupPrincipals());
            } else {
                throw new AccountNotFoundException("No account exists with name name \"" + nameCallback.getName() + "\"");
            }
        }, MetadataAccess.SERVICE);
        
        return true;
    }

    @Override
    protected boolean doCommit() throws Exception {
        getSubject().getPrincipals().addAll(this.principals);
        return true;
    }

    @Override
    protected boolean doAbort() throws Exception {
        return doLogout();
    }

    @Override
    protected boolean doLogout() throws Exception {
        getSubject().getPrincipals().removeAll(this.principals);
        return true;
    }

    /**
     * TODO
     *
     * @param cause
     * @return
     */
    @Nonnull
    private LoginException newLoginException(@Nonnull final Throwable cause) {
        final LoginException loginException = new LoginException("An unexpected login error occurred.");
        loginException.initCause(cause);
        return loginException;
    }
}
