package com.thinkbiganalytics.metadata.api.user;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.user.UserGroup.ID;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nonnull;

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

    /**
     * Deletes the specified group.
     *
     * @param group the group
     */
    void deleteGroup(@Nonnull UserGroup group);

    /**
     * Deletes the specified user.
     *
     * @param user the user
     */
    void deleteUser(@Nonnull User user);

    /**
     * Saves the specified group to the metastore.
     *
     * @param group the group
     * @return the group
     */
    @Nonnull
    UserGroup updateGroup(@Nonnull UserGroup group);

    /**
     * Saves the specified user to the metastore.
     *
     * @param user the user
     * @return the user
     */
    @Nonnull
    User updateUser(@Nonnull User user);
}
