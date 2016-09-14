package com.thinkbiganalytics.security.service.user;

import com.thinkbiganalytics.security.rest.model.GroupPrincipal;
import com.thinkbiganalytics.security.rest.model.UserPrincipal;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides access to Kylo users and groups.
 */
public interface UserService {

    /**
     * Deletes the specified group from Kylo.
     *
     * @param groupId the system name of the group
     * @return {@code true} if the group was deleted, or {@code false} if the group does not exist
     */
    boolean deleteGroup(@Nonnull String groupId);

    /**
     * Deletes the specified user from Kylo.
     *
     * @param userId the system name of the user
     * @return {@code true} if the user was deleted, or {@code false} if the user does not exist
     */
    boolean deleteUser(@Nonnull String userId);

    /**
     * Gets the group with the specified system name.
     *
     * @param groupId the system name of the group
     * @return the group, if found
     */
    @Nonnull
    Optional<GroupPrincipal> getGroup(@Nonnull String groupId);

    /**
     * Gets a list of all groups in Kylo.
     *
     * @return all groups
     */
    @Nonnull
    List<GroupPrincipal> getGroups();

    /**
     * Gets the user with the specified system name.
     *
     * @param userId the system name of the user
     * @return the user, if found
     */
    @Nonnull
    Optional<UserPrincipal> getUser(@Nonnull String userId);

    /**
     * Gets a list of all users in Kylo.
     *
     * @return all users
     */
    @Nonnull
    List<UserPrincipal> getUsers();

    /**
     * Adds or updates the specified group.
     *
     * @param group the group
     */
    void updateGroup(@Nonnull GroupPrincipal group);

    /**
     * Adds or updates the specified user.
     *
     * @param user the user
     */
    void updateUser(@Nonnull UserPrincipal user);
}
