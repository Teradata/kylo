package com.thinkbiganalytics.feedmgr.rest.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateDtoWrapper;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.support.Constants;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/12/16.
 */
@Api(value = "feed-manager-templates", produces = "application/json")
@Path("/v1/feedmgr/templates")
public class TemplatesRestController {



    @Autowired
    NifiRestClient nifiRestClient;

    @Autowired
    MetadataService metadataService;

    public TemplatesRestController() {


    }

    private MetadataService getMetadataService(){
        return metadataService;
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON })
    public Response getTemplates(@QueryParam("includeDetails")boolean includeDetails) throws JerseyClientException{
        TemplatesEntity templatesEntity = nifiRestClient.getTemplates(includeDetails);
        List<RegisteredTemplate> registeredTemplates = metadataService.getRegisteredTemplates();

        Set<TemplateDtoWrapper> dtos = new HashSet<>();
        for(final TemplateDTO dto : templatesEntity.getTemplates()){
            RegisteredTemplate match = metadataService.getRegisteredTemplateForNifiProperties(dto.getId(), dto.getName());
            TemplateDtoWrapper wrapper = new TemplateDtoWrapper(dto);
            if(match != null) {
                wrapper.setRegisteredTemplateId(match.getId());
            }
            dtos.add(wrapper);
        }
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/unregistered")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getUnregisteredTemplates(@QueryParam("includeDetails")boolean includeDetails) throws JerseyClientException{
        TemplatesEntity templatesEntity = nifiRestClient.getTemplates(includeDetails);
        List<RegisteredTemplate> registeredTemplates = metadataService.getRegisteredTemplates();

        Set<TemplateDtoWrapper> dtos = new HashSet<>();
        for(final TemplateDTO dto : templatesEntity.getTemplates()){
            RegisteredTemplate match = metadataService.getRegisteredTemplateForNifiProperties(dto.getId(),dto.getName());
            if(match == null) {
                dtos.add(new TemplateDtoWrapper(dto));
            }
        }
        return Response.ok(dtos).build();
    }


    @GET
    @Path("/nifi/{templateId}/ports")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getPortsForNifiTemplate(@PathParam("templateId")String nifiTemplateId) throws JerseyClientException {
        Set<PortDTO> ports = nifiRestClient.getPortsForTemplate(nifiTemplateId);
        return Response.ok(ports).build();
    }

    @GET
    @Path("/nifi/{templateId}/input-ports")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getInputPortsForNifiTemplate(@PathParam("templateId")String nifiTemplateId) throws JerseyClientException {
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
    @Path("/nifi/{templateId}/out-ports")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getOutputPortsForNifiTemplate(@PathParam("templateId")String nifiTemplateId) throws JerseyClientException {
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
     * Get a Nifi TemplateDTO for a given templateId
     * @param templateId
     * @return
     * @throws JerseyClientException
     */
    /*
    @GET
    @Path("/{templateId}")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getTemplate(@PathParam("templateId")String templateId) throws JerseyClientException{
       TemplateDTO template = NifiService.getInstance().getNifiRestClient().getTemplateById(templateId);
        return Response.ok(template).build();
    }
*/


    /**
     * get all the registered Templates
     * @return
     * @throws JerseyClientException
     */
    @GET
    @Path("/registered")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getRegisteredTemplates() throws JerseyClientException{
        List<RegisteredTemplate>templates =  getMetadataService().getRegisteredTemplates();
        return Response.ok(templates).build();

/*
        List<String>templateIds = MetadataService.getInstance().getRegisteredTemplateIds();
        List<TemplateDTO> registeredTemplates = new ArrayList<>();
        if(!templateIds.isEmpty()) {
            TemplatesEntity templatesEntity = NifiService.getInstance().getNifiRestClient().getTemplates(false);
            for(TemplateDTO template: templatesEntity.getTemplates()) {
                if(templateIds.contains(template.getId())) {
                    registeredTemplates.add(template);
                }
            }
        }
        */
    }




    /**
     * get a given Registered Templates properties
     * @param templateId
     * @return
     * @throws JerseyClientException
     */
    @GET
    @Path("/registered/{templateId}/properties")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getRegisteredTemplateProperties(@PathParam("templateId")String templateId) throws JerseyClientException{
        return Response.ok(getMetadataService().getTemplateProperties(templateId)).build();
    }

    /**
     * get a registeredTemplate
     * @param templateId
     * @return
     * @throws JerseyClientException
     */
    @GET
    @Path("/registered/{templateId}")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getRegisteredTemplate(@PathParam("templateId")String templateId, @QueryParam("allProperties")boolean allProperties, @QueryParam("feedName") String feedName) throws JerseyClientException{
        RegisteredTemplate registeredTemplate = null;
        if(allProperties){
            registeredTemplate = getMetadataService().getRegisteredTemplateWithAllProperties(templateId);
        }
        else {
         registeredTemplate = getMetadataService().getRegisteredTemplate(templateId);
        }

        //if savedFeedId is passed in merge the properties with the saved values
        if(feedName != null) {
            FeedMetadata feedMetadata = getMetadataService().getFeedByName(feedName);

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

        // fetch ports for this template
        Set<PortDTO> ports = nifiRestClient.getPortsForTemplate(registeredTemplate.getNifiTemplateId());
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
                if(reusableTemplateConnectionInfos != null && !reusableTemplateConnectionInfos.isEmpty()) {
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
                if(reusableTemplateConnectionInfo == null) {
                    reusableTemplateConnectionInfo = new ReusableTemplateConnectionInfo();
                    reusableTemplateConnectionInfo.setFeedOutputPortName(port.getName());
                }
                updatedConnectionInfo.add(reusableTemplateConnectionInfo);

            }


        registeredTemplate.setReusableTemplateConnections(updatedConnectionInfo);

        return Response.ok(registeredTemplate).build();
    }


    /**
     * Register and save a given template and its properties
     * @param registeredTemplate
     * @return
     * @throws JerseyClientException
     */
    @POST
    @Path("/register")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON })
    public Response registerTemplate(RegisteredTemplate registeredTemplate) throws JerseyClientException{

        getMetadataService().registerTemplate(registeredTemplate);
        if(registeredTemplate.isReusableTemplate()){
            //attempt to auto create the Feed using this template
            FeedMetadata metadata = metadataService.getFeedByName(registeredTemplate.getTemplateName());
            if(metadata == null) {
                metadata = new FeedMetadata();

                FeedCategory category = metadataService.getCategoryByName(Constants.REUSABLE_TEMPLATES_CATEGORY_NAME);
                if(category == null){
                    category = new FeedCategory();
                    category.setName(Constants.REUSABLE_TEMPLATES_CATEGORY_NAME);
                    metadataService.saveCategory(category);
                }
                metadata.setCategory(category);
                metadata.setTemplateId(registeredTemplate.getId());
                metadata.setFeedName(registeredTemplate.getTemplateName());
                metadata.setSystemFeedName(SystemNamingService.generateSystemName(registeredTemplate.getTemplateName()));
            }
            metadata.setRegisteredTemplate(registeredTemplate);
            NifiFeed feed = getMetadataService().createFeed(metadata);
            int i = 0;
        }
        return Response.ok(registeredTemplate).build();
    }







}
