package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.feedmgr.ConfigurationPropertyReplacer;
import com.thinkbiganalytics.nifi.feedmgr.NifiEnvironmentProperties;
import com.thinkbiganalytics.nifi.feedmgr.ReusableTemplateCreationCallback;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.feedmgr.TemplateInstanceCreator;
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptor;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitorCache;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.AboutDTO;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;
import org.apache.nifi.web.api.dto.search.ComponentSearchResultDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;

@Deprecated
public class LegacyNifiRestClient implements NiFiFlowVisitorClient {

    private static final Logger log = LoggerFactory.getLogger(LegacyNifiRestClient.class);

    @Inject
    private NiFiRestClient client;

    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;

    @Inject
    private NiFiObjectCache niFiObjectCache;



    /**
     * Gets Template data, either a quick view or including all its content
     */
    @Deprecated
    public Set<TemplateDTO> getTemplates(boolean includeFlow) {
        return client.templates().findAll();
    }

    @Deprecated
    public boolean deleteTemplate(String templateId) {
        return client.templates().delete(templateId);
    }

    @Deprecated
    public TemplateDTO importTemplate(String templateXml) {
        return client.templates().create(null, templateXml);
    }

    @Deprecated
    public TemplateDTO importTemplate(String templateName, String templateXml) {
        return client.templates().create(templateName, templateXml);
    }

    /**
     * return the Template as an XML string
     */
    @Deprecated
    public String getTemplateXml(String templateId) throws NifiComponentNotFoundException {
        return client.templates().download(templateId)
            .orElseThrow(() -> new NifiComponentNotFoundException(templateId, NifiConstants.NIFI_COMPONENT_TYPE.TEMPLATE, null));
    }


    /**
     * Return a template, populated along with its Flow snippet
     */
    @Deprecated
    public TemplateDTO getTemplateById(String templateId) throws NifiComponentNotFoundException {
        return client.templates().findById(templateId)
            .orElseThrow(() -> new NifiComponentNotFoundException(templateId, NifiConstants.NIFI_COMPONENT_TYPE.TEMPLATE, null));
    }

    /**
     * Returns a Map of the Template Name and the Template XML that has a matching Input Port in the template.
     */
    @Deprecated
    public Map<String, String> getTemplatesAsXmlMatchingInputPortName(final String inputPortName) {
        final Set<TemplateDTO> templates = client.templates().findByInputPortName(inputPortName);
        final Map<String, String> result = new HashMap<>(templates.size());
        for (TemplateDTO template : templates) {
            client.templates().download(template.getId())
                .ifPresent(xml -> result.put(template.getId(), xml));
        }
        return result;
    }

    public Map<String, String> getTemplatesAsXmlMatchingInputPortName(final String inputPortName, String templateName) {
         Set<TemplateDTO> templates = client.templates().findByInputPortName(inputPortName);

        if(templates.size() >1 && StringUtils.isBlank(templateName))
        {
            templates = templates.stream().filter(templateDTO -> templateDTO.getName().equalsIgnoreCase(templateName)).collect(Collectors.toSet());
        }

        final Map<String, String> result = new HashMap<>(templates.size());


        for (TemplateDTO template : templates) {

            client.templates().download(template.getId())
                .ifPresent(xml -> result.put(template.getId(), xml));
        }
        return result;
    }

    /**
     * return a template by Name, populated with its Flow snippet If not found it returns null
     */
    @Deprecated
    public TemplateDTO getTemplateByName(String templateName) {
        return client.templates().findByName(templateName).orElse(null);
    }

    @Deprecated
    public FlowSnippetDTO instantiateFlowFromTemplate(String processGroupId, String templateId) throws NifiComponentNotFoundException {
        return client.processGroups().instantiateTemplate(processGroupId, templateId);
    }


    @Deprecated
    public NifiProcessGroup createNewTemplateInstance(String templateId, Map<String, Object> staticConfigProperties, boolean createReusableFlow, ReusableTemplateCreationCallback creationCallback) {
        TemplateInstanceCreator creator = new TemplateInstanceCreator(this, templateId, staticConfigProperties, createReusableFlow, creationCallback,null);
        NifiProcessGroup group = creator.createTemplate();
        return group;
    }
    @Deprecated
    public NifiProcessGroup createNewTemplateInstance(String templateId, List<NifiProperty> templateProperties, Map<String, Object> staticConfigProperties, boolean createReusableFlow,
                                                      ReusableTemplateCreationCallback creationCallback) {
     return createNewTemplateInstance(templateId,templateProperties,staticConfigProperties,createReusableFlow,creationCallback,null);
    }

    public NifiProcessGroup createNewTemplateInstance(String templateId, List<NifiProperty> templateProperties, Map<String, Object> staticConfigProperties, boolean createReusableFlow,
                                                      ReusableTemplateCreationCallback creationCallback, String versionIdentifier) {
        TemplateInstanceCreator creator = new TemplateInstanceCreator(this, templateId, templateProperties, staticConfigProperties, createReusableFlow, creationCallback,versionIdentifier);
        NifiProcessGroup group = creator.createTemplate();

        return group;
    }

