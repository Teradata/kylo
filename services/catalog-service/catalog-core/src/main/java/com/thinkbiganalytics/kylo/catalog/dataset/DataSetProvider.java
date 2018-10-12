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
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.metadata.api.MetadataAccess;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link DataSet} objects.
 */
@Component("dataSetService")
public class DataSetProvider {

    private static final Logger log = LoggerFactory.getLogger(DataSetProvider.class);

    @Nonnull
    private final com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider dataSourceMetadataProvider;

    /**
     * Provides access to data sources
     */
    @Nonnull
    private final DataSourceProvider dataSourceProvider;

    @Nonnull
    private final com.thinkbiganalytics.metadata.api.catalog.DataSetProvider metadataProvider;

    @Nonnull
    private final MetadataAccess metadataService;

    @Nonnull
    private final CatalogModelTransform modelTransform;

    /**
     * Validates data set paths
     */
    @Nonnull
    private PathValidator pathValidator;

    /**
     * Constructs a {@code DataSetProvider}.
     */
    @Autowired
    public DataSetProvider(@Nonnull final com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider dataSourceMetadataProvider,
                           @Nonnull final DataSourceProvider dataSourceProvider,
                           @Nonnull final com.thinkbiganalytics.metadata.api.catalog.DataSetProvider metadataProvider,
                           @Nonnull final MetadataAccess metadataService,
                           @Nonnull final CatalogModelTransform modelTransform,
                           @Nonnull final PathValidator pathValidator) {
        this.dataSourceMetadataProvider = dataSourceMetadataProvider;
        this.dataSourceProvider = dataSourceProvider;
        this.metadataProvider = metadataProvider;
        this.metadataService = metadataService;
        this.modelTransform = modelTransform;
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

        // Validate data set
        final DataSet dataSet = new DataSet(source);
        dataSet.setDataSource(dataSource);
        validateDataSet(dataSet);

        // Create and store data set
        return metadataService.commit(() -> {
            // Resolve the real data set if possible, otherwise create
            com.thinkbiganalytics.metadata.api.catalog.DataSource.ID dataSourceId = dataSourceMetadataProvider.resolveId(dataSource.getId());
            com.thinkbiganalytics.metadata.api.catalog.DataSet ds = modelTransform.buildDataSet(source, metadataProvider.build(dataSourceId));
            return modelTransform.dataSetToRestModel().apply(ds);
        });
    }

    /**
     * Finds the data set with the specified id.
     */
    @Nonnull
    public Optional<DataSet> findDataSet(@Nonnull final String id) {
        return metadataService.read(() -> {
            com.thinkbiganalytics.metadata.api.catalog.DataSet.ID domainId = metadataProvider.resolveId(id);
            return metadataProvider.find(domainId).map(modelTransform.dataSetToRestModel());
        });
    }

    @Nonnull
    public DataSet findOrCreateDataSet(@Nonnull final DataSet source) {
        // Resolve the real data set if possible, otherwise create
        if (StringUtils.isBlank(source.getId())) {
            return createDataSet(source);
        } else {
            return findDataSet(source.getId()).orElseThrow(() -> new CatalogException("catalog.dataset.notFound"));
        }
    }

    @Nonnull
    public DataSet updateDataSet(@Nonnull final DataSet dataSet) {
        return metadataService.commit(() -> {
            final com.thinkbiganalytics.metadata.api.catalog.DataSet.ID dSetId = metadataProvider.resolveId(dataSet.getId());

            return metadataProvider.find(dSetId)
                .map(domain -> modelTransform.updateDataSet(dataSet, domain))
                .map(modelTransform.dataSetToRestModel())
                .orElseThrow(() -> {
                    log.debug("Data set not found with ID: {}", dSetId);
                    return new CatalogException("catalog.dataset.notfound.id", dSetId);
                });
        });
    }

    private void validateDataSet(@Nonnull final DataSet dataSet) {
        // Validate paths
        DataSetUtil.getPaths(dataSet).ifPresent(paths -> paths.stream()
            .map(pathString -> {
                try {
                    return new Path(pathString);
                } catch (final IllegalArgumentException e) {
                    throw new CatalogException(e, "catalog.dataset.path.invalid", pathString);
                }
            })
            .forEach(path -> {
                if (!pathValidator.isPathAllowed(path, dataSet)) {
                    throw new CatalogException("catalog.dataset.path.denied", path);
                }
            }));
    }
}
