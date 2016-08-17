package com.thinkbiganalytics.auth.metadata;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserProvider;

import java.security.Principal;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * A login module that authorizes users using the metadata store.
 */
public class MetadataLoginModule implements LoginModule {

    /** Application interaction handler */
    @Nullable
    private CallbackHandler callbackHandler;

    /** Metadata store */
    @Inject
    MetadataAccess metadata;

    /** Metadata user */
    @Nullable
    private Principal principal;

    /** User being logged in */
    @Nullable
    private Subject subject;

    /** Metadata user provider */
    @Inject
    UserProvider userProvider;

    @Override
    public void initialize(@Nonnull final Subject subject, @Nonnull final CallbackHandler callbackHandler, @Nonnull final Map<String, ?> sharedState, @Nonnull final Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        // Get username and create principal
        principal = null;

        if (callbackHandler != null) {
            final NameCallback nameCallback = new NameCallback("Username: ");
            try {
                callbackHandler.handle(new Callback[] {nameCallback});
            } catch (final Exception cause) {
                final LoginException e = new LoginException();
                e.initCause(cause);
                throw e;
            }

            if (nameCallback.getName() != null) {
                principal = new MetadataPrincipal(nameCallback.getName());
            }
        }

        if (principal == null) {
            throw new LoginException("Anonymous users are not allowed.");
        }

        // Check for access
        final boolean enabled = metadata.read(() -> userProvider.findByUsername(principal.getName()).map(User::isEnabled)).orElseThrow(AccountNotFoundException::new);
        if (enabled) {
            return true;
        } else {
            throw new AccountLockedException();
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (principal == null || subject == null) {
            throw new LoginException();
        }

        subject.getPrincipals().add(principal);
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }
}
