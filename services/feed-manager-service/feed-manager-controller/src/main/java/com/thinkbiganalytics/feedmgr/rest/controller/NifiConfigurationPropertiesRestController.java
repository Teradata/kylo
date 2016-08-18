package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.SpringEnvironmentProperties;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;

import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/13/16.
 */
@Api(value = "feed-manager-nifi-configuration", produces = "application/json")
@Path("/v1/feedmgr/nifi")
@Component
public class NifiConfigurationPropertiesRestController {

    private static final Logger log = LoggerFactory.getLogger(NifiConfigurationPropertiesRestController.class);
    @Autowired
    private NifiRestClient nifiRestClient;

    @Autowired
    private SpringEnvironmentProperties environmentProperties;

    public NifiConfigurationPropertiesRestController() {
    }


    @GET
    @Path("/flow/{processGroupId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlow(@PathParam("processGroupId")String processGroupId) {
        NifiFlowProcessGroup flow = nifiRestClient.getFeedFlow(processGroupId);
        NifiFlowDeserializer.prepareForSerialization(flow);
        return Response.ok(flow).build();
    }

    @GET
    @Path("/flow/feed/{categoryAndFeedName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlowForCategoryAndFeed(@PathParam("categoryAndFeedName")String categoryAndFeedName) {
        NifiFlowProcessGroup flow = nifiRestClient.getFeedFlowForCategoryAndFeed(categoryAndFeedName);
        NifiFlowDeserializer.prepareForSerialization(flow);
        return Response.ok(flow).build();
    }


    //walk entire graph
    @GET
    @Path("/flows")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlows() {
        List<NifiFlowProcessGroup> feedFlows = nifiRestClient.getFeedFlows();
        if (feedFlows != null) {
            log.info("********************** getAllFlows  ({})", feedFlows.size());
            feedFlows.stream().forEach(group -> NifiFlowDeserializer.prepareForSerialization(group));
        }
        return Response.ok(feedFlows).build();
    }








    @GET
    @Path("/configuration/properties")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFeeds() {
        Map<String, Object> properties = environmentProperties.getPropertiesStartingWith(PropertyExpressionResolver.configPropertyPrefix);
        if (properties == null) {
            properties = new HashMap<>();
        }
        return Response.ok(properties).build();
    }

    @GET
    @Path("/reusable-input-ports")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getReusableFeedInputPorts() {
        Set<PortDTO> ports = new HashSet<>();
        ProcessGroupDTO processGroup = nifiRestClient.getProcessGroupByName("root",  TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME);
        if (processGroup != null) {
            //fetch the ports
            InputPortsEntity inputPortsEntity = nifiRestClient.getInputPorts(processGroup.getId());
            if (inputPortsEntity != null && inputPortsEntity.getInputPorts() != null && !inputPortsEntity.getInputPorts().isEmpty()) {
                ports.addAll(inputPortsEntity.getInputPorts());
            }
        }
        return Response.ok(ports).build();
    }




}
