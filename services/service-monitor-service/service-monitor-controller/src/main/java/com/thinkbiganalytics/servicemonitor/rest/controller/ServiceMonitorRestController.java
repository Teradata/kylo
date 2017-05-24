package com.thinkbiganalytics.servicemonitor.rest.controller;

/*-
 * #%L
 * thinkbig-service-monitor-controller
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

import com.thinkbiganalytics.servicemonitor.ServiceMonitorRepository;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Operations Manager - Services", produces = "application/json")
@Path(ServiceMonitorRestController.V1_SERVICE_MONITOR)
public class ServiceMonitorRestController {

    public static final String V1_SERVICE_MONITOR = "/v1/service-monitor";

    @Inject
    private ServiceMonitorRepository serviceRepository;

    /**
     * Return a list of all services
     *
     * @return A list of json objects representing the executions.  Http error code thrown on any error in execution
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of all services and their health")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the services.", response = ServiceStatusResponse.class, responseContainer = "List")
    )
    public Response listServices() {
        return Response.ok(serviceRepository.listServices()).build();
    }
}
