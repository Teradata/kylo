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
import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.catalog.DataSetSparkParamsSupplierMixin;
import com.thinkbiganalytics.metadata.modeshape.catalog.connector.JcrConnector;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSet;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/**
 *
 */
public class JcrDataSource extends JcrEntity<JcrDataSource.DataSourceId> implements DataSource, AuditableMixin, SystemEntityMixin, AccessControlledMixin, DataSetSparkParamsSupplierMixin {

    public static final String NODE_TYPE = "tba:DataSource";
    public static final String DATA_SETS_NODE_TYPE = "tba:DataSets";
    public static final String DATA_SETS = "dataSets";
    public static final String NIFI_CONTROLLER_SVC_ID = "tba:nifiControllerServiceId";

    /**
     * @param node
     */
    public JcrDataSource(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSource#getId()
     */
    @Override
    public DataSourceId getId() {
        try {
            return new DataSourceId(getObjectId());
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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSource#getNifiControllerServiceId()
     */
    @Override
    public String getNifiControllerServiceId() {
        return getProperty(NIFI_CONTROLLER_SVC_ID, String.class, null);
    }
    
    @Override
    public void setNifiControllerServiceId(String id) {
        setProperty(NIFI_CONTROLLER_SVC_ID, id);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.catalog.DataSetSparkParamsSupplierMixin#getSparkParametersChain()
     */
    @Override
    public List<DataSetSparkParameters> getSparkParametersChain() {
        return Arrays.asList(DataSetSparkParamsSupplierMixin.super.getSparkParameters(), getConnector().getSparkParameters());                               
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin#getJcrAllowedActionsType()
     */
    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        return JcrDataSourceAllowedActions.class;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessControlled#getLogId()
     */
    @Override
    public String getAuditId() {
        return "Data Source:" + getId();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSource#getConnector()
     */
    @Override
    public Connector getConnector() {
        Node dsNode = JcrUtil.getParent(getNode());
        Node connNode = JcrUtil.getParent(dsNode);
        
        return JcrUtil.getJcrObject(connNode, JcrConnector.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSource#getDataSets()
     */
    @Override
    public List<? extends DataSet> getDataSets() {
        try {
            Node dsNode = getDataSetsNode();
            NodeType type = JcrUtil.getNodeType(getNode().getSession(), JcrDataSet.NODE_TYPE);
            return JcrUtil.getJcrObjects(dsNode, type, JcrDataSet.class);
        }catch (RepositoryException e){
            log.error("Unablt to get datasets for datasource ",e);
            return Collections.emptyList();
        }



    }

    public Node getDataSetsNode(){
        return JcrUtil.getNode(getNode(), DATA_SETS);
    }
    
    public static class DataSourceId extends JcrEntity.EntityId implements DataSource.ID {
        
        private static final long serialVersionUID = 1L;

        public DataSourceId(Serializable ser) {
            super(ser);
        }
    }

}
