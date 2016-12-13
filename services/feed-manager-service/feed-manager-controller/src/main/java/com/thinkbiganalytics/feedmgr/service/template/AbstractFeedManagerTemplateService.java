package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/4/16.
 */
public abstract class AbstractFeedManagerTemplateService {

    private static final Logger log = LoggerFactory.getLogger(AbstractFeedManagerTemplateService.class);

    @Inject
    private AccessController accessController;

    @Autowired
    protected LegacyNifiRestClient nifiRestClient;

    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;

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

    public List<RegisteredTemplate.Processor> getInputProcessorsInNifTemplate(RegisteredTemplate registeredTemplate) {
        TemplateDTO nifiTemplate = registeredTemplate.getNifiTemplate();
        if(nifiTemplate == null) {
            nifiTemplate = ensureNifiTemplate(registeredTemplate);
        }
        return getInputProcessorsInNifTemplate(nifiTemplate);
    }



    public List<RegisteredTemplate.Processor> getInputProcessorsInNifTemplate(TemplateDTO nifiTemplate) {
            List<RegisteredTemplate.Processor> processors = new ArrayList<>();
            if(nifiTemplate != null) {
                List<ProcessorDTO> inputProcessors = NifiTemplateUtil.getInputProcessorsForTemplate(nifiTemplate);
                if (inputProcessors != null) {
                    inputProcessors.stream().forEach(processorDTO -> {
                        RegisteredTemplate.Processor p = new RegisteredTemplate.Processor(processorDTO.getId());
                        p.setInputProcessor(true);
                        p.setGroupId(processorDTO.getParentGroupId());
                        p.setName(processorDTO.getName());
                        p.setType(processorDTO.getType());
                        processors.add(p);
                    });
                }
            }
        return processors;
    }


