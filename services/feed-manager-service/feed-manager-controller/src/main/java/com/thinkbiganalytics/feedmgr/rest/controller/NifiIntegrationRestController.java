package com.thinkbiganalytics.feedmgr.rest.controller;

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.nifi.DBCPConnectionPoolTableInfo;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.SpringEnvironmentProperties;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignNiFiComponents;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignProcessGroupComponents;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/13/16.
 */
@Api(value = "feed-manager-nifi-configuration", produces = "application/json")
@Path("/v1/feedmgr/nifi")
@Component
public class NifiIntegrationRestController {

    private static final Logger log = LoggerFactory.getLogger(NifiIntegrationRestController.class);
    @Autowired
    private LegacyNifiRestClient nifiRestClient;

    @Autowired
    private SpringEnvironmentProperties environmentProperties;

    @Autowired
    DBCPConnectionPoolTableInfo dbcpConnectionPoolTableInfo;

    @Autowired
    FeedManagerTemplateService feedManagerTemplateService;


    public NifiIntegrationRestController() {
    }

    @GET
    @Path("/auto-align/{processGroupId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response autoAlign(@PathParam("processGroupId") String processGroupId) {
        RestResponseStatus status;
        if ("all".equals(processGroupId)) {
            AlignNiFiComponents alignNiFiComponents = new AlignNiFiComponents();
            alignNiFiComponents.setNiFiRestClient(nifiRestClient.getNiFiRestClient());
            alignNiFiComponents.autoLayout();
            String message = "";
            if (alignNiFiComponents.isAligned()) {
                message = "Aligned All of NiFi.  " + alignNiFiComponents.getAlignedProcessGroups() + " process groups were aligned ";
            } else {
                message =
                    "Alignment failed while attempting to align all of NiFi. " + alignNiFiComponents.getAlignedProcessGroups()
                    + " were successfully aligned. Please look at the logs for more information";
            }
            status = new RestResponseStatus.ResponseStatusBuilder().message(message).buildSuccess();
        } else {
            AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(nifiRestClient.getNiFiRestClient(), processGroupId);
            ProcessGroupDTO alignedGroup = alignProcessGroupComponents.autoLayout();
            String message = "";
            if (alignProcessGroupComponents.isAligned()) {
                message = "Aligned " + alignedGroup.getContents().getProcessGroups().size() + " process groups under " + alignedGroup.getName();
            } else {
                message = "Alignment failed for process group " + processGroupId + ". Please look at the logs for more information";
            }
            status = new RestResponseStatus.ResponseStatusBuilder().message(message).buildSuccess();
        }

        return Response.ok(status).build();
    }

    @GET
    @Path("/flow/{processGroupId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlow(@PathParam("processGroupId") String processGroupId) {
        NifiFlowProcessGroup flow = nifiRestClient.getFeedFlow(processGroupId);
        NifiFlowDeserializer.prepareForSerialization(flow);
        return Response.ok(flow).build();
    }

    @GET
    @Path("/flow/feed/{categoryAndFeedName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFlowForCategoryAndFeed(@PathParam("categoryAndFeedName") String categoryAndFeedName) {
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
        Set<PortDTO> ports = feedManagerTemplateService.getReusableFeedInputPorts();
        return Response.ok(ports).build();
    }


    @GET
    @Path("/controller-services/{processGroupId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getControllerServices(@PathParam("processGroupId") String processGroupId, @QueryParam("type") String type) {
        Set<ControllerServiceDTO> controllerServiceDTOs = null;
        if ("all".equalsIgnoreCase(processGroupId)) {
            controllerServiceDTOs = nifiRestClient.getControllerServices();
        } else {
            if ("root".equalsIgnoreCase(processGroupId)) {
                processGroupId = nifiRestClient.getRootProcessGroup().getId();
            }
            controllerServiceDTOs = nifiRestClient.getNiFiRestClient().processGroups().getControllerServices(processGroupId);
        }

        if (controllerServiceDTOs != null && StringUtils.isNotBlank(type)) {
            controllerServiceDTOs = controllerServiceDTOs.stream().filter(controllerServiceDTO -> controllerServiceDTO.getType().equalsIgnoreCase(type)).collect(Collectors.toSet());
        }
        return Response.ok(controllerServiceDTOs).build();
    }




    @GET
    @Path("/controller-services")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getServices() {
        final Set<ControllerServiceDTO> controllerServices = nifiRestClient.getControllerServices();
        return Response.ok(ImmutableMap.of("controllerServices", controllerServices)).build();
    }


    @GET
    @Path("/controller-services/types")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getServiceTypes() {
        final ControllerServiceTypesEntity entity = new ControllerServiceTypesEntity();
        entity.setControllerServiceTypes(nifiRestClient.getControllerServiceTypes());
        return Response.ok(entity).build();
    }

    @GET
    @Path("/controller-services/{serviceId}/tables")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTableNames(@PathParam("serviceId") String serviceId, @QueryParam("serviceName") @DefaultValue("") String serviceName, @QueryParam("schema") String schema) {
        log.info("Query for Table Names against service: {}({})", serviceName, serviceId);
        List<String> tables = dbcpConnectionPoolTableInfo.getTableNamesForControllerService(serviceId, serviceName, schema);

        return Response.ok(tables).build();
    }

    @GET
    @Path("/controller-services/{serviceId}/tables/{tableName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response describeTable(@PathParam("serviceId") String serviceId, @PathParam("tableName") String tableName, @QueryParam("serviceName") @DefaultValue("") String serviceName,
                                  @QueryParam("schema") String schema) {
        log.info("Describe Table {} against service: {}({})", tableName, serviceName, serviceId);
        TableSchema tableSchema = dbcpConnectionPoolTableInfo.describeTableForControllerService(serviceId, serviceName, schema, tableName);
        return Response.ok(tableSchema).build();
    }


}
