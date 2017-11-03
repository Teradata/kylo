package com.thinkbiganalytics.metadata.modeshape.domaintype;

/*-
 * #%L
 * kylo-metadata-modeshape
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
    public static final String NODE_TYPE = "tba:domainType";

    /**
     * Name of field metadata JCR field
     */
    private final static String FIELD_JSON = "tba:fieldJson";

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
     * Name of regex flags JCR field
     */
    private final static String REGEX_FLAGS = "tba:regexFlags";

    /**
     * Name of regex pattern JCR field
     */
    private final static String REGEX_PATTERN = "tba:regexPattern";

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
    public String getFieldJson() {
        return getProperty(FIELD_JSON, String.class);
    }

    @Override
    public void setFieldJson(String value) {
        setProperty(FIELD_JSON, value);
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
    public String getRegexFlags() {
        return getProperty(REGEX_FLAGS, String.class);
    }

    @Override
    public void setRegexFlags(String value) {
        setProperty(REGEX_FLAGS, value);
    }

    @Override
    public String getRegexPattern() {
        return getProperty(REGEX_PATTERN, String.class);
    }

    @Override
    public void setRegexPattern(final String value) {
        setProperty(REGEX_PATTERN, value);
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
