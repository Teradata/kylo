package com.thinkbiganalytics.metadata.api.user;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.thinkbiganalytics.metadata.api.user.UserGroup.ID;

/**
 * Provides access to stored users and user groups.
 */
public interface UserProvider {
    
    User.ID resolveUserId(@Nonnull final Serializable id);
    
    UserGroup.ID resolveGroupId(@Nonnull final Serializable id);
    
    boolean userExists(@Nonnull String systemName);

    @Nonnull
    Optional<User> findUserById(@Nonnull User.ID id);

    /**
     * Gets or creates the user with the specified system name.
     *
     * @param systemName the username
     * @return the user
     */
    @Nonnull
    User ensureUser(@Nonnull String systemName);
    
    /**
     * Creates the user with the specified system name if one does not already exists.
     *
     * @param systemName the username
     * @return the new user
     * @throws UserAlreadyExistsException thrown if a user with the same name already exists
     */
    @Nonnull
    User createUser(@Nonnull String systemName);

    /**
     * Finds the user with the specified system name.
     *
     * @param systemName the system name to find
     * @return the user, if found
     */
    @Nonnull
    Optional<User> findUserBySystemName(@Nonnull String systemName);

    @Nonnull
    Iterable<User> findUsers();
    
    @Nonnull
    UserGroup ensureGroup(@Nonnull String groupName);
    
    @Nonnull
    UserGroup createGroup(@Nonnull String groupName);

    @Nonnull
    Optional<UserGroup> findGroupById(ID id);

    @Nonnull
    Optional<UserGroup> findGroupByName(@Nonnull String groupName);
    
    @Nonnull
    Iterable<UserGroup> findGroups();
}
