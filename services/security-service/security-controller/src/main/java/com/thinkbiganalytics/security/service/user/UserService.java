package com.thinkbiganalytics.security.service.user;

/*-
 * #%L
 * thinkbig-security-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.security.rest.model.UserGroup;
import com.thinkbiganalytics.security.rest.model.User;

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
    Optional<UserGroup> getGroup(@Nonnull String groupId);

    /**
     * Gets a list of all groups in Kylo.
     *
     * @return all groups
     */
    @Nonnull
    List<UserGroup> getGroups();

    /**
     * Gets the user with the specified system name.
     *
     * @param userId the system name of the user
     * @return the user, if found
     */
    @Nonnull
    Optional<User> getUser(@Nonnull String userId);

    /**
     * Gets a list of all users in Kylo.
     *
     * @return all users
     */
    @Nonnull
    List<User> getUsers();

    /**
     * Gets the list of users belonging to the specified group.
     *
     * @param groupId the system name of the group
     * @return the users
     */
    @Nonnull
    Optional<List<User>> getUsersByGroup(@Nonnull String groupId);

    /**
     * Adds or updates the specified group.
     *
     * @param group the group
     */
    void updateGroup(@Nonnull UserGroup group);

    /**
     * Adds or updates the specified user.
     *
     * @param user the user
     */
    void updateUser(@Nonnull User user);
}
