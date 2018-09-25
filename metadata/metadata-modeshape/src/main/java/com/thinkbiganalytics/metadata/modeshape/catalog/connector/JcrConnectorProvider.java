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
import com.thinkbiganalytics.metadata.api.catalog.Connector.ID;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.MetadataPaths;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Optional;

import javax.jcr.Node;

/**
 *
 */
public class JcrConnectorProvider extends BaseJcrProvider<Connector, Connector.ID> implements ConnectorProvider {

    public static final Path CATALOG_PATH = JcrUtil.path("metadata", "catalog");
    public static final Path CONNECTORS_PATH = CATALOG_PATH.resolve("connectors");

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.BaseProvider#resolveId(java.io.Serializable)
     */
    @Override
    public ID resolveId(Serializable fid) {
        return new JcrConnector.ConnectorId(fid);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider#create(java.lang.String, java.lang.String)
     */
    @Override
    public Connector create(String pluginId, String title) {
        String systemName = generateSystemName(title);
        Path connPath = MetadataPaths.connectorPath(systemName);
        
        if (JcrUtil.hasNode(getSession(), connPath)) {
            throw ConnectorAlreadyExistsException.fromSystemName(systemName);
        } else {
            Node connNode = JcrUtil.createNode(getSession(), connPath, JcrConnector.NODE_TYPE);
            JcrConnector conn = JcrUtil.createJcrObject(connNode, JcrConnector.class, pluginId);
            conn.setTitle(title);
            return conn;
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider#find(com.thinkbiganalytics.metadata.api.catalog.Connector.ID)
     */
    @Override
    public Optional<Connector> find(ID id) {
        return Optional.ofNullable(findById(id));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider#findByPlugin(java.lang.String)
     */
    @Override
    public Optional<Connector> findByPlugin(String pluginId) {
        String query = startBaseQuery().append(" WHERE [").append(JcrConnector.PLUGIN_ID).append("] = '").append(pluginId).append("'").toString();
        return Optional.ofNullable(findFirst(query));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getJcrEntityClass()
     */
    @Override
    public Class<? extends JcrEntity<?>> getJcrEntityClass() {
        return JcrConnector.class;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getNodeType(java.lang.Class)
     */
    @Override
    public String getNodeType(Class<? extends JcrObject> jcrEntityType) {
        return JcrConnector.NODE_TYPE;
    }

    private String generateSystemName(String title) {
        return title.replaceAll("\\s+", "_").toLowerCase();
    }

}
