package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import java.util.List;

/**
 * Created by sr186054 on 5/1/16.
 */
public interface FeedManagerTemplateService {

    String templateIdForTemplateName(String templateName);

    RegisteredTemplate registerTemplate(RegisteredTemplate registeredTemplate);

    List<NifiProperty> getTemplateProperties(String templateId);

    RegisteredTemplate getRegisteredTemplate(String templateId);

    RegisteredTemplate getRegisteredTemplateByName(String templateName);

    RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName);

    RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId);

    void deleteRegisteredTemplate(String templateId);

    List<RegisteredTemplate> getRegisteredTemplates();

    /**
     * Ensures the RegisteredTemplate#inputProcessors contains processors that both have properties exposed for the end user and those that dont
     */
    public void ensureRegisteredTemplateInputProcessors(RegisteredTemplate registeredTemplate);

    /**
     * Synchronize the Nifitemplate Ids to make sure its in sync with the id stored in our metadata store for the RegisteredTemplate
     * @param template
     */
    RegisteredTemplate syncTemplateId(RegisteredTemplate template);


}
