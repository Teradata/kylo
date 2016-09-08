/**
 * 
 */
package com.thinkbiganalytics.metadata.api.user;

import java.io.Serializable;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Represents a group of users and/or other groups of users.
 * 
 * @author Sean Felten
 */
public interface UserGroup {
    
    /**
     * A unique identifier for a group.
     */
    interface ID extends Serializable {}
    
    
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
     * @param title the new title
     */
    void setTitle(String title);
    
    /**
     * @return the description of this group
     */
    String getDescription();
    
    /**
     * Sets the description of this group.
     * @param descr the new description
     */
    void setDescription(String descr);

    /**
     * @return the users that are direct members of this group
     */
    @Nonnull
    Iterable<User> getUsers();
    
    /**
     * Collects all users that are direct members of this group, as well as the transitive members
     * of all of the groups contained within this group.
     * @return an Iterable of all users of this group
     */
    @Nonnull
    Stream<User> streamAllUsers();
    
    /**
     * Adds a new user member to this group.
     * @param user the user to add
     * @return true if the user was not already a member, otherwise false
     */
    boolean addUser(@Nonnull User user);
    
    /**
     * Removes a member from this group.
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
     * Streams all groups transitively from the member groups this group
     * in a depth-first order.
     * @return an stream of all groups contained in this group
     */
    @Nonnull
    Stream<UserGroup> streamAllGroups();
    
    /**
     * Adds a new group member to this group.
     * @param group the group to add
     * @return true if the group was not already a member, otherwise false
     */
    boolean addGroup(@Nonnull UserGroup group);
    
    /**
     * Removes a group member from this group.
     * @param group the group to remove
     * @return true if the group was a member of the group, otherwise false
     */
    boolean removeGroup(@Nonnull UserGroup group);
}
