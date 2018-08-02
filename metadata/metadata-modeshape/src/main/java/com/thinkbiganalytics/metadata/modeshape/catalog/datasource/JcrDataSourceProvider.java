/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog.datasource;

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
import com.thinkbiganalytics.metadata.api.catalog.Connector.ID;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorNotFoundException;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.catalog.connector.JcrConnector;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.MetadataPaths;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 *
 */
public class JcrDataSourceProvider extends BaseJcrProvider<DataSource, DataSource.ID> implements DataSourceProvider {
    
    @Inject
    private ConnectorProvider connectorProvider;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.BaseProvider#resolveId(java.io.Serializable)
     */
    @Override
    public DataSource.ID resolveId(Serializable fid) {
        return new JcrDataSource.DataSourceId(fid);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider#create(com.thinkbiganalytics.metadata.api.catalog.Connector.ID, java.lang.String)
     */
    @Override
    public DataSource create(ID connIdStr, String systemName) {
        Connector.ID connId = this.connectorProvider.resolveId(connIdStr);
        Connector conn = this.connectorProvider.findById(connId);
        
        if (conn != null) {
            Path dsPath = MetadataPaths.connectorPath(systemName);
            if (JcrUtil.hasNode(getSession(), dsPath)) {
                throw ConnectorAlreadyExistsException.fromSystemName(systemName);
            } else {
                Node connNode = JcrUtil.createNode(getSession(), dsPath, JcrConnector.NODE_TYPE);
                return JcrUtil.createJcrObject(connNode, JcrDataSource.class);
            } 
        } else {
            throw new ConnectorNotFoundException(connId);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider#findByConnector(com.thinkbiganalytics.metadata.api.catalog.Connector.ID, com.thinkbiganalytics.metadata.api.catalog.Connector.ID[])
     */
    @Override
    public List<DataSource> findByConnector(ID connId, ID... moreIds) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider#findByConnector(java.util.Collection)
     */
    @Override
    public List<DataSource> findByConnector(Collection<ID> connIds) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider#find(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID)
     */
    @Override
    public Optional<DataSource> find(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getEntityClass()
     */
    @Override
    public Class<? extends DataSource> getEntityClass() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getJcrEntityClass()
     */
    @Override
    public Class<? extends JcrEntity<?>> getJcrEntityClass() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getNodeType(java.lang.Class)
     */
    @Override
    public String getNodeType(Class<? extends JcrObject> jcrEntityType) {
        // TODO Auto-generated method stub
        return null;
    }


}
