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

import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginDescriptor;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginNiFiControllerService;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginNiFiControllerServicePropertyDescriptor;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.UserDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.security.context.SecurityContextUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
@Component("dataSourceService")
public class DataSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(DataSourceProvider.class);

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

    @Nonnull
    private final NiFiRestClient nifiRestClient;

    @Nonnull
    private final ConnectorPluginManager pluginManager;

    @Nonnull
    private final PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");

    @Nonnull
    private final MetadataAccess metadataService;

    @Nonnull
    private final com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider metadataProvider;

    @Nonnull
    private final CatalogModelTransform modelTransform;

    /**
     * Constructs a {@code ConnectorProvider}.
     */
    @Autowired
    public DataSourceProvider(@Nonnull final DatasourceProvider feedDataSourceProvider,
                              @Nonnull final DataSourceCredentialManager credentialManager,
                              @Nonnull final DatasourceModelTransform feedDataSourceTransform,
                              @Nonnull final ConnectorProvider connectorProvider,
                              @Nonnull final NiFiRestClient nifiRestClient,
                              @Nonnull final ConnectorPluginManager pluginManager,
                              @Nonnull final MetadataAccess metadataService,
                              @Nonnull final com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider metadataProvider,
                              @Nonnull final CatalogModelTransform modelTransform) {
        this.feedDataSourceProvider = feedDataSourceProvider;
        this.credentialManager = credentialManager;
        this.feedDataSourceTransform = feedDataSourceTransform;
        this.connectorProvider = connectorProvider;
        this.nifiRestClient = nifiRestClient;
        this.pluginManager = pluginManager;
        this.metadataService = metadataService;
        this.metadataProvider = metadataProvider;
        this.modelTransform = modelTransform;
    }

    /**
     * Creates a new data source using the specified template.
     *
     * @throws CatalogException if the data source is not valid
     */
    @Nonnull
    public DataSource createDataSource(@Nonnull final DataSource source, boolean checkExisting) throws PotentialControllerServiceConflictException{
        return metadataService.commit(() -> {

            // Find connector
            final com.thinkbiganalytics.metadata.api.catalog.Connector connector = Optional.ofNullable(source.getConnector()).map(Connector::getId).map(connectorProvider::resolveId)
                .flatMap(connectorProvider::find).orElseThrow(() -> new CatalogException("catalog.datasource.connector.invalid"));

            // Create data source
            final com.thinkbiganalytics.metadata.api.catalog.DataSource domain = metadataProvider.create(connector.getId(), source.getTitle());

            // Create or update controller service
            final ConnectorPluginDescriptor plugin = pluginManager.getPlugin(connector.getPluginId()).map(ConnectorPlugin::getDescriptor).orElse(null);

            if (plugin != null && plugin.getNifiControllerService() != null) {
                createOrUpdateNiFiControllerService(source, plugin, checkExisting);
            }

            // Update catalog
            final DataSource updatedDataSource = this.credentialManager.applyPlaceholders(source, SecurityContextUtil.getCurrentPrincipals());
            modelTransform.updateDataSource(updatedDataSource, domain);

            // Return a copy with the connector
            return modelTransform.dataSourceToRestModel().apply(domain);
        });
    }

    /**
     * Gets the connector with the specified id.
     */
    @Nonnull
    @SuppressWarnings("squid:S2259")
    public Optional<DataSource> findDataSource(@Nonnull final String id) {
        return findDataSource(id, false);
    }

    /**
     * Gets the connector with the specified id.
     */
    @Nonnull
    @SuppressWarnings("squid:S2259")
    public Optional<DataSource> findDataSource(@Nonnull final String id, final boolean includeCredentials) {
        return metadataService.read(() -> {
            // Find the data source
            DataSource dataSource = metadataProvider.find(metadataProvider.resolveId(id)).map(modelTransform.dataSourceToRestModel()).orElse(null);
            if (dataSource != null) {
                dataSource = new DataSource(dataSource);
            } else {
                try {
                    final Datasource feedDataSource = feedDataSourceProvider.getDatasource(feedDataSourceProvider.resolve(id));
                    DatasourceModelTransform.Level level = DatasourceModelTransform.Level.FULL;
                    if (includeCredentials) {
                        level = DatasourceModelTransform.Level.ADMIN;
                    }
                    dataSource = toDataSource(feedDataSource, level);
                } catch (final IllegalArgumentException e) {
                    log.debug("Failed to resolve data source {}: {}", id, e, e);
                }
            }

            // Set connector
            final Optional<Connector> connector = Optional.ofNullable(dataSource).map(DataSource::getConnector).map(Connector::getId).map(connectorProvider::resolveId).flatMap(connectorProvider::find)
                .map(modelTransform.connectorToRestModel());
            if (connector.isPresent()) {
                dataSource.setConnector(connector.get());
                return Optional.of(dataSource);
            } else {
                if (dataSource != null) {
                    log.error("Unable to find connector for data source: {}", dataSource);
                }
                return Optional.empty();
            }
        });
    }

    /**
     * Gets the list of available connectors.
     */
    @Nonnull
    public Page<DataSource> findAllDataSources(@Nonnull final Pageable pageable, @Nullable final String filter) {
        // Filter catalog data sources
        final Supplier<Stream<DataSource>> catalogDataSources =
            () -> metadataService.read(
                () -> metadataProvider.findAll().stream().map(modelTransform.dataSourceToRestModel())
            );

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
     * Updates a data source using the specified template.
     *
     * @throws CatalogException if the data source is not valid
     */
    @Nonnull
    public DataSource updateDataSource(@Nonnull final DataSource source) {
        return metadataService.commit(() -> {
            // Find data source
            final Optional<com.thinkbiganalytics.metadata.api.catalog.DataSource.ID> domainId = Optional.ofNullable(source.getId()).map(metadataProvider::resolveId);
            final com.thinkbiganalytics.metadata.api.catalog.DataSource domain = domainId.flatMap(metadataProvider::find)
                .orElseThrow(() -> new CatalogException("catalog.datasource.notFound.id", source.getId()));

            // Create or update controller service
            final ConnectorPluginDescriptor plugin = pluginManager.getPlugin(domain.getConnector().getPluginId()).map(ConnectorPlugin::getDescriptor).orElse(null);

            if (plugin != null && plugin.getNifiControllerService() != null) {
                createOrUpdateNiFiControllerService(source, plugin,false);
            }

            // Update catalog
            final DataSource updatedDataSource = this.credentialManager.applyPlaceholders(source, SecurityContextUtil.getCurrentPrincipals());
            modelTransform.updateDataSource(updatedDataSource, domain);

            // Return a copy with the connector
            return modelTransform.dataSourceToRestModel().apply(domain);
        });
    }

    private List<ControllerServiceDTO> findMatchingControllerService(@Nonnull final DataSource dataSource, @Nonnull final Map<String, String> properties, @Nonnull final ConnectorPluginDescriptor connectorPluginDescriptor) {

        ConnectorPluginNiFiControllerService plugin = connectorPluginDescriptor.getNifiControllerService();
        String type = plugin.getType();
        Map<String,String> identityProperties = plugin.getIdentityProperties().stream().collect(Collectors.toMap(propertyKey -> propertyKey, propertyKey -> properties.get(propertyKey)));
        //TODO hit cache first
        String rootProcessGroupId = nifiRestClient.processGroups().findRoot().getId();
        return nifiRestClient.processGroups().getControllerServices(rootProcessGroupId).stream().filter(controllerServiceDTO -> isMatch(controllerServiceDTO,type,dataSource.getTitle(),identityProperties) ).collect(Collectors.toList());
    }

    /**
     * Does the controller service match either the name or the map of identity properties
     * @param controllerServiceDTO
     * @param name
     * @param identityProperties
     * @return
     */
    public boolean isMatch(ControllerServiceDTO controllerServiceDTO, String controllerServiceType,String name, Map<String,String> identityProperties){
        if(controllerServiceDTO.getName().equalsIgnoreCase(name)){
            return true;
        }
        else {
            return controllerServiceDTO.getType().equalsIgnoreCase(controllerServiceType) && identityProperties.entrySet().stream().allMatch(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                String csValue = controllerServiceDTO.getProperties().get(key);
                return csValue != null && csValue.equalsIgnoreCase(value);
            });
        }
    }

    /**
     * Creates or updates the NiFi controller service linked to the specified data source.
     */
    private void createOrUpdateNiFiControllerService(@Nonnull final DataSource dataSource, @Nonnull final ConnectorPluginDescriptor connectorPluginDescriptor,boolean checkExisting) throws PotentialControllerServiceConflictException{

        ConnectorPluginNiFiControllerService plugin = connectorPluginDescriptor.getNifiControllerService();
        // Resolve properties
        final PropertyPlaceholderHelper.PlaceholderResolver resolver = new DataSourcePlaceholderResolver(dataSource);
        final Map<String, String> properties = new HashMap<>(plugin.getProperties().size());
        final Map<String, ConnectorPluginNiFiControllerServicePropertyDescriptor> propertyDescriptors = plugin.getPropertyDescriptors();

        plugin.getProperties().forEach((key, value) -> {
            final String resolvedValue = placeholderHelper.replacePlaceholders(value, resolver);
            //set empty string to null
            ConnectorPluginNiFiControllerServicePropertyDescriptor descriptor = propertyDescriptors != null ? propertyDescriptors.get(key) : null;
            if (StringUtils.isBlank(resolvedValue) && (descriptor == null || (descriptor != null && !descriptor.isEmptyStringIfNull()))) {
                //set the value to null if its not explicitly configured to be set to an empty string
                properties.put(key, null);
            } else if (resolvedValue != null && !resolvedValue.startsWith("{cipher}")) {
                properties.put(key, resolvedValue);
            }
        });

        // Update or create the controller service
        ControllerServiceDTO controllerService = null;

        if (dataSource.getNifiControllerServiceId() != null && dataSource.getId() != null) {
            controllerService = new ControllerServiceDTO();
            controllerService.setId(dataSource.getNifiControllerServiceId());
            controllerService.setName(dataSource.getTitle());
            controllerService.setProperties(properties);
            try {
                controllerService = nifiRestClient.controllerServices().updateServiceAndReferencingComponents(controllerService);
                dataSource.setNifiControllerServiceId(controllerService.getId());
            } catch (final NifiComponentNotFoundException e) {
                log.warn("Controller service is missing for data source: {}", dataSource.getId(), e);
                controllerService = null;
            }
        }
        if (controllerService == null && StringUtils.isBlank(dataSource.getNifiControllerServiceId())) {
            if(checkExisting) {
                List<ControllerServiceDTO> matchingServices = findMatchingControllerService(dataSource,properties,connectorPluginDescriptor);
                if(matchingServices != null && !matchingServices.isEmpty()){
                    Map<String,String> identityProperties = plugin.getIdentityProperties().stream().collect(Collectors.toMap(propertyKey -> propertyKey, propertyKey -> properties.get(propertyKey)));
                    throw new PotentialControllerServiceConflictException(new ControllerServiceConflictEntity(dataSource.getTitle(),identityProperties,matchingServices));
                }
            }
            controllerService = new ControllerServiceDTO();
            controllerService.setType(plugin.getType());
            controllerService.setName(dataSource.getTitle());
            controllerService.setProperties(properties);
            controllerService = nifiRestClient.controllerServices().create(controllerService);
            try {
                nifiRestClient.controllerServices().updateStateById(controllerService.getId(), NiFiControllerServicesRestClient.State.ENABLED);
            } catch (final NifiClientRuntimeException e) {
                log.error("Failed to enable controller service for data source: {}", dataSource.getId(), e);
                nifiRestClient.controllerServices().disableAndDeleteAsync(controllerService.getId());
                throw e;
            }
            dataSource.setNifiControllerServiceId(controllerService.getId());
        }
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
                connector = connectorProvider.find(connectorProvider.resolveId(connectorId)).map(modelTransform.connectorToRestModel());
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
            jdbcConnectorId = connectorProvider.findAll().stream()
                .map(modelTransform.connectorToRestModel())
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
     *
     * @param dataSourceId datasource to be deleted
     */
    public void delete(String dataSourceId) {
        metadataService.commit(() -> {
            com.thinkbiganalytics.metadata.api.catalog.DataSource domain = metadataProvider.find(metadataProvider.resolveId(dataSourceId)).orElse(null);
            if (domain != null) {
                metadataProvider.deleteById(metadataProvider.resolveId(dataSourceId));
                credentialManager.removeCredentials(modelTransform.dataSourceToRestModel().apply(domain));
            }
        });
    }
}
