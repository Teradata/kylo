/**
 *
 */
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

import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;

/**
 *
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
        } else if (code == PropertyType.WEAKREFERENCE) {
//                return prop.get
            return Type.WEAK_REFERENCE;
        } else {
            // Use string by default
            return FieldDescriptor.Type.STRING;
        }
    }

}
