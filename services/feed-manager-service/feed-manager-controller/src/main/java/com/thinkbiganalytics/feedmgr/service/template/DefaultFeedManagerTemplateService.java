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

import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCacheImpl;
import com.thinkbiganalytics.feedmgr.rest.model.PortDTOWithGroupInfo;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChange;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChangeEvent;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 */
public class DefaultFeedManagerTemplateService implements FeedManagerTemplateService {

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

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    private TemplateConnectionUtil templateConnectionUtil;



    @Inject
    private SecurityService securityService;

    protected RegisteredTemplate saveRegisteredTemplate(final RegisteredTemplate registeredTemplate) {
        return registeredTemplateService.saveRegisteredTemplate(registeredTemplate);

    }

    /**
     * pass in the Template Ids in Order
     */
    public void orderTemplates(List<String> orderedTemplateIds, Set<String> exclude) {
        registeredTemplateService.orderTemplates(orderedTemplateIds, exclude);


    }


    @Override
    public List<RegisteredTemplate.Processor> getRegisteredTemplateProcessors(String templateId, boolean includeReusableProcessors) {
        List<RegisteredTemplate.Processor> processorProperties = new ArrayList<>();

        RegisteredTemplate
            template =
            registeredTemplateService.findRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(templateId).nifiTemplateId(templateId).includeAllProperties(true).build());
        if (template != null) {
            template.initializeProcessors();
            processorProperties.addAll(template.getInputProcessors());
            processorProperties.addAll(template.getNonInputProcessors());

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
                        .map(reusableTemplateConnectionInfo -> reusableTemplateInputPorts.get(reusableTemplateConnectionInfo.getReusableTemplateInputPortName()).getId())
                        .collect(Collectors.toList());

                List<RegisteredTemplate.Processor> reusableProcessors = getReusableTemplateProcessorsForInputPorts(matchingPortIds);
                processorProperties.addAll(reusableProcessors);
            }
        }
        return processorProperties;

    }

    /**
     * Ensures that the {@code RegisteredTemplate#inputProcessors} list is populated not only with the processors which were defined as having user inputs, but also those that done require any input
     */
    public void ensureRegisteredTemplateInputProcessors(RegisteredTemplate registeredTemplate) {
        registeredTemplateService.ensureRegisteredTemplateInputProcessors(registeredTemplate);

    }

    /**
     * Updates the Streaming flag on the feeds related to this template, when the template is updated and the streamign flag is changed
     * @param templateName the name of the template
     * @param previousTemplate the previous template
     */
    private void notifyOperationsManagerOfStreamingUpdate(String templateName,RegisteredTemplate previousTemplate) {

            RegisteredTemplate
                newTemplate =
                registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestAccessAsServiceAccountByTemplateName(templateName));
            if(newTemplate != null) {
                //notify if this is the first template update, or if we have changed the streaming flag
                boolean notifyOfStreamingChange = (previousTemplate == null || previousTemplate.isStream() != newTemplate.isStream());
                boolean notifyOfTimeChange = (previousTemplate == null || previousTemplate.getTimeBetweenStartingBatchJobs() != newTemplate.getTimeBetweenStartingBatchJobs());

                if (notifyOfStreamingChange) {
                    Set<String> feedNames = newTemplate.getFeedNames();
                    if(feedNames != null && !feedNames.isEmpty()) {
                        metadataAccess.commit(() -> opsManagerFeedProvider.updateStreamingFlag(feedNames, newTemplate.isStream()),MetadataAccess.SERVICE);
                    }
                }

                if (notifyOfTimeChange) {
                    Set<String> feedNames = newTemplate.getFeedNames();
                    if(feedNames != null && !feedNames.isEmpty()) {
                        metadataAccess.commit(() -> opsManagerFeedProvider.updateTimeBetweenBatchJobs(feedNames, newTemplate.getTimeBetweenStartingBatchJobs()),MetadataAccess.SERVICE);
                    }
                }
            }

    }

    @Override
    public RegisteredTemplate registerTemplate(RegisteredTemplate registeredTemplate) {
        boolean isNew = StringUtils.isBlank(registeredTemplate.getId());
        //detect if we changed the stream setting
        RegisteredTemplate previousTemplate = null;
        if(!isNew){
            //run as service account
            previousTemplate = registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestAccessAsServiceAccountByTemplateName(registeredTemplate.getTemplateName()));
        }

        RegisteredTemplate template = saveRegisteredTemplate(registeredTemplate);
        if (template.isUpdated()) {
            nifiFlowCache.updateRegisteredTemplate(template,true);
            //update ops manager for feed.isStream
            notifyOperationsManagerOfStreamingUpdate(template.getTemplateName(),previousTemplate);

            //only allow update to the Access control if the user has the CHANGE_PERMS permission on this template entity
            if (accessController.isEntityAccessControlled() && template.hasAction(TemplateAccessControl.CHANGE_PERMS.getSystemName())) {
                //update the access control
                registeredTemplate.toRoleMembershipChangeList().stream().forEach(roleMembershipChange -> securityService.changeTemplateRoleMemberships(template.getId(), roleMembershipChange));
            }
            //notify audit of the change

            FeedManagerTemplate.State state = FeedManagerTemplate.State.valueOf(template.getState());
            FeedManagerTemplate.ID id = templateProvider.resolveId(registeredTemplate.getId());
            MetadataChange.ChangeType changeType = isNew ? MetadataChange.ChangeType.CREATE : MetadataChange.ChangeType.UPDATE;
            notifyTemplateStateChange(registeredTemplate, id, state, changeType);
        }
        return template;
    }


    public boolean deleteRegisteredTemplate(final String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_TEMPLATES);

            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            return templateProvider.deleteTemplate(domainId);
        });

    }

    public RegisteredTemplate findRegisteredTemplateByName(final String templateName){
       return registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(templateName));
    }

    public RegisteredTemplate getRegisteredTemplate(final String templateId) {
        return registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateId(templateId));

    }


    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        return registeredTemplateService.getRegisteredTemplates();
    }

    @Override
    public RegisteredTemplate enableTemplate(String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_TEMPLATES);
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
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_TEMPLATES);
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
     *
     * @param template   the template
     * @param templateId the template id
     * @param state      the new state
     * @param changeType the type of change
     */
    private void notifyTemplateStateChange(RegisteredTemplate template, FeedManagerTemplate.ID templateId, FeedManagerTemplate.State state, MetadataChange.ChangeType changeType) {
        final Principal principal = SecurityContextHolder.getContext().getAuthentication() != null
                                    ? SecurityContextHolder.getContext().getAuthentication()
                                    : null;
        TemplateChange change = new TemplateChange(changeType, template != null ? template.getTemplateName() : "", templateId, state);
        TemplateChangeEvent event = new TemplateChangeEvent(change, DateTime.now(), principal);
        metadataEventService.notify(event);
    }


    /**
     * Return properties registered for a template
     *
     * @param templateId a RegisteredTemplate id
     * @return the properties registered for the template
     */
    public List<NifiProperty> getTemplateProperties(String templateId) {
        List<NifiProperty> list = new ArrayList<>();
        RegisteredTemplate template = registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateId(templateId));
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
        return registeredTemplateService.getInputProcessorsInNifTemplate(registeredTemplate);
    }

    /**
     * Return the input processors (processors without any incoming connections) in a NiFi template object
     *
     * @param nifiTemplate the NiFi template
     * @return the input processors (processors without any incoming connections) in a NiFi template object
     */
    public List<RegisteredTemplate.Processor> getInputProcessorsInNifTemplate(TemplateDTO nifiTemplate) {
        return registeredTemplateService.getInputProcessorsInNifTemplate(nifiTemplate);
    }


    /**
     * @return all input ports under the {@link TemplateCreationHelper#REUSABLE_TEMPLATES_PROCESS_GROUP_NAME} process group
     */
    public Set<PortDTO> getReusableFeedInputPorts() {
        Set<PortDTO> ports = new HashSet<>();
        String reusableProcessGroupId = templateConnectionUtil.getReusableTemplateProcessGroupId();
        if(reusableProcessGroupId != null) {
            ProcessGroupFlowDTO processGroup = nifiRestClient.getNiFiRestClient().processGroups().flow(reusableProcessGroupId);
            if (processGroup != null) {

                //fetch the ports
                Set<PortDTO> inputPortsEntity = processGroup.getFlow().getInputPorts().stream()
                    .map(portEntity -> {
                        PortDTOWithGroupInfo portDTOWithGroupInfo = new PortDTOWithGroupInfo(portEntity.getComponent());
                        //find the connection destination processgroup id

                        List<ConnectionDTO> connList = processGroup.getFlow().getConnections().stream()
                            .map(connectionEntity -> connectionEntity.getComponent()).collect(Collectors.toList());

                        Optional<ConnectionDTO> connection = processGroup.getFlow().getConnections().stream()
                            .map(connectionEntity -> connectionEntity.getComponent())
                            .filter(connectionDTO -> connectionDTO.getSource().getId().equals(portEntity.getComponent().getId()))
                            .findFirst();

                        Optional<ProcessGroupDTO> pg1 = processGroup.getFlow().getProcessGroups().stream()
                            .map(processGroupEntity -> processGroupEntity.getComponent())
                            .filter(processGroupDTO -> processGroupDTO.getId().equals(connection.get().getDestination().getGroupId()))
                            .findFirst();

                        Optional<ProcessGroupDTO> destinationGroup = processGroup.getFlow().getConnections().stream()
                            .map(connectionEntity -> connectionEntity.getComponent())
                            .filter(connectionDTO -> connectionDTO.getSource().getId().equals(portEntity.getComponent().getId()))
                            .flatMap(connectionDTO -> processGroup.getFlow().getProcessGroups().stream().map(processGroupEntity -> processGroupEntity.getComponent())
                                .filter(processGroupDTO -> processGroupDTO.getId().equals(connectionDTO.getDestination().getGroupId())))
                            .findFirst();
                        if (destinationGroup.isPresent()) {
                            portDTOWithGroupInfo.setDestinationProcessGroupName(destinationGroup.get().getName());
                        }

                        return portDTOWithGroupInfo;
                    }).collect(Collectors.toSet());
                if (inputPortsEntity != null && !inputPortsEntity.isEmpty()) {
                    ports.addAll(inputPortsEntity);
                }
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
        List<RegisteredTemplate.Processor>
            processorProperties =
            processorDTOs.stream().map(processorDTO -> registeredTemplateUtil.toRegisteredTemplateProcessor(processorDTO, true)).collect(Collectors.toList());
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

        List<RegisteredTemplate.Processor>
            processorProperties =
            processorDTOs.stream().map(processorDTO -> registeredTemplateUtil.toRegisteredTemplateProcessor(processorDTO, true)).collect(Collectors.toList());
        return processorProperties;
    }


}
