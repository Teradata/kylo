package com.thinkbiganalytics.feedmgr.nifi.controllerservice;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.nifi.NifiControllerServiceProperties;
import com.thinkbiganalytics.feedmgr.nifi.controllerservice.DescribeTableControllerServiceRequest.DescribeTableControllerServiceRequestBuilder;
import com.thinkbiganalytics.feedmgr.nifi.controllerservice.ExecuteQueryControllerServiceRequest.ExecuteQueryControllerServiceRequestBuilder;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.schema.DBSchemaParser;
import com.thinkbiganalytics.schema.QueryRunner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Allow Kylo to use a NiFi database pool connection to display database metadata and execute queries.
 */
@Service
public class DBCPConnectionPoolService {

    private static final Logger log = LoggerFactory.getLogger(DBCPConnectionPoolService.class);

    @Autowired
    private NifiControllerServiceProperties nifiControllerServiceProperties;

    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosHiveConfiguration;

    @Inject
    private DatasourceProvider datasetProvider;

    @Inject
    private DatasourceModelTransform datasourceTransform;

    @Inject
    private MetadataAccess metadataAccess;

    /**
     * Executes the specified SELECT query in the context of the specified controller service.
     *
     * @param serviceId   a NiFi controller service id
     * @param serviceName a NiFi controller service name
     * @param query       the query to execute
     * @return the query results
     * @throws DataAccessException      if the query cannot be executed
     * @throws IllegalArgumentException if the controller service cannot be found
     */
    @Nonnull
    public QueryResult executeQueryForControllerService(@Nonnull final String serviceId, @Nonnull final String serviceName, @Nonnull final String query) {
        final ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);
        if (controllerService != null) {
            final ExecuteQueryControllerServiceRequestBuilder builder = new ExecuteQueryControllerServiceRequestBuilder(controllerService);
            final ExecuteQueryControllerServiceRequest serviceProperties = builder.query(query).build();
            final PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(serviceProperties);
            return executeQueryForControllerService(dataSourceProperties, serviceProperties);
        } else {
            log.error("Cannot execute query for controller service. Unable to obtain controller service: {}, {}", serviceId, serviceName);
            throw new IllegalArgumentException("Not a valid controller service: " + serviceId + ", " + serviceName);
        }
    }
    
    /**
     * Generates a preview query on a table, in the context of the specified controller service, based
     * on the supplied schema (if provided) and limit.
     *
     * @param serviceId   a NiFi controller service id
     * @param serviceName a NiFi controller service name
     * @param schema the schema determining the SQL dialect, or null/empty for the default dialect
     * @param tableName the name of the table
     * @param limit the result size
     * @return the query results
     * @throws DataAccessException      if the query cannot be executed
     * @throws IllegalArgumentException if the controller service cannot be found, the tableName is empty, or the limit is less than zero
     */
    public String generatePreviewQueryForControllerService(final String serviceId, final String serviceName, @Nonnull final String schema, @Nonnull final String tableName, final int limit) {
        Validate.notEmpty(schema, "No schema provided");
        Validate.notEmpty(tableName, "No table name provided");
        Validate.isTrue(limit >= 0, "The query result size must be greater than or equal to 0");
        
        final ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);
        if (controllerService != null) {
            final ExecuteQueryControllerServiceRequest serviceProperties = new ExecuteQueryControllerServiceRequestBuilder(controllerService).build();
            final PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(serviceProperties);
            final DatabaseType dbType = DatabaseType.fromJdbcConnectionString(dataSourceProperties.getUrl());
            final ExecuteQueryControllerServiceRequest previewRequest = new ExecuteQueryControllerServiceRequestBuilder(controllerService)
                            .using(serviceProperties)
                            .previewQuery(dbType, schema, tableName, limit)
                            .build();
            return previewRequest.getQuery();
        } else {
            log.error("Cannot execute query for controller service. Unable to obtain controller service: {}, {}", serviceId, serviceName);
            throw new IllegalArgumentException("Not a valid controller service: " + serviceId + ", " + serviceName);
        }
    }

    /**
     * Executes preview query on a table, in the context of the specified controller service, based
     * on the supplied schema (if provided) and limit.
     *
     * @param serviceId   a NiFi controller service id
     * @param serviceName a NiFi controller service name
     * @param tableName the name of the table
     * @param schema the schema determining the SQL dialect, or null/empty for the default dialect
     * @param limit the result size
     * @return the query results
     * @throws DataAccessException      if the query cannot be executed
     * @throws IllegalArgumentException if the controller service cannot be found, the schema/tableName is empty, or the limit is less than zero
     */
    public QueryResult executePreviewQueryForControllerService(final String serviceId, final String serviceName, @Nonnull final String schema, @Nonnull final String tableName, final int limit) {
        Validate.notEmpty(schema, "No schema provided");
        Validate.notEmpty(tableName, "No table name provided");
        Validate.isTrue(limit >= 0, "The query result size must be greater than or equal to 0");
        
        final ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);
        if (controllerService != null) {
            final ExecuteQueryControllerServiceRequest serviceProperties = new ExecuteQueryControllerServiceRequestBuilder(controllerService).build();
            final PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(serviceProperties);
            final DatabaseType dbType = DatabaseType.fromJdbcConnectionString(dataSourceProperties.getUrl());
            final ExecuteQueryControllerServiceRequest previewRequest = new ExecuteQueryControllerServiceRequestBuilder(controllerService)
                            .using(serviceProperties)
                            .previewQuery(dbType, schema, tableName, limit)
                            .build();
            return executeQueryForControllerService(dataSourceProperties, previewRequest);
        } else {
            log.error("Cannot execute query for controller service. Unable to obtain controller service: {}, {}", serviceId, serviceName);
            throw new IllegalArgumentException("Not a valid controller service: " + serviceId + ", " + serviceName);
        }
    }
    
    /**
     * Generates a preview query on a table that is appropriate for the type of specified data source.
     *
     * @param datasource the JDBC datasource
     * @param schema the schema determining the SQL dialect, or null/empty for the default dialect
     * @param tableName the name of the table
     * @param limit the result size
     * @return the query string
     * @throws IllegalArgumentException if the datasource is invalid
     */
    @Nonnull
    public String generatePreviewQueryForDatasource(@Nonnull final JdbcDatasource datasource, @Nonnull final String schema, @Nonnull final String tableName, final int limit) {
        final Optional<ControllerServiceDTO> controllerService = Optional.ofNullable(datasource.getControllerServiceId())
                        .map(id -> getControllerService(id, null));
        if (controllerService.isPresent()) {
            final DatabaseType dbType = DatabaseType.fromJdbcConnectionString(datasource.getDatabaseConnectionUrl());
            final ExecuteQueryControllerServiceRequest serviceProperties = new ExecuteQueryControllerServiceRequestBuilder(controllerService.get())
                            .previewQuery(dbType, schema, tableName, limit)
                            .build();
            return serviceProperties.getQuery();
        } else {
            log.error("Cannot execute query for datasource: {}", datasource);
            throw new IllegalArgumentException("Missing controller service for datasource: " + datasource);
        }
    }

    /**
     * Tests connection for specified data source using Validation Query defined in DatabaseType
     *
     * @param datasource the JDBC datasource
     * @param query validation query
     * @throws DataAccessException      if the query cannot be executed
     * @throws IllegalArgumentException if the datasource is invalid
     */
    @Nonnull
    public QueryResult testConnectionForDatasource(@Nonnull final JdbcDatasource datasource, @Nonnull String query) {
        ControllerServiceDTO dummyService = new ControllerServiceDTO(); //this service does not exist in Nifi, just to reuse existing code
        dummyService.setType("org.apache.nifi.dbcp.DBCPConnectionPool");
        dummyService.setName(datasource.getName()); //service name may be used to look up values from application.properties, e.g. nifi.service.<service_name>.password=***
        dummyService.setId("Test Connection Service"); //arbitrary, appears in the logs
        HashMap<String, String> properties = new HashMap<>();
        dummyService.setProperties(properties);

        if (isMasked(datasource.getPassword())) {

            if( StringUtils.isNotBlank(datasource.getId())){
            //if this is already saved, try to use the saved password
                Optional.of(this.datasetProvider.resolve(datasource.getId())).map(datasetProvider::getDatasource)
                    .map(ds -> datasourceTransform.toDatasource(ds, DatasourceModelTransform.Level.ADMIN))
                    .filter(JdbcDatasource.class::isInstance)
                    .map(JdbcDatasource.class::cast).ifPresent((ds) -> {
                        datasource.setPassword(ds.getPassword());
                });

            }else {
                datasource.setPassword(null);
            }


        }

        final ExecuteQueryControllerServiceRequestBuilder builder = new ExecuteQueryControllerServiceRequestBuilder(dummyService);
        final ExecuteQueryControllerServiceRequest serviceProperties = builder.password(datasource.getPassword()).query(query).useEnvironmentProperties(true).build();
        properties.put(serviceProperties.getConnectionStringPropertyKey(), datasource.getDatabaseConnectionUrl());
        properties.put(serviceProperties.getUserNamePropertyKey(), datasource.getDatabaseUser());
        properties.put(serviceProperties.getDriverClassNamePropertyKey(), datasource.getDatabaseDriverClassName());
        final PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(serviceProperties);
        return executeQueryForControllerService(dataSourceProperties, serviceProperties);
    }
    
    /**
     * Executes the specified SELECT query in the context of the specified data source.
     *
     * @param datasource the JDBC datasource
     * @param query      the query to execute
     * @return the query results
     * @throws DataAccessException      if the query cannot be executed
     * @throws IllegalArgumentException if the datasource is invalid
     */
    @Nonnull
    public QueryResult executeQueryForDatasource(@Nonnull final JdbcDatasource datasource, @Nonnull final String query) {
        final Optional<ControllerServiceDTO> controllerService = Optional.ofNullable(datasource.getControllerServiceId())
            .map(id -> getControllerService(id, null));
        if (controllerService.isPresent()) {
            final ExecuteQueryControllerServiceRequestBuilder builder = new ExecuteQueryControllerServiceRequestBuilder(controllerService.get());
            final ExecuteQueryControllerServiceRequest serviceProperties = builder.password(datasource.getPassword()).query(query).useEnvironmentProperties(false).build();
            final PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(serviceProperties);
            return executeQueryForControllerService(dataSourceProperties, serviceProperties);
        } else {
            log.error("Cannot execute query for datasource: {}", datasource);
            throw new IllegalArgumentException("Missing controller service for datasource: " + datasource);
        }
    }

    /**
     * Executes the specified SELECT query in the context of the specified data source.
     *
     * @param datasource the JDBC datasource
     * @param query      the query to execute
     * @return the query results
     * @throws DataAccessException      if the query cannot be executed
     * @throws IllegalArgumentException if the datasource is invalid
     */
    @Nonnull
    public QueryResult executePreviewQueryForDatasource(@Nonnull final JdbcDatasource datasource, @Nonnull final String schema, @Nonnull final String tableName, final int limit) {
        final Optional<ControllerServiceDTO> controllerService = Optional.ofNullable(datasource.getControllerServiceId())
                        .map(id -> getControllerService(id, null));
        if (controllerService.isPresent()) {
            final DatabaseType dbType = DatabaseType.fromJdbcConnectionString(datasource.getDatabaseConnectionUrl());
            final ExecuteQueryControllerServiceRequest serviceProperties = new ExecuteQueryControllerServiceRequestBuilder(controllerService.get())
                            .password(datasource.getPassword())
                            .previewQuery(dbType, schema, tableName, limit)
                            .useEnvironmentProperties(false)
                            .build();
            final PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(serviceProperties);
            return executeQueryForControllerService(dataSourceProperties, serviceProperties);
        } else {
            log.error("Cannot execute query for datasource: {}", datasource);
            throw new IllegalArgumentException("Missing controller service for datasource: " + datasource);
        }
    }

    /**
     * Returns a list of table names matching a pattern
     *
     * @param serviceId   a NiFi controller service id
     * @param serviceName a NiFi controller service name
     * @param schema      A schema pattern to look for
     * @param tableName   A table pattern to look for
     * @return a list of schema.table names matching the pattern for the database
     */
    public List<String> getTableNamesForControllerService(String serviceId, String serviceName, String schema, String tableName) {
        ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);

        if (controllerService != null) {
            DescribeTableControllerServiceRequestBuilder builder = new DescribeTableControllerServiceRequestBuilder(controllerService);
            DescribeTableControllerServiceRequest serviceProperties = builder.schemaName(schema).tableName(tableName).build();
            return getTableNamesForControllerService(serviceProperties);
        } else {
            log.error("Cannot getTable Names for Controller Service. Unable to obtain Controller Service for serviceId or Name ({} , {})", serviceId, serviceName);
        }
        return null;
    }

    /**
     * Returns a list of schema names for the specified data source.
     *
     * @param datasource the data source
     * @return a list of schema names, or {@code null} if not accessible
     */
    @Nullable
    public List<String> getSchemaNamesForDatasource(@Nonnull final JdbcDatasource datasource) {
        final Optional<ControllerServiceDTO> controllerService = Optional.ofNullable(datasource.getControllerServiceId())
            .map(id -> getControllerService(id, null));
        if (controllerService.isPresent()) {
            final DescribeTableControllerServiceRequestBuilder builder = new DescribeTableControllerServiceRequestBuilder(controllerService.get());
            final DescribeTableControllerServiceRequest serviceProperties = builder.password(datasource.getPassword()).useEnvironmentProperties(false).build();
            return getSchemaNamesForControllerService(serviceProperties);
        } else {
            log.error("Cannot get table names for data source: {}", datasource);
            return null;
        }
    }

    /**
     * Returns a list of table names for the specified data source.
     *
     * @param datasource the data source
     * @param schema     the schema name, or {@code null} for all schemas
     * @param tableName  a table pattern to look for
     * @return a list of schema.table names, or {@code null} if not accessible
     */
    @Nullable
    public List<String> getTableNamesForDatasource(@Nonnull final JdbcDatasource datasource, @Nullable final String schema, @Nullable final String tableName) {
        final Optional<ControllerServiceDTO> controllerService = Optional.ofNullable(datasource.getControllerServiceId())
            .map(id -> getControllerService(id, null));
        if (controllerService.isPresent()) {
            final DescribeTableControllerServiceRequestBuilder builder = new DescribeTableControllerServiceRequestBuilder(controllerService.get());
            final DescribeTableControllerServiceRequest serviceProperties = builder.schemaName(schema).tableName(tableName).password(datasource.getPassword()).useEnvironmentProperties(false).build();
            return getTableNamesForControllerService(serviceProperties);
        } else {
            log.error("Cannot get table names for data source: {}", datasource);
            return null;
        }
    }

    /**
     * Describe the database table and fields available for a given NiFi controller service
     *
     * @param serviceId   a NiFi controller service id
     * @param serviceName a NiFi controller service name
     * @param schema      A schema  to look for
     * @param tableName   A table  to look for
     * @return the database table and fields
     */
    public TableSchema describeTableForControllerService(String serviceId, String serviceName, String schema, String tableName) {

        ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);
        if (controllerService != null) {
            DescribeTableControllerServiceRequestBuilder builder = new DescribeTableControllerServiceRequestBuilder(controllerService);
            DescribeTableControllerServiceRequest serviceProperties = builder.schemaName(schema).tableName(tableName).build();
            return describeTableForControllerService(serviceProperties);
        } else {
            log.error("Cannot describe Table for Controller Service. Unable to obtain Controller Service for serviceId or Name ({} , {})", serviceId, serviceName);
        }
        return null;
    }

    /**
     * Describes the specified database table accessed through the specified data source.
     *
     * @param datasource the data source
     * @param schema     the schema name, or {@code null} to search all schemas
     * @param tableName  the table name
     * @return the database table and fields, or {@code null} if not found
     */
    public TableSchema describeTableForDatasource(@Nonnull final JdbcDatasource datasource, @Nullable final String schema, @Nonnull final String tableName) {
        final Optional<ControllerServiceDTO> controllerService = Optional.ofNullable(datasource.getControllerServiceId())
            .map(id -> getControllerService(id, null));
        if (controllerService.isPresent()) {
            final DescribeTableControllerServiceRequestBuilder builder = new DescribeTableControllerServiceRequestBuilder(controllerService.get());
            final DescribeTableControllerServiceRequest serviceProperties = builder.schemaName(schema).tableName(tableName).password(datasource.getPassword()).useEnvironmentProperties(false).build();
            return describeTableForControllerService(serviceProperties);
        } else {
            log.error("Cannot describe table for data source: {}", datasource);
            return null;
        }
    }

    /**
     * Executes the specified SELECT query in the context of the specified controller service.
     *
     * @param serviceProperties properties describing the data source and the query
     * @return the query results
     * @throws DataAccessException if the query cannot be executed
     */
    @Nonnull
    private QueryResult executeQueryForControllerService(@Nonnull final PoolingDataSourceService.DataSourceProperties dataSourceProperties, 
                                                         @Nonnull final ExecuteQueryControllerServiceRequest serviceProperties) {
        if (evaluateWithUserDefinedDatasources(dataSourceProperties, serviceProperties)) {
            log.info("Execute query against Controller Service: {} ({}) with uri of {}.  ", serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(),
                     dataSourceProperties.getUrl());
            final DataSource dataSource = PoolingDataSourceService.getDataSource(dataSourceProperties);
            return new QueryRunner(dataSource).query(serviceProperties.getQuery());
        } else {
            throw new DataAccessResourceFailureException("Unable to determine connection properties for controller service: " + serviceProperties.getControllerServiceName() + "("
                                                         + serviceProperties.getControllerServiceId() + ")");
        }
    }

    private PoolingDataSourceService.DataSourceProperties getDataSourceProperties(final ExecuteQueryControllerServiceRequest serviceProperties) {
        final Map<String, String> properties = serviceProperties.useEnvironmentProperties()
                                               ? nifiControllerServiceProperties.mergeNifiAndEnvProperties(serviceProperties.getControllerServiceDTO().getProperties(),
                                                                                                           serviceProperties.getControllerServiceName())
                                               : serviceProperties.getControllerServiceDTO().getProperties();

        final PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(properties, serviceProperties);
        return dataSourceProperties;
    }

    /**
     * Return a list of schema.table_name
     *
     * @param serviceProperties properties describing where and what to look for
     * @return a list of schema.table_name
     */
    private List<String> getSchemaNamesForControllerService(DescribeTableControllerServiceRequest serviceProperties) {

        if (serviceProperties != null) {
            Map<String, String> properties = serviceProperties.useEnvironmentProperties()
                                             ? nifiControllerServiceProperties.mergeNifiAndEnvProperties(serviceProperties.getControllerServiceDTO().getProperties(),
                                                                                                         serviceProperties.getControllerServiceName())
                                             : serviceProperties.getControllerServiceDTO().getProperties();

            PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(properties, serviceProperties);

            boolean valid = evaluateWithUserDefinedDatasources(dataSourceProperties, serviceProperties);

            if (valid) {
                log.info("Search For Tables against Controller Service: {} ({}) with uri of {}.  ", serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(),
                         dataSourceProperties.getUrl());
                DataSource dataSource = PoolingDataSourceService.getDataSource(dataSourceProperties);
                DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
                return schemaParser.listSchemasOrCatalogs();
            }
        }
        return null;
    }

    /**
     * Return a list of schema.table_name
     *
     * @param serviceProperties properties describing where and what to look for
     * @return a list of schema.table_name
     */
    private List<String> getTableNamesForControllerService(DescribeTableControllerServiceRequest serviceProperties) {

        if (serviceProperties != null) {
            Map<String, String> properties = serviceProperties.useEnvironmentProperties()
                                             ? nifiControllerServiceProperties.mergeNifiAndEnvProperties(serviceProperties.getControllerServiceDTO().getProperties(),
                                                                                                         serviceProperties.getControllerServiceName())
                                             : serviceProperties.getControllerServiceDTO().getProperties();

            PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(properties, serviceProperties);

            boolean valid = evaluateWithUserDefinedDatasources(dataSourceProperties, serviceProperties);

            if (valid) {
                log.info("Search For Tables against Controller Service: {} ({}) with uri of {}.  ", serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(),
                         dataSourceProperties.getUrl());
                DataSource dataSource = PoolingDataSourceService.getDataSource(dataSourceProperties);
                DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
                return schemaParser.listTables(serviceProperties.getSchemaName());
            }
        }
        return null;
    }

    private boolean evaluateWithUserDefinedDatasources(PoolingDataSourceService.DataSourceProperties dataSourceProperties, AbstractControllerServiceRequest serviceProperties) {
        boolean valid = !isMasked(dataSourceProperties.getPassword());
        if (!valid) {
            List<Datasource> matchingDatasources = metadataAccess.read(() -> {
                //attempt to get the properties from the stored datatsource
                return datasetProvider
                    .getDatasources(datasetProvider.datasetCriteria().type(com.thinkbiganalytics.metadata.api.datasource.UserDatasource.class).name(serviceProperties.getControllerServiceName()))
                    .stream()
                    .map(ds -> datasourceTransform.toDatasource(ds, DatasourceModelTransform.Level.ADMIN)).filter(datasource -> datasource instanceof JdbcDatasource)
                    .collect(Collectors.toList());
            }, MetadataAccess.SERVICE);

            if (matchingDatasources != null && !matchingDatasources.isEmpty()) {
                JdbcDatasource
                    userDatasource =
                    (JdbcDatasource) matchingDatasources.stream().filter(ds -> ((JdbcDatasource) ds).getDatabaseUser().equalsIgnoreCase(dataSourceProperties.getUser())).findFirst().orElse(null);
                if (userDatasource == null) {
                    userDatasource = (JdbcDatasource) matchingDatasources.get(0);
                }
                if (userDatasource != null) {
                    dataSourceProperties.setUser(userDatasource.getDatabaseUser());
                    dataSourceProperties.setPassword(userDatasource.getPassword());
                    log.info("Returned user defined datasource for {} service and user {} ", serviceProperties.getControllerServiceName(), userDatasource.getDatabaseUser());
                    valid = true;
                }
            }
            if (!valid) {
                String propertyKey = nifiControllerServiceProperties.getEnvironmentControllerServicePropertyPrefix(serviceProperties.getControllerServiceName()) + ".password";
                String example = propertyKey + "=PASSWORD";
                log.error("Unable to connect to Controller Service {}, {}.  You need to specify a configuration property as {} with the password for user: {}. ",
                          serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(), example, dataSourceProperties.getUser());
            }
        }
        return valid;

    }

    /**
     * isMasked(null)    == false
     * isMasked("")      == false
     * isMasked(" ")     == false
     * isMasked("**")    == true
     * isMasked("abc")   == false
     */
    private boolean isMasked(String password) {
        return StringUtils.isNotBlank(password) && password.startsWith("**");
    }


    /**
     * get the validation query from the db name that is parsed from the
     */
    private String parseValidationQueryFromConnectionString(String connectionString) {
        String validationQuery = null;
        try {
            if (StringUtils.isNotBlank(connectionString)) {
                DatabaseType databaseType = DatabaseType.fromJdbcConnectionString(connectionString);
                validationQuery = databaseType.getValidationQuery();
            }
        } catch (IllegalArgumentException e) {
            //if we cant find it in the map its ok.
        }
        return validationQuery;
    }


    private TableSchema describeTableForControllerService(DescribeTableControllerServiceRequest serviceProperties) {

        String type = serviceProperties.getControllerServiceType();
        if (serviceProperties.getControllerServiceType() != null && serviceProperties.getControllerServiceType().equalsIgnoreCase(type)) {
            Map<String, String> properties = serviceProperties.useEnvironmentProperties()
                                             ? nifiControllerServiceProperties.mergeNifiAndEnvProperties(serviceProperties.getControllerServiceDTO().getProperties(),
                                                                                                         serviceProperties.getControllerServiceName())
                                             : serviceProperties.getControllerServiceDTO().getProperties();

            PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(properties, serviceProperties);
            boolean valid = evaluateWithUserDefinedDatasources(dataSourceProperties, serviceProperties);
            if (valid) {
                log.info("describing Table {}.{} against Controller Service: {} ({}) with uri of {} ", serviceProperties.getSchemaName(), serviceProperties.getTableName(),
                         serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(), dataSourceProperties.getUrl());
                DataSource dataSource = PoolingDataSourceService.getDataSource(dataSourceProperties);
                DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
                return schemaParser.describeTable(serviceProperties.getSchemaName(), serviceProperties.getTableName());
            } else {
                return null;
            }
        }
        return null;

    }


    private ControllerServiceDTO getControllerService(String serviceId, String serviceName) {
        return nifiControllerServiceProperties.getControllerService(serviceId, serviceName);
    }


    public PoolingDataSourceService.DataSourceProperties getDataSourceProperties(Map<String, String> properties, AbstractControllerServiceRequest serviceProperties) {
        String uri = properties.get(serviceProperties.getConnectionStringPropertyKey());
        String user = properties.get(serviceProperties.getUserNamePropertyKey());
        String password = (serviceProperties.getPassword() != null) ? serviceProperties.getPassword() : properties.get(serviceProperties.getPasswordPropertyKey());
        String driverClassName = properties.get(serviceProperties.getDriverClassNamePropertyKey());
        if (StringUtils.isBlank(driverClassName)) {
            driverClassName = nifiControllerServiceProperties.getEnvironmentPropertyValueForControllerService(serviceProperties.getControllerServiceName(), "database_driver_class_name");
        }
        if (StringUtils.isBlank(password)) {
            password = nifiControllerServiceProperties.getEnvironmentPropertyValueForControllerService(serviceProperties.getControllerServiceName(), "password");
        }

        String validationQuery = nifiControllerServiceProperties.getEnvironmentPropertyValueForControllerService(serviceProperties.getControllerServiceName(), "validationQuery");
        if (StringUtils.isBlank(validationQuery) && StringUtils.isNotBlank(uri)) {
            //attempt to get it from parsing the connection string
            validationQuery = parseValidationQueryFromConnectionString(uri);
        }
        boolean testOnBorrow = StringUtils.isNotBlank(validationQuery);
        return new PoolingDataSourceService.DataSourceProperties(user, password, uri, driverClassName, testOnBorrow, validationQuery);
    }
}
