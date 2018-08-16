package com.thinkbiganalytics.kylo.catalog.spark;

/*-
 * #%L
 * Kylo Catalog Core
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

import com.google.common.annotations.VisibleForTesting;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogDataSetAccess;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A skeletal implementation of a {@link KyloCatalogDataSetAccess} that stores values in a {@link DataSetOptions} object.
 *
 * @param <R> builder type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDataSetOptionsAccess<R extends KyloCatalogDataSetAccess> extends AbstractKyloCatalogDataSetAccess<R> {

    /**
     * Hadoop configuration
     */
    @Nonnull
    protected final Configuration hadoopConfiguration;

    /**
     * Read options
     */
    @Nonnull
    protected final DataSetOptions options;

    /**
     * Loads resources for accessing data sets
     */
    @Nonnull
    protected final DataSourceResourceLoader resourceLoader;

    /**
     * Constructs an {@code AbstractKyloCatalogDataSetAccess}.
     */
    public AbstractDataSetOptionsAccess(@Nonnull final DataSetOptions options, @Nonnull final Configuration hadoopConfiguration, @Nonnull final DataSourceResourceLoader resourceLoader) {
        this.options = options;
        this.hadoopConfiguration = hadoopConfiguration;
        this.resourceLoader = resourceLoader;
    }

    @Nonnull
    @Override
    public final R addFile(@Nullable final String path) {
        if (path != null) {
            resourceLoader.addFile(path);
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public final R addJar(@Nullable final String path) {
        if (path != null && resourceLoader.addJar(path)) {
            options.addJar(path);
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public final R addJars(@Nullable final List<String> paths) {
        if (paths != null && resourceLoader.addJars(paths)) {
            options.addJars(paths);
        }
        return (R) this;
    }


    @Nonnull
    public R dataSet(@Nonnull final DataSetTemplate dataSet) {
        if (dataSet.getFiles() != null) {
            addFiles(dataSet.getFiles());
        }
        if (dataSet.getFormat() != null) {
            format(dataSet.getFormat());
        }
        if (dataSet.getJars() != null) {
            addJars(dataSet.getJars());
        }
        if (dataSet.getOptions() != null) {
            options(dataSet.getOptions());
        }
        if (dataSet.getPaths() != null) {
            options.setPaths(dataSet.getPaths());
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public final R format(@Nonnull final String source) {
        options.setFormat(source);
        return (R) this;
    }

    @Nonnull
    @Override
    public R option(@Nonnull final String key, @Nullable final String value) {
        if (key.startsWith(KyloCatalogConstants.HADOOP_CONF_PREFIX)) {
            hadoopConfiguration.set(key.substring(KyloCatalogConstants.HADOOP_CONF_PREFIX.length()), value);
        }
        options.setOption(key, value);
        return (R) this;
    }

    /**
     * Gets the data set options.
     */
    @Nonnull
    @VisibleForTesting
    DataSetOptions getOptions() {
        return options;
    }
}
