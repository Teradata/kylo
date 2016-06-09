/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrExtensibleType implements ExtensibleType {
    
    private Node typeNode;
    private final NodeType nodeType;

    /**
     * 
     */
    public JcrExtensibleType(Node typeNode, NodeType nodeDef) {
        this.typeNode = typeNode;
        this.nodeType = nodeDef;
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
        // TODO Auto-generated method stub
        return null;
    }

    public String getJcrName() {
        return this.nodeType.getName();
    }

    @Override
    public Set<FieldDescriptor> getPropertyDescriptors() {
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
    public FieldDescriptor getPropertyDescriptor(String name) {
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

}
