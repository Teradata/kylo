package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public abstract class AbstractFeedManagerTemplateService {


    @Autowired
    private NifiRestClient nifiRestClient;

    public String templateIdForTemplateName(String templateName) {

        TemplateDTO templateDTO = null;
        try {
            templateDTO = nifiRestClient.getTemplateByName(templateName);
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }
        if(templateDTO != null){
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
     * Get Registered Template for incoming RegisteredTemplate.id or Nifi Template Id
     * if there is no RegisteredTEmplate matching the incoming id it is assumed to be a new Tempate and it tries to fetch it from Nifi
     */
    public RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException {
        RegisteredTemplate registeredTemplate = getRegisteredTemplate(templateId);
        //if it is null check to see if the template exists in nifi and is already registered
        if(registeredTemplate == null){
            registeredTemplate = getRegisteredTemplateForNifiProperties(templateId,null);
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
        return registeredTemplate;
    }

    /**
     * Ensure that the NIFI template Ids are correct and match our metadata for the Template Name
     * @param template
     */
    private void syncTemplateId(RegisteredTemplate template){
        String nifiTemplateId = templateIdForTemplateName(template.getTemplateName());
        template.setNifiTemplateId(nifiTemplateId);

        RegisteredTemplate t = getRegisteredTemplate(template.getId());
        template.setProperties(t.getProperties());
        saveRegisteredTemplate(template);
    }

    public abstract RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName);

    protected abstract RegisteredTemplate saveRegisteredTemplate(RegisteredTemplate template);

    public abstract RegisteredTemplate getRegisteredTemplate(String id);



    public RegisteredTemplate mergeRegisteredTemplateProperties(RegisteredTemplate registeredTemplate) throws JerseyClientException {

        if(registeredTemplate != null){
            List<NifiProperty> properties = null;
            int matchCount = 0;
            try {
                properties = nifiRestClient.getPropertiesForTemplate(registeredTemplate.getNifiTemplateId());
                List<NifiProperty> matchedProperties = NifiPropertyUtil
                        .matchAndSetPropertyByIdKey(properties, registeredTemplate.getProperties());
                matchCount = matchedProperties.size();
            }catch(JerseyClientException e) {

            }
            if(properties == null || matchCount == 0) {
                //sync templateId for name
                properties = nifiRestClient.getPropertiesForTemplateByName(registeredTemplate.getTemplateName());
                if(properties != null) {
                    //   property = NifiPropertyUtil.findPropertyByProcessorType(properties, "com.thinkbiganalytics.nifi.GetTableData", "Archive Unit");
                    NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties());
                    //    registeredTemplate.setProperties(properties);
                }
                syncTemplateId(registeredTemplate);

            }
            if(properties == null){
                properties = new ArrayList<>();
            }
            //merge with the registered properties

            RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
            copy.setProperties(properties);

            registeredTemplate = copy;

        }
        return registeredTemplate;
    }




}
