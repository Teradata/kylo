package com.thinkbiganalytics.feedmgr.rest.controller;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateDtoWrapper;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateProcessorDatasourceDefinition;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.support.Constants;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/12/16.
 */
@Api(value = "feed-manager-templates", produces = "application/json")
@Path("/v1/feedmgr/templates")
public class TemplatesRestController {

    private static final Logger log = LoggerFactory.getLogger(TemplatesRestController.class);

    @Autowired
    LegacyNifiRestClient nifiRestClient;

    @Autowired
    MetadataService metadataService;

    @Autowired
    FeedManagerTemplateService feedManagerTemplateService;

    @Autowired
    DatasourceService datasourceService;


    public TemplatesRestController() {

    }

    private MetadataService getMetadataService() {
        return metadataService;
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTemplates(@QueryParam("includeDetails") boolean includeDetails) {
        Set<TemplateDTO> nifiTemplates = nifiRestClient.getTemplates(includeDetails);
        List<RegisteredTemplate> registeredTemplates = metadataService.getRegisteredTemplates();

        Set<TemplateDtoWrapper> dtos = new HashSet<>();
        for (final TemplateDTO dto : nifiTemplates) {
            RegisteredTemplate match = metadataService.getRegisteredTemplateForNifiProperties(dto.getId(), dto.getName());
            TemplateDtoWrapper wrapper = new TemplateDtoWrapper(dto);
            if (match != null) {
                wrapper.setRegisteredTemplateId(match.getId());
            }
            dtos.add(wrapper);
        }
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/unregistered")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUnregisteredTemplates(@QueryParam("includeDetails") boolean includeDetails) {
        Set<TemplateDTO> nifiTemplates = nifiRestClient.getTemplates(includeDetails);
        List<RegisteredTemplate> registeredTemplates = metadataService.getRegisteredTemplates();

        Set<TemplateDtoWrapper> dtos = new HashSet<>();
        for (final TemplateDTO dto : nifiTemplates) {
            RegisteredTemplate match = metadataService.getRegisteredTemplateForNifiProperties(dto.getId(), dto.getName());
            if (match == null) {
                dtos.add(new TemplateDtoWrapper(dto));
            }
        }
        return Response.ok(dtos).build();
    }


    @GET
    @Path("/nifi/{templateId}/ports")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPortsForNifiTemplate(@PathParam("templateId") String nifiTemplateId) {
        Set<PortDTO> ports = nifiRestClient.getPortsForTemplate(nifiTemplateId);
        return Response.ok(ports).build();
    }

    @GET
    @Path("/nifi/{templateId}/input-ports")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInputPortsForNifiTemplate(@PathParam("templateId") String nifiTemplateId) {
        Set<PortDTO> ports = nifiRestClient.getPortsForTemplate(nifiTemplateId);
        List<PortDTO> list = Lists.newArrayList(Iterables.filter(ports, new Predicate<PortDTO>() {
            @Override
            public boolean apply(PortDTO portDTO) {
                return portDTO.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
            }
        }));
        return Response.ok(list).build();
    }


    @GET
    @Path("/nifi/reusable-input-ports-processors")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RegisteredTemplate.Processor> getReusableTemplateProcessorsForInputPorts(@QueryParam("inputPorts") String inputPortIds) {
        List<RegisteredTemplate.Processor> processorProperties = new ArrayList<>();
        if (StringUtils.isNotBlank(inputPortIds)) {
            List<String> inputPortIdsList = Arrays.asList(StringUtils.split(inputPortIds, ","));
            processorProperties = feedManagerTemplateService.getReusableTemplateProcessorsForInputPorts(inputPortIdsList);
        }
        return processorProperties;
    }


    @GET
    @Path("/nifi/{templateId}/processors")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getNiFiTemplateProcessors(@PathParam("templateId") String templateId) {
        List<RegisteredTemplate.Processor> processorProperties = feedManagerTemplateService.getNiFiTemplateProcessors(templateId);
        return Response.ok(processorProperties).build();
    }

    @GET
    @Path("/nifi/{templateId}/datasource-definitions")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatasourceDefinitionsForProcessors(@PathParam("templateId") String templateId, @QueryParam("inputPorts") String inputPortIds) {

        Map<RegisteredTemplate.Processor, DatasourceDefinition> map = new HashMap<>();
        List<RegisteredTemplate.Processor> processors = new ArrayList<>();
        List<RegisteredTemplate.Processor> reusableProcessors = getReusableTemplateProcessorsForInputPorts(inputPortIds);

        List<RegisteredTemplate.Processor> thisTemplateProcessors = feedManagerTemplateService.getNiFiTemplateProcessors(templateId);

        Set<DatasourceDefinition> defs = datasourceService.getDatasourceDefinitions();
        Map<String, DatasourceDefinition> datasourceDefinitionMap = new HashMap<>();
        if (defs != null) {
            defs.stream().forEach(def -> datasourceDefinitionMap.put(def.getProcessorType(), def));
        }

        //join the two lists
        processors.addAll(thisTemplateProcessors);
        processors.addAll(reusableProcessors);

        List<TemplateProcessorDatasourceDefinition> templateProcessorDatasourceDefinitions = new ArrayList<>();

        templateProcessorDatasourceDefinitions = processors.stream().filter(processor -> datasourceDefinitionMap.containsKey(processor.getType())).map(p -> {
            TemplateProcessorDatasourceDefinition definition = new TemplateProcessorDatasourceDefinition();
            definition.setProcessorType(p.getType());
            definition.setProcessorName(p.getName());
            definition.setProcessorId(p.getId());
            definition.setDatasourceDefinition(datasourceDefinitionMap.get(p.getType()));
            return definition;
        }).collect(Collectors.toList());

        return Response.ok(templateProcessorDatasourceDefinitions).build();


    }


    @GET
    @Path("/reload-data-source-definitions")
    @Produces({MediaType.APPLICATION_JSON})
    public Response reloadDatasources() {
        datasourceService.loadDefinitionsFromFile();
        return Response.ok(RestResponseStatus.SUCCESS).build();
    }


    @GET
    @Path("/nifi/{templateId}/out-ports")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getOutputPortsForNifiTemplate(@PathParam("templateId") String nifiTemplateId) {
        Set<PortDTO> ports = nifiRestClient.getPortsForTemplate(nifiTemplateId);
        List<PortDTO> list = Lists.newArrayList(Iterables.filter(ports, new Predicate<PortDTO>() {
            @Override
            public boolean apply(PortDTO portDTO) {
                return portDTO.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
            }
        }));
        return Response.ok(list).build();
    }


    /**
     * get all the registered Templates
     *
     * @
     */
    @GET
    @Path("/registered")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getRegisteredTemplates() {
        List<RegisteredTemplate> templates = getMetadataService().getRegisteredTemplates();
        return Response.ok(templates).build();

    }


    /**
     * Gets the template and optionally all reusable flow processors and properties
     */
    @GET
    @Path("/registered/{templateId}/processor-properties")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RegisteredTemplate.Processor> getReusableTemplateProcessorsForInputPorts(@PathParam("templateId") String templateId,
                                                                                         @QueryParam("includeReusableTemplates") boolean includeReusableTemplates) {
        List<RegisteredTemplate.Processor> processorProperties = new ArrayList<>();

        processorProperties = feedManagerTemplateService.getRegisteredTemplateProcessors(templateId, includeReusableTemplates);

        return processorProperties;
    }


    /**
     * get a given Registered Templates properties
     *
     * @
     */
    @GET
    @Path("/registered/{templateId}/properties")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getRegisteredTemplateProperties(@PathParam("templateId") String templateId) {
        return Response.ok(getMetadataService().getTemplateProperties(templateId)).build();
    }

    /**
     * get a registeredTemplate
     *
     * @
     */
    @GET
    @Path("/registered/{templateId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getRegisteredTemplate(@PathParam("templateId") String templateId, @QueryParam("allProperties") boolean allProperties, @QueryParam("feedName") String feedName) {
        RegisteredTemplate registeredTemplate = null;
        if (allProperties) {
            registeredTemplate = getMetadataService().getRegisteredTemplateWithAllProperties(templateId);
        } else {
            registeredTemplate = getMetadataService().getRegisteredTemplate(templateId);
        }

        log.info("Returning Registered template for id {} as {} ", templateId, (registeredTemplate != null ? registeredTemplate.getTemplateName() : null));





        //if savedFeedId is passed in merge the properties with the saved values
        if(feedName != null) {
            //TODO pass in the Category to this method
            FeedMetadata feedMetadata = getMetadataService().getFeedByName("",feedName);
            if (feedMetadata != null) {
                List<NifiProperty> list = new ArrayList<>();
                for (NifiProperty p : registeredTemplate.getProperties()) {
                    list.add(new NifiProperty(p));
                }
                registeredTemplate.setProperties(list);
                NifiPropertyUtil.matchAndSetTemplatePropertiesWithSavedProperties(registeredTemplate.getProperties(),
                                                                                  feedMetadata.getProperties());
            }
        }
        Set<PortDTO> ports = null;
        // fetch ports for this template
        try {
            ports = nifiRestClient.getPortsForTemplate(registeredTemplate.getNifiTemplateId());
        } catch (NifiComponentNotFoundException notFoundException) {
            feedManagerTemplateService.syncTemplateId(registeredTemplate);
            ports = nifiRestClient.getPortsForTemplate(registeredTemplate.getNifiTemplateId());
        }
        if (ports == null) {
            ports = new HashSet<>();
        }
        List<PortDTO> outputPorts = Lists.newArrayList(Iterables.filter(ports, new Predicate<PortDTO>() {
            @Override
            public boolean apply(PortDTO portDTO) {
                return portDTO.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
            }
        }));

        List<PortDTO> inputPorts = Lists.newArrayList(Iterables.filter(ports, new Predicate<PortDTO>() {
            @Override
            public boolean apply(PortDTO portDTO) {
                return portDTO.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
            }
        }));
        registeredTemplate.setReusableTemplate(inputPorts != null && !inputPorts.isEmpty());
        List<ReusableTemplateConnectionInfo> reusableTemplateConnectionInfos = registeredTemplate.getReusableTemplateConnections();
        List<ReusableTemplateConnectionInfo> updatedConnectionInfo = new ArrayList<>();

        for (final PortDTO port : outputPorts) {

            ReusableTemplateConnectionInfo reusableTemplateConnectionInfo = null;
            if (reusableTemplateConnectionInfos != null && !reusableTemplateConnectionInfos.isEmpty()) {
                reusableTemplateConnectionInfo = Iterables.tryFind(reusableTemplateConnectionInfos,
                                                                   new Predicate<ReusableTemplateConnectionInfo>() {
                                                                       @Override
                                                                       public boolean apply(
                                                                           ReusableTemplateConnectionInfo reusableTemplateConnectionInfo) {
                                                                           return reusableTemplateConnectionInfo
                                                                               .getFeedOutputPortName()
                                                                               .equalsIgnoreCase(port.getName());
                                                                       }
                                                                   }).orNull();
            }
            if (reusableTemplateConnectionInfo == null) {
                reusableTemplateConnectionInfo = new ReusableTemplateConnectionInfo();
                reusableTemplateConnectionInfo.setFeedOutputPortName(port.getName());
            }
            updatedConnectionInfo.add(reusableTemplateConnectionInfo);

        }

        registeredTemplate.setReusableTemplateConnections(updatedConnectionInfo);
        registeredTemplate.initializeProcessors();
        feedManagerTemplateService.ensureRegisteredTemplateInputProcessors(registeredTemplate);

        return Response.ok(registeredTemplate).build();
    }

    @POST
    @Path("/registered/{templateId}/enable")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public Response enableTemplate(@PathParam("templateId") String templateId) {
        RegisteredTemplate enabledTemplate = feedManagerTemplateService.enableTemplate(templateId);
        return Response.ok(enabledTemplate).build();
    }

    @POST
    @Path("/registered/{templateId}/disable")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public Response disableTemplate(@PathParam("templateId") String templateId) {
        RegisteredTemplate disabledTemplate = feedManagerTemplateService.disableTemplate(templateId);
        return Response.ok(disabledTemplate).build();
    }

    @DELETE
    @Path("/registered/{templateId}/delete")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteTemplate(@PathParam("templateId") String templateId) {
       boolean deleted = feedManagerTemplateService.deleteRegisteredTemplate(templateId);
        return Response.ok( deleted ? new RestResponseStatus.ResponseStatusBuilder().buildSuccess() : new RestResponseStatus.ResponseStatusBuilder().buildError()).build();
    }


    /**
     * Register and save a given template and its properties
     *
     * @
     */
    @POST
    @Path("/register")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public Response registerTemplate(RegisteredTemplate registeredTemplate) {

        RegisteredTemplate saved = getMetadataService().registerTemplate(registeredTemplate);
        
        if (saved.isReusableTemplate()) {
            //attempt to auto create the Feed using this template
            FeedMetadata metadata = metadataService.getFeedByName(Constants.REUSABLE_TEMPLATES_CATEGORY_NAME, saved.getTemplateName());
            if (metadata == null) {
                metadata = new FeedMetadata();
                FeedCategory category = metadataService.getCategoryBySystemName(TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME);
                if(category == null){
                    category = new FeedCategory();
                    category.setName(Constants.REUSABLE_TEMPLATES_CATEGORY_NAME);
                    metadataService.saveCategory(category);
                }
                metadata.setCategory(category);
                metadata.setTemplateId(saved.getId());
                metadata.setFeedName(saved.getTemplateName());
                metadata.setSystemFeedName(SystemNamingService.generateSystemName(saved.getTemplateName()));
            }
            metadata.setRegisteredTemplate(saved);
            getMetadataService().createFeed(metadata);
        }
        return Response.ok(saved).build();
    }


}