    /**
     * Get Registered Template for incoming RegisteredTemplate.id or Nifi Template Id if there is no RegisteredTEmplate matching the incoming id it is assumed to be a new Tempate and it tries to fetch
     * it from Nifi
     */
    public RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId, String templateName) {
        RegisteredTemplate registeredTemplate = getRegisteredTemplate(templateId);
        //if it is null check to see if the template exists in nifi and is already registered
        if (registeredTemplate == null) {
            log.info("Attempt to get Template with ID {}, returned Null.  This ID must be one registed in Nifi... attempt to query Nifi for this template ", templateId);
            registeredTemplate = getRegisteredTemplateForNifiProperties(templateId, templateName);
        }
        if (registeredTemplate == null) {
            List<NifiProperty> properties = new ArrayList<>();
                TemplateDTO nifiTemplate = nifiRestClient.getTemplateById(templateId);
            registeredTemplate = new RegisteredTemplate();
            registeredTemplate.setNifiTemplateId(templateId);

            if(nifiTemplate != null) {
                properties = nifiRestClient.getPropertiesForTemplate(nifiTemplate);
                registeredTemplate.setNifiTemplate(nifiTemplate);
                registeredTemplate.setTemplateName(nifiTemplate.getName());
            }
            registeredTemplate.setProperties(properties);
        } else {
            registeredTemplate = mergeRegisteredTemplateProperties(registeredTemplate);

        }
        if(registeredTemplate != null)
        {
            if (NifiPropertyUtil.containsPropertiesForProcessorMatchingType(registeredTemplate.getProperties(), NifiFeedConstants.TRIGGER_FEED_PROCESSOR_CLASS)) {
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
    public RegisteredTemplate syncTemplateId(RegisteredTemplate template) {
        String oldId = template.getNifiTemplateId();
        if(oldId == null) {
            oldId = "";
        }
        String nifiTemplateId = templateIdForTemplateName(template.getTemplateName());
        template.setNifiTemplateId(nifiTemplateId);

        RegisteredTemplate t = getRegisteredTemplate(template.getId());
        template.setProperties(t.getProperties());
        if (!oldId.equalsIgnoreCase(template.getNifiTemplateId())) {
            log.info("Updating Registered Template {} with new Nifi Template Id.  Old Id: {}, New Id: {} ", template.getTemplateName(), oldId, template.getNifiTemplateId());
        }
        RegisteredTemplate updatedTemplate = saveRegisteredTemplate(template);
        if (!oldId.equalsIgnoreCase(template.getNifiTemplateId())) {
            log.info("Successfully updated and synchronized Registered Template {} with new Nifi Template Id.  Old Id: {}, New Id: {} ", template.getTemplateName(), oldId,
                     template.getNifiTemplateId());
        }
        return updatedTemplate;
    }

    public abstract RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName);

    protected abstract RegisteredTemplate saveRegisteredTemplate(RegisteredTemplate template);

    public abstract RegisteredTemplate getRegisteredTemplate(String id);


    /**
     * Gets the NiFi TemplateDTO object fully populated.  first looking at the registered nifiTemplateId associated with the {@code registeredTemplate}. If not found it will attempt to lookup the template by the Name
     * @param registeredTemplate
     * @return
     */
    protected  TemplateDTO ensureNifiTemplate(RegisteredTemplate registeredTemplate){
        TemplateDTO templateDTO = null;
        try {
            try {
             templateDTO = nifiRestClient.getTemplateById(registeredTemplate.getNifiTemplateId());
            } catch (NifiComponentNotFoundException e) {
                //this is fine... we can safely proceeed if not found.
            }
            if(templateDTO == null) {
                templateDTO = nifiRestClient.getTemplateByName(registeredTemplate.getTemplateName());
                if (templateDTO != null) {
                    //getting the template by the name will not get all the properties.
                    //refetch it by the name to get the FlowSnippet
                    //populate the snippet
                    templateDTO = nifiRestClient.getTemplateById(templateDTO.getId());

                }
            }
            if(templateDTO != null){
                registeredTemplate.setNifiTemplate(templateDTO);
                registeredTemplate.setNifiTemplateId(registeredTemplate.getNifiTemplate().getId());
            }

        } catch (NifiClientRuntimeException e) {
            log.error("Error attempting to get the NifiTemplate TemplateDTO object for {} using nifiTemplateId of {} ",registeredTemplate.getTemplateName(),registeredTemplate.getNifiTemplateId());
        }
        return templateDTO;
    }


    public RegisteredTemplate mergeRegisteredTemplateProperties(RegisteredTemplate registeredTemplate) {
        if (registeredTemplate != null) {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

            log.info("Merging properties for template {} ({})", registeredTemplate.getTemplateName(), registeredTemplate.getId());
            List<NifiProperty> properties = null;
            int matchCount = 0;
            //get the nifi template associated with this one that is registered
            TemplateDTO templateDTO = registeredTemplate.getNifiTemplate();
            if(templateDTO == null){
                templateDTO = ensureNifiTemplate(registeredTemplate);
            }

            if(templateDTO != null) {
                    registeredTemplate.setNifiTemplate(templateDTO);
                    properties = nifiRestClient.getPropertiesForTemplate(templateDTO);
                    List<NifiProperty> matchedProperties = NifiPropertyUtil
                        .matchAndSetPropertyByIdKey(properties, registeredTemplate.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
                    matchCount = matchedProperties.size();
            }

            if (properties == null || matchCount == 0) {
                if (properties != null) {
                    NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties(),NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
                }
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

    public Set<PortDTO> getReusableFeedInputPorts() {
        Set<PortDTO> ports = new HashSet<>();
        ProcessGroupDTO processGroup = nifiRestClient.getProcessGroupByName("root", TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME);
        if (processGroup != null) {
            //fetch the ports
            Set<PortDTO> inputPortsEntity = nifiRestClient.getInputPorts(processGroup.getId());
            if (inputPortsEntity != null && !inputPortsEntity.isEmpty()) {
                ports.addAll(inputPortsEntity);
            }
        }
        return ports;
    }

    /**
     * gets a List of Processors and their properties for the incoming template
     */
    public List<RegisteredTemplate.Processor> getNiFiTemplateProcessors(String nifiTemplateId) {
        Set<ProcessorDTO> processorDTOs = nifiRestClient.getProcessorsForTemplate(nifiTemplateId);
        List<RegisteredTemplate.Processor> processorProperties = processorDTOs.stream().map(processorDTO -> {
            RegisteredTemplate.Processor p = new RegisteredTemplate.Processor(processorDTO.getId());
            p.setGroupId(processorDTO.getParentGroupId());
            p.setType(processorDTO.getType());
            p.setName(processorDTO.getName());
            p.setProperties(NifiPropertyUtil.getPropertiesForProcessor(new ProcessGroupDTO(), processorDTO, propertyDescriptorTransform));
            return p;
        }).collect(Collectors.toList());
        return processorProperties;

    }

    public List<RegisteredTemplate.Processor> getReusableTemplateProcessorsForInputPorts(List<String> inputPortIds) {
        Set<ProcessorDTO> processorDTOs = new HashSet<>();
        if (inputPortIds != null && !inputPortIds.isEmpty()) {
            ProcessGroupDTO processGroup = nifiRestClient.getProcessGroupByName("root", TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME);
            if (processGroup != null) {
                //fetch the Content
                ProcessGroupDTO content = nifiRestClient.getProcessGroup(processGroup.getId(), true, true);
                processGroup.setContents(content.getContents());
                Set<PortDTO> ports = getReusableFeedInputPorts();
                ports.stream()
                    .filter(portDTO -> inputPortIds.contains(portDTO.getId()))
                    .forEach(port -> {
                        List<ConnectionDTO> connectionDTOs = NifiConnectionUtil.findConnectionsMatchingSourceId(processGroup.getContents().getConnections(), port.getId());
                        if (connectionDTOs != null) {
                            connectionDTOs.stream().forEach(connectionDTO -> {
                                String processGroupId = connectionDTO.getDestination().getGroupId();
                                Set<ProcessorDTO> processors = nifiRestClient.getProcessorsForFlow(processGroupId);
                                if (processors != null) {
                                    processorDTOs.addAll(processors);
                                }
                            });
                        }
                    });

            }
        }

        List<RegisteredTemplate.Processor> processorProperties = processorDTOs.stream().map(processorDTO -> {
            RegisteredTemplate.Processor p = new RegisteredTemplate.Processor(processorDTO.getId());
            p.setGroupId(processorDTO.getParentGroupId());
            p.setType(processorDTO.getType());
            p.setName(processorDTO.getName());
            p.setProperties(NifiPropertyUtil.getPropertiesForProcessor(new ProcessGroupDTO(), processorDTO, propertyDescriptorTransform));
            return p;
        }).collect(Collectors.toList());
        return processorProperties;
    }

}
