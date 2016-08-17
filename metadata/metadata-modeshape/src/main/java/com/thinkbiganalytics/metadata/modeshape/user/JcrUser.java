package com.thinkbiganalytics.metadata.modeshape.user;

import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * A {@link User} stored in a JCR repository.
 */
public class JcrUser extends AbstractJcrAuditableSystemEntity implements User {

    /** JCR node type for users */
    static final String NODE_TYPE = "tba:user";

    /** Name of the "enabled" property */
    private static final String ENABLED = "tba:enabled";

    /**
     * Constructs a {@code JcrUser} using the specified node.
     *
     * @param node the JCR node for the user
     */
    public JcrUser(@Nonnull final Node node) {
        super(node);
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

    @Nonnull
    @Override
    public String getUsername() {
        return getSystemName();
    }

    @Override
    public boolean isEnabled() {
        try {
            final Property property = node.getProperty(ENABLED);
            return property.getBoolean();
        } catch (PathNotFoundException e) {
            return false;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + ENABLED, e);
        }
    }

    /**
     * A {@link com.thinkbiganalytics.metadata.api.user.User.ID} representing a JCR User.
     */
    static class UserId extends JcrEntity.EntityId implements User.ID {

        UserId(@Nonnull final Serializable ser) {
            super(ser);
        }
    }
}
