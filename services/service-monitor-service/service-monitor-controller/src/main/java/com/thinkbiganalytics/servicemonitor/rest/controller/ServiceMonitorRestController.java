/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.servicemonitor.rest.controller;

import com.thinkbiganalytics.servicemonitor.ServiceMonitorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(value = "service-monitoring", produces = "application/json")
@Path("/v1/service-monitor")
public class ServiceMonitorRestController {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceMonitorRestController.class);

  @Inject
  private ServiceMonitorRepository serviceRepository;

  /**
   * Return a list of all services
   *
   * @return A list of json objects representing the executions.  Http error code thrown on any error in execution
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response listServices() {
    return Response.ok(serviceRepository.listServices()).build();
  }

}
