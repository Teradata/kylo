package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 11/14/16.
 */
public class JcrDatasourceDefinition extends AbstractJcrAuditableSystemEntity implements DatasourceDefinition {

    public static final String CONNECTION_TYPE = "tba:connectionType";

    public static final String DATASOURCE_TYPE = "tba:datasourceType";

    public static final String PROCESSOR_TYPE = "tba:processorType";

    public static final String DATASOURCE_PROPERTY_KEYS = "tba:datasourcePropertyKeys";

    public static final String IDENTITY_STRING = "tba:identityString";



    public static final String NODE_TYPE = "tba:datasourceDefinition";

    public JcrDatasourceDefinition(Node node) {
        super(node);
    }


    public Set<String> getDatasourcePropertyKeys() {
        Set<String> props = JcrPropertyUtil.getSetProperty(this.node, DATASOURCE_PROPERTY_KEYS);
        return props;
    }

    public void setDatasourcePropertyKeys(Set<String> propertyKeys) {
        JcrPropertyUtil.setProperty(this.node, DATASOURCE_PROPERTY_KEYS, propertyKeys);
    }

    @Override
    public ConnectionType getConnectionType() {
        return JcrPropertyUtil.getEnum(this.node, CONNECTION_TYPE, DatasourceDefinition.ConnectionType.class, null);
    }

    @Override
    public void setConnectionType(ConnectionType type) {
        JcrPropertyUtil.setProperty(this.node, CONNECTION_TYPE, type.name());
    }

    @Override
    public String getProcessorType() {
        return JcrPropertyUtil.getProperty(this.node, PROCESSOR_TYPE, true);
    }

    @Override
    public void setProcessorType(String processorType) {
        JcrPropertyUtil.setProperty(this.node, PROCESSOR_TYPE, processorType);
    }


    public DatasourceDefinitionId getId() {
        try {
            return new JcrDatasourceDefinition.DatasourceDefinitionId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    public static class DatasourceDefinitionId extends JcrEntity.EntityId implements DatasourceDefinition.ID {

        public DatasourceDefinitionId(Serializable ser) {
            super(ser);
        }
    }


    @Override
    public void setIdentityString(String identityString) {
        JcrPropertyUtil.setProperty(this.node, IDENTITY_STRING, identityString);
    }

    @Override
    public String getIdentityString() {
        String identityString = JcrPropertyUtil.getProperty(this.node, IDENTITY_STRING, true);
        if (StringUtils.isBlank(identityString)) {
            identityString = getDatasourcePropertyKeys().stream().map(key -> "${" + key + "}").collect(Collectors.joining(","));
        }
        return identityString;
    }

    @Override
    public String getDatasourceType() {
        return JcrPropertyUtil.getProperty(this.node, DATASOURCE_TYPE);
    }

    @Override
    public void setDatasourceType(String dsType) {
        JcrPropertyUtil.setProperty(this.node, DATASOURCE_TYPE, dsType);
    }

    @Override
    public void setTile(String title) {
        JcrPropertyUtil.setProperty(this.node, TITLE, title);
    }

    public String getTitle() {
        return JcrPropertyUtil.getProperty(this.node, TITLE);
    }
}
