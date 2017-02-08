package com.thinkbiganalytics.feedmgr.service.template;

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

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.nifi.web.api.dto.ConnectableDTO;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Base service for managing templates
 */
public abstract class AbstractFeedManagerTemplateService implements FeedManagerTemplateService {

    private static final Logger log = LoggerFactory.getLogger(AbstractFeedManagerTemplateService.class);

    @Inject
    private AccessController accessController;

    @Autowired
    protected LegacyNifiRestClient nifiRestClient;

    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;

    /**
     * Return a RegisteredTemplate that is populated with all of its {@link RegisteredTemplate#properties}
     *
     * @param nifiTemplateId   a NiFi template id
     * @param nifiTemplateName the name of the NiFi template
     * @return a RegisteredTemplate that is populated with all of its {@link RegisteredTemplate#properties}
     */
    public abstract RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName);

    /**
     * Save the template
     *
     * @param template a template to save
     * @return the saved template
     */
    protected abstract RegisteredTemplate saveRegisteredTemplate(RegisteredTemplate template);

    /**
     * Return a template matching a {@link RegisteredTemplate#id}
     *
     * @param id the id of a template
     * @return a template matching a {@link RegisteredTemplate#id}
     */
    public abstract RegisteredTemplate getRegisteredTemplate(String id);

    /**
     * Return the NiFi template id for the incoming template name
     *
     * @param templateName the name of the template
     * @return the NiFi template id for the incoming template name, null if not found
     */
    public String nifiTemplateIdForTemplateName(String templateName) {

        TemplateDTO templateDTO = null;
        templateDTO = nifiRestClient.getTemplateByName(templateName);

        if (templateDTO != null) {
            return templateDTO.getId();
        }
        return null;
    }

    /**
     * Return properties registered for a template
     *
     * @param templateId a RegisteredTemplate id
     * @return the properties registered for the template
     */
    public List<NifiProperty> getTemplateProperties(String templateId) {
        List<NifiProperty> list = new ArrayList<>();
        RegisteredTemplate template = getRegisteredTemplate(templateId);
        if (template != null) {
            list = template.getProperties();
        }
        return list;
    }

    /**
     * Return the processors in RegisteredTemplate that are input processors ( processors without any incoming connections).
     * This will call out to NiFi to inspect and obtain the NiFi template if it doesn't exist on the registeredTemplate
     *
     * @param registeredTemplate the template to inspect
     * @return the processors in RegisteredTemplate that are input processors without any incoming connections
     */
    public List<RegisteredTemplate.Processor> getInputProcessorsInNifTemplate(RegisteredTemplate registeredTemplate) {
        TemplateDTO nifiTemplate = registeredTemplate.getNifiTemplate();
        if (nifiTemplate == null) {
            nifiTemplate = ensureNifiTemplate(registeredTemplate);
        }
        return getInputProcessorsInNifTemplate(nifiTemplate);
    }

    /**
     * Return the input processors (processors without any incoming connections) in a NiFi template object
     *
     * @param nifiTemplate the NiFi template
     * @return the input processors (processors without any incoming connections) in a NiFi template object
     */
    public List<RegisteredTemplate.Processor> getInputProcessorsInNifTemplate(TemplateDTO nifiTemplate) {
        List<RegisteredTemplate.Processor> processors = new ArrayList<>();
        if (nifiTemplate != null) {
            List<ProcessorDTO> inputProcessors = NifiTemplateUtil.getInputProcessorsForTemplate(nifiTemplate);
            if (inputProcessors != null) {
                inputProcessors.stream().forEach(processorDTO -> {
                    RegisteredTemplate.Processor p = toRegisteredTemplateProcessor(processorDTO, false);
                    p.setInputProcessor(true);
                    processors.add(p);
                });
            }
        }
        return processors;
    }


    /**
     * Return a Registered Template for incoming {@link RegisteredTemplate#id} or a {@link TemplateDTO#id}
     * If there is no RegisteredTemplate matching the incoming id it will attempt to fetch the template from NiFi either by the id or name
     *
     * @param templateId   a {@link RegisteredTemplate#id} or a {@link TemplateDTO#id}
     * @param templateName the name of the template
     * @return a Registered Template object either for an existing registerd template in the system or a non registered NiFi template
     */
    public RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId, String templateName) {
        RegisteredTemplate registeredTemplate = getRegisteredTemplate(templateId);
        //if it is null check to see if the template exists in nifi and is already registered
        if (registeredTemplate == null) {
            log.info("Attempt to get Template with id {}, returned null.  This id must be one registed in Nifi... attempt to query Nifi for this template ", templateId);
            registeredTemplate = getRegisteredTemplateForNifiProperties(templateId, templateName);
        }
        if (registeredTemplate == null) {
            List<NifiProperty> properties = new ArrayList<>();
            TemplateDTO nifiTemplate = nifiRestClient.getTemplateById(templateId);
            registeredTemplate = new RegisteredTemplate();
            registeredTemplate.setNifiTemplateId(templateId);

            if (nifiTemplate != null) {
                properties = nifiRestClient.getPropertiesForTemplate(nifiTemplate);
                registeredTemplate.setNifiTemplate(nifiTemplate);
                registeredTemplate.setTemplateName(nifiTemplate.getName());
            }
            registeredTemplate.setProperties(properties);
        } else {
            registeredTemplate = mergeRegisteredTemplateProperties(registeredTemplate);

        }
        if (registeredTemplate != null) {
            if (NifiPropertyUtil.containsPropertiesForProcessorMatchingType(registeredTemplate.getProperties(), NifiFeedConstants.TRIGGER_FEED_PROCESSOR_CLASS)) {
                registeredTemplate.setAllowPreconditions(true);
            } else {
                registeredTemplate.setAllowPreconditions(false);
            }
        }
        return registeredTemplate;
    }

    /**
     * Ensure that the NiFi template Ids are correct and match our metadata for the Template Name
     *
     * @param template a registered template
     * @return the updated template with the {@link RegisteredTemplate#nifiTemplateId} correctly matching NiFi
     */
    public RegisteredTemplate syncTemplateId(RegisteredTemplate template) {
        String oldId = template.getNifiTemplateId();
        if (oldId == null) {
            oldId = "";
        }
        String nifiTemplateId = nifiTemplateIdForTemplateName(template.getTemplateName());
        if (nifiTemplateId != null && !oldId.equalsIgnoreCase(nifiTemplateId)) {
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
        } else {
            return template;
        }
    }


    /**
     * Return the NiFi {@link TemplateDTO} object fully populated and sets this to the incoming {@link RegisteredTemplate#nifiTemplate}
     * If at first looking at the {@link RegisteredTemplate#nifiTemplateId} it is unable to find the template it will then fallback and attempt to find the template by its name
     *
     * @param registeredTemplate a registered template object
     * @return the NiFi template
     */
    protected TemplateDTO ensureNifiTemplate(RegisteredTemplate registeredTemplate) {
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

    /**
     * Merge all saved properties on the RegisteredTemplate along with the properties in the NiFi template
     * The resulting object will have the {@link RegisteredTemplate#properties} updated so they are in sync with NiFi.
     *
     * @return a RegisteredTemplate that has the properties updated with those in NiFi
     */
    public RegisteredTemplate mergeRegisteredTemplateProperties(RegisteredTemplate registeredTemplate) {
        if (registeredTemplate != null) {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

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
     * @return all input ports under the {@link TemplateCreationHelper#REUSABLE_TEMPLATES_PROCESS_GROUP_NAME} process group
     */
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
     * Return a list of Processors and their properties for the incoming template
     *
     * @param nifiTemplateId a NiFi template id
     * @return a list of Processors and their properties for the incoming template
     */
    public List<RegisteredTemplate.Processor> getNiFiTemplateProcessorsWithProperties(String nifiTemplateId) {
        Set<ProcessorDTO> processorDTOs = nifiRestClient.getProcessorsForTemplate(nifiTemplateId);
        List<RegisteredTemplate.Processor> processorProperties = processorDTOs.stream().map(processorDTO -> toRegisteredTemplateProcessor(processorDTO, true)).collect(Collectors.toList());
        return processorProperties;

    }


    /**
     * For a given Template and its related connection info to the reusable templates, walk the graph to return the Processors.
     * The system will first walk the incoming templateid.  If the {@code connectionInfo} parameter is set it will make the connections to the incoming template and continue walking those processors
     *
     * @param nifiTemplateId the NiFi templateId required to start walking the flow
     * @param connectionInfo the connections required to connect
     * @return a list of all the processors for a template and possible connections
     */
    public List<RegisteredTemplate.FlowProcessor> getNiFiTemplateFlowProcessors(String nifiTemplateId, List<ReusableTemplateConnectionInfo> connectionInfo) {

        TemplateDTO templateDTO = nifiRestClient.getTemplateById(nifiTemplateId);

        //make the connection
        if (connectionInfo != null && !connectionInfo.isEmpty()) {
            Set<PortDTO> templatePorts = templateDTO.getSnippet().getOutputPorts();
            Map<String, PortDTO> outputPorts = templateDTO.getSnippet().getOutputPorts().stream().collect(Collectors.toMap(portDTO -> portDTO.getName(), Function.identity()));
            Map<String, PortDTO> inputPorts = getReusableFeedInputPorts().stream().collect(Collectors.toMap(portDTO -> portDTO.getName(), Function.identity()));
            connectionInfo.stream().forEach(reusableTemplateConnectionInfo -> {
                PortDTO outputPort = outputPorts.get(reusableTemplateConnectionInfo.getFeedOutputPortName());
                PortDTO inputPort = inputPorts.get(reusableTemplateConnectionInfo.getReusableTemplateInputPortName());

                ConnectionDTO connectionDTO = new ConnectionDTO();
                ConnectableDTO source = new ConnectableDTO();
                source.setName(reusableTemplateConnectionInfo.getFeedOutputPortName());
                source.setType(outputPort.getType());
                source.setId(outputPort.getId());
                source.setGroupId(outputPort.getParentGroupId());

                ConnectableDTO dest = new ConnectableDTO();
                dest.setName(inputPort.getName());
                dest.setType(inputPort.getType());
                dest.setId(inputPort.getId());
                dest.setGroupId(inputPort.getParentGroupId());

                connectionDTO.setSource(source);
                connectionDTO.setDestination(dest);
                connectionDTO.setId(UUID.randomUUID().toString());
                templateDTO.getSnippet().getConnections().add(connectionDTO);

            });
        }

        NifiFlowProcessGroup template = nifiRestClient.getTemplateFeedFlow(templateDTO);
        return template.getProcessorMap().values().stream().map(flowProcessor -> {
            RegisteredTemplate.FlowProcessor p = new RegisteredTemplate.FlowProcessor(flowProcessor.getId());
            p.setGroupId(flowProcessor.getParentGroupId());
            p.setType(flowProcessor.getType());
            p.setName(flowProcessor.getName());
            p.setFlowId(flowProcessor.getFlowId());
            p.setIsLeaf(flowProcessor.isLeaf());
            return p;
        }).collect(Collectors.toList());
    }

    /**
     * Return all the processors that are connected to a given NiFi input port
     *
     * @param inputPortIds the ports to inspect
     * @return all the processors that are connected to a given NiFi input port
     */
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

        List<RegisteredTemplate.Processor> processorProperties = processorDTOs.stream().map(processorDTO -> toRegisteredTemplateProcessor(processorDTO, true)).collect(Collectors.toList());
        return processorProperties;
    }

    /**
     * convert a NiFi processor to a RegisteredTemplate processor object
     *
     * @param processorDTO  the NiFi processor
     * @param setProperties true to set the properties on the RegisteredTemplate.Processor, false to skip
     * @return a NiFi processor to a RegisteredTemplate processor object
     */
    private RegisteredTemplate.Processor toRegisteredTemplateProcessor(ProcessorDTO processorDTO, boolean setProperties) {
        RegisteredTemplate.Processor p = new RegisteredTemplate.Processor(processorDTO.getId());
        p.setGroupId(processorDTO.getParentGroupId());
        p.setType(processorDTO.getType());
        p.setName(processorDTO.getName());
        if (setProperties) {
            p.setProperties(NifiPropertyUtil.getPropertiesForProcessor(new ProcessGroupDTO(), processorDTO, propertyDescriptorTransform));
        }
        return p;
    }

}
