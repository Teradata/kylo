package com.thinkbiganalytics.metadata.api.user;

import com.thinkbiganalytics.metadata.api.BaseProvider;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides access to stored users.
 */
public interface UserProvider extends BaseProvider<User, User.ID> {

    /**
     * Gets or creates the user with the specified username.
     *
     * @param systemName the username
     * @return the user
     */
    @Nonnull
    User ensureUser(@Nonnull String systemName);

    /**
     * Finds the user with the specified username.
     *
     * @param systemName the username to find
     * @return the user, if found
     */
    @Nonnull
    Optional<User> findBySystemName(@Nonnull String systemName);
}
