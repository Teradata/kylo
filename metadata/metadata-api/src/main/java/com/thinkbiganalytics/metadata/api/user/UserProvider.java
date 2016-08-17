package com.thinkbiganalytics.metadata.api.user;

import com.thinkbiganalytics.metadata.api.BaseProvider;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides access to stored users.
 */
public interface UserProvider extends BaseProvider<User, User.ID> {

    /**
     * Finds the user with the specified login name.
     *
     * @param username the login name to find
     * @return the user, if found
     */
    @Nonnull
    Optional<User> findByUsername(@Nonnull String username);
}
