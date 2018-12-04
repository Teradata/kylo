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

import com.thinkbiganalytics.metadata.api.catalog.Connector.ID;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorNotFoundException;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.catalog.connector.JcrConnector;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.MetadataPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 *
 */
public class JcrDataSourceProvider extends BaseJcrProvider<DataSource, DataSource.ID> implements DataSourceProvider {
    
    @Inject
    private ConnectorProvider connectorProvider;

    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;
    
    @Inject
    private SecurityRoleProvider roleProvider;

    @Inject
    private AccessController accessController;

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
    public DataSource create(ID connId, String title) {
        return this.connectorProvider.find(connId)
                .map(conn -> {
                    String systemName = generateSystemName(title);
                    Path dsPath = MetadataPaths.dataSourcePath(conn.getSystemName(), systemName);
                    if (JcrUtil.hasNode(getSession(), dsPath)) {
                        throw DataSourceAlreadyExistsException.fromSystemName(title);
                    } else {
                        Node connNode = JcrUtil.createNode(getSession(), dsPath, JcrDataSource.NODE_TYPE);
                        JcrDataSource dsrc = JcrUtil.createJcrObject(connNode, JcrDataSource.class);
                        dsrc.setTitle(title);
                        
                        if (this.accessController.isEntityAccessControlled()) {
                            final List<SecurityRole> roles = roleProvider.getEntityRoles(SecurityRole.DATASOURCE);
                            actionsProvider.getAvailableActions(AllowedActions.DATASOURCE)
                                .ifPresent(actions -> dsrc.enableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), roles));
                        } else {
                            actionsProvider.getAvailableActions(AllowedActions.DATASOURCE)
                                .ifPresent(actions -> dsrc.disableAccessControl(JcrMetadataAccess.getActiveUser()));
                        }
                        
                        return dsrc;
                    } 
                })
                .orElseThrow(() -> new ConnectorNotFoundException(connId));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider#findByConnector(com.thinkbiganalytics.metadata.api.catalog.Connector.ID, com.thinkbiganalytics.metadata.api.catalog.Connector.ID[])
     */
    @Override
    public List<DataSource> findByConnector(ID connId, ID... moreIds) {
        return findByConnector(Stream.concat(Stream.of(connId), 
                                             Arrays.asList(moreIds).stream())
                                   .collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider#findByConnector(java.util.Collection)
     */
    @Override
    public List<DataSource> findByConnector(Collection<ID> connIds) {
        String ids = connIds.stream()
                .map(id -> "'" + id.toString() + "'")
                .collect(Collectors.joining(",", "(", ")"));
        String query = startBaseQuery()
                .append(" JOIN [").append(JcrConnector.DATASOURCES_NODE_TYPE).append("] AS cds ON ISCHILDNODE(e, cds) ")
                .append(" JOIN [").append(JcrConnector.NODE_TYPE).append("] AS c ON ISCHILDNODE(cds, c) ")
                .append(" WHERE c.[mode:id] IN ").append(ids).toString();
        return find(query);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider#find(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID)
     */
    @Override
    public Optional<DataSource> find(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID id) {
        return Optional.ofNullable(findById(id));
    }
//
//    /* (non-Javadoc)
//     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getEntityClass()
//     */
//    @Override
//    public Class<? extends DataSource> getEntityClass() {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getJcrEntityClass()
     */
    @Override
    public Class<? extends JcrEntity<?>> getJcrEntityClass() {
        return JcrDataSource.class;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getNodeType(java.lang.Class)
     */
    @Override
    public String getNodeType(Class<? extends JcrObject> jcrEntityType) {
        return JcrDataSource.NODE_TYPE;
    }

    @Override
    public Optional<DataSource> findByNifiControlerServiceId(final String serviceId) {
        final String query = startBaseQuery()
            .append(" WHERE e.[tba:nifiControllerServiceId] = '").append(serviceId.replaceAll("'", "''")).append("'")
            .toString();
        return Optional.ofNullable(findFirst(query));
    }
    
    public String generateSystemName(String title) {
        return JcrUtil.toSystemName(title);
    }
}
