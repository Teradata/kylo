/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;

import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrFieldDescriptor implements FieldDescriptor {
    
    public static final String DESCRIPTION = JcrExtensibleType.DESCRIPTION;
    public static final String NAME = JcrExtensibleType.NAME;

    private Node descriptorNode;
    private PropertyDefinition propertyDef;
    
    public JcrFieldDescriptor(Node descNode, PropertyDefinition def) {
        this.propertyDef = def;
        this.descriptorNode = descNode;
    }

    @Override
    public Type getType() {
        return asType(this.propertyDef.getRequiredType());
    }

    @Override
    public String getName() {
        return this.propertyDef.getName();
    }

    @Override
    public String getDisplayName() {
        try {
            return this.descriptorNode.getProperty(JcrExtensibleType.NAME).getString();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("'jcr:title' missing from property descriptor", e);
        }
    }

    @Override
    public String getDescription() {
        try {
            return this.descriptorNode.getProperty(JcrExtensibleType.DESCRIPTION).getString();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("'jcr:description' missing from property descriptor", e);
        }
    }

    @Override
    public boolean isCollection() {
        return this.propertyDef.isMultiple();
    }

    @Override
    public boolean isRequired() {
        return this.propertyDef.isMandatory();
    }


    protected FieldDescriptor.Type asType(int code) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        if (code == PropertyType.BOOLEAN) {
            return FieldDescriptor.Type.BOOLEAN;
        } else if (code == PropertyType.STRING) {
            return FieldDescriptor.Type.STRING;
        } else if (code == PropertyType.LONG) {
            return FieldDescriptor.Type.LONG;
        } else if (code == PropertyType.DOUBLE) {
            return FieldDescriptor.Type.DOUBLE;
        } else if (code == PropertyType.REFERENCE) {
//                return prop.get
            return FieldDescriptor.Type.ENTITY;  // TODO look up relationship
        }
        else if (code == PropertyType.WEAKREFERENCE) {
//                return prop.get
            return Type.WEAK_REFERENCE;
        }
        else {
            // Use string by default
            return FieldDescriptor.Type.STRING;
        }
    }

}
