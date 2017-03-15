package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 3/14/17.
 */
public class RegisteredTemplateService {

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    TemplateModelTransform templateModelTransform;

    @Inject
    private AccessController accessController;

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;


    /**
     * this will return a RegisteredTemplate object for a given NiFi template Id.
     * This is to be used for new templates that are going to be registered with Kylo.
     * Callers of this method should ensure that a template of this id is not already registered
     * @param nifiTemplateId the nifi template identifier
     * @return a RegisteredTemplate object of null if not found in NiFi
     */
    private RegisteredTemplate nifiTemplateToRegisteredTemplate(String nifiTemplateId){

        RegisteredTemplate registeredTemplate = null;
            List<NifiProperty> properties = new ArrayList<>();
            TemplateDTO nifiTemplate = nifiRestClient.getTemplateById(nifiTemplateId);
            if(nifiTemplate != null) {
                registeredTemplate =  new RegisteredTemplate();
                registeredTemplate.setNifiTemplateId(nifiTemplateId);

                    properties = nifiRestClient.getPropertiesForTemplate(nifiTemplate);
                    registeredTemplate.setNifiTemplate(nifiTemplate);
                    registeredTemplate.setTemplateName(nifiTemplate.getName());
                registeredTemplate.setProperties(properties);
            }

        return registeredTemplate;

    }


    /**
     * Gets a Registered Template or returns null if not found by various means passed in via the request object
     * @param registeredTemplateRequest a request to get a registered template
     * @return the RegisteredTemplate or null if not found
     */
    public RegisteredTemplate getRegisteredTemplate(RegisteredTemplateRequest registeredTemplateRequest){

        String templateId = registeredTemplateRequest.getTemplateId();
        String templateName = registeredTemplateRequest.getTemplateName();

        RegisteredTemplate registeredTemplate = getRegisteredTemplateById(templateId);
        //if it is null check to see if the template exists in nifi and is already registered
        if (registeredTemplate == null) {
            log.info("Attempt to get Template with id {}, returned null.  This id must be one registed in Nifi... attempt to query Nifi for this template ", templateId);
            registeredTemplate = getRegisteredTemplateByNiFiIdentifier(templateId);
        }
        if(registeredTemplate == null) {
            //attempt to look by name
            registeredTemplate = getRegisteredTemplateByName(templateName);

        }
        if (registeredTemplate != null){
            if(registeredTemplateRequest.isIncludeAllProperties()) {
              registeredTemplate = mergeRegisteredTemplateProperties(registeredTemplate);
            }
            if (NifiPropertyUtil.containsPropertiesForProcessorMatchingType(registeredTemplate.getProperties(), NifiFeedConstants.TRIGGER_FEED_PROCESSOR_CLASS)) {
                registeredTemplate.setAllowPreconditions(true);
            } else {
                registeredTemplate.setAllowPreconditions(false);
            }
        }
        return registeredTemplate;
    }


    /**
     * Return a registered template object that is populated for use with updating in Kylo
     * @param registeredTemplateRequest the request to get a registered template
     * @return a RegisteredTemplate object mapping either one already defined in Kylo, or a new template that maps to one in NiFi
     */
    public RegisteredTemplate getRegisteredTemplateForUpdate(RegisteredTemplateRequest registeredTemplateRequest){
        RegisteredTemplate registeredTemplate = getRegisteredTemplate(registeredTemplateRequest);
        if(registeredTemplate == null) {
            registeredTemplate = nifiTemplateToRegisteredTemplate(registeredTemplate.getNifiTemplateId());
        }
        return registeredTemplate;
    }


    /**
     * Find a template by the Kylo Id
     * @param templateId The Kylo {@link RegisteredTemplate#id}
     * @return the RegisteredTemplate matching the id or null if not found
     */
    public RegisteredTemplate getRegisteredTemplateById(final String templateId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            FeedManagerTemplate domainTemplate = templateProvider.findById(domainId);
            if (domainTemplate != null) {
                //transform it
                registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domainTemplate);
            }
            if (registeredTemplate != null) {
                registeredTemplate.initializeProcessors();
                ensureNifiTemplate(registeredTemplate);
            }

