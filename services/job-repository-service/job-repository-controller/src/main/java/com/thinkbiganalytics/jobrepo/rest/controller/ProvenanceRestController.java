package com.thinkbiganalytics.jobrepo.rest.controller;
/*-
 * #%L
 * thinkbig-job-repository-controller
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
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.NifiStatsJmsReceiver;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.ProvenanceEventReceiver;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.GroupedStatsUtil;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Operations Manager - Provenance", produces = "application/json")
@Path("/v1/provenance")
public class ProvenanceRestController {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceRestController.class);

    @Inject
    ProvenanceEventReceiver provenanceEventReceiver;

    @Inject
    NifiStatsJmsReceiver statsJmsReceiver;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("add custom provenance events to a job")
    @ApiResponses(
        @ApiResponse(code = 200, message = "add custom provenance events to a job", response = String.class)
    )
    public Response addProvenance(ProvenanceEventRecordDTOHolder eventRecordDTOHolder ){
        ProvenanceEventRecordDTOHolder batchEventEntity = new ProvenanceEventRecordDTOHolder();
        List<ProvenanceEventRecordDTO> events = eventRecordDTOHolder.getEvents();
        List<ProvenanceEventRecordDTO> batchEvents = new ArrayList<>();
        for(ProvenanceEventRecordDTO event : events){
            if(!event.isStream()){
                batchEvents.add(event);
            }
        }
        //reassign the events
        batchEventEntity.setEvents(batchEvents);

        AggregatedFeedProcessorStatisticsHolder stats = GroupedStatsUtil.gatherStats(events);
        log.info("Processing {} batch  events", batchEventEntity);
        provenanceEventReceiver.receiveEvents(batchEventEntity);
        log.info("Processing {} stats ", stats);
        statsJmsReceiver.receiveTopic(stats);

        return Response.ok(new RestResponseStatus.ResponseStatusBuilder().message("Processed "+eventRecordDTOHolder).buildSuccess()).build();


    }

}
