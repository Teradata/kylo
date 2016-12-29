package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.nifi.web.api.dto.PortDTO;

import java.util.List;
import java.util.Set;

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

    RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId, String templateName);

    boolean deleteRegisteredTemplate(String templateId);

    RegisteredTemplate enableTemplate(String templateId);

    RegisteredTemplate disableTemplate(String templateId);

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


    /**
     * Returns the Ports available in the Reusable_templates process group
     */
    Set<PortDTO> getReusableFeedInputPorts();


    /**
     * Walks the flow of children connected to each {@code inputPortIds} and builds a List of objects containing Processor and property info
     */
    List<RegisteredTemplate.Processor> getReusableTemplateProcessorsForInputPorts(List<String> inputPortIds);

    List<RegisteredTemplate.Processor> getRegisteredTemplateProcessors(String templateId, boolean includeReusableProcessors);


    List<RegisteredTemplate.FlowProcessor>  getNiFiTemplateFlowProcessors(String templateId, List<ReusableTemplateConnectionInfo> connectionInfo);

    List<RegisteredTemplate.Processor> getNiFiTemplateProcessorsWithProperties(String templateId);


    /**
     * saves the order as indicated via index in the supplied {@code orderedTemplateIds}
     * @param orderedTemplateIds
     * @param exclude
     */
    void orderTemplates(List<String> orderedTemplateIds, Set<String> exclude);


}
