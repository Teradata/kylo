/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeBuilder;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptorBuilder;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor.Type;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.TypeAlreadyExistsException;

/**
 *
 * @author Sean Felten
 */
public class JcrExtensibleTypeProvider implements ExtensibleTypeProvider {

    /**
     * 
     */
    public JcrExtensibleTypeProvider() {
    }


    @Override
    public ExtensibleType getType(String name) {
        Session session = getSession();
        String typeName = name.matches("^\\w*:.*") ? name : JcrMetadataAccess.TBA_PREFIX + ":" + name;
        
        try {
            NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            NodeType nodeType = typeMgr.getNodeType(typeName);
            Node typeNode = session.getRootNode().getNode(ExtensionsConstants.TYPES + "/" + typeName);
            
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (NoSuchNodeTypeException e) {
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
    public List<ExtensibleType> getTypes(ExtensibleType type) {
        return getTypesList(type.getName());
    }
    
    /**
     * Return the Property names and types for a given NodeType (i.e. pass in tba:feed)
     * @param nodeType
     * @return
     * @throws RepositoryException
     */
    public Set<FieldDescriptor> getPropertyDescriptors(String nodeType) {
        ExtensibleType type = getType(nodeType);
        return type.getPropertyDescriptors();
    }

    @Override
    public ExtensibleTypeBuilder buildType(String name) {
        return new TypeBuilder(name);
    }

    private List<ExtensibleType> getTypesList(String typeName) {
        Session session = getSession();
        
        try {
            List<ExtensibleType> list = new ArrayList<ExtensibleType>();
            NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            NodeTypeIterator typeItr = typeMgr.getPrimaryNodeTypes();
            NodeType extensibleType = typeMgr.getNodeType(typeName);
            
            while (typeItr.hasNext()) {
                NodeType nodeType = (NodeType) typeItr.next();
                
                if (nodeType.isNodeType(extensibleType.getName()) && ! nodeType.equals(extensibleType)) {
                    Node typeNode = session.getRootNode().getNode(ExtensionsConstants.TYPES + "/" + nodeType.getName());
                    
                    list.add(new JcrExtensibleType(typeNode, nodeType));
                }
            }
            
            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to lookup all extensible types", e);
        }
    }

    private int asCode(Type type) {
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
            default:
                return PropertyType.STRING;
        }
    }

    private Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

    
    @SuppressWarnings("unchecked")
    private ExtensibleType createType(TypeBuilder typeBldr, Set<FieldBuilder> fieldBldrs) {
        try {
            Session session = getSession();
            String typeName = JcrMetadataAccess.TBA_PREFIX + ":" + typeBldr.name;
            Node typeNode = null;
            
            if (! session.nodeExists("/" + ExtensionsConstants.TYPES + "/" + typeBldr.name)) {
                typeNode = session.getRootNode().addNode(ExtensionsConstants.TYPES + "/" + typeName, ExtensionsConstants.TYPE_DESCRIPTOR_TYPE);
                typeNode.setProperty("jcr:title", typeBldr.displayName);
                typeNode.setProperty("jcr:description", typeBldr.description);
            } else {
                throw new TypeAlreadyExistsException(typeBldr.name);
            }
            
            NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            NodeTypeTemplate nodeTemplate = typeMgr.createNodeTypeTemplate();
            nodeTemplate.setName(typeName);
            
            if (typeBldr.supertype != null) {
                JcrExtensibleType superImpl = (JcrExtensibleType) typeBldr.supertype;
                String supername = superImpl.getJcrName();
                nodeTemplate.setDeclaredSuperTypeNames(new String[] { ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE, supername });
            } else {
                nodeTemplate.setDeclaredSuperTypeNames(new String[] { ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE });
            }
            
            for (FieldBuilder bldr : fieldBldrs) {
                PropertyDefinitionTemplate propDef = typeMgr.createPropertyDefinitionTemplate();
                propDef.setName(bldr.name);
                propDef.setRequiredType(asCode(bldr.type));
                propDef.setMandatory(bldr.required);
                propDef.setMultiple(bldr.collection);
                nodeTemplate.getPropertyDefinitionTemplates().add(propDef);
                
                Node fieldNode = typeNode.addNode(bldr.name, ExtensionsConstants.FIELD_DESCRIPTOR_TYPE);
                fieldNode.setProperty("jcr:title", bldr.displayName);
                fieldNode.setProperty("jcr:description", bldr.description);
            }
            
            NodeType nodeType = typeMgr.registerNodeType(nodeTemplate, true);
            
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new extensible type: " + typeBldr.name, e);
        }
    }

    private class TypeBuilder implements ExtensibleTypeBuilder {
        
        private Set<FieldBuilder> fieldBuilders = new HashSet<>();
        private ExtensibleType supertype;
        private String name;
        private String displayName;
        private String description;

        public TypeBuilder(String name) {
            this.name = name;
        }

        @Override
        public ExtensibleTypeBuilder supertype(ExtensibleType type) {
            this.supertype = type;
            return this;
        }
        
        @Override
        public ExtensibleTypeBuilder displayName(String dispName) {
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

        @Override
        public ExtensibleType build() {
            return createType(this, this.fieldBuilders);
        }
    }
    
    private class FieldBuilder implements FieldDescriptorBuilder {
        
        private final TypeBuilder typeBuilder;
        private String name;
        private Type type;
        private String displayName;
        private String description;
        private boolean collection;
        private boolean required;
        
        public FieldBuilder(TypeBuilder typeBldr) {
            this.typeBuilder = typeBldr;
        }

        @Override
        public FieldDescriptorBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public FieldDescriptorBuilder type(Type type) {
            this.type = type;
            return this;
        }

        @Override
        public FieldDescriptorBuilder displayName(String name) {
            this.displayName = name;
            return this;
        }

        @Override
        public FieldDescriptorBuilder description(String descr) {
            this.description = descr;
            return this;
        }

        @Override
        public FieldDescriptorBuilder collection() {
            this.collection = true;
            return this;
        }

        @Override
        public FieldDescriptorBuilder required() {
            this.required = true;
            return this;
        }

        @Override
        public ExtensibleTypeBuilder add() {
            this.typeBuilder.fieldBuilders.add(this);
            return this.typeBuilder;
        }
        
    }

}
