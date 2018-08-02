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
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSource.ID;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class JcrDataSetProvider extends BaseJcrProvider<DataSet, DataSet.ID> implements DataSetProvider {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.BaseProvider#resolveId(java.io.Serializable)
     */
    @Override
    public DataSet.ID resolveId(Serializable fid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#create(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID, java.lang.String)
     */
    @Override
    public DataSet create(ID dataSourceId, String systemName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#findByDataSource(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID, com.thinkbiganalytics.metadata.api.catalog.DataSource.ID[])
     */
    @Override
    public List<DataSet> findByDataSource(ID dsId, ID... otherIds) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#findByDataSource(java.util.Collection)
     */
    @Override
    public List<DataSet> findByDataSource(Collection<ID> dsIds) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getEntityClass()
     */
    @Override
    public Class<? extends DataSet> getEntityClass() {
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
