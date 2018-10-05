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
import com.thinkbiganalytics.metadata.api.catalog.DataSetAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSource.ID;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceNotFoundException;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.catalog.datasource.JcrDataSource;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.MetadataPaths;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 *
 */
public class JcrDataSetProvider extends BaseJcrProvider<DataSet, DataSet.ID> implements DataSetProvider {
    
    @Inject
    private DataSourceProvider dsProvider;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.BaseProvider#resolveId(java.io.Serializable)
     */
    @Override
    public DataSet.ID resolveId(Serializable fid) {
        return new JcrDataSet.DataSetId(fid);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#create(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID, java.lang.String)
     */
    @Override
    public DataSet create(ID dataSourceId, String title) {
        return this.dsProvider.find(dataSourceId)
                .map(dsrc -> {
                    String dsSystemName = UUID.randomUUID().toString();
                    Path dataSetPath = MetadataPaths.dataSetPath(dsrc.getConnector().getSystemName(), dsrc.getSystemName(), dsSystemName);
                    
                    if (JcrUtil.hasNode(getSession(), dataSetPath)) {
                        throw DataSetAlreadyExistsException.fromSystemName(title);
                    } else {
                        Node dataSetNode = JcrUtil.createNode(getSession(), dataSetPath, JcrDataSet.NODE_TYPE);
                        JcrDataSet ds = JcrUtil.createJcrObject(dataSetNode, JcrDataSet.class);
                        ds.setTitle(title);
                        return ds;
                    } 
                })
                .orElseThrow(() -> new DataSourceNotFoundException(dataSourceId));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#find(com.thinkbiganalytics.metadata.api.catalog.DataSet.ID)
     */
    @Override
    public Optional<DataSet> find(com.thinkbiganalytics.metadata.api.catalog.DataSet.ID id) {
        return Optional.ofNullable(findById(id));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#findByDataSource(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID, com.thinkbiganalytics.metadata.api.catalog.DataSource.ID[])
     */
    @Override
    public List<DataSet> findByDataSource(DataSource.ID dsId, DataSource.ID... otherIds) {
        return findByDataSource(Stream.concat(Stream.of(dsId), 
                                              Arrays.asList(otherIds).stream())
                                   .collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#findByDataSource(java.util.Collection)
     */
    @Override
    public List<DataSet> findByDataSource(Collection<ID> dsIds) {
        String ids = dsIds.stream()
                        .map(id -> "'" + id.toString() + "'")
                        .collect(Collectors.joining(",", "(", ")"));
        String query = startBaseQuery()
                        .append(" JOIN [").append(JcrDataSource.DATA_SETS_NODE_TYPE).append("] AS dsn ON ISCHILDNODE(e, dsn) ")
                        .append(" JOIN [").append(JcrDataSource.NODE_TYPE).append("] AS ds ON ISCHILDNODE(dsn, ds) ")
                        .append(" WHERE ds.[mode:id] IN ").append(ids).toString();
                return find(query);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getJcrEntityClass()
     */
    @Override
    public Class<? extends JcrEntity<?>> getJcrEntityClass() {
        return JcrDataSet.class;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#getNodeType(java.lang.Class)
     */
    @Override
    public String getNodeType(Class<? extends JcrObject> jcrEntityType) {
        return JcrDataSet.NODE_TYPE;
    }


    private String generateSystemName(String title) {
        return title.replaceAll("\\s+", "_").toLowerCase();
    }
}
