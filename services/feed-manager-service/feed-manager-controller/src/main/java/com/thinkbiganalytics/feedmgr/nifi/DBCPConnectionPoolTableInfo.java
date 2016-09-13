package com.thinkbiganalytics.feedmgr.nifi;

import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
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
    NifiRestClient nifiRestClient;

    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosHiveConfiguration;

    public List<String> getTableNamesForControllerService(String serviceId, String serviceName, String schema) {
     ControllerServiceDTO controllerService = getControllerService(serviceId,serviceName);

     if(controllerService != null) {
         String type = controllerService.getType();
         if("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(type)){

             Map<String,String> properties = nifiControllerServiceProperties.mergeNifiAndEnvProperties(
                 controllerService.getProperties(), controllerService.getName());

             String uri = properties.get("Database Connection URL");
            // uri = StringUtils.replace(uri, "3306", "3307");
             String user = properties.get("Database User");
             String password = properties.get("Password");
             if(StringUtils.isNotBlank(password) && password.startsWith("**")){
                 String propertyKey = nifiControllerServiceProperties.getEnvironmentControllerServicePropertyPrefix(controllerService.getName())+".password";
                 String example = propertyKey+"=PASSWORD";
                 log.error("Unable to connect to Controller Service {}, {}.  You need to specifiy a configuration property as {} with the password for user: {}. ",controllerService.getName(),controllerService.getId(),example,user);
             }
             log.info("Search For Tables against Controller Service: {} ({}) with uri of {}.  ",controllerService.getName(),controllerService.getId(),uri);
             DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
             DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
             return schemaParser.listTables(schema);
         }
     }
     else {
         log.error("Cannot getTable Names for Controller Service. Unable to obtain Controller Service for serviceId or Name ({} , {})",serviceId,serviceName);
     }
     return null;
 }

    private ControllerServiceDTO getControllerService(String serviceId, String serviceName) {
        ControllerServiceDTO controllerService = nifiControllerServiceProperties.getControllerServiceById(serviceId);
        if(controllerService == null){
            controllerService = nifiControllerServiceProperties.getControllerServiceByName(serviceName);
        }
return controllerService;
    }


    public TableSchema describeTableForControllerService(String serviceId, String serviceName, String schema, String tableName) {

        ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);
        if(controllerService != null) {
            String type = controllerService.getType();
            if("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(type)){
                Map<String,String> properties = nifiControllerServiceProperties.mergeNifiAndEnvProperties(controllerService.getProperties(),controllerService.getName());
                String uri = properties.get("Database Connection URL");
               // uri = StringUtils.replace(uri, "3306", "3307");
                String user = properties.get("Database User");
                String password = properties.get("Password");
                log.info("describing Table {}.{} against Controller Service: {} ({}) with uri of {} ",schema,tableName,controllerService.getName(),controllerService.getId(),uri);
                DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
                DBSchemaParser schemaParser = new DBSchemaParser(dataSource, kerberosHiveConfiguration);
                return schemaParser.describeTable(schema, tableName);
            }
        }else {
            log.error("Cannot describe Table for Controller Service. Unable to obtain Controller Service for serviceId or Name ({} , {})",serviceId,serviceName);
        }
        return null;
    }

}
