package com.thinkbiganalytics.metadata.modeshape.domaintype;

import com.thinkbiganalytics.metadata.api.domaintype.DomainType;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * An implementation of {@link DomainType} backed by a JCR store.
 */
public class JcrDomainType extends AbstractJcrAuditableSystemEntity implements DomainType {

    /**
     * JCR node type
     */
    public static String NODE_TYPE = "tba:domainType";

    /**
     * Name of field policy JCR field
     */
    private final static String FIELD_POLICY_JSON = "tba:fieldPolicyJson";

    /**
     * Name of icon JCR field
     */
    private final static String ICON = "tba:icon";

    /**
     * Name of icon color JCR field
     */
    private final static String ICON_COLOR = "tba:iconColor";

    /**
     * Name of regex JCR field
     */
    private final static String REGEX = "tba:regex";

    /**
     * Constructs a domain type from the specified JCR node.
     */
    public JcrDomainType(@Nonnull final Node node) {
        super(node);
    }

    @Override
    public DomainTypeId getId() {
        try {
            return new JcrDomainType.DomainTypeId(getObjectId());
        } catch (final RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Override
    public String getFieldPolicyJson() {
        return getProperty(FIELD_POLICY_JSON, String.class);
    }

    @Override
    public void setFieldPolicyJson(final String value) {
        setProperty(FIELD_POLICY_JSON, value);
    }

    @Override
    public String getIcon() {
        return getProperty(ICON, String.class);
    }

    @Override
    public void setIcon(final String value) {
        setProperty(ICON, value);
    }

    @Override
    public String getIconColor() {
        return getProperty(ICON_COLOR, String.class);
    }

    @Override
    public void setIconColor(final String value) {
        setProperty(ICON_COLOR, value);
    }

    @Override
    public String getRegex() {
        return getProperty(REGEX, String.class);
    }

    @Override
    public void setRegex(final String value) {
        setProperty(REGEX, value);
    }

    /**
     * A domain type identifier backed by a JCR store.
     */
    public static class DomainTypeId extends JcrEntity.EntityId implements DomainType.ID {

        /**
         * Constructs a domain type identifier.
         */
        public DomainTypeId(@Nonnull final Serializable ser) {
            super(ser);
        }
    }
}
