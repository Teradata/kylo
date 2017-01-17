package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(value = "nifi-provenance", produces = "application/json",
     description = "Used by the NiFi KyloProvenanceEventReportingTask.  Provides information about the flows and the max event id processed by kylo")
@Path("/metadata/nifi-provenance")
@Component
public class NifiProvenanceRestController {

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    @Autowired
    private NifiFeedProcessorStatisticsProvider statsProvider;


    private static final Logger log = LoggerFactory.getLogger(NifiProvenanceRestController.class);

    @Inject
    NifiFlowCache nifiFlowCache;

    @GET
    @Path("/nifi-flow-cache/get-flow-updates")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlowUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.syncAndReturnUpdates(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/nifi-flow-cache/get-cache")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCache(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync cache = nifiFlowCache.getCache(syncId);
        return Response.ok(cache).build();
    }

    @GET
    @Path("/nifi-flow-cache/preview-flow-updates")
    @Produces({MediaType.APPLICATION_JSON})
    public Response previewFlowUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.previewUpdates(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/nifi-flow-cache/cache-summary")
    @Produces({MediaType.APPLICATION_JSON})
    public Response previewFlowUpdates() {
        NifiFlowCache.CacheSummary summary = nifiFlowCache.cacheSummary();
        return Response.ok(summary).build();
    }

    @GET
    @Path("/nifi-flow-cache/reset-flow-updates")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSyncUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.refreshAll(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/nifi-flow-cache/reset-cache")
    @Produces({MediaType.APPLICATION_JSON})
    public Response resetCache() {
        nifiFlowCache.rebuildAll();
        return Response.ok().build();
    }


    @GET
    @Path("/nifi-flow-cache/available")
    @Produces({MediaType.APPLICATION_JSON})
    public Response isAvailable() {
        return Response.ok(nifiFlowCache.isAvailable()).build();
    }


    @GET
    @Path("/max-event-id")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findMaxEventId(@QueryParam("clusterNodeId") String clusterNodeId) {
        return metadataAccess.read(() -> {
            Long maxId = 0L;
            if (StringUtils.isNotBlank(clusterNodeId)) {
                maxId = statsProvider.findMaxEventId(clusterNodeId);
            } else {
                maxId = statsProvider.findMaxEventId();
            }
            if (maxId == null) {
                maxId = -1L;
            }
            return Response.ok(maxId).build();
        }, MetadataAccess.SERVICE);
    }


}
