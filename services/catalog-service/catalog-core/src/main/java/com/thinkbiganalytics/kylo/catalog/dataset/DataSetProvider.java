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

import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceProvider;
import com.thinkbiganalytics.kylo.catalog.file.PathValidator;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link DataSet} objects.
 */
@Component
public class DataSetProvider {

    private static final Logger log = LoggerFactory.getLogger(DataSetProvider.class);

    /**
     * Map of data set id to data set
     */
    @Nonnull
    private final Map<String, DataSet> dataSets = new HashMap<>();

    /**
     * Provides access to data sources
     */
    @Nonnull
    private final DataSourceProvider dataSourceProvider;

    /**
     * Validates data set paths
     */
    @Nonnull
    private PathValidator pathValidator;

    /**
     * Constructs a {@code DataSetProvider}.
     */
    @Autowired
    public DataSetProvider(@Nonnull final DataSourceProvider dataSourceProvider, @Nonnull final PathValidator pathValidator) {
        this.dataSourceProvider = dataSourceProvider;
        this.pathValidator = pathValidator;
    }

    /**
     * Creates a new data set using the specified template.
     *
     * @throws CatalogException if the data set is not valid
     */
    @Nonnull
    public DataSet createDataSet(@Nonnull final DataSet source) {
        // Find data source
        final DataSource dataSource = Optional.of(source).map(DataSet::getDataSource).map(DataSource::getId).flatMap(dataSourceProvider::findDataSource)
            .orElseThrow(() -> new CatalogException("catalog.dataset.datasource.invalid"));

        // Validate paths
        final List<String> paths = DataSetUtil.getPaths(source).orElse(null);
        if (paths != null) {
            final DataSet dataSet = new DataSet(source);
            dataSet.setDataSource(dataSource);
            paths.stream()
                .map(pathString -> {
                    try {
                        return new Path(pathString);
                    } catch (IllegalArgumentException e) {
                        throw new CatalogException(e, "catalog.dataset.path.invalid", pathString);
                    }
                })
                .forEach(path -> {
                    if (!pathValidator.isPathAllowed(path, dataSet)) {
                        throw new CatalogException("catalog.dataset.path.denied", path);
                    }
                });
        }

        // Create and store data set
        final DataSet dataSet = new DataSet(source);
        dataSet.setId(UUID.randomUUID().toString());
        dataSet.setDataSource(new DataSource());
        dataSet.getDataSource().setId(dataSource.getId());
        dataSets.put(dataSet.getId(), dataSet);

        // Return a copy with the data source
        final DataSet copy = new DataSet(dataSet);
        copy.setDataSource(dataSource);
        return copy;
    }

    /**
     * Finds the data set with the specified id.
     */
    @Nonnull
    public Optional<DataSet> findDataSet(@Nonnull final String id) {
        final DataSet dataSet = dataSets.get(id);
        final Optional<DataSource> dataSource = Optional.ofNullable(dataSet).map(DataSet::getDataSource).map(DataSource::getId).flatMap(dataSourceProvider::findDataSource);
        if (dataSource.isPresent()) {
            final DataSet copy = new DataSet(dataSet);
            copy.setDataSource(dataSource.get());
            return Optional.of(copy);
        } else {
            if (dataSet != null) {
                log.error("Unable to find data source for data set: {}", dataSet);
            }
            return Optional.empty();
        }
    }
}
