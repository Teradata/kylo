package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.nifi.DBCPConnectionPoolTableInfo;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/13/16.
 */
@Api(value = "feed-manager-controller-services", produces = "application/json")
@Path("/v1/feedmgr/nifi/controller-services")
public class NifiControllerServicesRestController {

    private static final Logger log = LoggerFactory.getLogger(NifiControllerServicesRestController.class);

    @Autowired
    NifiRestClient nifiRestClient;

    @Autowired
    DBCPConnectionPoolTableInfo dbcpConnectionPoolTableInfo;

    public NifiControllerServicesRestController() {
        int i = 0;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getServices() {
        ControllerServicesEntity entity = nifiRestClient.getControllerServices("NODE");
        return Response.ok(entity).build();
    }


    @GET
    @Path("/types")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getServiceTypes() {
        ControllerServiceTypesEntity entity = nifiRestClient.getControllerServiceTypes();
        return Response.ok(entity).build();
    }

    @GET
    @Path("/{serviceId}/tables")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTableNames(@PathParam("serviceId") String serviceId, @QueryParam("serviceName") @DefaultValue("") String serviceName, @QueryParam("schema") String schema) {
        log.info("Query for Table Names against service: {}({})", serviceName, serviceId);
        List<String> tables = dbcpConnectionPoolTableInfo.getTableNamesForControllerService(serviceId, serviceName, schema);

        return Response.ok(tables).build();
    }

    @GET
    @Path("/{serviceId}/tables/{tableName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response describeTable(@PathParam("serviceId") String serviceId, @PathParam("tableName") String tableName, @QueryParam("serviceName") @DefaultValue("") String serviceName,
                                  @QueryParam("schema") String schema) {
        log.info("Describe Table {} against service: {}({})", tableName, serviceName, serviceId);
        TableSchema tableSchema = dbcpConnectionPoolTableInfo.describeTableForControllerService(serviceId, serviceName, schema, tableName);
        return Response.ok(tableSchema).build();
    }


}
