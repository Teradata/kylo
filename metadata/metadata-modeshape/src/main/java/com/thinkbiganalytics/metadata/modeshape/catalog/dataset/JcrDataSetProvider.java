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
import com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
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

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
//    
//    @Inject
//    private ConnectorPluginManager pluginManager;
    

//    public static long generateHashCode(String format, Collection<String> paths, Collection<String> jars, Collection<String> files, Map<String, String> options) {
    public static long generateHashCode(String format, Collection<String> paths, Map<String, String> options) {
        return Objects.hash(format, paths, options);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.BaseProvider#resolveId(java.io.Serializable)
     */
    @Override
    public DataSet.ID resolveId(Serializable fid) {
        return new JcrDataSet.DataSetId(fid);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#build(com.thinkbiganalytics.metadata.api.catalog.DataSource.ID)
     */
    @Override
    public DataSetBuilder build(ID dataSourceId) {
        return this.dsProvider.find(dataSourceId)
            .map(dataSource -> new Builder(dataSource))
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
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetProvider#findByDataSource(java.util.Collection)
     */
    @Override
    public DataSet findByDataSourceAndTitle(ID dataSourceId, String title) {
        String query = startBaseQuery()
            .append(" JOIN [").append(JcrDataSource.DATA_SETS_NODE_TYPE).append("] AS dsn ON ISCHILDNODE(e, dsn) ")
            .append(" JOIN [").append(JcrDataSource.NODE_TYPE).append("] AS ds ON ISCHILDNODE(dsn, ds) ")
            .append(" WHERE ds.[mode:id] = '").append(dataSourceId.toString()).append("' ")
        .append(" AND e.[jcr:title] = '").append(title).append("'").toString();
        return findFirst(query);
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

    private Optional<DataSet> findByParamsHash(long hash) {
        String query = startBaseQuery().append(" WHERE e.[tba:paramsHashCode] = ").append(hash).toString();
        return Optional.ofNullable(findFirst(query));
    }

    private String generateSystemName(String title) {
        return JcrUtil.toSystemName(title);
    }

    private String generateTitle(DataSource dataSource, String title) {
        return StringUtils.isEmpty(title) ? dataSource.getSystemName() + "-" + UUID.randomUUID() : title;
    }

    private class Builder implements DataSetBuilder {

        private final JcrDataSource dataSource;
        
        private String title;
        private String description;
        private String format;
        private final Set<String> paths = new HashSet<>();
        private final Set<String> jars = new HashSet<>();
        private final Set<String> files = new HashSet<>();
        private final Map<String, String> options = new HashMap<>();
        
        /**
         * @param dataSource
         */
        public Builder(DataSource dataSource) {
            DataSetSparkParameters sparkParams = dataSource.getEffectiveSparkParameters();
            
            this.dataSource = (JcrDataSource) dataSource;
            this.format = sparkParams.getFormat();
            this.options.putAll(sparkParams.getOptions());
            this.paths.addAll(sparkParams.getPaths());
            this.jars.addAll(sparkParams.getJars());
            this.files.addAll(sparkParams.getFiles());
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#title(java.lang.String)
         */
        @Override
        public DataSetBuilder title(String title) {
            this.title = title;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#description(java.lang.String)
         */
        @Override
        public DataSetBuilder description(String description) {
            this.description = description;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#format(java.lang.String)
         */
        @Override
        public DataSetBuilder format(String format) {
            this.format = format;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addOption(java.lang.String, java.lang.String)
         */
        @Override
        public DataSetBuilder addOption(String name, String value) {
            this.options.put(name, value);
            return this;
       }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addOptions(java.util.Map)
         */
        @Override
        public DataSetBuilder addOptions(Map<String, String> options) {
            if (options != null) {
                options.entrySet().forEach(entry -> addOption(entry.getKey(), entry.getValue()));
            }
            return this;
        }
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addPath(java.lang.String)
         */
        @Override
        public DataSetBuilder addPath(String path) {
            if (path != null) {
                this.paths.add(path);
            }
            return this;
        }
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addPaths(java.lang.Iterable)
         */
        @Override
        public DataSetBuilder addPaths(Iterable<String> paths) {
            if (paths != null) {
                paths.forEach(this::addPath);
            }
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addJar(java.lang.String)
         */
        @Override
        public DataSetBuilder addJar(String jarPath) {
            if (jarPath != null) {
                this.jars.add(jarPath);
            }
            return this;
        }
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addJars(java.lang.Iterable)
         */
        @Override
        public DataSetBuilder addJars(Iterable<String> jarPaths) {
            if (jarPaths != null) {
                jarPaths.forEach(this::addJar);
            }
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addFile(java.lang.String)
         */
        @Override
        public DataSetBuilder addFile(String filePath) {
            if (filePath != null ) {
                this.files.add(filePath);
            }
            return this;
        }
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#addFiles(java.lang.Iterable)
         */
        @Override
        public DataSetBuilder addFiles(Iterable<String> filePaths) {
            if (filePaths != null) {
                filePaths.forEach(this::addFile);
            }
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.catalog.DataSetBuilder#build()
         */
        @Override
        public DataSet build() {
            long hash = generateDataSetHash();
            
            return findByParamsHash(hash).orElseGet(() -> create(hash));
        }

        /**
         * @return a hash code uniquely identifying the underlying data
         */
        private long generateDataSetHash() {
            // TODO delegate to the connector plugin somehow to get the hash.
            // For now just hash all contents of this builder with the values inherited from the data source.
            DataSetSparkParameters sparkParams = this.dataSource.getEffectiveSparkParameters();
            String effectiveFormat = this.format != null ? this.format : sparkParams.getFormat();
            Set<String> totalPaths = Stream.concat(sparkParams.getPaths().stream(), this.paths.stream()).collect(Collectors.toSet());
//            Set<String> totalJars = Stream.concat(sparkParams.getJars().stream(), this.jars.stream()).collect(Collectors.toSet());
//            Set<String> totalFiles = Stream.concat(sparkParams.getFiles().stream(), this.files.stream()).collect(Collectors.toSet());
            Map<String, String> totalOptions = Stream.concat(sparkParams.getOptions().entrySet().stream(), this.options.entrySet().stream())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2));

            return generateHashCode(effectiveFormat, totalPaths, totalOptions);
        }

        private DataSet create(long hash) {
            String ensuredTitle = generateTitle(this.dataSource, this.title);
            String dsSystemName = generateSystemName(ensuredTitle);
            Path dataSetPath = MetadataPaths.dataSetPath(this.dataSource.getConnector().getSystemName(), this.dataSource.getSystemName(), dsSystemName);
            
            if (JcrUtil.hasNode(getSession(), dataSetPath)) {
                throw DataSetAlreadyExistsException.fromSystemName(ensuredTitle);
            } else {
                Node dataSetNode = JcrUtil.createNode(getSession(), dataSetPath, JcrDataSet.NODE_TYPE);
                JcrDataSet ds = JcrUtil.createJcrObject(dataSetNode, JcrDataSet.class);
                DataSetSparkParameters params = ds.getSparkParameters();
                
                ds.setTitle(ensuredTitle);
                ds.setDescription(this.description);
                ds.setParamsHash(hash);
                params.setFormat(this.format);
                params.getOptions().putAll(this.options);
                params.getPaths().addAll(this.paths);
                params.getFiles().addAll(this.files);
                params.getJars().addAll(this.jars);
                return ds;
            } 
        }
        
    }
}
