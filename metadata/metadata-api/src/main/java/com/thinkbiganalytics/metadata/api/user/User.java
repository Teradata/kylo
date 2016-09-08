package com.thinkbiganalytics.metadata.api.user;

import java.io.Serializable;

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
     *
     * @param enabled {@code true} if the user may login, or {@code false} otherwise
     */
    void setEnabled(boolean enabled);
}
