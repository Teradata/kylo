package com.thinkbiganalytics.feedmgr.rest.controller;

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

import com.thinkbiganalytics.feedmgr.nifi.cache.CacheSummary;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventProvider;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Used by the NiFi KyloProvenanceEventReportingTask. Provides information about the flows and the max event id processed by Kylo.
 */
@Api(tags = "NiFi - Provenance", produces = "application/json")
@Path("/v1/metadata/nifi-provenance")
@Component
@SwaggerDefinition(tags = @Tag(name = "NiFi - Provenance", description = "event reporting"))
public class NifiProvenanceRestController {

    private static final Logger log = LoggerFactory.getLogger(NifiProvenanceRestController.class);
    @Inject
    NifiFlowCache nifiFlowCache;
    @Inject
    private MetadataAccess metadataAccess;
    @Inject
    private AccessController accessController;
    @Autowired
    private NifiFeedProcessorStatisticsProvider statsProvider;

    @Autowired
    private NifiEventProvider eventProvider;

    @GET
    @Path("/nifi-flow-cache/get-flow-updates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets flow updates since the last sync.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the flow updates.", response = NiFiFlowCacheSync.class)
    )
    public Response getFlowUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.syncAndReturnUpdates(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/nifi-flow-cache/get-cache")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the flows.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the flows.", response = NiFiFlowCacheSync.class)
    )
    public Response getCache(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync cache = nifiFlowCache.getCache(syncId);
        return Response.ok(cache).build();
    }

    @GET
    @Path("/nifi-flow-cache/preview-flow-updates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets flow updates without syncing.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the flow updates.", response = NiFiFlowCacheSync.class)
    )
    public Response previewFlowUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.previewUpdates(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/nifi-flow-cache/cache-summary")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the flow cache status.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the cache status.", response = CacheSummary.class)
    )
    public Response previewFlowUpdates() {
        CacheSummary summary = new CacheSummary();
        return Response.ok(summary).build();
    }

    @GET
    @Path("/nifi-flow-cache/reset-flow-updates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Resets the updates to the flow cache since the last sync.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the updated flow cache.", response = NiFiFlowCacheSync.class)
    )
    public Response getSyncUpdates(@QueryParam("syncId") String syncId) {
        NiFiFlowCacheSync updates = nifiFlowCache.refreshAll(syncId);
        return Response.ok(updates).build();
    }

    @GET
    @Path("/nifi-flow-cache/reset-cache")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Resets the flow cache.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The cache was reset.", response = String.class)
    )
    public Response resetCache() {
       boolean updated = nifiFlowCache.rebuildAll();
        return Response.ok(updated ?"Reset the Cache" :" Unable to reset the cache.  Refer to the kylo-services.log file for more information.").build();
    }


    @GET
    @Path("/nifi-flow-cache/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Indicates if the cache is available.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the cache status.", response = Boolean.class)
    )
    public Response isAvailable() {
        return Response.ok(nifiFlowCache.isAvailable()).build();
    }


}
