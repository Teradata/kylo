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

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateDtoWrapper;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
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

        FeedMetadata feedMetadata = getMetadataService().getFeed(feedName);

        if(feedMetadata != null) {
            List<NifiProperty> list = new ArrayList<>();
            for(NifiProperty p: registeredTemplate.getProperties()){
                list.add(new NifiProperty(p));
            }
            registeredTemplate.setProperties(list);
            NifiPropertyUtil.matchAndSetTemplatePropertiesWithSavedProperties(registeredTemplate.getProperties(),
                                                                              feedMetadata.getProperties());
        }
        return Response.ok(registeredTemplate).build();
    }

    /**
     * get a Templates Properties from the Nifi REST api, merged with any saved registered properties for a given templateId
     * @param templateId
     * @return
     * @throws JerseyClientException

    @GET
    @Path("/{templateId}/properties")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getTemplateProperties(@PathParam("templateId")String templateId) throws JerseyClientException{
        //get the
        List<NifiProperty> properties = nifiRestClient.getPropertiesForTemplate(templateId);
        //merge with the registered properties
        List<NifiProperty> registeredProperties = getMetadataService().getTemplateProperties(templateId);
        NifiPropertyUtil.matchAndSetPropertyByIdKey(properties,registeredProperties);
        return Response.ok(properties).build();
    }

     */

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
        return Response.ok(registeredTemplate).build();
    }







}
