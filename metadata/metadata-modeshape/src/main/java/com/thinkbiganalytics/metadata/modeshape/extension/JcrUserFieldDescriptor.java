package com.thinkbiganalytics.metadata.modeshape.extension;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.google.common.base.MoreObjects;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * A {@link UserFieldDescriptor} backed by a JCR {@link PropertyDefinition}.
 */
public class JcrUserFieldDescriptor implements UserFieldDescriptor {

    /**
     * JCR node property for the {@code code} value
     */
    public static final String ORDER = JcrMetadataAccess.USR_PREFIX + ":order";

    /**
     * JCR node property for the {@code required} value
     */
    public static final String REQUIRED = JcrMetadataAccess.USR_PREFIX + ":mandatory";

    /**
     * JCR property definition
     */
    private PropertyDefinition definition;

    /**
     * JCR property node
     */
    private Node node;

    /**
     * Constructs a {@code JcrUserFieldDescriptor} for the specified node and definition.
     *
     * @param node       the property node
     * @param definition the property definition
     */
    public JcrUserFieldDescriptor(final Node node, final PropertyDefinition definition) {
        this.node = node;
        this.definition = definition;
    }

    @Nullable
    @Override
    public String getDescription() {
        try {
            return node.getProperty(JcrExtensibleType.DESCRIPTION).getString();
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to access field node: " + node, e);
        }
    }

    @Nullable
    @Override
    public String getDisplayName() {
        try {
            return node.getProperty(JcrExtensibleType.NAME).getString();
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to access field node: " + node, e);
        }
    }

    @Override
    public int getOrder() {
        try {
            final String value = node.getProperty(ORDER).getString();
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
        } catch (PathNotFoundException e) {
            return 0;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to access field node: " + node, e);
        }
    }

    @Override
    public boolean isRequired() {
        try {
            final String value = node.getProperty(REQUIRED).getString();
            return (value != null && !value.isEmpty()) && Boolean.parseBoolean(value);
        } catch (PathNotFoundException e) {
            return false;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to access field node: " + node, e);
        }
    }

    @Nonnull
    @Override
    public String getSystemName() {
        try {
            return URLDecoder.decode(definition.getName().substring(JcrMetadataAccess.USR_PREFIX.length() + 1), JcrPropertyUtil.USER_PROPERTY_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding for property \"" + definition.getName() + "\" on definition: " + definition, e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSystemName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(UserFieldDescriptor.class)
            .add("name", getSystemName())
            .toString();
    }
}
