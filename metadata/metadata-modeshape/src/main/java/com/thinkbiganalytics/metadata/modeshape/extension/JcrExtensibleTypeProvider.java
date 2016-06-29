/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
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
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType.ID;
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
    public ID resolve(Serializable ser) {
        if (ser instanceof ID) {
            return (ID) ser;
        } else {
            return new JcrExtensibleType.TypeId(ser);
        }
    }
    
    @Override
    public ExtensibleType getType(ID id) {
        JcrExtensibleType.TypeId typeId = (JcrExtensibleType.TypeId) id;
        Session session = getSession();
        try {
            Node typeNode = session.getNodeByIdentifier(typeId.getIdValue());
            NodeType nodeType = session.getWorkspace().getNodeTypeManager().getNodeType(typeNode.getName());
            
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (ItemNotFoundException e) {
            return null;
        } catch (NoSuchNodeTypeException e) {
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure retriving exstenible type with ID: " + id, e);
        }
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
    
    @Override
    public boolean deleteType(ID id) {
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     * Return the Property names and types for a given NodeType (i.e. pass in tba:feed)
     * @param nodeType
     * @return
     * @throws RepositoryException
     */
    public Set<FieldDescriptor> getPropertyDescriptors(String nodeType) {
        ExtensibleType type = getType(nodeType);
        return type.getFieldDescriptors();
    }

    @Override
    public ExtensibleTypeBuilder buildType(String name) {
        return new TypeBuilder(name);
    }

    @Override
    public ExtensibleTypeBuilder updateType(ID id) {
        JcrExtensibleType.TypeId typeId = (JcrExtensibleType.TypeId) id;
        return new TypeBuilder(typeId);
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
                typeNode.setProperty(JcrExtensibleType.NAME, typeBldr.displayName);
                typeNode.setProperty(JcrExtensibleType.DESCRIPTION, typeBldr.description);
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
                fieldNode.setProperty(JcrExtensibleType.NAME, bldr.displayName);
                fieldNode.setProperty(JcrExtensibleType.DESCRIPTION, bldr.description);
            }
            
            NodeType nodeType = typeMgr.registerNodeType(nodeTemplate, true);
            
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new extensible type: " + typeBldr.name, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private ExtensibleType updateType(TypeBuilder typeBldr, Set<FieldBuilder> fieldBldrs) {
        try {
            Session session = getSession();
            Node typeNode = session.getNodeByIdentifier(typeBldr.id.getIdValue());
            
            NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            NodeTypeTemplate nodeTemplate = typeMgr.createNodeTypeTemplate();
            nodeTemplate.setName(typeNode.getName());
            
            // TODO Do we allow change of supertype?
//            if (typeBldr.supertype != null) {
//                JcrExtensibleType superImpl = (JcrExtensibleType) typeBldr.supertype;
//                String supername = superImpl.getJcrName();
//                nodeTemplate.setDeclaredSuperTypeNames(new String[] { ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE, supername });
//            } else {
//                nodeTemplate.setDeclaredSuperTypeNames(new String[] { ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE });
//            }
            
            for (FieldBuilder bldr : fieldBldrs) {
                PropertyDefinitionTemplate propDef = typeMgr.createPropertyDefinitionTemplate();
                propDef.setName(bldr.name);
                propDef.setRequiredType(asCode(bldr.type));
                propDef.setMandatory(bldr.required);
                propDef.setMultiple(bldr.collection);
                nodeTemplate.getPropertyDefinitionTemplates().add(propDef);
                
                Node fieldNode = typeNode.addNode(bldr.name, ExtensionsConstants.FIELD_DESCRIPTOR_TYPE);
                fieldNode.setProperty(JcrExtensibleType.NAME, bldr.displayName);
                fieldNode.setProperty(JcrExtensibleType.DESCRIPTION, bldr.description);
            }
            
            NodeType nodeType = typeMgr.registerNodeType(nodeTemplate, true);
            
            return new JcrExtensibleType(typeNode, nodeType);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new extensible type: " + typeBldr.name, e);
        }
    }

    private class TypeBuilder implements ExtensibleTypeBuilder {
        
        private final JcrExtensibleType.TypeId id;
        private final String name;
        private Set<FieldBuilder> fieldBuilders = new HashSet<>();
        private ExtensibleType supertype;
        private String displayName;
        private String description;

        public TypeBuilder(String name) {
            this.name = name;
            this.id = null;
        }
        
        public TypeBuilder(JcrExtensibleType.TypeId id) {
            this.name = null;
            this.id = id;
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
            if (this.name != null) {
                return createType(this, this.fieldBuilders);
            } else {
                return updateType(this, this.fieldBuilders);
            }
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
        public FieldDescriptorBuilder collection(boolean flag) {
            this.collection = flag;
            return this;
        }

        @Override
        public FieldDescriptorBuilder required(boolean flag) {
            this.required = flag;
            return this;
        }

        @Override
        public ExtensibleTypeBuilder add() {
            this.typeBuilder.fieldBuilders.add(this);
            return this.typeBuilder;
        }
        
    }

}
