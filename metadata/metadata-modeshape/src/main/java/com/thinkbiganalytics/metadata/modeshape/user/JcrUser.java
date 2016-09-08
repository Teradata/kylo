package com.thinkbiganalytics.metadata.modeshape.user;

import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * A {@link User} stored in a JCR repository.
 */
public class JcrUser extends AbstractJcrAuditableSystemEntity implements User {

    /** JCR node type for users */
    static final String NODE_TYPE = "tba:user";

    /** Name of the {@code displayName} property */
    private static final String DISPLAY_NAME = "tba:displayName";

    /** Name of the {@code email} property */
    private static final String EMAIL = "tba:email";

    /** Name of the {@code enabled} property */
    private static final String ENABLED = "tba:enabled";

    /** Name of the {@code password} property */
    private static final String PASSWORD = "tba:password";

    /**
     * Constructs a {@code JcrUser} using the specified node.
     *
     * @param node the JCR node for the user
     */
    public JcrUser(@Nonnull final Node node) {
        super(node);
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return getProperty(DISPLAY_NAME, String.class);
    }

    @Override
    public void setDisplayName(@Nullable final String displayName) {
        setProperty(DISPLAY_NAME, displayName);
    }

    @Nullable
    @Override
    public String getEmail() {
        return getProperty(EMAIL, String.class);
    }

    @Override
    public void setEmail(@Nullable final String email) {
        setProperty(EMAIL, email);
    }

    @Nonnull
    @Override
    public UserId getId() {
        try {
            return new UserId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return getProperty(ENABLED, false);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        setProperty(ENABLED, enabled);
    }

    @Nullable
    @Override
    public String getPassword() {
        return getProperty(PASSWORD, String.class);
    }

    @Override
    public void setPassword(@Nullable final String password) {
        setProperty(PASSWORD, password);
    }

    /**
     * A {@link com.thinkbiganalytics.metadata.api.user.User.ID} representing a JCR User.
     */
    static class UserId extends JcrEntity.EntityId implements User.ID {

        private static final long serialVersionUID = 1780033096808176536L;

        /**
         * Constructs a {@code UserId} with the specified username.
         *
         * @param ser the username
         */
        UserId(@Nonnull final Serializable ser) {
            super(ser);
        }
    }
}
