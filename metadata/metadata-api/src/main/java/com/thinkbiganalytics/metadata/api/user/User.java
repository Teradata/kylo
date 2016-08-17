package com.thinkbiganalytics.metadata.api.user;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * Authorization metadata for a user.
 *
 * <p>Authentication is handled separately in the security module. This interface only provides authorization metadata specific to Pipeline Controller about a user.</p>
 */
public interface User {

    /**
     * A unique identifier for a user.
     */
    interface ID extends Serializable {}

    /**
     * Gets the unique identifier for this user.
     *
     * @return the user identifier
     */
    @Nonnull
    ID getId();

    /**
     * Gets the login name for this user.
     *
     * @return the login name
     */
    @Nonnull
    String getUsername();

    /**
     * Indicates that the user may access the Pipeline Controller.
     *
     * @return {@code true} if the user may login, or {@code false} otherwise
     */
    boolean isEnabled();
}