            return registeredTemplate;
        });

    }


    /**
     * Find a template by the nifi id
     * @param nifiTemplateId the nifi id
     * @return the RegisteredTemplate matching the passed in nifiTemplateId, or null if not found
     */
    public RegisteredTemplate getRegisteredTemplateByNiFiIdentifier(final String nifiTemplateId) {
        if(StringUtils.isBlank(nifiTemplateId)){
            return null;
        }
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
            FeedManagerTemplate template = templateProvider.findByNifiTemplateId(nifiTemplateId);
            if (template != null) {
                registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
            }
            return registeredTemplate;
        });


    }


    /**
     * Find a template by the name
     * @param templateName the name of the template
     * @return the RegisteredTemplate matching the passed in name or null if not found
     */
    public RegisteredTemplate getRegisteredTemplateByName(final String templateName) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
                FeedManagerTemplate   template = templateProvider.findByName(templateName);
            if (template != null) {
                registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
            }
            return registeredTemplate;
        });


    }





    /**
     * Merge all saved properties on the RegisteredTemplate along with the properties in the NiFi template
     * The resulting object will have the {@link RegisteredTemplate#properties} updated so they are in sync with NiFi.
     *
     * @return a RegisteredTemplate that has the properties updated with those in NiFi
     */
    public RegisteredTemplate mergeRegisteredTemplateProperties(RegisteredTemplate registeredTemplate) {
        if (registeredTemplate != null) {
            log.info("Merging properties for template {} ({})", registeredTemplate.getTemplateName(), registeredTemplate.getId());
            List<NifiProperty> properties = null;
            int matchCount = 0;

            //get the nifi template associated with this one that is registered
            TemplateDTO templateDTO = registeredTemplate.getNifiTemplate();
            if (templateDTO == null) {
                templateDTO = ensureNifiTemplate(registeredTemplate);
            }

            if (templateDTO != null) {
                registeredTemplate.setNifiTemplate(templateDTO);
                properties = nifiRestClient.getPropertiesForTemplate(templateDTO);
                //first attempt to match the properties by the processorid and processor name
                NifiPropertyUtil
                    .matchAndSetPropertyByIdKey(properties, registeredTemplate.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
            }

            if (properties != null) {
                //match the properties to the processors by the processor name
                //expression ${metdata.} properties will not be reset
                NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties(),
                                                                    NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
            }
            if (templateDTO != null && !templateDTO.getId().equalsIgnoreCase(registeredTemplate.getNifiTemplateId())) {
                syncTemplateId(registeredTemplate);

            }
            if (properties == null) {
                properties = new ArrayList<>();
            }
            //merge with the registered properties

            RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
            copy.setProperties(properties);
            copy.setNifiTemplate(registeredTemplate.getNifiTemplate());

            registeredTemplate = copy;

        } else {
            log.info("Unable to merge Registered Template.  It is null");
        }
        return registeredTemplate;
    }






    /**
     * Return the NiFi {@link TemplateDTO} object fully populated and sets this to the incoming {@link RegisteredTemplate#nifiTemplate}
     * If at first looking at the {@link RegisteredTemplate#nifiTemplateId} it is unable to find the template it will then fallback and attempt to find the template by its name
     *
     * @param registeredTemplate a registered template object
     * @return the NiFi template
     */
    private TemplateDTO ensureNifiTemplate(RegisteredTemplate registeredTemplate) {
        TemplateDTO templateDTO = null;
        try {
            try {
                templateDTO = nifiRestClient.getTemplateById(registeredTemplate.getNifiTemplateId());
            } catch (NifiComponentNotFoundException e) {
                //this is fine... we can safely proceeed if not found.
            }
            if (templateDTO == null) {
                templateDTO = nifiRestClient.getTemplateByName(registeredTemplate.getTemplateName());
                if (templateDTO != null) {
                    //getting the template by the name will not get all the properties.
                    //refetch it by the name to get the FlowSnippet
                    //populate the snippet
                    templateDTO = nifiRestClient.getTemplateById(templateDTO.getId());

                }
            }
            if (templateDTO != null) {
                registeredTemplate.setNifiTemplate(templateDTO);
                registeredTemplate.setNifiTemplateId(registeredTemplate.getNifiTemplate().getId());
            }

        } catch (NifiClientRuntimeException e) {
            log.error("Error attempting to get the NifiTemplate TemplateDTO object for {} using nifiTemplateId of {} ", registeredTemplate.getTemplateName(), registeredTemplate.getNifiTemplateId());
        }
        return templateDTO;
    }


}
