package com.thinkbiganalytics.metadata.modeshape.catalog.schema;

import com.thinkbiganalytics.metadata.api.catalog.SchemaField;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.TaggableMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class JcrSchemaField extends JcrEntity<JcrSchemaField.SchemaFieldId> implements TaggableMixin, SchemaField {

    public static final String NODE_TYPE = "tba:SchemaField";
    private static final String SYSTEM_NAME = "tba:systemName";
    private static final String NAME = "tba:name";
    private static final String DESCRIPTION = "tba:description";
    private static final String DATATYPE = "tba:datatype";

    static class SchemaFieldId extends JcrEntity.EntityId implements SchemaField.ID {
        private static final long serialVersionUID = 1L;
        SchemaFieldId(Serializable ser) {
            super(ser);
        }
    }

    public JcrSchemaField(Node node) {
        super(node);
    }

    @Override
    public JcrSchemaField.SchemaFieldId getId() {
        try {
            return new JcrSchemaField.SchemaFieldId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Override
    public void setDatatype(String datatype) {
        try {
            getNode().setProperty(DATATYPE, datatype);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set " + DATATYPE, e);
        }
    }

    @Override
    public String getDatatype() {
        return JcrPropertyUtil.getString(getNode(), DATATYPE);
    }

    @Override
    public void setDescription(String description) {
        try {
            getNode().setProperty(DESCRIPTION, description);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set " + DESCRIPTION, e);
        }
    }

    @Override
    public String getDescription() {
        return JcrPropertyUtil.getString(getNode(), DESCRIPTION);
    }

    @Override
    public void setName(String name) {
        try {
            getNode().setProperty(NAME, name);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set " + NAME, e);
        }
    }


    @Override
    public String getName() {
        return JcrPropertyUtil.getString(getNode(), NAME);
    }

    @Override
    public void setSystemName(String systemName) {
        try {
            getNode().setProperty(SYSTEM_NAME, systemName);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set " + SYSTEM_NAME, e);
        }
    }

    @Override
    public String getSystemName() {
        return JcrPropertyUtil.getString(getNode(), SYSTEM_NAME);
    }

}
