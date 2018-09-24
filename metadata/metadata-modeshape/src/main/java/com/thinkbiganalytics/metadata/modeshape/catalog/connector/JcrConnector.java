/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog.connector;

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

import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.catalog.DataSetSparkParamsSupplierMixin;
import com.thinkbiganalytics.metadata.modeshape.catalog.datasource.JcrDataSource;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrConnector extends JcrEntity<JcrConnector.ConnectorId> implements Connector, AuditableMixin, SystemEntityMixin, DataSetSparkParamsSupplierMixin, AccessControlledMixin {
    
    public static final String NODE_TYPE = "tba:Connector";
    public static final String DATASOURCES_NODE_TYPE = "tba:ConnectorDataSources";
    
    public static final String IS_ACTIVE = "tba:isActive";
    public static final String PLUGIN_ID = "tba:pluginId";
    public static final String ICON = "tba:icon";
    public static final String ICON_COLOR = "tba:iconColor";
    public static final String DATASOURCES = "dataSources";
    
    public JcrConnector(Node node) {
        super(node);
    }

    public JcrConnector(Node node, String pluginId) {
        super(node);
        setProperty(PLUGIN_ID, pluginId);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.common.JcrEntity#getId()
     */
    @Override
    public ConnectorId getId() {
        try {
            return new ConnectorId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.SystemEntity#getSystemName()
     */
    @Override
    public String getSystemName() {
        // System name is just the node name.
        return JcrPropertyUtil.getName(getNode());
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.SystemEntity#setSystemName(java.lang.String)
     */
    @Override
    public void setSystemName(String name) {
        JcrUtil.rename(getNode(), name);
    }

    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        return JcrConnectorAllowedActions.class;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#isActive()
     */
    @Override
    public boolean isActive() {
        return getProperty(IS_ACTIVE);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#setActive(boolean)
     */
    @Override
    public void setActive(boolean flag) {
        setProperty(IS_ACTIVE, flag);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getPluginId()
     */
    @Override
    public String getPluginId() {
        return getProperty(PLUGIN_ID);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getIcon()
     */
    @Override
    public String getIcon() {
        return getProperty(ICON);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getColor()
     */
    @Override
    public String getColor() {
        return getProperty(ICON_COLOR);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getDataSources()
     */
    @Override
    public List<? extends DataSource> getDataSources() {
        Node dsNode = JcrUtil.getNode(getNode(), DATASOURCES);
        return JcrUtil.getJcrObjects(dsNode, JcrDataSource.class);
    }
    
    public static class ConnectorId extends JcrEntity.EntityId implements Connector.ID {
        
        private static final long serialVersionUID = 1L;

        public ConnectorId(Serializable ser) {
            super(ser);
        }
    }

}
