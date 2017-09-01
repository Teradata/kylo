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

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.nifi.CleanupStaleFeedRevisions;
import com.thinkbiganalytics.feedmgr.nifi.NifiConnectionService;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.controllerservice.DBCPConnectionPoolService;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignNiFiComponents;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignProcessGroupComponents;
import com.thinkbiganalytics.nifi.rest.model.NiFiClusterSummary;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.nifi.web.api.dto.AboutDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ControllerServiceReferencingComponentDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentEntity;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentsEntity;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

@Api(tags = "Feed Manager - NiFi", produces = "application/json")
@Path(NifiIntegrationRestController.BASE)
@Component
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - NiFi", description = "integration with NiFi"))
public class NifiIntegrationRestController {

    private static final Logger log = LoggerFactory.getLogger(NifiIntegrationRestController.class);

    /**
     * Messages for the default locale
     */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("com.thinkbiganalytics.feedmgr.rest.controller.NiFiIntegrationMessages");
    public static final String BASE = "/v1/feedmgr/nifi";
    public static final String FLOWS = "/flows";
    public static final String REUSABLE_INPUT_PORTS = "/reusable-input-ports";
    @Inject
    DBCPConnectionPoolService dbcpConnectionPoolTableInfo;
    @Inject
    FeedManagerTemplateService feedManagerTemplateService;
    @Inject
    NiFiPropertyDescriptorTransform propertyDescriptorTransform;

    @Inject
    NifiConnectionService nifiConnectionService;
    /**
     * Legacy NiFi REST client
     */
    @Inject
    private LegacyNifiRestClient legacyNifiRestClient;
    /**
     * New NiFi REST client
     */
    @Inject
    private NiFiRestClient nifiRestClient;
    @Inject
    private SpringEnvironmentProperties environmentProperties;

    @Inject
    private AccessController accessController;

    @Inject
    private DatasourceService datasourceService;

