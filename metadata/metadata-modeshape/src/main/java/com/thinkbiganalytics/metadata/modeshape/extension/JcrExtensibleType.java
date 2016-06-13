/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity.ID;
import com.thinkbiganalytics.metadata.core.BaseId;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrExtensibleType implements ExtensibleType {
    
    private TypeId id;
    private Node typeNode;
    private final NodeType nodeType;

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
    public ExtensibleType getParentType() {
        try {
            for (NodeType parent : this.nodeType.getDeclaredSupertypes()) {
                if (parent.isNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE) && 
                                ! parent.getName().equals(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE)) {
                    Node supertypeNode = this.typeNode.getParent().getNode(parent.getName());
                    return new JcrExtensibleType(supertypeNode, parent);
                }
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get parent type for type: " + this.nodeType.getName(), e);
        }
        
        return null;
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
