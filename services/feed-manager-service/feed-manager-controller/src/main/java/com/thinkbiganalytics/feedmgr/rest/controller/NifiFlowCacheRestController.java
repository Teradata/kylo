package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * TODO consider making them POST routines Created by sr186054 on 12/20/16.
 */
@Api(value = "nifi-provenance", produces = "application/json")
@Path("/metadata/nifi-flow-cache")
@Component
public class NifiFlowCacheRestController {


    private static final Logger log = LoggerFactory.getLogger(NifiFlowCacheRestController.class);

    @Inject
    NifiFlowCache nifiFlowCache;

    @GET
    @Path("/get-flow-updates")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlowUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.syncAndReturnUpdates(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/get-cache")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCache(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync cache = nifiFlowCache.getCache(syncId);
        return Response.ok(cache).build();
    }

    @GET
    @Path("/preview-flow-updates")
    @Produces({MediaType.APPLICATION_JSON})
    public Response previewFlowUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.previewUpdates(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/cache-summary")
    @Produces({MediaType.APPLICATION_JSON})
    public Response previewFlowUpdates() {
        NifiFlowCache.CacheSummary summary = nifiFlowCache.cacheSummary();
        return Response.ok(summary).build();
    }

    @GET
    @Path("/reset-flow-updates")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSyncUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.refreshAll(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/reset-cache")
    @Produces({MediaType.APPLICATION_JSON})
    public Response resetCache() {
        nifiFlowCache.rebuildAll();
        return Response.ok().build();
    }


    @GET
    @Path("/available")
    @Produces({MediaType.TEXT_HTML})
    public Response isAvailable() {
        return Response.ok(nifiFlowCache.isAvailable()).build();
    }


}
