package com.thinkbiganalytics.feedmgr.rest.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.feedmgr.nifi.NifiConfigurationPropertiesService;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.rest.JerseyClientException;
import com.thinkbiganalytics.feedmgr.support.Constants;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/13/16.
 */
@Api(value = "feed-manager-nifi-configuration", produces = "application/json")
@Path("/v1/feedmgr/nifi")
@Component
public class NifiConfigurationPropertiesRestController {

  @Autowired
  private NifiRestClient nifiRestClient;

    public NifiConfigurationPropertiesRestController() {
    }

    @GET
    @Path("/configuration/properties")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getFeeds(){
       Properties properties = NifiConfigurationPropertiesService.getInstance().getPropertiesWithConfigPrefix();
        return Response.ok(properties).build();
    }

  @GET
  @Path("/reusable-input-ports")
  @Produces({MediaType.APPLICATION_JSON })
  public Response getReusableFeedInputPorts( ) throws JerseyClientException {
    Set<PortDTO> ports = new HashSet<>();
    ProcessGroupDTO processGroup = nifiRestClient.getProcessGroupByName("root", SystemNamingService.generateSystemName(Constants.REUSABLE_TEMPLATES_CATEGORY_NAME));
    if(processGroup != null) {
        //fetch the ports
        InputPortsEntity inputPortsEntity = nifiRestClient.getInputPorts(processGroup.getId());
        if (inputPortsEntity != null && inputPortsEntity.getInputPorts() != null && !inputPortsEntity.getInputPorts().isEmpty()) {
          ports.addAll(inputPortsEntity.getInputPorts());
        }
    }
    return Response.ok(ports).build();
  }

}
