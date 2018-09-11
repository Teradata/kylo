package com.thinkbiganalytics.kylo.catalog.datasource;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.ConnectorProvider;
import com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.UserDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.security.context.SecurityContextUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides access to {@link DataSource} objects.
 */
@Component
public class DataSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(DataSourceProvider.class);

    /**
     * Maps id to connection
     */
    @Nonnull
    private final Map<String, DataSource> catalogDataSources;

    /**
     * Provides access to connectors
     */
    @Nonnull
    private final ConnectorProvider connectorProvider;
    
    /**
     * Manages credentials of data sources
     */
    @Nonnull
    private final DataSourceCredentialManager credentialManager;

    /**
     * Provides access to feed data sources
     */
    @Nonnull
    private final DatasourceProvider feedDataSourceProvider;

    /**
     * Transforms domain data sources to rest models
     */
    @Nonnull
    private final DatasourceModelTransform feedDataSourceTransform;

    /**
     * Connector identifier for JDBC data sources from {@code feedDataSourceProvider}
     */
    @Nullable
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "squid:S2789"})
    private Optional<String> jdbcConnectorId;

    /**
     * Constructs a {@code ConnectorProvider}.
     */
    @Autowired
    public DataSourceProvider(@Nonnull final DatasourceProvider feedDataSourceProvider, 
                              @Nonnull final DataSourceCredentialManager credentialManager,
                              @Nonnull final DatasourceModelTransform feedDataSourceTransform,
                              @Nonnull final ConnectorProvider connectorProvider) throws IOException {
        this.feedDataSourceProvider = feedDataSourceProvider;
        this.credentialManager = credentialManager;
        this.feedDataSourceTransform = feedDataSourceTransform;
        this.connectorProvider = connectorProvider;

        // Load data sources
        final String connectionsJson = IOUtils.toString(getClass().getResourceAsStream("/catalog-datasources.json"), StandardCharsets.UTF_8);
        final List<DataSource> connectionList = ObjectMapperSerializer.deserialize(connectionsJson, new TypeReference<List<DataSource>>() {
        });
        catalogDataSources = connectionList.stream()
            .peek(connection -> {
                if (connection.getId() == null) {
                    connection.setId(connection.getTitle().toLowerCase().replaceAll("\\W", "-").replaceAll("-{2,}", "-"));
                }
            })
            .collect(Collectors.toMap(DataSource::getId, Function.identity()));
    }

    /**
     * Creates a new data source using the specified template.
     *
     * @throws CatalogException if the data source is not valid
     */
    @Nonnull
    public DataSource createOrUpdateDataSource(@Nonnull final DataSource source) {
        // Find connector
        final Connector connector = Optional.ofNullable(source.getConnector()).map(Connector::getId).flatMap(connectorProvider::findConnector)
            .orElseThrow(() -> new CatalogException("catalog.datasource.connector.invalid"));

        final DataSource dataSource;
        if (StringUtils.isBlank(source.getId())) {
            dataSource = new DataSource(source);
            dataSource.setId(UUID.randomUUID().toString());
        } else {
            dataSource = source;
        }
        final DataSource updatedDataSource = this.credentialManager.applyPlaceholders(dataSource, SecurityContextUtil.getCurrentPrincipals());
        
        catalogDataSources.put(dataSource.getId(), updatedDataSource);

        // Return a copy with the connector
        final DataSource copy = new DataSource(dataSource);
        copy.setConnector(connector);
        return copy;
    }

    /**
     * Gets the connector with the specified id.
     */
    @Nonnull
    @SuppressWarnings("squid:S2259")
    public Optional<DataSource> findDataSource(@Nonnull final String id){
        return findDataSource(id,false);
    }

   /**
    * Gets the connector with the specified id.
    */
    @Nonnull
    @SuppressWarnings("squid:S2259")
    public Optional<DataSource> findDataSource(@Nonnull final String id, final boolean includeCredentials) {
        // Find the data source
        DataSource dataSource = catalogDataSources.get(id);
        if (dataSource != null) {
            dataSource = new DataSource(dataSource);
        } else {
            try {
                final Datasource feedDataSource = feedDataSourceProvider.getDatasource(feedDataSourceProvider.resolve(id));
                DatasourceModelTransform.Level level = DatasourceModelTransform.Level.FULL;
                if(includeCredentials){
                    level = DatasourceModelTransform.Level.ADMIN;
                }
                dataSource = toDataSource(feedDataSource, level);
            } catch (final IllegalArgumentException e) {
                log.debug("Failed to resolve data source {}: {}", id, e, e);
            }
        }

        // Set connector
        final Optional<Connector> connector = Optional.ofNullable(dataSource).map(DataSource::getConnector).map(Connector::getId).flatMap(connectorProvider::findConnector);
        if (connector.isPresent()) {
            dataSource.setConnector(connector.get());
            return Optional.of(dataSource);
        } else {
            if (dataSource != null) {
                log.error("Unable to find connector for data source: {}", dataSource);
            }
            return Optional.empty();
        }
    }

    /**
     * Gets the list of available connectors.
     */
    @Nonnull
    public Page<DataSource> findAllDataSources(@Nonnull final Pageable pageable, @Nullable final String filter) {
        // Filter catalog data sources
        @SuppressWarnings("squid:HiddenFieldCheck") final Supplier<Stream<DataSource>> catalogDataSources = () -> this.catalogDataSources.values().stream()
            .filter(containsIgnoreCase(DataSource::getTitle, filter))
            .map(DataSource::new);

        // Filter feed data sources
        final DatasourceCriteria feedDataSourcesCriteria = feedDataSourceProvider.datasetCriteria().type(UserDatasource.class);
        final Supplier<Stream<DataSource>> feedDataSources = () -> feedDataSourceProvider.getDatasources(feedDataSourcesCriteria).stream()
            .filter(containsIgnoreCase(Datasource::getName, filter))
            .map(datasource -> toDataSource(datasource, DatasourceModelTransform.Level.BASIC))
            .filter(Objects::nonNull);

        // Sort and paginate
        final List<DataSource> filteredDataSources = Stream.concat(catalogDataSources.get(), feedDataSources.get())
            .sorted(Comparator.comparing(DataSource::getTitle))
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .peek(findConnector())
            .collect(Collectors.toList());
        return new PageImpl<>(filteredDataSources, pageable, catalogDataSources.get().count() + feedDataSources.get().count());
    }

    /**
     * Creates a data set template for the specified JDBC data source.
     */
    @Nullable
    private DefaultDataSetTemplate createTemplate(@Nonnull final JdbcDatasource jdbcDatasource) {
        final DefaultDataSetTemplate template = new DefaultDataSetTemplate();
        template.setOptions(new HashMap<>());

        if (jdbcDatasource.getDatabaseConnectionUrl() != null) {
            template.getOptions().put("url", jdbcDatasource.getDatabaseConnectionUrl());
        }
        if (jdbcDatasource.getDatabaseDriverClassName() != null) {
            template.getOptions().put("driver", jdbcDatasource.getDatabaseDriverClassName());
        }
        if (jdbcDatasource.getDatabaseDriverLocation() != null) {
            template.setJars(Arrays.asList(jdbcDatasource.getDatabaseDriverLocation().split(",")));
        }
        if (jdbcDatasource.getDatabaseUser() != null) {
            template.getOptions().put("user", jdbcDatasource.getDatabaseUser());
        }
        if (jdbcDatasource.getPassword() != null) {
            template.getOptions().put("password", jdbcDatasource.getPassword());
        }

        return (template.getJars() != null || !template.getOptions().isEmpty()) ? template : null;
    }

    /**
     * Checks if {@code field} contains {@code searchString} irrespective of case, handling {@code null}.
     */
    @Nonnull
    private <T> Predicate<T> containsIgnoreCase(@Nonnull Function<T, String> field, @Nullable final String searchString) {
        if (searchString != null) {
            return obj -> StringUtils.containsIgnoreCase(field.apply(obj), searchString);
        } else {
            return obj -> true;
        }
    }

    /**
     * Attaches the connector to a data source.
     */
    @Nonnull
    @SuppressWarnings("squid:S2789")
    private Consumer<DataSource> findConnector() {
        final Map<String, Optional<Connector>> connectors = new HashMap<>();
        return dataSource -> {
            // Find connector
            final String connectorId = dataSource.getConnector().getId();
            Optional<Connector> connector = connectors.get(connectorId);
            if (connector == null) {
                connector = connectorProvider.findConnector(connectorId);
                connectors.put(connectorId, connector);
            }

            // Apply connector to data source
            connector.ifPresent(dataSource::setConnector);
        };
    }

    /**
     * Finds a connector to use for JDBC data sources.
     */
    @Nonnull
    @SuppressWarnings("squid:S2789")
    private Optional<String> getJdbcConnectorId() {
        if (jdbcConnectorId == null) {
            final Comparator<Object> nullComparator = Comparator.nullsFirst((a, b) -> 0);
            final Comparator<DataSetTemplate> templateComparator = Comparator
                .comparing(DataSetTemplate::getFiles, nullComparator)
                .thenComparing(DataSetTemplate::getJars, nullComparator)
                .thenComparing(DataSetTemplate::getOptions, nullComparator)
                .thenComparing(DataSetTemplate::getPaths, nullComparator);
            jdbcConnectorId = connectorProvider.findAllConnectors().stream()
                .filter(connector -> {
                    final String format = (connector.getTemplate() != null) ? connector.getTemplate().getFormat() : null;
                    return StringUtils.equalsAnyIgnoreCase(format, "jdbc", "org.apache.spark.sql.jdbc", "org.apache.spark.sql.jdbc.DefaultSource", "org.apache.spark.sql.execution.datasources.jdbc",
                                                           "org.apache.spark.sql.execution.datasources.jdbc.DefaultSource", "org.apache.spark.sql.execution.datasources.jdbc.JdbcRelationProvider");
                })
                .sorted(Comparator.comparing(Connector::getTemplate, Comparator.nullsFirst(templateComparator)))
                .map(Connector::getId)
                .findFirst();
        }
        return jdbcConnectorId;
    }

    /**
     * Converts a feed data source to a REST data source.
     */
    @Nullable
    @SuppressWarnings("squid:S3655")
    private DataSource toDataSource(@Nullable final Datasource feedDataSource, @Nonnull final DatasourceModelTransform.Level level) {
        // Transform to metadata data source
        final com.thinkbiganalytics.metadata.rest.model.data.Datasource metadataDataSource;
        if (feedDataSource != null) {
            metadataDataSource = feedDataSourceTransform.toDatasource(feedDataSource, level);
        } else {
            return null;
        }

        // Add properties to data source
        final DataSource dataSource = new DataSource();
        dataSource.setId(metadataDataSource.getId());
        dataSource.setTitle(metadataDataSource.getName());

        // Set properties based on type
        final Connector connector = new Connector();
        final DefaultDataSetTemplate template;

        if (metadataDataSource instanceof JdbcDatasource && getJdbcConnectorId().isPresent()) {
            connector.setId(getJdbcConnectorId().get());
            template = createTemplate((JdbcDatasource) metadataDataSource);
        } else {
            return null;
        }

        dataSource.setConnector(connector);
        dataSource.setTemplate(template);
        return dataSource;
    }

    /**
     * Deletes datasource and any credentials stored for this data source
     * @param dataSourceId datasource to be deleted
     */
    public void delete(String dataSourceId) {
        DataSource ds = catalogDataSources.remove(dataSourceId);
        credentialManager.removeCredentials(ds);
    }
}
