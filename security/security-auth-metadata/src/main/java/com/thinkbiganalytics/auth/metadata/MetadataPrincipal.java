package com.thinkbiganalytics.auth.metadata;

import com.google.common.base.MoreObjects;

import java.security.Principal;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A user from the metadata store.
 */
public class MetadataPrincipal implements Principal {

    /** Login name for the user */
    @Nonnull
    private final String username;

    /**
     * Constructs a {@code MetadataPrincipal} with the specified user's login name.
     *
     * @param username the login name
     */
    public MetadataPrincipal(@Nonnull final String username) {
        this.username = username;
    }

    @Override
    public String getName() {
        return this.username;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return (obj instanceof MetadataPrincipal && Objects.equals(username, ((MetadataPrincipal) obj).username));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", username).toString();
    }
}
