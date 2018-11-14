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

public class JcrDataSetProvider extends BaseJcrProvider<DataSet, DataSet.ID> implements DataSetProvider {

    @Inject
    private DataSourceProvider dsProvider;

    public static long generateHashCode(String format, Collection<String> paths, Map<String, String> options) {
        return (StringUtils.isNotEmpty(format) || !paths.isEmpty() || !options.isEmpty()) ? Objects.hash(format, paths, options) : 0;
    }

    @Override
    public DataSet.ID resolveId(Serializable fid) {
        return new JcrDataSet.DataSetId(fid);
    }

    @Override
    public DataSetBuilder build(ID dataSourceId) {
        return this.dsProvider.find(dataSourceId)
            .map(Builder::new)
            .orElseThrow(() -> new DataSourceNotFoundException(dataSourceId));
    }

    @Override
    public Optional<DataSet> find(com.thinkbiganalytics.metadata.api.catalog.DataSet.ID id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public List<DataSet> findByDataSource(DataSource.ID dsId, DataSource.ID... otherIds) {
        return findByDataSource(Stream.concat(Stream.of(dsId), Arrays.stream(otherIds)).collect(Collectors.toSet()));
    }

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

    @Override
    public DataSet findByDataSourceAndTitle(ID dataSourceId, String title) {
        String query = startBaseQuery()
            .append(" JOIN [").append(JcrDataSource.DATA_SETS_NODE_TYPE).append("] AS dsn ON ISCHILDNODE(e, dsn) ")
            .append(" JOIN [").append(JcrDataSource.NODE_TYPE).append("] AS ds ON ISCHILDNODE(dsn, ds) ")
            .append(" WHERE ds.[mode:id] = '").append(dataSourceId.toString()).append("' ")
            .append(" AND e.[jcr:title] = '").append(title).append("'").toString();
        return findFirst(query);
    }

    @Override
    public Class<? extends JcrEntity<?>> getJcrEntityClass() {
        return JcrDataSet.class;
    }

    @Override
    public String getNodeType(Class<? extends JcrObject> jcrEntityType) {
        return JcrDataSet.NODE_TYPE;
    }

    private Optional<DataSet> findByParamsHash(long hash) {
        if (hash != 0) {
            String query = startBaseQuery().append(" WHERE e.[tba:paramsHashCode] = ").append(hash).toString();
            return Optional.ofNullable(findFirst(query));
        } else {
            return Optional.empty();
        }
    }

    private String generateSystemName(String title) {
        return JcrUtil.toSystemName(title);
    }

    private boolean isFileUpload(DataSource dataSource){
        return dataSource.getConnector() != null ? dataSource.getConnector().getPluginId().equalsIgnoreCase("file-upload") : false;
    }

    private boolean isJdbcSource(DataSource dataSource, String format) {
        DataSetSparkParameters srcParams = dataSource.getEffectiveSparkParameters();
        String effectiveFormat = StringUtils.isEmpty(format) ? srcParams.getFormat() : format;
        return effectiveFormat.equals("jdbc");
    }

    private String generateTitle(DataSource dataSource, String title, String format, Map<String, String> options, Set<String> paths) {
        String derivedTile = title;
        DataSetSparkParameters srcParams = dataSource.getEffectiveSparkParameters();
        String effectiveFormat = StringUtils.isEmpty(format) ? srcParams.getFormat() : format;

        if (StringUtils.isEmpty(title)) {
            if (effectiveFormat.equals("jdbc")) {
                derivedTile = Stream.concat(srcParams.getOptions().entrySet().stream(), options.entrySet().stream())
                        .filter(e -> e.getKey().equals("dbtable"))
                        .map(Entry::getValue)
                        .findFirst()
                        .orElse(null); 
            } else {
                derivedTile = Stream.concat(srcParams.getPaths().stream(), paths.stream()).collect(Collectors.joining(","));
            }
        }
        
        return StringUtils.isEmpty(derivedTile) ? dataSource.getSystemName() + "-" + UUID.randomUUID() : derivedTile;
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

        public Builder(DataSource dataSource) {
            this.dataSource = (JcrDataSource) dataSource;
            
            // TODO do we really need to replicate the inherited properties?
//            DataSetSparkParameters sparkParams = dataSource.getEffectiveSparkParameters();
//            this.format = sparkParams.getFormat();
//            this.options.putAll(sparkParams.getOptions());
//            this.jars.addAll(sparkParams.getJars());
//            this.files.addAll(sparkParams.getFiles());
        }

        @Override
        public DataSetBuilder title(String title) {
            this.title = title;
            return this;
        }

        @Override
        public DataSetBuilder description(String description) {
            this.description = description;
            return this;
        }

        @Override
        public DataSetBuilder format(String format) {
            this.format = format;
            return this;
        }

        @Override
        public DataSetBuilder addOption(String name, String value) {
            this.options.put(name, value);
            return this;
        }

        @Override
        public DataSetBuilder addOptions(Map<String, String> options) {
            if (options != null) {
                options.forEach(this::addOption);
            }
            return this;
        }

        @Override
        public DataSetBuilder addPath(String path) {
            if (path != null) {
                this.paths.add(path);
            }
            return this;
        }

        @Override
        public DataSetBuilder addPaths(Iterable<String> paths) {
            if (paths != null) {
                paths.forEach(this::addPath);
            }
            return this;
        }

        @Override
        public DataSetBuilder addJar(String jarPath) {
            if (jarPath != null) {
                this.jars.add(jarPath);
            }
            return this;
        }

        @Override
        public DataSetBuilder addJars(Iterable<String> jarPaths) {
            if (jarPaths != null) {
                jarPaths.forEach(this::addJar);
            }
            return this;
        }

        @Override
        public DataSetBuilder addFile(String filePath) {
            if (filePath != null) {
                this.files.add(filePath);
            }
            return this;
        }

        @Override
        public DataSetBuilder addFiles(Iterable<String> filePaths) {
            if (filePaths != null) {
                filePaths.forEach(this::addFile);
            }
            return this;
        }

        @Override
        public DataSet build() {
            long hash = generateDataSetHash();
            return findByParamsHash(hash).orElseGet(() -> create(hash, false));
        }

        @Override
        public Optional<DataSet> find() {
            long hash = generateDataSetHash();
            return findByParamsHash(hash);
        }

        /**
         * @return a hash code uniquely identifying the underlying data
         */
        private long generateDataSetHash() {
            // TODO delegate to the connector plugin somehow to get the hash.
            // For now just hash all contents of this builder with the values inherited from the data source.
            DataSetSparkParameters sparkParams = this.dataSource.getEffectiveSparkParameters();
            String effectiveFormat = this.format != null ? this.format : sparkParams.getFormat();
//            Set<String> totalJars = Stream.concat(sparkParams.getJars().stream(), this.jars.stream()).collect(Collectors.toSet());
//            Set<String> totalFiles = Stream.concat(sparkParams.getFiles().stream(), this.files.stream()).collect(Collectors.toSet());
            Map<String, String> totalOptions = Stream.concat(sparkParams.getOptions().entrySet().stream(), this.options.entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2));

            //if we dont have a title and we are a file upload create the title and set it
            if (StringUtils.isBlank(this.title) && isFileUpload(dataSource)) {
                String title = UUID.randomUUID().toString();
                String ensuredTitle = generateTitle(this.dataSource, title, this.format, this.options, this.paths);
                this.title = ensuredTitle;
            }
            return generateHashCode(effectiveFormat, paths, totalOptions);
        }

        private DataSet create(long hash, boolean errorIfExisting) {
            String ensuredTitle = generateTitle(this.dataSource, this.title, this.format, this.options, this.paths);
            String dsSystemName = generateSystemName(ensuredTitle);
            Path dataSetPath = MetadataPaths.dataSetPath(this.dataSource.getConnector().getSystemName(), this.dataSource.getSystemName(), dsSystemName);

            Node dataSetNode = null;
            if (JcrUtil.hasNode(getSession(), dataSetPath)) {
                if (!errorIfExisting) {
                    dataSetNode = JcrUtil.getNode(getSession(), dataSetPath);
                } else {
                    throw DataSetAlreadyExistsException.fromSystemName(ensuredTitle);
                }
            } else {
                dataSetNode = JcrUtil.createNode(getSession(), dataSetPath, JcrDataSet.NODE_TYPE);
            }

            JcrDataSet ds = JcrUtil.createJcrObject(dataSetNode, JcrDataSet.class);
            DataSetSparkParameters params = ds.getSparkParameters();

            ds.setTitle(ensuredTitle);
            ds.setDescription(this.description);
            ds.setParamsHash(hash);
            params.setFormat(this.format);
            if(this.options != null) {
                for (Map.Entry<String, String> entry : this.options.entrySet()) {
                    params.addOption(entry.getKey(), entry.getValue());
                }
            }
            params.getPaths().addAll(this.paths);
            params.getFiles().addAll(this.files);
            params.getJars().addAll(this.jars);
            return ds;
        }

    }
}
