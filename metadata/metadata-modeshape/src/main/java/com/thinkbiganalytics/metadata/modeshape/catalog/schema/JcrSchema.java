package com.thinkbiganalytics.metadata.modeshape.catalog.schema;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.metadata.api.catalog.Schema;
import com.thinkbiganalytics.metadata.api.catalog.SchemaField;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.collections.ListUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

public class JcrSchema extends JcrEntity<JcrSchema.SchemaId> implements SystemEntityMixin, Schema {

    public static final String NODE_TYPE = "tba:Schema";

    private static final String CHARSET = "tba:charset";
    private static final String FIELDS = "fields";


    static class SchemaId extends JcrEntity.EntityId implements Schema.ID {

        private static final long serialVersionUID = 1L;

        SchemaId(Serializable ser) {
            super(ser);
        }
    }

    public JcrSchema(Node node) {
        super(node);
    }

    @Override
    public JcrSchema.SchemaId getId() {
        try {
            return new JcrSchema.SchemaId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Override
    public String getCharset() {
        return JcrPropertyUtil.getString(getNode(), CHARSET);
    }

    @Override
    public void setCharset(String name) {
        try {
            getNode().setProperty(CHARSET, name);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set " + CHARSET, e);
        }
    }

    @Override
    public List<? extends SchemaField> getFields() {
        try {
            Node fieldsNode = getSchemaFieldsNode();
            NodeType type = JcrUtil.getNodeType(getNode().getSession(), JcrSchemaField.NODE_TYPE);
            return JcrUtil.getJcrObjects(fieldsNode, type, JcrSchemaField.class);
        } catch (RepositoryException e) {
            log.error("Unable to get dataset fields ", e);
            return Collections.emptyList();
        }
    }

    private Node getSchemaFieldsNode() {
        return JcrUtil.getNode(getNode(), FIELDS);
    }

    @Override
    public void setFields(List<? extends SchemaField> fields) {
        //remove previous fields
        List<? extends SchemaField> jcrObjects = getFields();
        jcrObjects.forEach(f -> {
            ((JcrSchemaField) f).remove();
        });

        //create new fields
        if (fields != null) {
            Node schemaFieldsNode = getSchemaFieldsNode();
            fields.forEach(schemaField -> {
                Node fieldNode = JcrUtil.createNode(schemaFieldsNode, schemaField.getSystemName(), JcrSchemaField.NODE_TYPE);
                JcrSchemaField field = JcrUtil.createJcrObject(fieldNode, JcrSchemaField.class);
                field.setDatatype(schemaField.getDatatype());
                field.setDescription(schemaField.getDescription());
                field.setName(schemaField.getName());
                field.setSystemName(schemaField.getSystemName());
            });
        }
    }
}
