package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/4/16.
 */
public abstract class AbstractFeedManagerTemplateService {

    private static final Logger log = LoggerFactory.getLogger(AbstractFeedManagerTemplateService.class);

    @Inject
    private AccessController accessController;

    @Autowired
    private NifiRestClient nifiRestClient;

    public String templateIdForTemplateName(String templateName) {

        TemplateDTO templateDTO = null;
        templateDTO = nifiRestClient.getTemplateByName(templateName);

        if (templateDTO != null) {
            return templateDTO.getId();
        }
        return null;
    }

    public List<NifiProperty> getTemplateProperties(String templateId) {
        List<NifiProperty> list = new ArrayList<>();
        RegisteredTemplate template = getRegisteredTemplate(templateId);
        if (template != null) {
            list = template.getProperties();
        }
        return list;
    }


    /**
     * Get Registered Template for incoming RegisteredTemplate.id or Nifi Template Id if there is no RegisteredTEmplate matching the incoming id it is assumed to be a new Tempate and it tries to fetch
     * it from Nifi
     */
    public RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) {
        RegisteredTemplate registeredTemplate = getRegisteredTemplate(templateId);
        //if it is null check to see if the template exists in nifi and is already registered
        if (registeredTemplate == null) {
            log.info("Attempt to get Template with ID {}, returned Null.  This ID must be one registed in Nifi... attempt to query Nifi for this template ", templateId);
            registeredTemplate = getRegisteredTemplateForNifiProperties(templateId, null);
        }
        if (registeredTemplate == null) {
            List<NifiProperty> properties = nifiRestClient.getPropertiesForTemplate(templateId);
            registeredTemplate = new RegisteredTemplate();
            registeredTemplate.setProperties(properties);
            registeredTemplate.setNifiTemplateId(templateId);
            //get the template Name
            TemplateDTO templateDTO = nifiRestClient.getTemplateById(templateId);
            if (templateDTO != null) {
                registeredTemplate.setTemplateName(templateDTO.getName());
            }
        } else {
            registeredTemplate = mergeRegisteredTemplateProperties(registeredTemplate);

        }
        if(registeredTemplate != null)
        {
            if(NifiPropertyUtil.containsPropertiesForProcessorMatchingType(registeredTemplate.getProperties(), NifiConstants.TRIGGER_FEED_PROCESSOR_CLASS)){
                registeredTemplate.setAllowPreconditions(true);
            }
            else {
                registeredTemplate.setAllowPreconditions(false);
            }
        }
        return registeredTemplate;
    }

    /**
     * Ensure that the NIFI template Ids are correct and match our metadata for the Template Name
     */
    private void syncTemplateId(RegisteredTemplate template) {
        String oldId = template.getNifiTemplateId();
        String nifiTemplateId = templateIdForTemplateName(template.getTemplateName());
        template.setNifiTemplateId(nifiTemplateId);

        RegisteredTemplate t = getRegisteredTemplate(template.getId());
        template.setProperties(t.getProperties());
        if (!oldId.equalsIgnoreCase(template.getNifiTemplateId())) {
            log.info("Updating Registered Template {} with new Nifi Template Id.  Old Id: {}, New Id: {} ", template.getTemplateName(), oldId, template.getNifiTemplateId());
        }
        saveRegisteredTemplate(template);
        if (!oldId.equalsIgnoreCase(template.getNifiTemplateId())) {
            log.info("Successfully updated and synchronized Registered Template {} with new Nifi Template Id.  Old Id: {}, New Id: {} ", template.getTemplateName(), oldId,
                     template.getNifiTemplateId());
        }
    }

    public abstract RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName);

    protected abstract RegisteredTemplate saveRegisteredTemplate(RegisteredTemplate template);

    public abstract RegisteredTemplate getRegisteredTemplate(String id);


    public RegisteredTemplate mergeRegisteredTemplateProperties(RegisteredTemplate registeredTemplate) {
        if (registeredTemplate != null) {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);
            
            log.info("Merging properties for template {} ({})", registeredTemplate.getTemplateName(), registeredTemplate.getId());
            List<NifiProperty> properties = null;
            int matchCount = 0;
            try {
                properties = nifiRestClient.getPropertiesForTemplate(registeredTemplate.getNifiTemplateId());
                List<NifiProperty> matchedProperties = NifiPropertyUtil
                    .matchAndSetPropertyByIdKey(properties, registeredTemplate.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
                matchCount = matchedProperties.size();
            } catch (NifiClientRuntimeException e) {

            }
            if (properties == null || matchCount == 0) {
                log.info(
                    "Unable to find Nifi Template associated with saved Id of {}.  Possibly the Nifi Template was updated and saved again.  Attempt to query Nifi for template with the name of {}",
                    registeredTemplate.getNifiTemplateId(), registeredTemplate.getTemplateName());
                //sync templateId for name
                properties = nifiRestClient.getPropertiesForTemplateByName(registeredTemplate.getTemplateName());
                if (properties != null) {
                    //   property = NifiPropertyUtil.findPropertyByProcessorType(properties, "com.thinkbiganalytics.nifi.GetTableData", "Archive Unit");
                    NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties(),NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
                    //    registeredTemplate.setProperties(properties);
                }
                syncTemplateId(registeredTemplate);

            }
            if (properties == null) {
                properties = new ArrayList<>();
            }
            //merge with the registered properties

            RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
            copy.setProperties(properties);

            registeredTemplate = copy;

        } else {
            log.info("Unable to merge Registered Template.  It is null");
        }
        return registeredTemplate;
    }


}
