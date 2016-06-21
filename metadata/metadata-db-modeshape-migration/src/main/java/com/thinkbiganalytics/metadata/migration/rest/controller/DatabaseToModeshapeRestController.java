package com.thinkbiganalytics.metadata.migration.rest.controller;

import com.thinkbiganalytics.metadata.migration.DataMigration;
import com.thinkbiganalytics.metadata.migration.FeedMigrationService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 6/15/16.
 */
@Api(value = "migrate jpa 2 modeshape", produces = "application/json")
@Path("/v1/jpa2modeshape")
public class DatabaseToModeshapeRestController {


    @Inject
    FeedMigrationService feedMigrationService;


    @POST
    @Path("/migrate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response migrate() {
        DataMigration migration = feedMigrationService.migrate();
        return Response.ok(migration.getStatistics()).build();

    }

    @GET
    @Path("/migrate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response migrateGet() {
        DataMigration migration = feedMigrationService.migrate();
        return Response.ok(migration.getStatistics()).build();

    }


}
