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

import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.core.BaseId;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * An implementation of {@link ExtensibleType} backed by JCR {@link NodeType} objects.
 */
public class JcrExtensibleType implements ExtensibleType {

    public static final String LAST_MODIFIED_TIME = "jcr:lastModified";
    public static final String CREATED_TIME = "jcr:created";
    public static final String DESCRIPTION = "jcr:description";
    public static final String NAME = "jcr:title";
    private final NodeType nodeType;
    private TypeId id;
    private Node typeNode;

    /**
     *
     */
    public JcrExtensibleType(Node typeNode, NodeType nodeDef) {
        this.typeNode = typeNode;
        this.nodeType = nodeDef;
        try {
            this.id = new TypeId(this.typeNode.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }

    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.nodeType.toString();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleType#getId()
     */
    @Override
    public ID getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleType#getName()
     */
    @Override
    public String getName() {
        return getJcrName().replace(JcrMetadataAccess.TBA_PREFIX + ":", "");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleType#getParentType()
     */
    @Override
    public ExtensibleType getSupertype() {
        try {
            for (NodeType parent : this.nodeType.getDeclaredSupertypes()) {
                if (parent.isNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE) &&
                    !parent.getName().equals(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE)) {
                    Node supertypeNode = this.typeNode.getParent().getNode(parent.getName());
                    return new JcrExtensibleType(supertypeNode, parent);
                }
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get parent type for type: " + this.nodeType.getName(), e);
        }

        return null;
    }

    @Override
    public String getDiplayName() {
        try {
            return this.typeNode.getProperty(NAME).getString();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the display name (title) for type: " + this.nodeType.getName(), e);
        }
    }

    @Override
    public String getDesciption() {
        try {
            return this.typeNode.getProperty(DESCRIPTION).getString();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the description of type: " + this.nodeType.getName(), e);
        }
    }

    @Override
    public DateTime getCreatedTime() {
        try {
            return new DateTime(this.typeNode.getProperty(CREATED_TIME).getDate());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get created time for type: " + this.nodeType.getName(), e);
        }
    }

    @Override
    public DateTime getModifiedTime() {
        try {
            return new DateTime(this.typeNode.getProperty(LAST_MODIFIED_TIME).getDate());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get modified time for type: " + this.nodeType.getName(), e);
        }
    }

    public String getJcrName() {
        return this.nodeType.getName();
    }

    @Override
    public Set<FieldDescriptor> getFieldDescriptors() {
        try {
            Set<FieldDescriptor> set = new HashSet<>();

            for (PropertyDefinition def : this.nodeType.getPropertyDefinitions()) {
                if (this.typeNode.hasNode(def.getName())) {
                    Node descrNode = this.typeNode.getNode(def.getName());
                    set.add(new JcrFieldDescriptor(descrNode, def));
                }
            }

            return set;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get property descriptors for type: " + this.nodeType.getName(), e);
        }
    }

    @Override
    public FieldDescriptor getFieldDescriptor(String name) {
        try {
            for (PropertyDefinition def : this.nodeType.getPropertyDefinitions()) {
                if (def.getName().equalsIgnoreCase(name)) {
                    Node descrNode = this.typeNode.getNode(def.getName());
                    return new JcrFieldDescriptor(descrNode, def);
                }
            }

            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get property descriptor for type: " + this.nodeType.getName(), e);
        }
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getUserFieldDescriptors() {
        final String prefix = JcrMetadataAccess.USR_PREFIX + ":";
        return Arrays.stream(nodeType.getPropertyDefinitions())
            .filter(property -> property.getName().startsWith(prefix))
            .map(property -> {
                try {
                    final Node descNode = typeNode.getNode(property.getName());
                    return new JcrUserFieldDescriptor(descNode, property);
                } catch (RepositoryException e) {
                    throw new MetadataRepositoryException("Unable to access property: " + property, e);
                }
            })
            .collect(Collectors.toSet());
    }

    public static class TypeId extends BaseId implements ID {

        private static final long serialVersionUID = -7707175033124386499L;

        private String idValue;

        public TypeId() {
        }

        public TypeId(Serializable ser) {
            super(ser);
        }

        public String getIdValue() {
            return idValue;
        }

        @Override
        public String toString() {
            return idValue;
        }

        @Override
        public UUID getUuid() {
            return UUID.fromString(idValue);
        }

        @Override
        public void setUuid(UUID uuid) {
            this.idValue = uuid.toString();

        }
    }
}
