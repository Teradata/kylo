package com.thinkbiganalytics.feedmgr.nifi;

import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
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

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Created by sr186054 on 1/28/16.
 */
@Service
public class DBCPConnectionPoolTableInfo {

    @Autowired
    private NifiControllerServiceProperties nifiControllerServiceProperties;

    private static final Logger log = LoggerFactory.getLogger(DBCPConnectionPoolTableInfo.class);

    @Autowired
    LegacyNifiRestClient nifiRestClient;

    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosHiveConfiguration;

    public List<String> getTableNamesForControllerService(String serviceId, String serviceName, String schema) {
        ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);

        if (controllerService != null) {
            DescribeTableWithControllerServiceBuilder builder = new DescribeTableWithControllerServiceBuilder(controllerService);
            DescribeTableWithControllerService serviceProperties = builder.schemaName(schema).build();
           return getTableNamesForControllerService(serviceProperties);
        } else {
            log.error("Cannot getTable Names for Controller Service. Unable to obtain Controller Service for serviceId or Name ({} , {})", serviceId, serviceName);
        }
        return null;
    }


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


    private List<String> getTableNamesForControllerService(DescribeTableWithControllerService serviceProperties) {

        if (serviceProperties != null) {
            String type = serviceProperties.getControllerServiceType();
            Map<String, String> properties = nifiControllerServiceProperties.mergeNifiAndEnvProperties(
                serviceProperties.getControllerServiceDTO().getProperties(), serviceProperties.getControllerServiceName());
            String uri = properties.get(serviceProperties.getConnectionStringPropertyKey());
            String user = properties.get(serviceProperties.getUserNamePropertyKey());
            String password = properties.get(serviceProperties.getPasswordPropertyKey());
            if(StringUtils.isBlank(password)){
             password =  nifiControllerServiceProperties.getEnvironmentPropertyValueForControllerService(serviceProperties.getControllerServiceName(),"password");
            }
            if (StringUtils.isNotBlank(password) && password.startsWith("**")) {
                String propertyKey = nifiControllerServiceProperties.getEnvironmentControllerServicePropertyPrefix(serviceProperties.getControllerServiceName()) + ".password";
                String example = propertyKey + "=PASSWORD";
                log.error("Unable to connect to Controller Service {}, {}.  You need to specifiy a configuration property as {} with the password for user: {}. ",
                          serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(), example, user);
            }
            log.info("Search For Tables against Controller Service: {} ({}) with uri of {}.  ", serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(), uri);
            DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
            DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
            return schemaParser.listTables(serviceProperties.getSchemaName());
        }
        return null;
    }


    private TableSchema describeTableForControllerService(DescribeTableWithControllerService serviceProperties) {

        String type = serviceProperties.getControllerServiceType();
        if (serviceProperties.getControllerServiceType() != null && serviceProperties.getControllerServiceType().equalsIgnoreCase(type)) {
            Map<String, String> properties = nifiControllerServiceProperties.mergeNifiAndEnvProperties(serviceProperties.getControllerServiceDTO().getProperties(),
                                                                                                       serviceProperties.getControllerServiceName());
            String uri = properties.get(serviceProperties.getConnectionStringPropertyKey());
            String user = properties.get(serviceProperties.getUserNamePropertyKey());
            String password = properties.get(serviceProperties.getPasswordPropertyKey());
            if(StringUtils.isBlank(password)){
                password =  nifiControllerServiceProperties.getEnvironmentPropertyValueForControllerService(serviceProperties.getControllerServiceName(),"password");
            }
            log.info("describing Table {}.{} against Controller Service: {} ({}) with uri of {} ", serviceProperties.getSchemaName(), serviceProperties.getTableName(),
                     serviceProperties.getControllerServiceName(), serviceProperties.getControllerServiceId(), uri);
            DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
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


    private static class DescribeTableWithControllerServiceBuilder {

        private String connectionStringPropertyKey;
        private String userNamePropertyKey;
        private String passwordPropertyKey;
        private String controllerServiceType;
        private String controllerServiceName;
        private String controllerServiceId;
        private String tableName;
        private String schemaName;
        private ControllerServiceDTO controllerServiceDTO;


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

        public DescribeTableWithControllerServiceBuilder controllerServiceType(String controllerServiceType) {
            this.controllerServiceType = controllerServiceType;

            return this;
        }

        private void initializePropertiesFromControllerServiceType() {
            if ("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(controllerServiceType)) {
                this.connectionStringPropertyKey = "Database Connection URL";
                this.userNamePropertyKey = "Database User";
                this.passwordPropertyKey = "Password";
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
            return serviceProperties;
        }
    }


    private static class DescribeTableWithControllerService {

        private String connectionStringPropertyKey;
        private String userNamePropertyKey;
        private String passwordPropertyKey;
        private String controllerServiceType;
        private String controllerServiceName;
        private String controllerServiceId;
        private String tableName;
        private String schemaName;
        private ControllerServiceDTO controllerServiceDTO;

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
    }


}