    @GET
    @Path("/auto-align/{processGroupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Organizes the components of the specified process group.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The result of the operation.", response = RestResponseStatus.class)
    )
    public Response autoAlign(@PathParam("processGroupId") String processGroupId) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

        RestResponseStatus status;
        if ("all".equals(processGroupId)) {
            AlignNiFiComponents alignNiFiComponents = new AlignNiFiComponents();
            alignNiFiComponents.setNiFiRestClient(legacyNifiRestClient.getNiFiRestClient());
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
            AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(legacyNifiRestClient.getNiFiRestClient(), processGroupId);
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
    @Path("/cleanup-versions/{processGroupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Performs a cleanup of the specified process group.",
                  notes = "This method will list all of the child process groups and delete the ones where the name matches the regular expression: .* - \\d{13}")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the number of process groups deleted.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The process group is unavailable.", response = RestResponseStatus.class)
                  })
    public Response cleanupVersionedProcessGroups(@PathParam("processGroupId") String processGroupId) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

        RestResponseStatus status;
        CleanupStaleFeedRevisions cleanupStaleFeedRevisions = new CleanupStaleFeedRevisions(legacyNifiRestClient, processGroupId, propertyDescriptorTransform);
        cleanupStaleFeedRevisions.cleanup();
        String msg = "Cleaned up " + cleanupStaleFeedRevisions.getDeletedProcessGroups().size() + " Process Groups";
        status = new RestResponseStatus.ResponseStatusBuilder().message(msg).buildSuccess();

        return Response.ok(status).build();
    }

    @GET
    @Path("/flow/{processGroupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the flow of the specified process group.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the flow.", response = NifiFlowProcessGroup.class),
                      @ApiResponse(code = 500, message = "The process group is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFlow(@PathParam("processGroupId") String processGroupId) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

        NifiFlowProcessGroup flow = legacyNifiRestClient.getFeedFlow(processGroupId);
        NifiFlowDeserializer.prepareForSerialization(flow);
        return Response.ok(flow).build();
    }

    @GET
    @Path("/flow/feed/{categoryAndFeedName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the flow of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the flow.", response = NifiFlowProcessGroup.class),
                      @ApiResponse(code = 500, message = "The process group is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFlowForCategoryAndFeed(@PathParam("categoryAndFeedName") String categoryAndFeedName) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

        NifiFlowProcessGroup flow = legacyNifiRestClient.getFeedFlowForCategoryAndFeed(categoryAndFeedName);
        NifiFlowDeserializer.prepareForSerialization(flow);
        return Response.ok(flow).build();
    }


    //walk entire graph
    @GET
    @Path(FLOWS)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a list of all flows.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the flows.", response = NifiFlowProcessGroup.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "NiFi is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFlows() {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

        List<NifiFlowProcessGroup> feedFlows = legacyNifiRestClient.getFeedFlows();
        if (feedFlows != null) {
            log.info("********************** getAllFlows  ({})", feedFlows.size());
            feedFlows.stream().forEach(group -> NifiFlowDeserializer.prepareForSerialization(group));
        }
        return Response.ok(feedFlows).build();
    }


    @GET
    @Path("/configuration/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets user properties for NiFi.", notes = "These are the properties beginning with 'config.' in the application.properties file.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the user properties.", response = Map.class)
    )
    public Response getFeeds() {
        Map<String, Object> properties = environmentProperties.getPropertiesStartingWith(PropertyExpressionResolver.configPropertyPrefix);
        if (properties == null) {
            properties = new HashMap<>();
        }
        return Response.ok(properties).build();
    }

    @GET
    @Path(REUSABLE_INPUT_PORTS)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the input ports to reusable templates.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the input ports.", response = PortDTO.class, responseContainer = "Set"),
                      @ApiResponse(code = 500, message = "NiFi is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getReusableFeedInputPorts() {
        Set<PortDTO> ports = feedManagerTemplateService.getReusableFeedInputPorts();
        return Response.ok(ports).build();
    }

    /**
     * Finds controller services of the specified type.
     *
     * @param processGroupId the process group id
     * @param type           the type to match
     * @return the list of matching controller services
     */
    @GET
    @Path("/controller-services/process-group/{processGroupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Finds controller services of the specified type.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the matching controller services.", response = ControllerServiceDTO.class, responseContainer = "Set"),
                      @ApiResponse(code = 400, message = "The type cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The process group cannot be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The process group is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getControllerServices(@Nonnull @PathParam("processGroupId") final String processGroupId, @Nullable @QueryParam("type") final String type) {
        // Verify parameters
        if (StringUtils.isBlank(processGroupId)) {
            throw new NotFoundException(STRINGS.getString("getControllerServices.missingProcessGroup"));
        }
        if (StringUtils.isBlank(type)) {
            throw new BadRequestException(STRINGS.getString("getControllerServices.missingType"));
        }

        // Determine allowed service types
        final Stream<String> subTypes = nifiRestClient.controllerServices().getTypes(type).stream().map(DocumentedTypeDTO::getType);
        final Set<String> allowedTypes = Stream.concat(Stream.of(type), subTypes).collect(Collectors.toSet());

        // Filter controller services
        final Set<ControllerServiceDTO> controllerServices = ("all".equalsIgnoreCase(processGroupId) || "root".equalsIgnoreCase(processGroupId))
                                                             ? nifiRestClient.processGroups().getControllerServices("root")
                                                             : nifiRestClient.processGroups().getControllerServices(processGroupId);
        final Set<ControllerServiceDTO> matchingControllerServices = controllerServices.stream()
            .filter(controllerService -> allowedTypes.contains(controllerService.getType()))
            .filter(datasourceService.getControllerServiceAccessControlFilter())
            .collect(Collectors.toSet());
        return Response.ok(matchingControllerServices).build();
    }

    @GET
    @Path("/controller-services")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a list of available controller services.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the controller services.", response = ControllerServiceDTO.class, responseContainer = "Set"),
                      @ApiResponse(code = 500, message = "NiFi is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getServices() {
        final Set<ControllerServiceDTO> controllerServices = legacyNifiRestClient.getControllerServices();
        return Response.ok(ImmutableMap.of("controllerServices", controllerServices)).build();
    }


    @GET
    @Path("/controller-services/types")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a list of the available controller service types.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the controller service types.", response = ControllerServiceTypesEntity.class),
                      @ApiResponse(code = 500, message = "NiFi is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getServiceTypes() {
        final ControllerServiceTypesEntity entity = new ControllerServiceTypesEntity();
        entity.setControllerServiceTypes(legacyNifiRestClient.getControllerServiceTypes());
        return Response.ok(entity).build();
    }

    @POST
    @Path("/controller-services/{serviceId}/query")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Executes a SELECT query.",
                  notes = "Connects to the database specified by the controller service using the password defined in Kylo's application.properties file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Result of the query.", response = QueryResult.class),
                      @ApiResponse(code = 404, message = "The controller service could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "NiFi is unavailable.", response = RestResponseStatus.class)
                  })
    public Response executeQuery(@PathParam("serviceId") final String serviceId, final String script) {
        log.debug("Execute query against service '{}': {}", serviceId, script);
        try {
            final QueryResult results = dbcpConnectionPoolTableInfo.executeQueryForControllerService(serviceId, "", script);
            return Response.ok(results).build();
        } catch (final DataAccessException e) {
            throw new BadRequestException(ExceptionUtils.getRootCause(e).getMessage(), e);
        } catch (final IllegalArgumentException e) {
            throw new NotFoundException("The controller service could not be found.", e);
        }
    }

    @GET
    @Path("/controller-services/{serviceId}/tables")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a list of table names from the specified database.",
                  notes = "Connects to the database specified by the controller service using the password defined in Kylo's application.properties file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table names.", response = String.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Nifi or the database are unavailable.", response = RestResponseStatus.class)
                  })
    public Response getTableNames(@PathParam("serviceId") String serviceId, @QueryParam("serviceName") @DefaultValue("") String serviceName, @QueryParam("schema") String schema,
                                  @QueryParam("tableName") String tableName) {
        log.info("Query for Table Names against service: {}({})", serviceName, serviceId);
        List<String> tables = dbcpConnectionPoolTableInfo.getTableNamesForControllerService(serviceId, serviceName, schema, tableName);

        return Response.ok(tables).build();
    }

    @GET
    @Path("/controller-services/{serviceId}/tables/{tableName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the schema of the specified table.",
                  notes = "Connects to the database specified by the controller service using the password defined in Kylo's application.properties file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table schema.", response = TableSchema.class),
                      @ApiResponse(code = 500, message = "Nifi or the database are unavailable.", response = RestResponseStatus.class)
                  })
    public Response describeTable(@PathParam("serviceId") String serviceId, @PathParam("tableName") String tableName, @QueryParam("serviceName") @DefaultValue("") String serviceName,
                                  @QueryParam("schema") String schema) {
        log.info("Describe Table {} against service: {}({})", tableName, serviceName, serviceId);
        TableSchema tableSchema = dbcpConnectionPoolTableInfo.describeTableForControllerService(serviceId, serviceName, schema, tableName);
        return Response.ok(tableSchema).build();
    }

    @GET
    @Path("/controller-services/{serviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a controller service.",
                  notes = "returns a Nifi controller service object by the supplied identifier")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the controller service.", response = ControllerServiceDTO.class),
                      @ApiResponse(code = 500, message = "Unable to find the controller service", response = RestResponseStatus.class)
                  })
    public Response getControllerService(@PathParam("serviceId") String serviceId) {
        try {
            final ControllerServiceDTO controllerService = legacyNifiRestClient.getControllerService(null, serviceId);
            return Response.ok(controllerService).build();
        } catch (Exception e) {
            RestResponseStatus error = new RestResponseStatus.ResponseStatusBuilder().message("Unable to find controller service for " + serviceId).buildError();
            return Response.ok(error).build();
        }
    }

    @GET
    @Path("/controller-services/{serviceId}/references")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a controller service references in a map by component type. (i.e. Processor -> list, Controller Service -> list ...)",
                  notes = "returns a map of the type and reference objects")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "returns a map of the type and reference objects", response = ControllerServiceDTO.class),
                      @ApiResponse(code = 500, message = "Unable to find the controller service", response = RestResponseStatus.class)
                  })
    public Response getControllerServiceReferencesMap(@PathParam("serviceId") String serviceId) {
        Map<String,List<ControllerServiceReferencingComponentDTO>> map = null;
        try {
            final ControllerServiceDTO controllerService = legacyNifiRestClient.getControllerService(null, serviceId);
            if(controllerService != null){
                Optional<ControllerServiceReferencingComponentsEntity> optional = legacyNifiRestClient.getNiFiRestClient().controllerServices().getReferences(serviceId);
               if(optional.isPresent()) {
                ControllerServiceReferencingComponentsEntity entity = optional.get();
                   map =  getReferencingComponents(entity.getControllerServiceReferencingComponents()).values().stream().map(c -> c.getComponent())
                    .collect(Collectors.groupingBy(x -> x.getReferenceType()));
                }
                else {
                   map = Collections.emptyMap();
               }
            }
            return Response.ok(map).build();
        } catch (Exception e) {
            RestResponseStatus error = new RestResponseStatus.ResponseStatusBuilder().message("Unable to find controller service references for " + serviceId).buildError();
            return Response.ok(error).build();
        }
    }

    /**
     * get all referencing components in a single hashmap
     *
     * @param references references
     * @return the map of id to component entity
     */
    private Map<String, ControllerServiceReferencingComponentEntity> getReferencingComponents(Collection<ControllerServiceReferencingComponentEntity> references) {
        Map<String, ControllerServiceReferencingComponentEntity> map = new HashMap<>();
        references.stream().forEach(c -> {
            map.put(c.getId(), c);
            if (c.getComponent().getReferencingComponents() != null && !c.getComponent().getReferencingComponents().isEmpty()) {
                map.putAll(getReferencingComponents(c.getComponent().getReferencingComponents()));
            }
        });
        return map;
    }


    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Retrieves details about NiFi.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns details about NiFi.", response = AboutDTO.class),
                      @ApiResponse(code = 500, message = "NiFi is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getAbout() {
        final AboutDTO about = nifiRestClient.about();
        final NiFiClusterSummary clusterSummary = nifiRestClient.clusterSummary();
        return Response.ok(ImmutableMap.of("version", about.getVersion(), "clustered", clusterSummary.getClustered())).build();
    }


    /**
     * Checks to see if NiFi is up and running
     *
     * @return true if running, false if not
     */
    @GET
    @Path("/running")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the status of the NiFi cluster.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of NiFi if its running or not"),
                      @ApiResponse(code = 500, message = "An error occurred accessing the NiFi status.", response = RestResponseStatus.class)
                  })
    public Response getRunning() {
        boolean isRunning = nifiConnectionService.isNiFiRunning();
        return Response.ok(isRunning).build();
    }
}
