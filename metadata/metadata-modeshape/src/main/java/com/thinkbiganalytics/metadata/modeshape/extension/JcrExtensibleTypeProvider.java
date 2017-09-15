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
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType.ID;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeBuilder;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor.Type;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptorBuilder;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.TypeAlreadyExistsException;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

public class JcrExtensibleTypeProvider implements ExtensibleTypeProvider {

    @Override
    public ID resolve(final Serializable ser) {
        return (ser instanceof ID) ? (ID) ser : new JcrExtensibleType.TypeId(ser);
    }

    @Override
    public ExtensibleType getType(final ID id) {
        final JcrExtensibleType.TypeId typeId = (JcrExtensibleType.TypeId) id;
        final Session session = getSession();
        try {
            final Node typeNode = session.getNodeByIdentifier(typeId.getIdValue());
            final NodeType nodeType = session.getWorkspace().getNodeTypeManager().getNodeType(typeNode.getName());
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (ItemNotFoundException e) {
            return null;
        } catch (NoSuchNodeTypeException | PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure retriving exstenible type with ID: " + id, e);
        }
    }

    @Override
    public ExtensibleType getType(final String name) {
        final Session session = getSession();
        final String typeName = ensureTypeName(name);
        try {
            final NodeType nodeType = session.getWorkspace().getNodeTypeManager().getNodeType(typeName);
            final Node typeNode = session.getRootNode().getNode(ExtensionsConstants.TYPES + "/" + typeName);
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (NoSuchNodeTypeException | PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to lookup extensible type: " + name, e);
        }
    }

    @Override
    public List<ExtensibleType> getTypes() {
        return getTypesList("tba:extensibleEntity");
    }

    @Override
    public List<ExtensibleType> getTypes(final ExtensibleType type) {
        return getTypesList(type.getName());
    }

    @Override
    public boolean deleteType(final ID id) {
        final Session session = getSession();
        final JcrExtensibleType.TypeId typeId = (JcrExtensibleType.TypeId) id;
        try {
            final Node typeNode = session.getNodeByIdentifier(typeId.getIdValue());
            session.getWorkspace().getNodeTypeManager().unregisterNodeType(typeNode.getName());
            session.getRootNode().getNode(ExtensionsConstants.TYPES + "/" + typeNode.getName()).remove();
            return true;
        } catch (ItemNotFoundException | NoSuchNodeTypeException | NullPointerException e) {  // KYLO-8: Ignore NPE caused by unregistering a node type
            return true;
        } catch (UnsupportedRepositoryOperationException e) {
            return false;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to retrieve extensible type: " + id, e);
        }
    }

    /**
     * Return the Property names and types for a given NodeType (i.e. pass in tba:feed).
     */
    @Override
    public Set<FieldDescriptor> getPropertyDescriptors(final String nodeType) {
        return getType(nodeType).getFieldDescriptors();
    }

    @Nonnull
    @Override
    public ExtensibleTypeBuilder buildType(@Nonnull final String name) {
        return new TypeBuilder(ensureTypeName(name), null);
    }

    @Nonnull
    @Override
    public ExtensibleTypeBuilder updateType(@Nonnull final ID id) {
        try {
            final JcrExtensibleType.TypeId typeId = (JcrExtensibleType.TypeId) id;
            final Node typeNode = getSession().getNodeByIdentifier(typeId.getIdValue());
            return new TypeBuilder(typeNode.getName(), typeNode);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to retrieve extensible type: " + id, e);
        }
    }

    public void ensureTypeDescriptors() {
        try {
            Session session = JcrMetadataAccess.getActiveSession();
            Node typesNode = session.getRootNode().getNode(ExtensionsConstants.TYPES);
            NodeTypeManager typeMgr = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();
            NodeTypeIterator typeItr = typeMgr.getPrimaryNodeTypes();
            NodeType extensionsType = typeMgr.getNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE);

            while (typeItr.hasNext()) {
                NodeType type = (NodeType) typeItr.next();

                if (type.isNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE) &&
                    !type.equals(extensionsType) &&
                    !typesNode.hasNode(type.getName())) {
                    Node descrNode = typesNode.addNode(type.getName(), ExtensionsConstants.TYPE_DESCRIPTOR_TYPE);

                    descrNode.setProperty("jcr:title", simpleName(type.getName()));
                    descrNode.setProperty("jcr:description", "");

                    PropertyDefinition[] defs = type.getPropertyDefinitions();

                    for (PropertyDefinition def : defs) {
                        String fieldName = def.getName();
                        String prefix = namePrefix(fieldName);

                        if (!ExtensionsConstants.STD_PREFIXES.contains(prefix) && !descrNode.hasNode(fieldName)) {
                            Node propNode = descrNode.addNode(def.getName(), ExtensionsConstants.FIELD_DESCRIPTOR_TYPE);
                            propNode.setProperty("jcr:title", def.getName().replace("^.*:", ""));
                            propNode.setProperty("jcr:description", "");
                        }
                    }
                }
            }

            NodeIterator nodeItr = typesNode.getNodes();

            while (nodeItr.hasNext()) {
                Node typeNode = (Node) nodeItr.next();

                if (!typeMgr.hasNodeType(typeNode.getName())) {
                    typeNode.remove();
                }
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to ensure extensible type metadata", e);
        }
    }

    private String namePrefix(String name) {
        Matcher m = ExtensionsConstants.NAME_PATTERN.matcher(name);

        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private String simpleName(String name) {
        Matcher m = ExtensionsConstants.NAME_PATTERN.matcher(name);

        if (m.matches()) {
            return m.group(2);
        } else {
            return null;
        }
    }

    private List<ExtensibleType> getTypesList(final String typeName) {
        final Session session = getSession();

        try {
            final List<ExtensibleType> list = new ArrayList<>();
            final NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            final NodeTypeIterator typeItr = typeMgr.getPrimaryNodeTypes();
            final NodeType extensibleType = typeMgr.getNodeType(typeName);

            while (typeItr.hasNext()) {
                final NodeType nodeType = (NodeType) typeItr.next();

                if (nodeType.isNodeType(extensibleType.getName()) && !nodeType.equals(extensibleType)) {
                    final Node typeNode = session.getRootNode().getNode(ExtensionsConstants.TYPES + "/" + nodeType.getName());
                    list.add(new JcrExtensibleType(typeNode, nodeType));
                }
            }

            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to lookup all extensible types", e);
        }
    }

    public int asCode(final Type type) {
        switch (type) {
            case BOOLEAN:
                return PropertyType.BOOLEAN;
            case DOUBLE:
                return PropertyType.DOUBLE;
            case INTEGER:
                return PropertyType.LONG;
            case LONG:
                return PropertyType.LONG;
            case STRING:
                return PropertyType.STRING;
            case ENTITY:
                return PropertyType.REFERENCE;
            case WEAK_REFERENCE:
                return PropertyType.WEAKREFERENCE;
            default:
                return PropertyType.STRING;
        }
    }

    private Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

    /**
     * Builds the type defined by the specified builder.
     *
     * @param builder the builder for the type
     * @return the type
     * @throws MetadataRepositoryException if the repository is unavailable
     * @throws TypeAlreadyExistsException  if a type with the same name already exists
     */
    @Nonnull
    private ExtensibleType buildType(@Nonnull final TypeBuilder builder) {
        try {
            // Remove existing type and subgraph
            if (builder.node != null) {
                builder.node.remove();
            }

            // Get or create the type node
            final Session session = getSession();
            final Node typeNode;

            if (!session.nodeExists(JcrUtil.path(session.getRootNode().getPath(), ExtensionsConstants.TYPES + "/" + builder.name).toString())) {
                typeNode = session.getRootNode().addNode(ExtensionsConstants.TYPES + "/" + builder.name, ExtensionsConstants.TYPE_DESCRIPTOR_TYPE);
            } else {
                throw new TypeAlreadyExistsException(builder.name);
            }

            // Update type metadata
            if (builder.displayName != null) {
                typeNode.setProperty(JcrExtensibleType.NAME, builder.displayName);
            }
            if (builder.description != null) {
                typeNode.setProperty(JcrExtensibleType.DESCRIPTION, builder.description);
            }

            // Update type definition
            final NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            final NodeTypeTemplate nodeTemplate = typeMgr.createNodeTypeTemplate();
            nodeTemplate.setName(builder.name);

            if (builder.supertype != null) {
                final JcrExtensibleType superImpl = (JcrExtensibleType) builder.supertype;
                final String supername = superImpl.getJcrName();
                nodeTemplate.setDeclaredSuperTypeNames(new String[]{ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE, supername});
            } else {
                nodeTemplate.setDeclaredSuperTypeNames(new String[]{ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE});
            }

            // Update field definitions
            @SuppressWarnings("unchecked")
            final List<PropertyDefinitionTemplate> propertyDefinitionTemplates = nodeTemplate.getPropertyDefinitionTemplates();

            for (final FieldBuilder bldr : builder.fieldBuilders) {
                // Create field
                PropertyDefinitionTemplate propDef = typeMgr.createPropertyDefinitionTemplate();
                propDef.setName(bldr.name);
                propDef.setRequiredType(asCode(bldr.type));
                propDef.setMandatory(bldr.required);
                propDef.setMultiple(bldr.collection);
                propertyDefinitionTemplates.add(propDef);

                // Set field metadata
                Node fieldNode = typeNode.addNode(bldr.name, ExtensionsConstants.FIELD_DESCRIPTOR_TYPE);
                for (final Map.Entry<String, String> entry : bldr.metadata.entrySet()) {
                    fieldNode.setProperty(entry.getKey(), entry.getValue());
                }
            }

            NodeType nodeType = typeMgr.registerNodeType(nodeTemplate, true);
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create type: " + builder.name, e);
        }
    }

    /**
     * Ensures that the specified name is a valid type name.
     *
     * <p>The {@code tba:} namespace will be added if the name is not already namespaced.</p>
     *
     * @param name the name to be checked
     * @return a valid type name
     * @throws NullPointerException if the name is {@code null}
     */
    @Nonnull
    private String ensureTypeName(@Nullable final String name) {
        if (name == null) {
            throw new NullPointerException("Type name cannot be null");
        }

        return name.matches("^\\w*:.*") ? name : JcrMetadataAccess.TBA_PREFIX + ":" + name;
    }

    /**
     * An {@link ExtensibleTypeBuilder} that builds JCR {@link NodeType} objects.
     */
    private class TypeBuilder implements ExtensibleTypeBuilder {

        /**
         * Field definitions
         */
        @Nonnull
        private final Set<FieldBuilder> fieldBuilders = new HashSet<>();
        /**
         * Name of the node type
         */
        @Nonnull
        private final String name;
        /**
         * Node containing type definition
         */
        @Nullable
        private final Node node;
        /**
         * Human-readable specification
         */
        @Nullable
        private String description;
        /**
         * Human-readable title
         */
        @Nullable
        private String displayName;
        /**
         * Parent type
         */
        @Nullable
        private ExtensibleType supertype;

        /**
         * Constructs an {@code ExtensibleTypeBuilder} with the specified type name and optional type node.
         *
         * @param name the name of the node type
         * @param node the node containing the type definition, or {@code null} if this is a new type
         */
        TypeBuilder(@Nonnull final String name, @Nullable final Node node) {
            this.name = name;
            this.node = node;
        }

        @Override
        public ExtensibleTypeBuilder supertype(final ExtensibleType type) {
            this.supertype = type;
            return this;
        }

        @Override
        public ExtensibleTypeBuilder displayName(final String dispName) {
            this.displayName = dispName;
            return this;
        }

        @Override
        public ExtensibleTypeBuilder description(String descr) {
            this.description = descr;
            return this;
        }

        @Override
        public ExtensibleTypeBuilder addField(String name, Type type) {
            return new FieldBuilder(this).name(name).type(type).add();
        }

        @Override
        public FieldDescriptorBuilder field(String name) {
            return new FieldBuilder(this).name(name);
        }

        @Nonnull
        @Override
        public ExtensibleType build() {
            return buildType(this);
        }
    }

    /**
     * A {@link FieldDescriptorBuilder} that builds JCR {@link PropertyDefinition} objects.
     */
    private class FieldBuilder implements FieldDescriptorBuilder {

        /**
         * Metadata for this field
         */
        @Nonnull
        private final Map<String, String> metadata = new HashMap<>();
        /**
         * Parent type builder
         */
        @Nonnull
        private final TypeBuilder typeBuilder;
        /**
         * Indicates that multiple values are allowed
         */
        private boolean collection;
        /**
         * Name of this field
         */
        @Nullable
        private String name;
        /**
         * Indicates that a value is required
         */
        private boolean required;
        /**
         * Value type
         */
        private Type type;

        /**
         * Constructs a {@code FieldBuilder} with the specified parent type builder.
         *
         * @param typeBldr the parent type builder
         */
        FieldBuilder(@Nonnull final TypeBuilder typeBldr) {
            this.typeBuilder = typeBldr;
        }

        @Override
        public FieldDescriptorBuilder name(final String name) {
            this.name = name;
            return this;
        }

        @Override
        public FieldDescriptorBuilder type(final Type type) {
            this.type = type;
            return this;
        }

        @Override
        public FieldDescriptorBuilder displayName(final String name) {
            metadata.put(JcrExtensibleType.NAME, name);
            return this;
        }

        @Override
        public FieldDescriptorBuilder description(final String descr) {
            metadata.put(JcrExtensibleType.DESCRIPTION, descr);
            return this;
        }

        @Override
        public FieldDescriptorBuilder collection(final boolean flag) {
            this.collection = flag;
            return this;
        }

        @Override
        public FieldDescriptorBuilder required(final boolean flag) {
            this.required = flag;
            return this;
        }

        @Nonnull
        @Override
        public FieldDescriptorBuilder property(@Nonnull final String name, @Nonnull final String value) {
            metadata.put(name, value);
            return this;
        }

        @Override
        public ExtensibleTypeBuilder add() {
            this.typeBuilder.fieldBuilders.add(this);
            return this.typeBuilder;
        }
    }
}