    public void markConnectionPortsAsRunning(ProcessGroupDTO entity) {
        //1 startAll
        try {
            startAll(entity.getId(), entity.getParentGroupId());
        } catch (NifiClientRuntimeException e) {
            log.error("Error trying to mark connection ports Running for {}", entity.getName());
        }

        Set<PortDTO> ports = null;
        try {
            ports = getPortsForProcessGroup(entity.getParentGroupId());
        } catch (NifiClientRuntimeException e) {
            log.error("Error getPortsForProcessGroup {}", entity.getName());
        }
        if (ports != null && !ports.isEmpty()) {
            for (PortDTO port : ports) {
                port.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                if (port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name())) {
                    try {
                        startInputPort(entity.getParentGroupId(), port.getId());
                    } catch (NifiClientRuntimeException e) {
                        log.error("Error starting Input Port {} for process group {}", port.getName(), entity.getName());
                    }
                } else if (port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name())) {
                    try {
                        startOutputPort(entity.getParentGroupId(), port.getId());
                    } catch (NifiClientRuntimeException e) {
                        log.error("Error starting Output Port {} for process group {}", port.getName(), entity.getName());
                    }
                }
            }

        }

    }

    public void startProcessGroupAndParentInputPorts(ProcessGroupDTO entity) {
        //1 startAll
        try {
            startAll(entity.getId(), entity.getParentGroupId());
        } catch (NifiClientRuntimeException e) {
            log.error("Error trying to mark connection ports Running for {}", entity.getName());
        }

        Set<PortDTO> ports = null;
        try {
            ports = getPortsForProcessGroup(entity.getParentGroupId());
        } catch (NifiClientRuntimeException e) {
            log.error("Error getPortsForProcessGroup {}", entity.getName());
        }
        if (ports != null && !ports.isEmpty()) {
            Map<String,PortDTO> portsById = ports.stream().collect(Collectors.toMap(port -> port.getId(), port->port));
            ProcessGroupFlowDTO flow = getNiFiRestClient().processGroups().flow(entity.getParentGroupId());
            if(flow != null) {
                List<PortDTO> matchingParentGroupPorts = flow.getFlow().getConnections().stream()
                    .map(connectionEntity -> connectionEntity.getComponent())
                    .filter(connectionDTO -> connectionDTO.getDestination().getGroupId().equalsIgnoreCase(entity.getId()))
                    .filter(connectionDTO -> portsById.containsKey(connectionDTO.getSource().getId()))
                    .map(connectionDTO -> portsById.get(connectionDTO.getSource().getId()))
                    .collect(Collectors.toList());

                for (PortDTO port : matchingParentGroupPorts) {
                    port.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                    if (port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name())) {
                        try {
                            startInputPort(entity.getParentGroupId(), port.getId());
                        } catch (NifiClientRuntimeException e) {
                            log.error("Error starting Input Port {} for process group {}", port.getName(), entity.getName());
                        }
                    } else if (port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name())) {
                        try {
                            startOutputPort(entity.getParentGroupId(), port.getId());
                        } catch (NifiClientRuntimeException e) {
                            log.error("Error starting Output Port {} for process group {}", port.getName(), entity.getName());
                        }
                    }
                }
            }

        }

    }

    /**
     * Expose all Properties for a given Template as parameters for external use
     */
    public List<NifiProperty> getPropertiesForTemplate(String templateId, boolean includePropertyDescriptors) {
        TemplateDTO dto = getTemplateById(templateId);
        return getPropertiesForTemplate(dto, includePropertyDescriptors);
    }

    /**
     * Expose all Properties for a given Template as parameters for external use
     *
     * @param dto                        the Template to parse
     * @param includePropertyDescriptors true to include propertyDescriptor details on each property, false to just include the property key
     * @return all the properties included in this template
     */
    public List<NifiProperty> getPropertiesForTemplate(TemplateDTO dto, boolean includePropertyDescriptors) {
        ProcessGroupDTO rootProcessGroup = getProcessGroup("root", false, false);
        return getPropertiesForTemplate(rootProcessGroup, dto, includePropertyDescriptors);
    }


    /**
     * @param parentProcessGroup         the parent group for which this template will reside
     * @param dto                        the template to parse
     * @param includePropertyDescriptors true to include propertyDescriptor details on each property, false to just include the property key
     * @return all the properties included in this template
     */
    public List<NifiProperty> getPropertiesForTemplate(ProcessGroupDTO parentProcessGroup, TemplateDTO dto, boolean includePropertyDescriptors) {
        if (includePropertyDescriptors) {
            TemplateCreationHelper helper = new TemplateCreationHelper(this, niFiObjectCache);
            ProcessGroupDTO groupDTO = helper.createTemporaryTemplateFlow(dto.getId());
            dto.setSnippet(groupDTO.getContents());
        }
        return NifiPropertyUtil.getPropertiesForTemplate(parentProcessGroup, dto, propertyDescriptorTransform);
    }


    public Set<PortDTO> getPortsForTemplate(String templateId) throws NifiComponentNotFoundException {
        TemplateDTO dto = getTemplateById(templateId);
        return getPortsForTemplate(dto);
    }

    public Set<PortDTO> getPortsForTemplate(TemplateDTO templateDTO) throws NifiComponentNotFoundException {
        Set<PortDTO> ports = new HashSet<>();
        if (templateDTO != null && templateDTO.getSnippet() != null) {
            Set<PortDTO> inputPorts = templateDTO.getSnippet().getInputPorts();
            if (inputPorts != null) {
                ports.addAll(inputPorts);
            }
            Set<PortDTO> outputPorts = templateDTO.getSnippet().getOutputPorts();
            if (outputPorts != null) {
                ports.addAll(outputPorts);
            }
        }
        return ports;
    }

    public Set<PortDTO> getPortsForProcessGroup(String processGroupId) throws NifiComponentNotFoundException {
        Set<PortDTO> ports = new HashSet<>();
        ProcessGroupDTO processGroupEntity = getProcessGroup(processGroupId, false, true);
        Set<PortDTO> inputPorts = processGroupEntity.getContents().getInputPorts();
        if (inputPorts != null) {
            ports.addAll(inputPorts);
        }
        Set<PortDTO> outputPorts = processGroupEntity.getContents().getOutputPorts();
        if (outputPorts != null) {
            ports.addAll(outputPorts);
        }
        return ports;
    }

    /**
     * Expose all Properties for a given Template as parameters for external use If template is not found it will return an Empty ArrayList()
     */
    public List<NifiProperty> getPropertiesForTemplateByName(String templateName) {
        TemplateDTO dto = getTemplateByName(templateName);
        if (dto != null) {
            //populate the snippet
            dto = getTemplateById(dto.getId());
        }
        ProcessGroupDTO rootProcessGroup = getProcessGroup("root", false, false);
        return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup, dto, propertyDescriptorTransform);
    }

    /**
     * Returns an Empty ArrayList of nothing is found
     */
    public List<NifiProperty> getAllProperties() throws NifiComponentNotFoundException {
        ProcessGroupDTO root = getRootProcessGroup();
        return NifiPropertyUtil.getProperties(root, propertyDescriptorTransform);
    }


    public List<NifiProperty> getPropertiesForProcessGroup(String processGroupId) throws NifiComponentNotFoundException {
        ProcessGroupDTO processGroup = getProcessGroup(processGroupId, true, true);
        return NifiPropertyUtil.getProperties(processGroup, propertyDescriptorTransform);
    }

    public ProcessGroupDTO createProcessGroup(String name) {

        return createProcessGroup("root", name);
    }

    @Deprecated
    public ProcessGroupDTO createProcessGroup(String parentGroupId, String name) throws NifiComponentNotFoundException {
        return client.processGroups().create(parentGroupId, name);
    }

    @Deprecated
    public ProcessGroupDTO markProcessorGroupAsRunning(ProcessGroupDTO groupDTO) {
        client.processGroups().schedule(groupDTO.getId(), groupDTO.getParentGroupId(), NiFiComponentState.RUNNING);
        return client.processGroups().findById(groupDTO.getId(), false, false).get();
    }

    public void stopProcessor(ProcessorDTO processorDTO) {
        if (NifiProcessUtil.PROCESS_STATE.RUNNING.name().equalsIgnoreCase(processorDTO.getState())) {
            stopProcessor(processorDTO.getParentGroupId(), processorDTO.getId());
        }
    }

    public void stopProcessor(String processorGroupId, String processorId) {
        ProcessorDTO dto = new ProcessorDTO();
        dto.setId(processorId);
        dto.setParentGroupId(processorGroupId);
        dto.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
        updateProcessor(dto);
    }

    public void startProcessor(String processorGroupId, String processorId) {
        ProcessorDTO dto = new ProcessorDTO();
        dto.setId(processorId);
        dto.setParentGroupId(processorGroupId);
        dto.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
        updateProcessor(dto);
    }

    public void stopInputs(ProcessGroupDTO groupDTO) {
        List<ProcessorDTO> inputs = NifiProcessUtil.getInputProcessors(groupDTO);
        if (inputs != null) {
            for (ProcessorDTO input : inputs) {
                stopProcessor(input);
            }
        }
        Set<PortDTO> inputPorts = getInputPorts(groupDTO.getId());
        if (inputPorts != null) {
            for (PortDTO port : inputPorts) {
                stopInputPort(groupDTO.getId(), port.getId());
            }
        }
    }

    public ProcessGroupDTO stopInputs(String processGroupId) {
        ProcessGroupDTO entity = getProcessGroup(processGroupId, false, true);
        if (entity != null) {
            stopInputs(entity);
            return entity;
        }
        return null;
    }

    @Deprecated
    public ProcessGroupDTO stopAllProcessors(String processGroupId, String parentProcessGroupId) throws NifiClientRuntimeException {
        client.processGroups().schedule(processGroupId, parentProcessGroupId, NiFiComponentState.STOPPED);
        return client.processGroups().findById(processGroupId, false, false).get();
    }

    @Deprecated
    public ProcessGroupDTO startAll(String processGroupId, String parentProcessGroupId) throws NifiClientRuntimeException {
        client.processGroups().schedule(processGroupId, parentProcessGroupId, NiFiComponentState.RUNNING);
        return client.processGroups().findById(processGroupId, false, false).get();
    }

    public PortDTO stopInputPort(String groupId, String portId) throws NifiClientRuntimeException {
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
        return client.ports().updateInputPort(groupId, portDTO);
    }

    public PortDTO stopOutputPort(String groupId, String portId) throws NifiClientRuntimeException {
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
        return client.ports().updateOutputPort(groupId, portDTO);
    }

    public PortDTO startInputPort(String groupId, String portId) throws NifiClientRuntimeException {
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
        return client.ports().updateInputPort(groupId, portDTO);
    }

    public PortDTO startOutputPort(String groupId, String portId) throws NifiClientRuntimeException {
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
        return client.ports().updateOutputPort(groupId, portDTO);
    }

    @Deprecated
    public ProcessGroupDTO deleteProcessGroup(ProcessGroupDTO groupDTO) throws NifiClientRuntimeException {
        return client.processGroups().delete(groupDTO)
            .orElseThrow(() -> new NifiClientRuntimeException("Unable to delete Process Group " + groupDTO.getName()));
    }

    public List<ProcessGroupDTO> deleteChildProcessGroups(String processGroupId) throws NifiClientRuntimeException {
        List<ProcessGroupDTO> deletedEntities = new ArrayList<>();
        ProcessGroupDTO entity = getProcessGroup(processGroupId, true, true);
        if (entity != null && entity.getContents().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : entity.getContents().getProcessGroups()) {
                deletedEntities.add(deleteProcessGroup(groupDTO));
            }
        }
        return deletedEntities;

    }

    /**
     * Deletes the specified process group and any matching connections.
     *
     * @param processGroup the process group to be deleted
     * @param connections  the list of all connections from the parent process group
     * @return the deleted process group
     * @throws NifiClientRuntimeException     if the process group could not be deleted
     * @throws NifiComponentNotFoundException if the process group does not exist
     */
    public ProcessGroupDTO deleteProcessGroupAndConnections(@Nonnull final ProcessGroupDTO processGroup, @Nonnull final Set<ConnectionDTO> connections) {
        if (!connections.isEmpty()) {
            disableAllInputProcessors(processGroup.getId());
            stopInputs(processGroup.getId());

            for (ConnectionDTO connection : NifiConnectionUtil.findConnectionsMatchingDestinationGroupId(connections, processGroup.getId())) {
                String type = connection.getSource().getType();
                if (NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)) {
                    stopInputPort(connection.getSource().getGroupId(), connection.getSource().getId());
                    deleteConnection(connection, false);
                }
            }
            for (ConnectionDTO connection : NifiConnectionUtil.findConnectionsMatchingSourceGroupId(connections, processGroup.getId())) {
                deleteConnection(connection, false);
            }
        }

        return deleteProcessGroup(processGroup);
    }

    public void deleteConnection(ConnectionDTO connection, boolean emptyQueue) {

        try {
            if (emptyQueue) {
                //empty connection Queue
                client.connections().deleteQueue(connection.getParentGroupId(), connection.getId());
            }
            //before deleting the connection we need to stop the source
            log.info("Before deleting the connection we need to stop Sources and destinations.");
            log.info("Stopping Source {} ({}) for connection {} ", connection.getSource().getId(), connection.getSource().getType(), connection.getId());
            String type = connection.getSource().getType();
            try {
                if (NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(type)) {
                    stopOutputPort(connection.getSource().getGroupId(), connection.getSource().getId());
                } else if (NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)) {
                    stopInputPort(connection.getSource().getGroupId(), connection.getSource().getId());
                } else if (NifiConstants.NIFI_PROCESSOR_TYPE.PROCESSOR.name().equalsIgnoreCase(type)) {
                    stopProcessor(connection.getSource().getGroupId(), connection.getSource().getId());
                }
            } catch (ClientErrorException e) {
                //swallow the  stop  exception.  it is ok at this point because Nifi might throw the exception if its still in the process of stopping...
                //TODO LOG IT
            }
            type = connection.getDestination().getType();
            log.info("Stopping Destination {} ({}) for connection {} ", connection.getDestination().getId(), connection.getDestination().getType(), connection.getId());
            try {
                if (NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(type)) {
                    stopOutputPort(connection.getDestination().getGroupId(), connection.getDestination().getId());
                } else if (NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)) {
                    stopInputPort(connection.getDestination().getGroupId(), connection.getDestination().getId());
                } else if (NifiConstants.NIFI_PROCESSOR_TYPE.PROCESSOR.name().equalsIgnoreCase(type)) {
                    stopProcessor(connection.getDestination().getGroupId(), connection.getDestination().getId());
                }
            } catch (ClientErrorException e) {
                //swallow the  stop  exception.  it is ok at this point because Nifi might throw the exception if its still in the process of stopping...
                //TODO LOG IT
            }
            log.info("Deleting the connection {} ", connection.getId());
            client.connections().delete(connection.getParentGroupId(), connection.getId());
            try {
                type = connection.getSource().getType();
                //now start the inputs again
                log.info("After Delete... Starting source again {} ({}) ", connection.getSource().getId(), connection.getSource().getType());
                if (NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(type)) {
                    startOutputPort(connection.getSource().getGroupId(), connection.getSource().getId());
                } else if (NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)) {
                    startInputPort(connection.getSource().getGroupId(), connection.getSource().getId());
                } else if (NifiConstants.NIFI_PROCESSOR_TYPE.PROCESSOR.name().equalsIgnoreCase(type)) {
                    startProcessor(connection.getSource().getGroupId(), connection.getSource().getId());
                }
            } catch (ClientErrorException e) {
                //swallow the  start  exception.  it is ok at this point because Nifi might throw the exception if its still in the process of starting...
                //TODO LOG IT
            }

            try {
                type = connection.getDestination().getType();
                //now start the inputs again
                log.info("After Delete... Starting dest again {} ({}) ", connection.getDestination().getId(), connection.getDestination().getType());
                if (NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(type)) {
                    startOutputPort(connection.getDestination().getGroupId(), connection.getDestination().getId());
                } else if (NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)) {
                    startInputPort(connection.getDestination().getGroupId(), connection.getDestination().getId());
                } else if (NifiConstants.NIFI_PROCESSOR_TYPE.PROCESSOR.name().equalsIgnoreCase(type)) {
                    startProcessor(connection.getDestination().getGroupId(), connection.getDestination().getId());
                }
            } catch (ClientErrorException e) {
                //swallow the  start  exception.  it is ok at this point because Nifi might throw the exception if its still in the process of starting...
                //TODO LOG IT
            }


        } catch (ClientErrorException e) {
            //swallow the  exception
            //TODO LOG IT
        }

    }

    @Deprecated
    public void deleteControllerService(String controllerServiceId) throws NifiClientRuntimeException {
        client.controllerServices().delete(controllerServiceId)
            .orElseThrow(() -> new NifiComponentNotFoundException(controllerServiceId, NifiConstants.NIFI_COMPONENT_TYPE.CONTROLLER_SERVICE, null));
    }

    public void deleteControllerServices(Collection<ControllerServiceDTO> services) throws NifiClientRuntimeException {
        //http://localhost:8079/nifi-api/controller/controller-services/node/3c475f44-b038-4cb0-be51-65948de72764?version=1210&clientId=86af0022-9ba6-40b9-ad73-6d757b6f8d25
        Set<String> unableToDelete = new HashSet<>();
        for (ControllerServiceDTO dto : services) {
            try {
                deleteControllerService(dto.getId());
            } catch (Exception e) {
                if (!(e instanceof NifiComponentNotFoundException)) {
                    unableToDelete.add(dto.getId());
                }
            }
        }
        if (!unableToDelete.isEmpty()) {
            throw new NifiClientRuntimeException("Unable to Delete the following Services " + unableToDelete);
        }
    }

    //get a process and its connections
    //http://localhost:8079/nifi-api/controller/process-groups/e40bfbb2-4377-43e6-b6eb-369e8f39925d/connections
    @Deprecated
    public Set<ConnectionDTO> getProcessGroupConnections(String processGroupId) throws NifiComponentNotFoundException {
        return client.processGroups().getConnections(processGroupId);
    }

    public void removeConnectionsToProcessGroup(String parentProcessGroupId, final String processGroupId) {
        Set<ConnectionDTO> connectionsEntity = getProcessGroupConnections(parentProcessGroupId);
        if (connectionsEntity != null) {
            List<ConnectionDTO> connections = Lists.newArrayList(Iterables.filter(connectionsEntity, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connectionDTO) {
                    return connectionDTO.getDestination().getGroupId().equals(processGroupId);
                }
            }));
            if (connections != null) {
                for (ConnectionDTO connectionDTO : connections) {
                    deleteConnection(connectionDTO, true);
                }
            }
        }

    }

    public void removeConnectionsFromProcessGroup(String parentProcessGroupId, final String processGroupId) {
        Set<ConnectionDTO> connectionsEntity = getProcessGroupConnections(parentProcessGroupId);
        if (connectionsEntity != null) {
                connectionsEntity.stream()
                    .filter(connectionDTO -> connectionDTO.getSource().getGroupId().equals(processGroupId))
                    .forEach(connectionDTO ->   deleteConnection(connectionDTO, true));

        }

    }

    public boolean removeProcessGroup(String processGroupId, String parentProcessGroupId) {
        //Now try to remove the processgroup associated with this template import
        ProcessGroupDTO e = null;
        try {
            e = getProcessGroup(processGroupId, false, false);
        } catch (NifiComponentNotFoundException notFound) {
            //if its not there then we already cleaned up :)
        }
        if (e != null) {
            try {
                stopAllProcessors(processGroupId, parentProcessGroupId);
                //remove connections
                removeConnectionsToProcessGroup(parentProcessGroupId, processGroupId);

                //remove output port connections
                removeConnectionsFromProcessGroup(parentProcessGroupId,processGroupId);
            } catch (Exception e2) {
                //this is ok. we are cleaning up the template so if an error occurs due to no connections it is fine since we ultimately want to remove this temp template.
            }
            try {
                deleteProcessGroup(e);  //importTemplate.getTemplateResults().getProcessGroupEntity()
                return true;
            } catch (NifiComponentNotFoundException nfe) {
                //this is ok
            }
        }
        return false;
    }




    @Deprecated
    public ProcessGroupDTO getProcessGroup(String processGroupId, boolean recursive, boolean verbose) throws NifiComponentNotFoundException {
        return client.processGroups().findById(processGroupId, recursive, verbose)
            .orElseThrow(() -> new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, null));
    }

    /**
     * if the parentGroup is found but it cannot find the group by Name then it will return null
     */
    @Nullable
    public ProcessGroupDTO getProcessGroupByName(@Nonnull final String parentGroupId, @Nonnull final String groupName) throws NifiComponentNotFoundException {
        return getProcessGroupByName(parentGroupId, groupName, false, false);
    }

    /**
     * Gets the child process group with the specified name, optionally including all sub-components.
     *
     * @param parentGroupId the id of the parent process group
     * @param groupName     the name of the process group to find
     * @param recursive     {@code true} to include all encapsulated components, or {@code false} for just the immediate children
     * @param verbose       {@code true} to include any encapsulated components, or {@code false} for just details about the process group
     * @return the child process group, or {@code null} if not found
     * @throws NifiComponentNotFoundException if the parent process group does not exist
     */
    @Deprecated
    @Nullable
    public ProcessGroupDTO getProcessGroupByName(@Nonnull final String parentGroupId, @Nonnull final String groupName, final boolean recursive, final boolean verbose) {
        return client.processGroups().findByName(parentGroupId, groupName, recursive, verbose).orElse(null);
    }

    @Deprecated
    public ProcessGroupDTO getRootProcessGroup() throws NifiComponentNotFoundException {
        return client.processGroups().findRoot();
    }


    /**
     *
     * Disables all inputs for a given process group
     *
     * @param processGroupId
     * @return processorDTO prior to disabling
     * @throws NifiComponentNotFoundException
     */
    public List<ProcessorDTO> disableAllInputProcessors(String processGroupId) throws NifiComponentNotFoundException {
        List<ProcessorDTO> processorDTOs = getInputProcessors(processGroupId);
        ProcessorDTO updateDto = new ProcessorDTO();
        if (processorDTOs != null) {
            for (ProcessorDTO dto : processorDTOs) {
                updateDto.setParentGroupId(dto.getParentGroupId());
                updateDto.setId(dto.getId());
                //fetch the processor and update it
                if (NifiProcessUtil.PROCESS_STATE.STOPPED.name().equals(dto.getState())) {
                    //if its stopped you can disable it.. otherwise stop it and then disable it
                    updateDto.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                    updateProcessorWithRetry(updateDto);
                }
                if (NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(dto.getState())) {
                    updateDto.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                    updateProcessorWithRetry(updateDto);
                    updateDto.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                    updateProcessorWithRetry(updateDto);
                }
            }
        }
        return processorDTOs;
    }

    /**
     * Finds an input processor of the specified type within the specified process group and sets it to {@code RUNNING}. Other input processors are set to {@code DISABLED}.
     *
     * @param processGroupId the id of the NiFi process group to be searched
     * @param type           the type (or Java class) of processor to set to {@code RUNNING}, or {@code null} to use the first processor
     * @return {@code true} if the processor was found, or {@code null} otherwise
     * @throws NifiComponentNotFoundException if the process group id is not valid
     */
    public boolean setInputAsRunningByProcessorMatchingType(@Nonnull final String processGroupId, @Nullable final String type) {
        // Get the processor list and the processor to be run
        return setInputProcessorState(processGroupId, type, NifiProcessUtil.PROCESS_STATE.RUNNING);
    }

    /**
     * Finds an input processor of the specified type within the specified process group and sets it to the passed in {@code state}. Other input processors are set to {@code DISABLED}.
     *
     * @param processGroupId the id of the NiFi process group to be searched
     * @param type           the type (or Java class) of processor to set to {@code state}, or {@code null} to use the first processor
     * @param state          the state to set the matched input processor
     * @return {@code true} if the processor was found, or {@code false} otherwise
     */
    public boolean setInputProcessorState(@Nonnull final String processGroupId, @Nullable final String type, NifiProcessUtil.PROCESS_STATE state) {
        // Get the processor list and the processor to be run
        final List<ProcessorDTO> processors = getInputProcessors(processGroupId);
        if (processors.isEmpty()) {
            return false;
        }

        final ProcessorDTO selected = StringUtils.isBlank(type) ? processors.get(0) : NifiProcessUtil.findFirstProcessorsByType(processors, type);
        if (selected == null) {
            return false;
        }

        // Set selected processor to RUNNING and others to DISABLED
        for (final ProcessorDTO processor : processors) {
            // Verify state of processor
            if (processor.equals(selected)) {
                updateProcessorState(processor, state);
            } else {
                updateProcessorState(processor, NifiProcessUtil.PROCESS_STATE.DISABLED);
            }
        }

        return true;
    }


    /**
     * Update the Processor state
     */
    private ProcessorDTO setProcessorState(ProcessorDTO processor, NifiProcessUtil.PROCESS_STATE state) {
        ProcessorDTO updateDto = new ProcessorDTO();
        updateDto.setId(processor.getId());
        updateDto.setParentGroupId(processor.getParentGroupId());
        updateDto.setState(state.name());
        return updateProcessor(updateDto);
    }

    /**
     * Transitions the Processor into the correct state:  {@code DISABLED, RUNNING, STOPPED}
     *
     * @param processorDTO the processor to update
     * @param state        the State which the processor should be set to
     */
    private ProcessorDTO updateProcessorState(ProcessorDTO processorDTO, NifiProcessUtil.PROCESS_STATE state) {
        NifiProcessUtil.PROCESS_STATE currentState = NifiProcessUtil.PROCESS_STATE.valueOf(processorDTO.getState());
        if (NifiProcessUtil.PROCESS_STATE.DISABLED.equals(state) && !NifiProcessUtil.PROCESS_STATE.DISABLED.equals(currentState)) {
            //disable it
            //first stop it
            ProcessorDTO updatedProcessor = updateProcessorState(processorDTO, NifiProcessUtil.PROCESS_STATE.STOPPED);
            // then disable it
            return setProcessorState(updatedProcessor, NifiProcessUtil.PROCESS_STATE.DISABLED);
        }
        if (NifiProcessUtil.PROCESS_STATE.RUNNING.equals(state) && !NifiProcessUtil.PROCESS_STATE.RUNNING.equals(currentState)) {
            //run it
            //first make sure its enabled
            ProcessorDTO updatedProcessor = updateProcessorState(processorDTO, NifiProcessUtil.PROCESS_STATE.ENABLED);
            return setProcessorState(updatedProcessor, NifiProcessUtil.PROCESS_STATE.RUNNING);

        }
        if (NifiProcessUtil.PROCESS_STATE.STOPPED.equals(state) && !NifiProcessUtil.PROCESS_STATE.STOPPED.equals(currentState)) {
            //stop it
            //first make sure its enabled
            ProcessorDTO updatedProcessor = updateProcessorState(processorDTO, NifiProcessUtil.PROCESS_STATE.ENABLED);
            setProcessorState(updatedProcessor, NifiProcessUtil.PROCESS_STATE.STOPPED);
        }
        if (NifiProcessUtil.PROCESS_STATE.ENABLED.equals(state) && !NifiProcessUtil.PROCESS_STATE.ENABLED.equals(currentState)) {
            //enable it
            //if disabled, enable it
            if (NifiProcessUtil.PROCESS_STATE.DISABLED.equals(currentState)) {
                return setProcessorState(processorDTO, NifiProcessUtil.PROCESS_STATE.STOPPED);
            }
            return processorDTO;
        }
        return processorDTO;

    }


    public boolean disableInputProcessors(@Nonnull final String processGroupId) {
        // Get the processor list
        boolean updated = false;
        final List<ProcessorDTO> processors = getInputProcessors(processGroupId);
        if (processors.isEmpty()) {
            return false;
        }
        for (final ProcessorDTO processor : processors) {
            // Verify state of processor
            if (!NifiProcessUtil.PROCESS_STATE.DISABLED.name().equals(processor.getState())) {
                // Stop processor before setting final state
                if (!NifiProcessUtil.PROCESS_STATE.STOPPED.name().equals(processor.getState())) {
                    stopProcessor(processor.getParentGroupId(), processor.getId());
                }

                // Set final state
                ProcessorDTO updateDto = new ProcessorDTO();
                updateDto.setId(processor.getId());
                updateDto.setParentGroupId(processor.getParentGroupId());
                updateDto.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                updateProcessor(updateDto);
                updated = true;
            }
        }
        return updated;
    }


    /**
     * return the Source Processors for a given group
     */
    public List<ProcessorDTO> getInputProcessors(String processGroupId) throws NifiComponentNotFoundException {
        //get the group
        ProcessGroupDTO processGroupEntity = getProcessGroup(processGroupId, false, true);
        //get the Source Processors
        List<String> sourceIds = getInputProcessorIds(processGroupId);
        return NifiProcessUtil.findProcessorsByIds(processGroupEntity.getContents().getProcessors(), sourceIds);
    }

    /**
     * get a set of all ProcessorDTOs in a template and optionally remove the initial input ones
     */
    public Set<ProcessorDTO> getProcessorsForTemplate(String templateId) throws NifiComponentNotFoundException {
        TemplateDTO dto = getTemplateById(templateId);
        if (dto != null) {
            return NifiProcessUtil.getProcessors(dto);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * returns a list of Processors in a group that dont have any connection destinations (1st in the flow)
     */
    public List<String> getInputProcessorIds(String processGroupId) throws NifiComponentNotFoundException {
        final Set<ConnectionDTO> connections = client.processGroups().getConnections(processGroupId);
        return NifiConnectionUtil.getInputProcessorIds(connections);
    }

    /**
     * gets a Processor
     */
    @Deprecated
    public ProcessorDTO getProcessor(String processGroupId, String processorId) throws NifiComponentNotFoundException {
        return client.processors().findById(processGroupId, processorId)
            .orElseThrow(() -> new NifiComponentNotFoundException(processorId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESSOR, null));
    }

    @Deprecated
    public ProcessorDTO updateProcessor(ProcessorDTO processorDTO) {
        return client.processors().update(processorDTO);
    }

    @Deprecated
    public ProcessorDTO updateProcessorWithRetry(ProcessorDTO processorDTO) {
        return client.processors().updateWithRetry(processorDTO, 10, 1000, TimeUnit.MILLISECONDS);
    }

    @Deprecated
    public ProcessGroupDTO updateProcessGroup(ProcessGroupDTO processGroupEntity) {
        return client.processGroups().update(processGroupEntity);
    }


    /**
     * Update the properties
     */
    public void updateProcessGroupProperties(List<NifiProperty> properties) {

        Map<String, Map<String, List<NifiProperty>>>
            processGroupProperties =
            NifiPropertyUtil.groupPropertiesByProcessGroupAndProcessor(properties);

        for (Map.Entry<String, Map<String, List<NifiProperty>>> processGroupEntry : processGroupProperties.entrySet()) {

            String processGroupId = processGroupEntry.getKey();
            for (Map.Entry<String, List<NifiProperty>> propertyEntry : processGroupEntry.getValue().entrySet()) {
                String processorId = propertyEntry.getKey();
                updateProcessorProperties(processGroupId, processorId, propertyEntry.getValue());
            }

        }
    }

    public void updateProcessorProperties(String processGroupId, String processorId, List<NifiProperty> properties) {
        Map<String, NifiProperty> propertyMap = NifiPropertyUtil.propertiesAsMap(properties);
        // fetch the processor
        ProcessorDTO processor = getProcessor(processGroupId, processorId);
        //iterate through and update the properties
        for (Map.Entry<String, NifiProperty> property : propertyMap.entrySet()) {
            processor.getConfig().getProperties().put(property.getKey(), property.getValue().getValue());
        }
        updateProcessor(processor);
    }

    public void updateProcessorProperty(String processGroupId, String processorId, NifiProperty property) {
        // fetch the processor
        ProcessorDTO processor = getProcessor(processGroupId, processorId);
        //iterate through and update the properties
        //only set this property
        processor.getConfig().getProperties().clear();
        processor.getConfig().getProperties().put(property.getKey(), property.getValue());
        updateProcessor(processor);
    }

    @Deprecated
    public Set<ControllerServiceDTO> getControllerServices() {
        return client.controllerServices().findAll();
    }

    @Deprecated
    public ControllerServiceDTO getControllerService(String type, String id) throws NifiComponentNotFoundException {
        return client.controllerServices().findById(id)
            .orElseThrow(() -> new NifiComponentNotFoundException(id, NifiConstants.NIFI_COMPONENT_TYPE.CONTROLLER_SERVICE, null));
    }

    /**
     * Returns Null if cant find it
     */
    public ControllerServiceDTO getControllerServiceByName(String type, final String serviceName) {
        ControllerServiceDTO controllerService = null;

        Set<ControllerServiceDTO> entity = getControllerServices();
        if (entity != null) {
            List<ControllerServiceDTO> services = Lists.newArrayList(Iterables.filter(entity, new Predicate<ControllerServiceDTO>() {
                @Override
                public boolean apply(ControllerServiceDTO controllerServiceDTO) {
                    return controllerServiceDTO.getName().equalsIgnoreCase(serviceName);
                }
            }));

            if (services != null) {

                for (ControllerServiceDTO controllerServiceDTO : services) {
                    if (controllerService == null) {
                        controllerService = controllerServiceDTO;
                    }
                    if (controllerServiceDTO.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())) {
                        controllerService = controllerServiceDTO;
                        break;
                    }
                }
            }
        }
        return controllerService;
    }

    /**
     * Enables the ControllerService and also replaces the properties if they match their keys
     */
    public ControllerServiceDTO enableControllerServiceAndSetProperties(String id, Map<String, String> properties)
        throws NifiClientRuntimeException {
        ControllerServiceDTO entity = getControllerService(null, id);
        ControllerServiceDTO dto = entity;
        //only need to do this if it is not enabled
        if (!dto.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())) {
            if (properties != null) {
                boolean changed;
                Map<String, String> resolvedProperties = NifiEnvironmentProperties.getEnvironmentControllerServiceProperties(properties, dto.getName());
                if (resolvedProperties != null && !resolvedProperties.isEmpty()) {
                    changed = ConfigurationPropertyReplacer.replaceControllerServiceProperties(dto, resolvedProperties, getPropertyDescriptorTransform());
                } else {
                    changed = ConfigurationPropertyReplacer.replaceControllerServiceProperties(dto, properties, getPropertyDescriptorTransform());
                }
                if (changed) {
                    //first save the property change
                    client.controllerServices().update(dto);
                }
            }

            if (!dto.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())) {
                dto.setState(NifiProcessUtil.SERVICE_STATE.ENABLED.name());
                entity = client.controllerServices().update(dto);
            }
        }
        //initially when trying to enable the service the state will be ENABLING
        //This will last until the service is up.
        //if it is in the ENABLING state it needs to wait and try again to get the status.

        dto = entity;
        if (dto.getState().equalsIgnoreCase(NifiProcessUtil.SERVICE_STATE.ENABLING.name())) {
            //attempt to retry x times before returning
            int retryCount = 0;
            int maxRetries = 5;
            while (retryCount <= maxRetries) {
                entity = getControllerService(null, id);
                if (!entity.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())) {
                    try {
                        Thread.sleep(3000);
                        retryCount++;
                    } catch (InterruptedException e2) {

                    }
                } else {
                    retryCount = maxRetries + 1;
                }
            }
        }
        return entity;
    }

    @Deprecated
    public Set<DocumentedTypeDTO> getControllerServiceTypes() {
        return client.controllerServices().getTypes();
    }

    @Deprecated
    public AboutDTO getNifiVersion() {
        return client.about();
    }


    public boolean isConnected() {
        AboutDTO aboutEntity = getNifiVersion();
        return aboutEntity != null;
    }

    public List<BulletinDTO> getProcessorBulletins(String processorId) {
        return client.getBulletins(processorId);
    }

    public List<BulletinDTO> getBulletinsMatchingSource(String regexPattern,Long after) {
        return client.getBulletinsMatchingSource(regexPattern,after);
    }

    public List<BulletinDTO> getBulletinsMatchingMessage(String regexPattern) {
        return client.getBulletinsMatchingMessage(regexPattern);
    }

    public List<BulletinDTO> getBulletinsMatchingMessage(String regexPattern, Long afterId) {
        return client.getBulletinsMatchingMessage(regexPattern, afterId);
    }


    @Deprecated
    public Set<PortDTO> getInputPorts(String groupId) throws NifiComponentNotFoundException {
        return client.processGroups().getInputPorts(groupId);

    }

    @Deprecated
    public Set<PortDTO> getOutputPorts(String groupId) throws NifiComponentNotFoundException {
        return client.processGroups().getOutputPorts(groupId);
    }

    public void createReusableTemplateInputPort(String reusableTemplateCategoryGroupId, String reusableTemplateGroupId) throws NifiComponentNotFoundException {
        ProcessGroupDTO reusableTemplateGroup = getProcessGroup(reusableTemplateGroupId, false, false);
        ProcessGroupDTO reusableTemplateCategoryGroup = getProcessGroup(reusableTemplateCategoryGroupId, false, false);
        Set<PortDTO> inputPortsEntity = getInputPorts(reusableTemplateGroupId);
        if (inputPortsEntity != null) {
            for (PortDTO inputPort : inputPortsEntity) {
                createReusableTemplateInputPort(reusableTemplateCategoryGroupId, reusableTemplateGroupId, inputPort.getName());
            }
        }
    }

    /**
     * Creates an INPUT Port and verifys/creates the connection from it to the child processgroup input port
     */
    public void createReusableTemplateInputPort(String reusableTemplateCategoryGroupId, String reusableTemplateGroupId,
                                                String inputPortName) {
        // ProcessGroupEntity reusableTemplateGroup = getProcessGroup(reusableTemplateGroupId, false, false);
        Set<PortDTO> inputPortsEntity = getInputPorts(reusableTemplateCategoryGroupId);
        PortDTO inputPort = NifiConnectionUtil.findPortMatchingName(inputPortsEntity, inputPortName);
        if (inputPort == null) {

            //1 create the inputPort on the Category Group
            PortDTO portDTO = new PortDTO();
            portDTO.setParentGroupId(reusableTemplateCategoryGroupId);
            portDTO.setName(inputPortName);
            inputPort = client.processGroups().createInputPort(reusableTemplateCategoryGroupId, portDTO);
        }
        //2 check and create the connection frmo the inputPort to the actual templateGroup
        PortDTO templateInputPort = null;
        Set<PortDTO> templatePorts = getInputPorts(reusableTemplateGroupId);
        templateInputPort = NifiConnectionUtil.findPortMatchingName(templatePorts, inputPortName);

        ConnectionDTO
            inputToTemplateConnection =
            NifiConnectionUtil.findConnection(findConnectionsForParent(reusableTemplateCategoryGroupId), inputPort.getId(),
                                              templateInputPort.getId());
        if (inputToTemplateConnection == null) {
            //create the connection
            ConnectableDTO source = new ConnectableDTO();
            source.setGroupId(reusableTemplateCategoryGroupId);
            source.setId(inputPort.getId());
            source.setName(inputPortName);
            source.setType(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
            ConnectableDTO dest = new ConnectableDTO();
            dest.setGroupId(reusableTemplateGroupId);
            dest.setName(inputPortName);
            dest.setId(templateInputPort.getId());
            dest.setType(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
            createConnection(reusableTemplateCategoryGroupId, source, dest);
        }


    }

    /**
     * Connect a Feed to a reusable Template
     */
    public void connectFeedToGlobalTemplate(final String feedProcessGroupId, final String feedOutputName,
                                            final String categoryProcessGroupId, String reusableTemplateCategoryGroupId,
                                            String inputPortName) throws NifiComponentNotFoundException {
        ProcessGroupDTO categoryProcessGroup = getProcessGroup(categoryProcessGroupId, false, false);
        ProcessGroupDTO feedProcessGroup = getProcessGroup(feedProcessGroupId, false, false);
        ProcessGroupDTO categoryParent = getProcessGroup(categoryProcessGroup.getParentGroupId(), false, false);
        ProcessGroupDTO reusableTemplateCategoryGroup = getProcessGroup(reusableTemplateCategoryGroupId, false, false);

        //Go into the Feed and find the output port that is to be associated with the global template
        Set<PortDTO> outputPortsEntity = getOutputPorts(feedProcessGroupId);
        PortDTO feedOutputPort = null;
        if (outputPortsEntity != null) {
            feedOutputPort = NifiConnectionUtil.findPortMatchingName(outputPortsEntity, feedOutputName);
        }
        if (feedOutputPort == null) {
            //ERROR  This feed needs to have an output port assigned on it to make the connection
        }

        Set<PortDTO> inputPortsEntity = getInputPorts(reusableTemplateCategoryGroupId);
        PortDTO inputPort = NifiConnectionUtil.findPortMatchingName(inputPortsEntity, inputPortName);
        String inputPortId = inputPort.getId();

        final String categoryOutputPortName = categoryProcessGroup.getName() + " to " + inputPort.getName();

        //Find or create the output port that will join to the globabl template at the Category Level

        Set<PortDTO> categoryOutputPortsEntity = getOutputPorts(categoryProcessGroupId);
        PortDTO categoryOutputPort = null;
        if (categoryOutputPortsEntity != null) {
            categoryOutputPort =
                NifiConnectionUtil.findPortMatchingName(categoryOutputPortsEntity, categoryOutputPortName);
        }
        if (categoryOutputPort == null) {
            //create it
            PortDTO portDTO = new PortDTO();
            portDTO.setParentGroupId(categoryProcessGroupId);
            portDTO.setName(categoryOutputPortName);
            categoryOutputPort = client.processGroups().createOutputPort(categoryProcessGroupId, portDTO);
        }
        //Now create a connection from the Feed PRocessGroup to this outputPortEntity

        ConnectionDTO
            feedOutputToCategoryOutputConnection =
            NifiConnectionUtil.findConnection(findConnectionsForParent(categoryProcessGroupId), feedOutputPort.getId(),
                                              categoryOutputPort.getId());
        if (feedOutputToCategoryOutputConnection == null) {
            //CONNECT FEED OUTPUT PORT TO THE Category output port
            ConnectableDTO source = new ConnectableDTO();
            source.setGroupId(feedProcessGroupId);
            source.setId(feedOutputPort.getId());
            source.setName(feedProcessGroup.getName());
            source.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
            ConnectableDTO dest = new ConnectableDTO();
            dest.setGroupId(categoryProcessGroupId);
            dest.setName(categoryOutputPort.getName());
            dest.setId(categoryOutputPort.getId());
            dest.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
            createConnection(categoryProcessGroup.getId(), source, dest);
        }

        ConnectionDTO
            categoryToReusableTemplateConnection =
            NifiConnectionUtil
                .findConnection(findConnectionsForParent(categoryParent.getId()), categoryOutputPort.getId(),
                                inputPortId);

        //Now connect the category PRocessGroup to the global template
        if (categoryToReusableTemplateConnection == null) {
            ConnectableDTO categorySource = new ConnectableDTO();
            categorySource.setGroupId(categoryProcessGroupId);
            categorySource.setId(categoryOutputPort.getId());
            categorySource.setName(categoryOutputPortName);
            categorySource.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
            ConnectableDTO categoryToGlobalTemplate = new ConnectableDTO();
            categoryToGlobalTemplate.setGroupId(reusableTemplateCategoryGroupId);
            categoryToGlobalTemplate.setId(inputPortId);
            categoryToGlobalTemplate.setName(inputPortName);
            categoryToGlobalTemplate.setType(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
            createConnection(categoryParent.getId(), categorySource, categoryToGlobalTemplate);
        }

    }


    public Set<ConnectionDTO> findConnectionsForParent(String parentProcessGroupId) throws NifiComponentNotFoundException {
        Set<ConnectionDTO> connections = new HashSet<>();
        //get all connections under parent group
        Set<ConnectionDTO> connectionsEntity = getProcessGroupConnections(parentProcessGroupId);
        if (connectionsEntity != null) {
            connections = connectionsEntity;
        }
        return connections;
    }

    @Deprecated
    public ConnectionDTO createConnection(String processGroupId, ConnectableDTO source, ConnectableDTO dest) {
        return client.processGroups().createConnection(processGroupId, source, dest);
    }


    @Deprecated
    public SearchResultsDTO search(String query) {
        return client.search(query);
    }

    public ProcessorDTO findProcessorById(String processorId) {
        SearchResultsDTO results = search(processorId);
        //log this
        if (results != null && results.getProcessorResults() != null && !results.getProcessorResults().isEmpty()) {
            log.info("Attempt to find processor by id {}. Processors Found: {} ", processorId, results.getProcessorResults().size());
            ComponentSearchResultDTO processorResult = results.getProcessorResults().get(0);
            String id = processorResult.getId();
            String groupId = processorResult.getGroupId();
            ProcessorDTO processorEntity = getProcessor(groupId, id);

            if (processorEntity != null) {
                return processorEntity;
            }
        } else {
            log.info("Unable to find Processor in Nifi for id: {}", processorId);
        }
        return null;
    }

    public NifiVisitableProcessGroup getFlowOrder(String processGroupId, NifiConnectionOrderVisitorCache cache) throws NifiComponentNotFoundException {
        return client.flows().getFlowOrder(processGroupId, cache);
    }

    public NifiVisitableProcessGroup getFlowOrder(ProcessGroupDTO processGroupEntity, NifiConnectionOrderVisitorCache cache) throws NifiComponentNotFoundException {
        return client.flows().getFlowOrder(processGroupEntity, cache);
    }


    public NifiFlowProcessGroup getFeedFlow(String processGroupId) throws NifiComponentNotFoundException {
        return client.flows().getFeedFlow(processGroupId);
    }

    public Set<ProcessorDTO> getProcessorsForFlow(String processGroupId) throws NifiComponentNotFoundException {
        return client.flows().getProcessorsForFlow(processGroupId);
    }

    public NifiFlowProcessGroup getTemplateFeedFlow(String templateId) {
        return client.flows().getTemplateFeedFlow(templateId);
    }

    public NifiFlowProcessGroup getTemplateFeedFlow(TemplateDTO template) {
        return client.flows().getTemplateFeedFlow(template);
    }


    public NifiFlowProcessGroup getFeedFlowForCategoryAndFeed(String categoryAndFeedName) {
        return client.flows().getFeedFlowForCategoryAndFeed(categoryAndFeedName);
    }


    //walk entire graph
    public List<NifiFlowProcessGroup> getFeedFlows() {
        return client.flows().getFeedFlows();
    }


    public List<NifiFlowProcessGroup> getFeedFlowsWithCache(NifiConnectionOrderVisitorCache cache) {
        return client.flows().getFeedFlowsWithCache(cache);
    }

    public List<NifiFlowProcessGroup> getFeedFlows(Collection<String> feedNames) {
        return client.flows().getFeedFlows(feedNames);
    }


    /**
     * Gets a transform for converting {@link NiFiPropertyDescriptor} objects to {@link PropertyDescriptorDTO}.
     *
     * @return the transform
     */
    public NiFiPropertyDescriptorTransform getPropertyDescriptorTransform() {
        return propertyDescriptorTransform;
    }

    /**
     * Returns the NiFi REST client.
     *
     * @return the NiFi REST client
     */
    public NiFiRestClient getNiFiRestClient() {
        return client;
    }

    public void setClient(NiFiRestClient client) {
        this.client = client;
    }
}
