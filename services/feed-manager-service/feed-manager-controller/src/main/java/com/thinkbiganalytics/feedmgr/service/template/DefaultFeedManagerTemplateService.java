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

import com.google.common.collect.Sets;
import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChange;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChangeEvent;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

/**
 */
public class DefaultFeedManagerTemplateService  implements FeedManagerTemplateService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFeedManagerTemplateService.class);

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    TemplateModelTransform templateModelTransform;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    NifiFlowCache nifiFlowCache;

    @Inject
    private MetadataEventService metadataEventService;


    @Inject
    private AccessController accessController;

    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;

    @Inject
    private LegacyNifiRestClient nifiRestClient;


    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    RegisteredTemplateUtil registeredTemplateUtil;

    protected RegisteredTemplate saveRegisteredTemplate(final RegisteredTemplate registeredTemplate) {
        List<String> templateOrder = registeredTemplate.getTemplateOrder();
        RegisteredTemplate savedTemplate = metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

            //ensure that the incoming template name doesnt already exist.
            //if so remove and replace with this one
            RegisteredTemplate template = registeredTemplateService.getRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(registeredTemplate.getTemplateName()));
            if (registeredTemplate.getId() == null && template != null) {
                registeredTemplate.setId(template.getId());
            }
            if (template != null && !template.getId().equalsIgnoreCase(registeredTemplate.getId())) {
                //Warning cant save.. duplicate Name
                log.error("Unable to save template {}.  There is already a template with this name registered in the system", registeredTemplate.getTemplateName());
                return null;
            } else {
                log.info("About to save Registered Template {} ({}), nifi template Id of {} ", registeredTemplate.getTemplateName(), registeredTemplate.getId(),
                         registeredTemplate.getNifiTemplateId());
                ensureRegisteredTemplateInputProcessors(registeredTemplate);

                FeedManagerTemplate domain = templateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(registeredTemplate);
                ensureNifiTemplateId(domain);
                if(domain != null ) {
                    log.info("Domain Object is {} ({}), nifi template Id of {}", domain.getName(), domain.getId(), domain.getNifiTemplateId());
                }
                domain = templateProvider.update(domain);
                //query it back to display to the ui
                domain = templateProvider.findById(domain.getId());
                return templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domain);
            }
        });

        if (StringUtils.isBlank(registeredTemplate.getId())) {
            templateOrder = templateOrder.stream().map(template -> {
                if ("NEW".equals(template)) {
                    return savedTemplate.getId();
                } else {
                    return template;
                }
            }).collect(Collectors.toList());
        }

        //order it
        orderTemplates(templateOrder, Sets.newHashSet(savedTemplate.getId()));

        return savedTemplate;

    }

    /**
     * pass in the Template Ids in Order
     */
    public void orderTemplates(List<String> orderedTemplateIds, Set<String> exclude) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

            if (orderedTemplateIds != null && !orderedTemplateIds.isEmpty()) {
                IntStream.range(0, orderedTemplateIds.size()).forEach(i -> {
                    String id = orderedTemplateIds.get(i);
                    if (!"NEW".equals(id) && (exclude == null || (exclude != null && !exclude.contains(id)))) {
                        FeedManagerTemplate template = templateProvider.findById(templateProvider.resolveId(id));
                        if (template != null) {
                            if (template.getOrder() == null || !template.getOrder().equals(new Long(i))) {
                                //save the new order
                                template.setOrder(new Long(i));
                                templateProvider.update(template);
                            }
                        }
                    }
                });
            }
        });


    }


    @Override
    public List<RegisteredTemplate.Processor> getRegisteredTemplateProcessors(String templateId, boolean includeReusableProcessors) {
        List<RegisteredTemplate.Processor> processorProperties = new ArrayList<>();

        RegisteredTemplate template = registeredTemplateService.getRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(templateId).nifiTemplateId(templateId).includeAllProperties(true).build());
        if (template != null) {
            template.initializeProcessors();
            processorProperties.addAll(template.getInputProcessors());
            processorProperties.addAll(template.getNonInputProcessors());
        }
        if (includeReusableProcessors && template.getReusableTemplateConnections() != null && !template.getReusableTemplateConnections().isEmpty()) {

            //1 fetch ports in reusable templates
            Map<String, PortDTO> reusableTemplateInputPorts = new HashMap<>();
            Set<PortDTO> ports = getReusableFeedInputPorts();
            if (ports != null) {
                ports.stream().forEach(portDTO -> reusableTemplateInputPorts.put(portDTO.getName(), portDTO));
            }

            //match to the name
            List<String>
                matchingPortIds =
                template.getReusableTemplateConnections().stream().filter(conn -> reusableTemplateInputPorts.containsKey(conn.getReusableTemplateInputPortName()))
                    .map(reusableTemplateConnectionInfo -> reusableTemplateInputPorts.get(reusableTemplateConnectionInfo.getReusableTemplateInputPortName()).getId()).collect(Collectors.toList());

            List<RegisteredTemplate.Processor> reusableProcessors = getReusableTemplateProcessorsForInputPorts(matchingPortIds);
            processorProperties.addAll(reusableProcessors);
        }
        return processorProperties;
    }

    /**
     * Ensures that the {@code RegisteredTemplate#inputProcessors} list is populated not only with the processors which were defined as having user inputs, but also those that done require any input
     */
    public void ensureRegisteredTemplateInputProcessors(RegisteredTemplate registeredTemplate) {
        registeredTemplate.initializeInputProcessors();
        List<RegisteredTemplate.Processor> nifiProcessors = getInputProcessorsInNifTemplate(registeredTemplate);
        if (nifiProcessors == null) {
            nifiProcessors = Collections.emptyList();
        }
        List<RegisteredTemplate.Processor> validInputProcessors = nifiProcessors.stream().filter(RegisteredTemplate.isValidInputProcessor()).collect(Collectors.toList());

        //add in any processors not in the map
        validInputProcessors.stream().forEach(processor -> {

            boolean match = registeredTemplate.getInputProcessors().stream().anyMatch(
                registeredProcessor -> registeredProcessor.getId().equals(processor.getId()) || (registeredProcessor.getType().equals(processor.getType()) && registeredProcessor.getName()
                    .equals(processor.getName())));
            if (!match) {
                log.info("Adding Processor {} to registered ", processor.getName());
                registeredTemplate.getInputProcessors().add(processor);
            }

        });

    }

    @Override
    public RegisteredTemplate registerTemplate(RegisteredTemplate registeredTemplate) {
        boolean isNew = StringUtils.isBlank(registeredTemplate.getId());
        RegisteredTemplate template = saveRegisteredTemplate(registeredTemplate);
        nifiFlowCache.updateRegisteredTemplate(template);
        //notify audit of the change

        FeedManagerTemplate.State state = FeedManagerTemplate.State.valueOf(template.getState());
        FeedManagerTemplate.ID id = templateProvider.resolveId(registeredTemplate.getId());
        MetadataChange.ChangeType changeType = isNew ? MetadataChange.ChangeType.CREATE : MetadataChange.ChangeType.UPDATE;
        notifyTemplateStateChange(registeredTemplate,id,state,changeType);
        return template;
    }


    public boolean deleteRegisteredTemplate(final String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            return templateProvider.deleteTemplate(domainId);
        });

    }


    public RegisteredTemplate getRegisteredTemplate(final String templateId) {
       return registeredTemplateService.getRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateId(templateId));

    }





    private FeedManagerTemplate ensureNifiTemplateId(FeedManagerTemplate feedManagerTemplate) {
        if (feedManagerTemplate.getNifiTemplateId() == null) {
            String nifiTemplateId = nifiTemplateIdForTemplateName(feedManagerTemplate.getName());
            feedManagerTemplate.setNifiTemplateId(nifiTemplateId);
        }
        return feedManagerTemplate;
    }

    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            List<RegisteredTemplate> registeredTemplates = null;
            List<FeedManagerTemplate> templates = templateProvider.findAll();
            if (templates != null) {
                templates.stream().filter(t -> t.getNifiTemplateId() == null).forEach(t -> {
                    ensureNifiTemplateId(t);
                });

                registeredTemplates = templateModelTransform.domainToRegisteredTemplateWithFeedNames(templates);

            }
            return registeredTemplates;
        });

    }

    @Override
    public RegisteredTemplate enableTemplate(String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_TEMPLATES);
            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            if (domainId != null) {
                FeedManagerTemplate template = templateProvider.enable(domainId);
                if (template != null) {
                    return templateModelTransform.domainToRegisteredTemplate(template);
                }
            }
            return null;
        });
    }

    @Override
    public RegisteredTemplate disableTemplate(String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ADMIN_TEMPLATES);
            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            if (domainId != null) {
                FeedManagerTemplate template = templateProvider.disable(domainId);
                if (template != null) {
                    return templateModelTransform.domainToRegisteredTemplate(template);
                }
            }
            return null;
        });
    }

    /**
     * update audit information for template changes
     * @param template the template
     * @param templateId the template id
     * @param state the new state
     * @param changeType the type of change
     */
    private void notifyTemplateStateChange(RegisteredTemplate template, FeedManagerTemplate.ID templateId,  FeedManagerTemplate.State state, MetadataChange.ChangeType changeType) {
        final Principal principal = SecurityContextHolder.getContext().getAuthentication() != null
                                    ? SecurityContextHolder.getContext().getAuthentication()
                                    : null;
        TemplateChange change = new TemplateChange(changeType, template != null ? template.getTemplateName() : "", templateId, state);
        TemplateChangeEvent event = new TemplateChangeEvent(change, DateTime.now(), principal);
        metadataEventService.notify(event);
    }







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
        RegisteredTemplate template = registeredTemplateService.getRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateId(templateId));
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
            nifiTemplate = registeredTemplateUtil.ensureNifiTemplate(registeredTemplate);
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

            RegisteredTemplate t = registeredTemplateService.getRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(template.getId()).includeAllProperties(true).build());
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
