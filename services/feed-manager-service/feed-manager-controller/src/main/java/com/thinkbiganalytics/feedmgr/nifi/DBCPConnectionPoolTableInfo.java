package com.thinkbiganalytics.feedmgr.nifi;

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
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.schema.DBSchemaParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Allow Kylo to use a NiFi database pool connection to display database metadata with tables and columns.
 */
@Service
public class DBCPConnectionPoolTableInfo {

    private static final Logger log = LoggerFactory.getLogger(DBCPConnectionPoolTableInfo.class);

    @Autowired
    private NifiControllerServiceProperties nifiControllerServiceProperties;

    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosHiveConfiguration;

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
            DescribeTableWithControllerServiceBuilder builder = new DescribeTableWithControllerServiceBuilder(controllerService);
            DescribeTableWithControllerService serviceProperties = builder.schemaName(schema).tableName(tableName).build();
            return getTableNamesForControllerService(serviceProperties);
        } else {
            log.error("Cannot getTable Names for Controller Service. Unable to obtain Controller Service for serviceId or Name ({} , {})", serviceId, serviceName);
        }
        return null;
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
            final DescribeTableWithControllerServiceBuilder builder = new DescribeTableWithControllerServiceBuilder(controllerService.get());
            final DescribeTableWithControllerService serviceProperties = builder.schemaName(schema).tableName(tableName).password(datasource.getPassword()).useEnvironmentProperties(false).build();
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
            DescribeTableWithControllerServiceBuilder builder = new DescribeTableWithControllerServiceBuilder(controllerService);
            DescribeTableWithControllerService serviceProperties = builder.schemaName(schema).tableName(tableName).build();
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
            final DescribeTableWithControllerServiceBuilder builder = new DescribeTableWithControllerServiceBuilder(controllerService.get());
            final DescribeTableWithControllerService serviceProperties = builder.schemaName(schema).tableName(tableName).password(datasource.getPassword()).useEnvironmentProperties(false).build();
            return describeTableForControllerService(serviceProperties);
        } else {
            log.error("Cannot describe table for data source: {}", datasource);
            return null;
        }
    }

    /**
     * Return a list of schema.table_name
     *
     * @param serviceProperties properties describing where and what to look for
     * @return a list of schema.table_name
     */
    private List<String> getTableNamesForControllerService(DescribeTableWithControllerService serviceProperties) {

        if (serviceProperties != null) {
            Map<String, String> properties = serviceProperties.useEnvironmentProperties()
                                             ? nifiControllerServiceProperties.mergeNifiAndEnvProperties(serviceProperties.getControllerServiceDTO().getProperties(),
                                                                                                         serviceProperties.getControllerServiceName())
                                             : serviceProperties.getControllerServiceDTO().getProperties();

            PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(properties, serviceProperties);

            if (StringUtils.isNotBlank(dataSourceProperties.getPassword()) && dataSourceProperties.getPassword().startsWith("**")) {
                String propertyKey = nifiControllerServiceProperties.getEnvironmentControllerServicePropertyPrefix(serviceProperties.getControllerServiceName()) + ".password";
                String example = propertyKey + "=PASSWORD";
                log.error("Unable to connect to Controller Service {}, {}.  You need to specifiy a configuration property as {} with the password for user: {}. ",
                          serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(), example, dataSourceProperties.getUser());
            }
            log.info("Search For Tables against Controller Service: {} ({}) with uri of {}.  ", serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(),
                     dataSourceProperties.getUrl());
            DataSource dataSource = PoolingDataSourceService.getDataSource(dataSourceProperties);
            DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
            return schemaParser.listTables(serviceProperties.getSchemaName(), serviceProperties.getTableName());
        }
        return null;
    }


    /**
     * get the validation query from the db name that is parsed from the
     */
    private String parseValidationQueryFromConnectionString(String connectionString) {
        String validationQuery = null;
        try {
            DatabaseType databaseType = DatabaseType.fromJdbcConnectionString(connectionString);
            validationQuery = databaseType.getValidationQuery();

        } catch (IllegalArgumentException e) {
            //if we cant find it in the map its ok.
        }
        return validationQuery;
    }


    private TableSchema describeTableForControllerService(DescribeTableWithControllerService serviceProperties) {

        String type = serviceProperties.getControllerServiceType();
        if (serviceProperties.getControllerServiceType() != null && serviceProperties.getControllerServiceType().equalsIgnoreCase(type)) {
            Map<String, String> properties = serviceProperties.useEnvironmentProperties()
                                             ? nifiControllerServiceProperties.mergeNifiAndEnvProperties(serviceProperties.getControllerServiceDTO().getProperties(),
                                                                                                         serviceProperties.getControllerServiceName())
                                             : serviceProperties.getControllerServiceDTO().getProperties();

            PoolingDataSourceService.DataSourceProperties dataSourceProperties = getDataSourceProperties(properties, serviceProperties);
            log.info("describing Table {}.{} against Controller Service: {} ({}) with uri of {} ", serviceProperties.getSchemaName(), serviceProperties.getTableName(),
                     serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(), dataSourceProperties.getUrl());
            DataSource dataSource = PoolingDataSourceService.getDataSource(dataSourceProperties);
            DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
            return schemaParser.describeTable(serviceProperties.getSchemaName(), serviceProperties.getTableName());
        }
        return null;

    }


    private ControllerServiceDTO getControllerService(String serviceId, String serviceName) {
        ControllerServiceDTO controllerService = nifiControllerServiceProperties.getControllerServiceById(serviceId);
        if (controllerService == null) {
            controllerService = nifiControllerServiceProperties.getControllerServiceByName(serviceName);
        }
        return controllerService;
    }


    public PoolingDataSourceService.DataSourceProperties getDataSourceProperties(Map<String, String> properties, DescribeTableWithControllerService serviceProperties) {
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
        if (StringUtils.isBlank(validationQuery)) {
            //attempt to get it from parsing the connection string
            validationQuery = parseValidationQueryFromConnectionString(uri);
        }
        boolean testOnBorrow = StringUtils.isNotBlank(validationQuery);
        return new PoolingDataSourceService.DataSourceProperties(user, password, uri, driverClassName, testOnBorrow, validationQuery);
    }


    private static class DescribeTableWithControllerServiceBuilder {

        private String connectionStringPropertyKey;
        private String userNamePropertyKey;
        private String passwordPropertyKey;
        private String driverClassNamePropertyKey;
        private String controllerServiceType;
        private String controllerServiceName;
        private String controllerServiceId;
        private String tableName;
        private String schemaName;
        private ControllerServiceDTO controllerServiceDTO;
        private String password;
        private boolean useEnvironmentProperties = true;


        public DescribeTableWithControllerServiceBuilder(ControllerServiceDTO controllerServiceDTO) {
            this.controllerServiceDTO = controllerServiceDTO;
            this.controllerServiceType = controllerServiceDTO != null ? controllerServiceDTO.getType() : null;
            this.controllerServiceId = controllerServiceDTO != null ? controllerServiceDTO.getId() : null;
            this.controllerServiceName = controllerServiceDTO != null ? controllerServiceDTO.getName() : null;
            initializePropertiesFromControllerServiceType();
        }


        public DescribeTableWithControllerServiceBuilder connectionStringPropertyKey(String connectionStringPropertyKey) {
            this.connectionStringPropertyKey = connectionStringPropertyKey;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder userNamePropertyKey(String userNamePropertyKey) {
            this.userNamePropertyKey = userNamePropertyKey;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder passwordPropertyKey(String passwordPropertyKey) {
            this.passwordPropertyKey = passwordPropertyKey;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder driverClassNamePropertyKey(String driverClassNamePropertyKey) {
            this.driverClassNamePropertyKey = driverClassNamePropertyKey;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder controllerServiceType(String controllerServiceType) {
            this.controllerServiceType = controllerServiceType;

            return this;
        }

        private void initializePropertiesFromControllerServiceType() {
            if ("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(controllerServiceType)) {
                this.connectionStringPropertyKey = "Database Connection URL";
                this.userNamePropertyKey = "Database User";
                this.passwordPropertyKey = "Password";
                this.driverClassNamePropertyKey = "Database Driver Class Name";
            } else if ("com.thinkbiganalytics.nifi.v2.sqoop.StandardSqoopConnectionService".equalsIgnoreCase(controllerServiceType)) {
                this.connectionStringPropertyKey = "Source Connection String";
                this.userNamePropertyKey = "Source User Name";
                this.passwordPropertyKey = "Password";  // users will need to add this as a different property to the application.properties file
            }
        }

        public DescribeTableWithControllerServiceBuilder controllerService(ControllerServiceDTO controllerServiceDTO) {
            this.controllerServiceDTO = controllerServiceDTO;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder controllerServiceName(String controllerServiceName) {
            this.controllerServiceName = controllerServiceName;
            return this;
        }


        public DescribeTableWithControllerServiceBuilder controllerServiceId(String controllerServiceId) {
            this.controllerServiceId = controllerServiceId;
            return this;
        }


        public DescribeTableWithControllerServiceBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder schemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder password(String password) {
            this.password = password;
            return this;
        }

        public DescribeTableWithControllerServiceBuilder useEnvironmentProperties(boolean useEnvironmentProperties) {
            this.useEnvironmentProperties = useEnvironmentProperties;
            return this;
        }

        public DescribeTableWithControllerService build() {
            DescribeTableWithControllerService serviceProperties = new DescribeTableWithControllerService();
            serviceProperties.setConnectionStringPropertyKey(this.connectionStringPropertyKey);
            serviceProperties.setControllerServiceName(this.controllerServiceName);
            serviceProperties.setControllerServiceId(this.controllerServiceId);
            serviceProperties.setControllerServiceType(this.controllerServiceType);
            serviceProperties.setSchemaName(schemaName);
            serviceProperties.setTableName(tableName);
            serviceProperties.setUserNamePropertyKey(userNamePropertyKey);
            serviceProperties.setPasswordPropertyKey(passwordPropertyKey);
            serviceProperties.setControllerServiceDTO(this.controllerServiceDTO);
            serviceProperties.setDriverClassNamePropertyKey(this.driverClassNamePropertyKey);
            serviceProperties.setPassword(password);
            serviceProperties.setUseEnvironmentProperties(useEnvironmentProperties);
            return serviceProperties;
        }
    }


    public static class DescribeTableWithControllerService {

        private String connectionStringPropertyKey;
        private String userNamePropertyKey;
        private String passwordPropertyKey;
        private String driverClassNamePropertyKey;
        private String controllerServiceType;
        private String controllerServiceName;
        private String controllerServiceId;
        private String tableName;
        private String schemaName;
        private ControllerServiceDTO controllerServiceDTO;
        private String password;
        private boolean useEnvironmentProperties;

        public String getConnectionStringPropertyKey() {
            return connectionStringPropertyKey;
        }

        public void setConnectionStringPropertyKey(String connectionStringPropertyKey) {
            this.connectionStringPropertyKey = connectionStringPropertyKey;
        }

        public String getUserNamePropertyKey() {
            return userNamePropertyKey;
        }

        public void setUserNamePropertyKey(String userNamePropertyKey) {
            this.userNamePropertyKey = userNamePropertyKey;
        }

        public String getPasswordPropertyKey() {
            return passwordPropertyKey;
        }

        public void setPasswordPropertyKey(String passwordPropertyKey) {
            this.passwordPropertyKey = passwordPropertyKey;
        }

        public String getDriverClassNamePropertyKey() {
            return driverClassNamePropertyKey;
        }

        public void setDriverClassNamePropertyKey(String driverClassNamePropertyKey) {
            this.driverClassNamePropertyKey = driverClassNamePropertyKey;
        }

        public String getControllerServiceType() {
            return controllerServiceType;
        }

        public void setControllerServiceType(String controllerServiceType) {
            this.controllerServiceType = controllerServiceType;
        }

        public String getControllerServiceName() {
            return controllerServiceName;
        }

        public void setControllerServiceName(String controllerServiceName) {
            this.controllerServiceName = controllerServiceName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getControllerServiceId() {
            return controllerServiceId;
        }

        public void setControllerServiceId(String controllerServiceId) {
            this.controllerServiceId = controllerServiceId;
        }

        public ControllerServiceDTO getControllerServiceDTO() {
            return controllerServiceDTO;
        }

        public void setControllerServiceDTO(ControllerServiceDTO controllerServiceDTO) {
            this.controllerServiceDTO = controllerServiceDTO;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean useEnvironmentProperties() {
            return useEnvironmentProperties;
        }

        public void setUseEnvironmentProperties(boolean useEnvironmentProperties) {
            this.useEnvironmentProperties = useEnvironmentProperties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DescribeTableWithControllerService that = (DescribeTableWithControllerService) o;

            if (connectionStringPropertyKey != null ? !connectionStringPropertyKey.equals(that.connectionStringPropertyKey) : that.connectionStringPropertyKey != null) {
                return false;
            }
            if (controllerServiceName != null ? !controllerServiceName.equals(that.controllerServiceName) : that.controllerServiceName != null) {
                return false;
            }
            if (controllerServiceId != null ? !controllerServiceId.equals(that.controllerServiceId) : that.controllerServiceId != null) {
                return false;
            }
            return schemaName != null ? schemaName.equals(that.schemaName) : that.schemaName == null;
        }

        @Override
        public int hashCode() {
            int result = connectionStringPropertyKey != null ? connectionStringPropertyKey.hashCode() : 0;
            result = 31 * result + (controllerServiceName != null ? controllerServiceName.hashCode() : 0);
            result = 31 * result + (controllerServiceId != null ? controllerServiceId.hashCode() : 0);
            result = 31 * result + (schemaName != null ? schemaName.hashCode() : 0);
            return result;
        }
    }
}
