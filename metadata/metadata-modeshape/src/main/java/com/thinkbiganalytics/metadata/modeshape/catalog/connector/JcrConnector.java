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
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParams;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.jcr.Node;

/**
 *
 */
public class JcrConnector extends JcrEntity<JcrConnector.ConnectorId> implements Connector, AuditableMixin, SystemEntityMixin, AccessControlledMixin {
    
    public static final String NODE_TYPE = "tba:Connector";
    public static final String PLUGIN_ID = "tba:pluginId";
    public static final String DATASOURCES = "datasources";

    /**
     * @param node
     */
    public JcrConnector(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.common.JcrEntity#getId()
     */
    @Override
    public ConnectorId getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParamsSupplier#getSparkParameters()
     */
    @Override
    public DataSetSparkParameters getSparkParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParamsSupplier#getEffectiveSparkParameters()
     */
    @Override
    public DataSetSparkParameters getEffectiveSparkParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#isActive()
     */
    @Override
    public boolean isActive() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getPluginId()
     */
    @Override
    public String getPluginId() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getIcon()
     */
    @Override
    public String getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getColor()
     */
    @Override
    public String getColor() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.Connector#getDataSources()
     */
    @Override
    public List<DataSource> getDataSources() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static class ConnectorId extends JcrEntity.EntityId implements Connector.ID {
        
        private static final long serialVersionUID = 1L;

        public ConnectorId(Serializable ser) {
            super(ser);
        }
    }

}
