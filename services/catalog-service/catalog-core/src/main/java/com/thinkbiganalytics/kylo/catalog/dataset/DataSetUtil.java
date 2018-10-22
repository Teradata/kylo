package com.thinkbiganalytics.kylo.catalog.dataset;

/*-
 * #%L
 * kylo-catalog-core
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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Static utility methods for {@link DataSet} instances.
 */
public class DataSetUtil {

    private static final Logger log = LoggerFactory.getLogger(DataSetUtil.class);

    /**
     * Gets the Hadoop configuration for the specified data set template.
     */
    @Nonnull
    public static Configuration getConfiguration(@Nonnull final DataSetTemplate template, @Nullable final Configuration parent) {
        final Configuration conf = (parent != null) ? new Configuration(parent) : new Configuration();

        if (template.getOptions() != null) {
            log.debug("Creating Hadoop configuration with options: {}", template.getOptions());
            template.getOptions().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(KyloCatalogConstants.HADOOP_CONF_PREFIX))
                .forEach(entry -> conf.set(entry.getKey().substring(KyloCatalogConstants.HADOOP_CONF_PREFIX.length()), entry.getValue()));
        }

        return conf;
    }

    /**
     * Gets the paths for the specified data set.
     */
    @Nonnull
    public static Optional<List<String>> getPaths(@Nonnull final DataSet dataSet) {
        final DataSetTemplate connectorTemplate = Optional.of(dataSet).map(DataSet::getDataSource).map(DataSource::getConnector).map(Connector::getTemplate).orElse(null);
        final DataSetTemplate dataSetTemplate = Optional.of(dataSet).map(DataSet::getDataSource).map(DataSource::getTemplate).orElse(null);
        List<String> paths = new ArrayList<>();

        // Add "path" option
        if (dataSet.getOptions() != null && dataSet.getOptions().get("path") != null) {
            paths.add(dataSet.getOptions().get("path"));
        } else if (dataSetTemplate != null && dataSetTemplate.getOptions() != null && dataSetTemplate.getOptions().get("path") != null) {
            paths.add(dataSetTemplate.getOptions().get("path"));
        } else if (connectorTemplate != null && connectorTemplate.getOptions() != null && connectorTemplate.getOptions().get("path") != null) {
            paths.add(connectorTemplate.getOptions().get("path"));
        }

        // Add paths list
        if (dataSet.getPaths() != null) {
            paths.addAll(dataSet.getPaths());
        } else if (dataSetTemplate != null && dataSetTemplate.getPaths() != null) {
            paths.addAll(dataSetTemplate.getPaths());
        } else if (connectorTemplate != null && connectorTemplate.getPaths() != null) {
            paths.addAll(connectorTemplate.getPaths());
        } else if (paths.isEmpty()) {
            paths = null;
        }

        return Optional.ofNullable(paths);
    }

    /**
     * Merges the data set, data source, and connector templates for the specified data set.
     */
    @Nonnull
    public static DefaultDataSetTemplate mergeTemplates(@Nonnull final DataSet dataSet) {
        final DefaultDataSetTemplate template;

        if (dataSet.getDataSource() != null) {
            template = DataSourceUtil.mergeTemplates(dataSet.getDataSource());
            mergeTemplates(template, dataSet);
        } else {
            template = new DefaultDataSetTemplate(dataSet);
        }

        return template;
    }

    /**
     * Merges the specified source template into the specified destination template.
     */
    public static void mergeTemplates(@Nonnull final DefaultDataSetTemplate dst, @Nonnull final DataSetTemplate src) {
        if (src.getFiles() != null) {
            if (dst.getFiles() != null) {
                dst.getFiles().addAll(src.getFiles());
            } else {
                dst.setFiles(new ArrayList<>(src.getFiles()));
            }
        }
        if (src.getJars() != null) {
            if (dst.getJars() != null) {
                dst.getJars().addAll(src.getJars());
            } else {
                dst.setJars(new ArrayList<>(src.getJars()));
            }
        }
        if (StringUtils.isNotBlank(src.getFormat())) {
            dst.setFormat(src.getFormat());
        }
        if (src.getOptions() != null) {
            if (dst.getOptions() != null) {
                dst.getOptions().putAll(src.getOptions());
            } else {
                dst.setOptions(new HashMap<>(src.getOptions()));
            }
        }
        if (src.getPaths() != null) {
            dst.setPaths(new ArrayList<>(src.getPaths()));
        }
    }

    /**
     * Instances of {@code DataSetUtil} should not be constructed.
     */
    private DataSetUtil() {
        throw new UnsupportedOperationException();
    }
}
