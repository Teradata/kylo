package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.SpringEnvironmentProperties;
import com.thinkbiganalytics.feedmgr.support.FeedNameUtil;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;

import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
       NifiFlowProcessGroup flow =  nifiRestClient.getSimpleFlowOrder(processGroupId);
        String categoryName = flow.getParentGroupName();
        String feedName = flow.getName();
        feedName = FeedNameUtil.fullName(categoryName,feedName);
        //if it is a versioned feed then strip the version to get the correct feed name
        feedName = TemplateCreationHelper.parseVersionedProcessGroupName(feedName);
        flow.setFeedName(feedName);
        return Response.ok(flow).build();
    }

    @GET
    @Path("/flow/feed/{categoryAndFeedName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlowForCategoryAndFeed(@PathParam("categoryAndFeedName")String categoryAndFeedName) {
        NifiFlowProcessGroup flow = null;
        String category = FeedNameUtil.category(categoryAndFeedName);
        String feed = FeedNameUtil.feed(categoryAndFeedName);
        //1 find the ProcessGroup under "root" matching the name category
        ProcessGroupEntity processGroupEntity = nifiRestClient.getRootProcessGroup();
        ProcessGroupDTO root = processGroupEntity.getProcessGroup();
        ProcessGroupDTO categoryGroup = root.getContents().getProcessGroups().stream().filter(group -> category.equalsIgnoreCase(group.getName())).findAny().orElse(null);
        if(categoryGroup != null) {
            ProcessGroupDTO feedGroup = categoryGroup.getContents().getProcessGroups().stream().filter(group -> feed.equalsIgnoreCase(group.getName())).findAny().orElse(null);
            if(feedGroup != null){
                flow = nifiRestClient.getSimpleFlowOrder(feedGroup.getId());
                NifiFlowDeserializer.prepareForSerialization(flow);
            }
        }
        return Response.ok(flow).build();
    }


    //walk entire graph
    @GET
    @Path("/flows")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlows() {
        log.info("get Graph of Nifi Flows");
        List<NifiFlowProcessGroup> feedFlows = new ArrayList<>();
            ProcessGroupEntity processGroupEntity = nifiRestClient.getRootProcessGroup();
            ProcessGroupDTO root = processGroupEntity.getProcessGroup();
            //first level is the category
            for (ProcessGroupDTO category : root.getContents().getProcessGroups()) {
                for (ProcessGroupDTO feedProcessGroup : category.getContents().getProcessGroups()) {
                    //second level is the feed
                    String feedName = FeedNameUtil.fullName(category.getName(),feedProcessGroup.getName());
                    //if it is a versioned feed then strip the version to get the correct feed name
                    feedName = TemplateCreationHelper.parseVersionedProcessGroupName(feedName);
                    NifiFlowProcessGroup feedFlow =  nifiRestClient.getSimpleFlowOrder(feedProcessGroup.getId());
                    feedFlow.setFeedName(feedName);
                    feedFlows.add(feedFlow);
                    NifiFlowDeserializer.prepareForSerialization(feedFlow);

                }
            }
        log.info("finished Graph of Nifi Flows.  Returning {} flows", feedFlows.size());
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
