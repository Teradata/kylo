package com.thinkbiganalytics.metadata.api.user;

import com.thinkbiganalytics.security.GroupPrincipal;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Metadata for a user principal.
 *
 * <p>Users must have an entry in the metastore in order to access Kylo. This class may be used for authentication if it has been enabled in the services configuration file.</p>
 */
public interface User {

    /**
     * A unique identifier for a user.
     */
    interface ID extends Serializable {}

    /**
     * Gets the display name for this user.
     *
     * @return the display name
     */
    @Nullable
    String getDisplayName();

    /**
     * Sets the display name for this user.
     *
     * @param displayName the display name
     */
    void setDisplayName(@Nullable String displayName);

    /**
     * Gets the email address for this user.
     *
     * @return the email address
     */
    @Nullable
    String getEmail();

    /**
     * Sets the email address for this user.
     *
     * @param email the email address
     */
    void setEmail(@Nullable String email);

    /**
     * Gets the unique identifier for this user.
     *
     * @return the user identifier
     */
    @Nonnull
    ID getId();

    /**
     * Gets the (hashed) password for this user.
     *
     * @return the password, typically hashed
     */
    @Nullable
    String getPassword();

    /**
     * Sets the (hashed) password for this user.
     *
     * @param password the password, typically hashed
     */
    void setPassword(@Nullable String password);

    /**
     * Gets the login name for this user.
     *
     * @return the login name
     */
    @Nonnull
    String getSystemName();

    /**
     * Indicates that the user may access Kylo.
     *
     * @return {@code true} if the user may login, or {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Enables or disables access to Kylo for this user.
     */
    void setEnabled(boolean enabled);

    /**
     * @return all of the groups of which this user is a direct member
     */
    Set<UserGroup> getContainingGroups();

    /**
     * @return all of the groups of which this user is a member; both directly and transitively.
     */
    Set<UserGroup> getAllContainingGroups();

    /**
     * @return the principal representing this user
     */
    Principal getPrincipal();

    /**
     * Collects a set of all group principals, both direct membership and transitive membership, 
     * associated with this user.
     * @return the set of group principals
     */
    @Nonnull
    Set<GroupPrincipal> getAllGroupPrincipals();

    /**
     * Gets the list of all groups this user belongs to.
     *
     * @return the user's groups
     */
    @Nonnull
    Set<UserGroup> getGroups();

    /**
     * Sets the groups this user belongs to.
     *
     * @param groups the groups
     */
    void setGroups(@Nonnull Set<UserGroup> groups);
}
