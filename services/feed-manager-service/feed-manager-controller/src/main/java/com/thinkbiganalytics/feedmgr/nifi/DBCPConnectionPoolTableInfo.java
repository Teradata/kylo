package com.thinkbiganalytics.feedmgr.nifi;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.rest.JerseyClientException;
import com.thinkbiganalytics.schema.DBSchemaParser;

/**
 * Created by sr186054 on 1/28/16.
 */
@Service
public class DBCPConnectionPoolTableInfo {

    @Autowired
    NifiRestClient nifiRestClient;


 public  List<String> getTableNamesForControllerService(String serviceId,String serviceName, String schema) throws JerseyClientException {
     ControllerServiceDTO controllerService = getControllerService(serviceId,serviceName);

     if(controllerService != null) {
         String type = controllerService.getType();
         if("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(type)){
             String uri = controllerService.getProperties().get("Database Connection URL");
             uri = StringUtils.replace(uri, "3306", "3307");
             String user = controllerService.getProperties().get("Database User");
             String password = controllerService.getProperties().get("Password");

             DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
             DBSchemaParser schemaParser = new DBSchemaParser(dataSource);
             return schemaParser.listTables(schema);
         }
     }
     return null;
 }

    private ControllerServiceDTO getControllerService(String serviceId, String serviceName) {
        ControllerServiceDTO controllerService = null;
        try {
            ControllerServiceEntity entity = nifiRestClient.getControllerService("NODE", serviceId);
            if (entity != null && entity.getControllerService() != null) {
                controllerService = entity.getControllerService();
            }
        }catch(JerseyClientException e) {

        }
        if (controllerService == null) {
            try {
                controllerService = nifiRestClient.getControllerServiceByName("NODE", serviceName);
            }catch (JerseyClientException e) {

            }
        }
return controllerService;
    }


    public TableSchema describeTableForControllerService(String serviceId, String serviceName,String schema,String tableName) throws
                                                                                                           JerseyClientException {

        ControllerServiceDTO controllerService = getControllerService(serviceId, serviceName);
        if(controllerService != null) {
            String type = controllerService.getType();
            if("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(type)){
                String uri = controllerService.getProperties().get("Database Connection URL");
                uri = StringUtils.replace(uri, "3306", "3307");
                String user = controllerService.getProperties().get("Database User");
                String password = controllerService.getProperties().get("Password");

                DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
                DBSchemaParser schemaParser = new DBSchemaParser(dataSource);
                return schemaParser.describeTable(schema, tableName);
            }
        }
        return null;
    }

}
