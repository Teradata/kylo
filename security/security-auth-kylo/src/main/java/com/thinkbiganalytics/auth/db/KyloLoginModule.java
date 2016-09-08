package com.thinkbiganalytics.auth.db;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import com.thinkbiganalytics.security.UsernamePrincipal;

/**
 * A login module that authorizes users using the metadata store.
 */
public class KyloLoginModule extends AbstractLoginModule implements LoginModule {

    /** Option for the {@link MetadataAccess} object */
    public static final String METADATA_ACCESS = "metadataAccess";

    /** Option for the {@link PasswordEncoder} object */
    public static final String PASSWORD_ENCODER = "passwordEncoder";

    /** Option for the {@link UserProvider} object */
    public static final String USER_PROVIDER = "userProvider";
    
    /** Option that indicates whether password authentication is required */
    public static final String AUTH_PASSWORKD = "authPassword";

    /** Metadata store */
    private MetadataAccess metadata;

    /** Password encoder */
    private PasswordEncoder passwordEncoder;
    
    private boolean authPassword = false;

    /** Metadata user provider */
    private UserProvider userProvider;
    
    /** Metadata user */
    @Nullable
    private UsernamePrincipal userPrincipal;

    @Override
    public void initialize(@Nonnull final Subject subject, @Nonnull final CallbackHandler callbackHandler, @Nonnull final Map<String, ?> sharedState, @Nonnull final Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        if (options.containsKey(METADATA_ACCESS)) {
            metadata = (MetadataAccess) options.get(METADATA_ACCESS);
        } else {
            throw new IllegalArgumentException("The \"" + METADATA_ACCESS + "\" option is required");
        }

        if (options.containsKey(PASSWORD_ENCODER)) {
            passwordEncoder = (PasswordEncoder) options.get(PASSWORD_ENCODER);
        } else {
            throw new IllegalArgumentException("The \"" + PASSWORD_ENCODER + "\" option is required");
        }

        if (options.containsKey(USER_PROVIDER)) {
            userProvider = (UserProvider) options.get(USER_PROVIDER);
        } else {
            throw new IllegalArgumentException("The \"" + USER_PROVIDER + "\" option is required");
        }
        
        if (options.containsKey(AUTH_PASSWORKD)) {
            authPassword = (Boolean) options.get(AUTH_PASSWORKD);
        }
    }

    @Override
    protected boolean doLogin() throws Exception {
        // Get username and password
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        
        if (authPassword) {
            handle(nameCallback, passwordCallback);
        } else {
            handle(nameCallback);
        }

        // Authenticate user
        this.userPrincipal = metadata.read(() -> {
            Optional<User> user = userProvider.findUserBySystemName(nameCallback.getName());
            
            if (user.isPresent()) {
                if (! authPassword && passwordEncoder.matches(new String(passwordCallback.getPassword()), user.get().getPassword())) {
                    throw new CredentialException("The username and/or credentials do not match");
                }
                
                return new UsernamePrincipal(user.get().getSystemName());
            } else {
                throw new AccountNotFoundException("No account exists with name name \"" + nameCallback.getName() + "\"")
            }
        }, MetadataAccess.SERVICE);
        
//        final Future<UsernamePrincipal> principalFuture = metadata.read(() -> {
//            final SettableFuture<UsernamePrincipal> result = SettableFuture.create();
//            final Optional<User> user = userProvider.findUserBySystemName(nameCallback.getName());
//            if (!user.isPresent()) {
//                result.setException(new AccountNotFoundException());
//            } else if (!user.get().isEnabled()) {
//                result.setException(new AccountException("The account has been disabled."));
//            } else if (!passwordEncoder.matches(new String(passwordCallback.getPassword()), user.get().getPassword())) {
//                result.setException(new FailedLoginException());
//            } else {
//                result.set(new UsernamePrincipal(user.get().getSystemName()));
//            }
//            return result;
//        });

        // Get userPrincipal
//        try {
//            userPrincipal = principalFuture.get();
//            return true;
//        } catch (CancellationException e) {
//            throw newLoginException(e);
//        } catch (ExecutionException e) {
//            Throwables.propagateIfInstanceOf(e.getCause(), LoginException.class);
//            throw newLoginException(e);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw newLoginException(e);
//        }
    }

    @Override
    protected boolean doCommit() throws Exception {
        getSubject().getPrincipals().add(userPrincipal);
        return true;
    }

    @Override
    protected boolean doAbort() throws Exception {
        return logout();
    }

    @Override
    protected boolean doLogout() throws Exception {
        getSubject().getPrincipals().remove(userPrincipal);
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
