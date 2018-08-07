/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog.dataset;

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

import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.catalog.DataSetSparkParamsSupplierMixin;
import com.thinkbiganalytics.metadata.modeshape.catalog.datasource.JcrDataSource;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrDataSet extends JcrEntity<JcrDataSet.DataSetId> implements DataSet, AuditableMixin, SystemEntityMixin, AccessControlledMixin, DataSetSparkParamsSupplierMixin {


    public JcrDataSet(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSet#getId()
     */
    @Override
    public DataSetId getId() {
        try {
            return new DataSetId(getObjectId());
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
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSet#getDataSource()
     */
    @Override
    public DataSource getDataSource() {
        Node dsNode = JcrUtil.getParent(getNode());
        Node connNode = JcrUtil.getParent(dsNode);
        
        return JcrUtil.getJcrObject(connNode, JcrDataSource.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin#getJcrAllowedActionsType()
     */
    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        return JcrDataSetAllowedActions.class;
    }
    
    
    public static class DataSetId extends JcrEntity.EntityId implements DataSet.ID {
        
        private static final long serialVersionUID = 1L;

        public DataSetId(Serializable ser) {
            super(ser);
        }
    }

}
