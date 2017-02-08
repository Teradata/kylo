/**
 *
 */
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

import com.thinkbiganalytics.security.GroupPrincipal;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Represents a group of users and/or other groups of users.
 */
public interface UserGroup {

    /**
     * @return the ID of this group
     */
    @Nonnull
    ID getId();

    /**
     * @return the unique name of this group
     */
    @Nonnull
    String getSystemName();

    /**
     * @return the human readable title for this group, or the system name if none has been set.
     */
    String getTitle();

    /**
     * Sets a human-readable title for this group.
     *
     * @param title the new title
     */
    void setTitle(String title);

    /**
     * @return the description of this group
     */
    String getDescription();

    /**
     * Sets the description of this group.
     *
     * @param descr the new description
     */
    void setDescription(String descr);

    /**
     * Indicates that the group is available to be assigned users.
     *
     * @return {@code true} if the group is enabled, or {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Enables or disables whether the group is available to be assigned users.
     */
    void setEnabled(boolean enabled);

    /**
     * @return the users that are direct members of this group
     */
    @Nonnull
    Iterable<User> getUsers();

    /**
     * Collects all users that are direct members of this group, as well as the transitive members
     * of all of the groups contained within this group.
     *
     * @return an Iterable of all users of this group
     */
    @Nonnull
    Stream<User> streamAllUsers();

    /**
     * Adds a new user member to this group.
     *
     * @param user the user to add
     * @return true if the user was not already a member, otherwise false
     */
    boolean addUser(@Nonnull User user);

    /**
     * Removes a member from this group.
     *
     * @param user the user to remove
     * @return true if the user was a member of the group, otherwise false
     */
    boolean removeUser(@Nonnull User user);

    /**
     * @return an Iterable of groups directly contained within this group
     */
    @Nonnull
    Iterable<UserGroup> getGroups();

    /**
     * @return an set of all groups of which this group is a direct member
     */
    @Nonnull
    Set<UserGroup> getContainingGroups();

    /**
     * @return all of the groups of which this group is a member; both directly and transitively.
     */
    Set<UserGroup> getAllContainingGroups();

    /**
     * Streams all groups transitively from the member groups this group
     * in a depth-first order.
     *
     * @return an stream of all groups contained in this group
     */
    @Nonnull
    Stream<UserGroup> streamAllGroups();

    /**
     * Adds a new group member to this group.
     *
     * @param group the group to add
     * @return true if the group was not already a member, otherwise false
     */
    boolean addGroup(@Nonnull UserGroup group);

    /**
     * Removes a group member from this group.
     *
     * @param group the group to remove
     * @return true if the group was a member of the group, otherwise false
     */
    boolean removeGroup(@Nonnull UserGroup group);

    /**
     * @return the full group principal (i.e. containing member principals) associated with this group
     */
    GroupPrincipal getPrincial();

    /**
     * @return the group root principal (i.e. for the group only without member principals) associated with this group
     */
    GroupPrincipal getRootPrincial();

    /**
     * A unique identifier for a group.
     */
    interface ID extends Serializable {

    }
}
