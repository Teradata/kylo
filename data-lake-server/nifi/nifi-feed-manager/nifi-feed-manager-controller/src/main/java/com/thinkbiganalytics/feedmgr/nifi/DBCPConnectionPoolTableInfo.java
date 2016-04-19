package com.thinkbiganalytics.feedmgr.nifi;

import com.thinkbiganalytics.schema.DBSchemaParser;
import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 1/28/16.
 */
@Service
public class DBCPConnectionPoolTableInfo {

    @Autowired
    NifiRestClient nifiRestClient;

    //type = org.apache.nifi.dbcp.DBCPConnectionPool
    //properties
    //Database Connection URL
    //Database Driver Class Name
    //Database Driver Jar Url
    //Database User
    //Password
    //Max Wait Time
    //Max Total Connections
 public  List<String> getTableNamesForControllerService(String serviceId, String schema) throws JerseyClientException {
     ControllerServiceEntity entity = nifiRestClient.getControllerService("NODE", serviceId);
     if(entity != null && entity.getControllerService() != null) {
         String type = entity.getControllerService().getType();
         if("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(type)){
             String uri = entity.getControllerService().getProperties().get("Database Connection URL");
             uri = StringUtils.replace(uri, "3306", "3307");
             String user = entity.getControllerService().getProperties().get("Database User");
             String password = entity.getControllerService().getProperties().get("Password");

             DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
             DBSchemaParser schemaParser = new DBSchemaParser(dataSource);
             return schemaParser.listTables(schema);
         }
     }
     return null;
 }

    public TableSchema describeTableForControllerService(String serviceId, String schema,String tableName) throws
                                                                                                           JerseyClientException {
        ControllerServiceEntity entity = nifiRestClient.getControllerService("NODE", serviceId);
        if(entity != null && entity.getControllerService() != null) {
            String type = entity.getControllerService().getType();
            if("org.apache.nifi.dbcp.DBCPConnectionPool".equalsIgnoreCase(type)){
                String uri = entity.getControllerService().getProperties().get("Database Connection URL");
                uri = StringUtils.replace(uri, "3306", "3307");
                String user = entity.getControllerService().getProperties().get("Database User");
                String password = entity.getControllerService().getProperties().get("Password");

                DataSource dataSource = PoolingDataSourceService.getDataSource(uri, user, password);
                DBSchemaParser schemaParser = new DBSchemaParser(dataSource);
                return schemaParser.describeTable(schema, tableName);
            }
        }
        return null;
    }

}
