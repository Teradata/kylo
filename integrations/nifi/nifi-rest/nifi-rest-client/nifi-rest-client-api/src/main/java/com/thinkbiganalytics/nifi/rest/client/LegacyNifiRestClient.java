package com.thinkbiganalytics.nifi.rest.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.feedmgr.ConfigurationPropertyReplacer;
import com.thinkbiganalytics.nifi.feedmgr.NifiEnvironmentProperties;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.feedmgr.TemplateInstanceCreator;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateUtil;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.search.ComponentSearchResultDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.BulletinBoardEntity;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.DropRequestEntity;
import org.apache.nifi.web.api.entity.Entity;
import org.apache.nifi.web.api.entity.InputPortEntity;
import org.apache.nifi.web.api.entity.LineageEntity;
import org.apache.nifi.web.api.entity.ListingRequestEntity;
import org.apache.nifi.web.api.entity.OutputPortEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ProvenanceEntity;
import org.apache.nifi.web.api.entity.ProvenanceEventEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

@Deprecated
public class LegacyNifiRestClient extends JerseyRestClient implements NifiFlowVisitorClient {

    private static final Logger log = LoggerFactory.getLogger(LegacyNifiRestClient.class);

    private String apiPath = "/nifi-api";

    @Inject
    private NiFiRestClient client;

    private NifiRestClientConfig clientConfig;

    public LegacyNifiRestClient(NifiRestClientConfig config) {
        super(config);
        this.clientConfig = config;

    }

    protected WebTarget getBaseTarget() {
        WebTarget target = super.getBaseTarget();
        return target.path(apiPath);
    }

    @Deprecated
    public String getClusterType() {
        return clientConfig.getClusterType();
    }


    /**
     * Gets Template data, either a quick view or including all its content
     */
    @Deprecated
    public Set<TemplateDTO> getTemplates(boolean includeFlow) {
        return client.templates().findAll();
    }

    @Deprecated
    public TemplateDTO deleteTemplate(String templateId) {
        return client.templates().delete(templateId).get();
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
     * @param inputPortName
     * @return
     */
    @Deprecated
    public Map<String,String> getTemplatesAsXmlMatchingInputPortName(final String inputPortName) {
        final Set<TemplateDTO> templates = client.templates().findByInputPortName(inputPortName);
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
    public NifiProcessGroup createNewTemplateInstance(String templateId, Map<String, Object> staticConfigProperties, boolean createReusableFlow) {
        TemplateInstanceCreator creator = new TemplateInstanceCreator(this, templateId, staticConfigProperties, createReusableFlow);
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

    /**
     * Gets the current Revision and Version of Nifi instance. This is needed when performing an update to pass over the revision.getVersion() for locking purposes
     */
    public Entity getControllerRevision() {
        return get("/controller/revision", null, Entity.class);
    }

    /**
     * Expose all Properties for a given Template as parameters for external use
     */
    public List<NifiProperty> getPropertiesForTemplate(String templateId) {
        TemplateDTO dto = getTemplateById(templateId);
        ProcessGroupDTO rootProcessGroup = getProcessGroup("root", false, false);
        return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup, dto);
    }


    public Set<PortDTO> getPortsForTemplate(String templateId) throws NifiComponentNotFoundException {
        Set<PortDTO> ports = new HashSet<>();
        TemplateDTO dto = getTemplateById(templateId);
        Set<PortDTO> inputPorts = dto.getSnippet().getInputPorts();
        if (inputPorts != null) {
            ports.addAll(inputPorts);
        }
        Set<PortDTO> outputPorts = dto.getSnippet().getOutputPorts();
        if (outputPorts != null) {
            ports.addAll(outputPorts);
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
        ProcessGroupDTO rootProcessGroup = getProcessGroup("root", false, false);
        return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup, dto);
    }

    /**
     * Returns an Empty ArrayList of nothing is found
     */
    public List<NifiProperty> getAllProperties() throws NifiComponentNotFoundException {
        ProcessGroupDTO root = getRootProcessGroup();
        return NifiPropertyUtil.getProperties(root);
    }


    public List<NifiProperty> getPropertiesForProcessGroup(String processGroupId) throws NifiComponentNotFoundException {
        ProcessGroupDTO processGroup = getProcessGroup(processGroupId, true, true);
        return NifiPropertyUtil.getProperties(processGroup);
    }

    private void updateEntityForSave(Entity entity) {
        Entity status = getControllerRevision();
        entity.setRevision(status.getRevision());
    }

    public ProcessGroupDTO createProcessGroup(String name) {

        return createProcessGroup("root", name);
    }

    @Deprecated
    public ProcessGroupDTO createProcessGroup(String parentGroupId, String name) throws NifiComponentNotFoundException {
        return client.processGroups().create(parentGroupId, name);
    }

    public ProcessGroupEntity markProcessorGroupAsRunning(ProcessGroupDTO groupDTO) {
        ProcessGroupEntity entity = new ProcessGroupEntity();
        entity.setProcessGroup(groupDTO);
        entity.getProcessGroup().setRunning(true);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),
                   entity, ProcessGroupEntity.class);


    }




    private Map<String, Object> getUpdateParams() {
        Entity status = getControllerRevision();
        Map<String, Object> params = new HashMap<>();
        params.put("version", status.getRevision().getVersion().toString());
        params.put("clientId", status.getRevision().getClientId());
        return params;
    }


    public void stopAllProcessors(ProcessGroupDTO groupDTO) {
        stopAllProcessors(groupDTO.getId(), groupDTO.getParentGroupId());
    }

    public void stopProcessor(ProcessorDTO processorDTO) {
        if (NifiProcessUtil.PROCESS_STATE.RUNNING.name().equalsIgnoreCase(processorDTO.getState())) {
            stopProcessor(processorDTO.getParentGroupId(), processorDTO.getId());
        }
    }

    public void stopProcessor(String processorGroupId, String processorId) {
        ProcessorEntity entity = new ProcessorEntity();
        ProcessorDTO dto = new ProcessorDTO();
        dto.setId(processorId);
        dto.setParentGroupId(processorGroupId);
        dto.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
        entity.setProcessor(dto);
        updateProcessor(entity);
    }

    public void startProcessor(String processorGroupId, String processorId) {
        ProcessorEntity entity = new ProcessorEntity();
        ProcessorDTO dto = new ProcessorDTO();
        dto.setId(processorId);
        dto.setParentGroupId(processorGroupId);
        dto.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
        entity.setProcessor(dto);
        updateProcessor(entity);
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
        client.processGroups().schedule(processGroupId, parentProcessGroupId, NiFiComponentState.RUNNING);
        return client.processGroups().findById(processGroupId, false, false).get();
    }

    @Deprecated
    public ProcessGroupDTO startAll(String processGroupId, String parentProcessGroupId) throws NifiClientRuntimeException {
        client.processGroups().schedule(processGroupId, parentProcessGroupId, NiFiComponentState.RUNNING);
        return client.processGroups().findById(processGroupId, false, false).get();
    }

    public InputPortEntity stopInputPort(String groupId, String portId) throws NifiClientRuntimeException {
        InputPortEntity entity = new InputPortEntity();
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
        entity.setInputPort(portDTO);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + groupId + "/input-ports/" + portId, entity, InputPortEntity.class);
    }

    public OutputPortEntity stopOutputPort(String groupId, String portId) throws NifiClientRuntimeException {
        OutputPortEntity entity = new OutputPortEntity();
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
        entity.setOutputPort(portDTO);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + groupId + "/output-ports/" + portId, entity, OutputPortEntity.class);
    }

    public InputPortEntity startInputPort(String groupId, String portId) throws NifiClientRuntimeException {
        InputPortEntity entity = new InputPortEntity();
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
        entity.setInputPort(portDTO);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + groupId + "/input-ports/" + portId, entity, InputPortEntity.class);
    }

    public OutputPortEntity startOutputPort(String groupId, String portId) throws NifiClientRuntimeException {
        OutputPortEntity entity = new OutputPortEntity();
        PortDTO portDTO = new PortDTO();
        portDTO.setId(portId);
        portDTO.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
        entity.setOutputPort(portDTO);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + groupId + "/output-ports/" + portId, entity, OutputPortEntity.class);
    }

    @Deprecated
    public ProcessGroupDTO deleteProcessGroup(ProcessGroupDTO groupDTO) throws NifiClientRuntimeException {
        return client.processGroups().delete(groupDTO)
                .orElseThrow(() -> new NifiClientRuntimeException("Unable to delete Process Group " + groupDTO.getName()));
    }

    @Deprecated
    public ProcessGroupDTO stopProcessGroup(ProcessGroupDTO groupDTO) throws NifiClientRuntimeException {
        client.processGroups().schedule(groupDTO.getId(), groupDTO.getParentGroupId(), NiFiComponentState.STOPPED);
        return client.processGroups().findById(groupDTO.getId(), false, false).get();
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
     * @param connections the list of all connections from the parent process group
     * @return the deleted process group
     * @throws NifiClientRuntimeException if the process group could not be deleted
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

    public ConnectionEntity getConnection(String processGroupId, String connectionId) throws NifiComponentNotFoundException {
        try {
            return get("/controller/process-groups/" + processGroupId + "/connections/" + connectionId, null, ConnectionEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("Unable to find Connection for process Group: " + processGroupId + " and Connection Id " + connectionId, connectionId,
                                                     NifiConstants.NIFI_COMPONENT_TYPE.CONNECTION, e);
        }
    }

    public ListingRequestEntity getConnectionQueue(String processGroupId, String connectionId) {
        return postForm("/controller/process-groups/" + processGroupId + "/connections/" + connectionId + "/listing-requests", null, ListingRequestEntity.class);
    }

    public void deleteConnection(ConnectionDTO connection, boolean emptyQueue) {

        Map<String, Object> params = getUpdateParams();
        try {
            if (emptyQueue) {
                //empty connection Queue
                DropRequestEntity
                    dropRequestEntity =
                    delete("/controller/process-groups/" + connection.getParentGroupId() + "/connections/" + connection.getId() + "/contents", params,
                           DropRequestEntity.class);
                if (dropRequestEntity != null && dropRequestEntity.getDropRequest() != null) {
                    params = getUpdateParams();
                    delete("/controller/process-groups/" + connection.getParentGroupId() + "/connections/" + connection.getId() + "/drop-requests/"
                           + dropRequestEntity.getDropRequest().getId(), params, DropRequestEntity.class);
                }
            }
            //before deleting the connection we need to stop the source
            LOG.info("Before deleting the connection we need to stop Sources and destinations.");
            LOG.info("Stopping Source {} ({}) for connection {} ", connection.getSource().getId(), connection.getSource().getType(), connection.getId());
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
            LOG.info("Stopping Destination {} ({}) for connection {} ", connection.getDestination().getId(), connection.getDestination().getType(), connection.getId());
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
            LOG.info("Deleting the connection {} ", connection.getId());
            delete("/controller/process-groups/" + connection.getParentGroupId() + "/connections/" + connection.getId(), params,
                   ConnectionEntity.class);
            try {
                type = connection.getSource().getType();
                //now start the inputs again
                LOG.info("After Delete... Starting source again {} ({}) ", connection.getSource().getId(), connection.getSource().getType());
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
                LOG.info("After Delete... Starting dest again {} ({}) ", connection.getDestination().getId(), connection.getDestination().getType());
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
                if(!(e instanceof NifiComponentNotFoundException)) {
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
     * @param groupName the name of the process group to find
     * @param recursive {@code true} to include all encapsulated components, or {@code false} for just the immediate children
     * @param verbose {@code true} to include any encapsulated components, or {@code false} for just details about the process group
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
     * Disables all inputs for a given process group
     */
    public void disableAllInputProcessors(String processGroupId) throws NifiComponentNotFoundException {
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
                    updateProcessor(updateDto);
                }
                if (NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(dto.getState())) {
                    updateDto.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                    updateProcessor(updateDto);
                    updateDto.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                    updateProcessor(updateDto);
                }
            }
        }
        //also stop any input ports

        /*
        List<String> inputPortIds = getInputPortIds(processGroupId);
        if(inputPortIds != null && !inputPortIds.isEmpty())
        {
            for(String inputPortId : inputPortIds) {
             InputPortEntity inputPortEntity =   stopInputPort(processGroupId, inputPortId);
                int i =0;
            }
        }
        */
    }


    public void stopAllInputProcessors(String processGroupId) throws NifiComponentNotFoundException {
        List<ProcessorDTO> processorDTOs = getInputProcessors(processGroupId);
        ProcessorDTO updateDto = new ProcessorDTO();
        if (processorDTOs != null) {
            for (ProcessorDTO dto : processorDTOs) {
                updateDto.setParentGroupId(dto.getParentGroupId());
                updateDto.setId(dto.getId());
                //fetch the processor and update it
                if (NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(dto.getState())) {
                    //if its stopped you can disable it.. otherwise stop it and then disable it
                    updateDto.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                    updateProcessor(updateDto);
                }
            }
        }
    }

    /**
     * Finds an input processor of the specified type within the specified process group and sets it to {@code RUNNING}. Other input processors are set to {@code DISABLED}.
     *
     * @param processGroupId the id of the NiFi process group to be searched
     * @param type the type (or Java class) of processor to set to {@code RUNNING}, or {@code null} to use the first processor
     * @return {@code true} if the processor was found, or {@code null} otherwise
     * @throws NifiComponentNotFoundException if the process group id is not valid
     */
    public boolean setInputAsRunningByProcessorMatchingType(@Nonnull final String processGroupId, @Nullable final String type) {
        // Get the processor list and the processor to be run
       return setInputProcessorState(processGroupId, type, NifiProcessUtil.PROCESS_STATE.RUNNING);
    }

    /**
     * Finds an input processor of the specified type within the specified process group and sets it to the passed in {@code state}. Other input processors are set to {@code DISABLED}.
     * @param processGroupId the id of the NiFi process group to be searched
     * @param type the type (or Java class) of processor to set to {@code state}, or {@code null} to use the first processor
     * @param state the state to set the matched input processor
     * @return {@code true} if the processor was found, or {@code false} otherwise
     */
    public boolean setInputProcessorState(@Nonnull final String processGroupId, @Nullable final String type,NifiProcessUtil.PROCESS_STATE state ) {
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
            }
            else {
                updateProcessorState(processor, NifiProcessUtil.PROCESS_STATE.DISABLED);
            }
        }

        return true;
    }


    /**
     * Update the Processor state
     * @param processor
     * @param state
     */
    private ProcessorDTO setProcessorState(ProcessorDTO processor, NifiProcessUtil.PROCESS_STATE state)
    {
        ProcessorEntity entity = new ProcessorEntity();
        ProcessorDTO updateDto = new ProcessorDTO();
        updateDto.setId(processor.getId());
        updateDto.setParentGroupId(processor.getParentGroupId());
        updateDto.setState(state.name());
        entity.setProcessor(updateDto);
        ProcessorEntity updatedProcessor = updateProcessor(entity);
        return updatedProcessor.getProcessor();
    }

    /**
     * Transitions the Processor into the correct state:  {@code DISABLED, RUNNING, STOPPED}
     * @param processorDTO the processor to update
     * @param state the State which the processor should be set to
     * @return
     */
    private ProcessorDTO updateProcessorState(ProcessorDTO processorDTO, NifiProcessUtil.PROCESS_STATE state){
        NifiProcessUtil.PROCESS_STATE currentState = NifiProcessUtil.PROCESS_STATE.valueOf(processorDTO.getState());
        if(NifiProcessUtil.PROCESS_STATE.DISABLED.equals(state) && !NifiProcessUtil.PROCESS_STATE.DISABLED.equals(currentState)){
            //disable it
            //first stop it
           ProcessorDTO updatedProcessor = updateProcessorState(processorDTO, NifiProcessUtil.PROCESS_STATE.STOPPED);
            // then disable it
          return  setProcessorState(updatedProcessor, NifiProcessUtil.PROCESS_STATE.DISABLED);
        }
        if(NifiProcessUtil.PROCESS_STATE.RUNNING.equals(state) && !NifiProcessUtil.PROCESS_STATE.RUNNING.equals(currentState)){
            //run it
           //first make sure its enabled
            ProcessorDTO updatedProcessor = updateProcessorState(processorDTO, NifiProcessUtil.PROCESS_STATE.ENABLED);
           return setProcessorState(updatedProcessor, NifiProcessUtil.PROCESS_STATE.RUNNING);

        }
        if(NifiProcessUtil.PROCESS_STATE.STOPPED.equals(state) && !NifiProcessUtil.PROCESS_STATE.STOPPED.equals(currentState)){
            //stop it
            //first make sure its enabled
            ProcessorDTO updatedProcessor = updateProcessorState(processorDTO, NifiProcessUtil.PROCESS_STATE.ENABLED);
            setProcessorState(updatedProcessor, NifiProcessUtil.PROCESS_STATE.STOPPED);
        }
        if(NifiProcessUtil.PROCESS_STATE.ENABLED.equals(state) && !NifiProcessUtil.PROCESS_STATE.ENABLED.equals(currentState)){
            //enable it
            //if disabled, enable it
            if(NifiProcessUtil.PROCESS_STATE.DISABLED.equals(currentState)){
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

                processor.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                ProcessorEntity entity = new ProcessorEntity();
                ProcessorDTO updateDto = new ProcessorDTO();
                updateDto.setId(processor.getId());
                updateDto.setParentGroupId(processor.getParentGroupId());
                updateDto.setState(processor.getState());
                entity.setProcessor(updateDto);
                updateProcessor(entity);
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
     * @param template
     * @return
     */
    public List<ProcessorDTO> getInputProcessorsForTemplate(TemplateDTO template) {
        return NifiTemplateUtil.getInputProcessorsForTemplate(template);
    }


    /**
     * get a set of all ProcessorDTOs in a template and optionally remove the initial input ones
     */
    public Set<ProcessorDTO> getProcessorsForTemplate(String templateId, boolean excludeInputs) throws NifiComponentNotFoundException {
        TemplateDTO dto = getTemplateById(templateId);
        Set<ProcessorDTO> processors = NifiProcessUtil.getProcessors(dto);

        return processors;

    }

    /**
     * returns a list of Processors in a group that dont have any connection destinations (1st in the flow)
     */
    public List<String> getInputProcessorIds(String processGroupId) throws NifiComponentNotFoundException {
        List<String> processorIds = new ArrayList<>();
        ConnectionsEntity connections = null;
        try {
            connections = get("/controller/process-groups/" + processGroupId + "/connections", null,
                              ConnectionsEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
        if (connections != null) {
            processorIds = NifiConnectionUtil.getInputProcessorIds(connections.getConnections());
        }
        return processorIds;
    }

    public List<String> getInputPortIds(String processGroupId) throws NifiComponentNotFoundException {
        List<String> ids = new ArrayList<>();
        ConnectionsEntity connections = null;
        try {
            connections = get("/controller/process-groups/" + processGroupId + "/connections", null,
                              ConnectionsEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
        if (connections != null) {
            ids = NifiConnectionUtil.getInputPortIds(connections.getConnections());
        }
        return ids;
    }

    /**
     * returns a list of Processors in a group that dont have any connection destinations (1st in the flow)
     */
    public List<String> getEndingProcessorIds(String processGroupId) throws NifiComponentNotFoundException {
        List<String> processorIds = new ArrayList<>();
        ConnectionsEntity connections = null;
        try {
            connections = get("/controller/process-groups/" + processGroupId + "/connections", null,
                              ConnectionsEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
        if (connections != null) {
            processorIds = NifiConnectionUtil.getEndingProcessorIds(connections.getConnections());
        }
        return processorIds;
    }

    /**
     * return the Source Processors for a given group
     */
    public List<ProcessorDTO> getEndingProcessors(String processGroupId) {
        //get the group
        ProcessGroupDTO processGroupEntity = getProcessGroup(processGroupId, false, true);
        //get the Source Processors
        List<String> sourceIds = getEndingProcessorIds(processGroupId);

        return NifiProcessUtil.findProcessorsByIds(processGroupEntity.getContents().getProcessors(), sourceIds);
    }


    /**
     * gets a Processor
     */
    public ProcessorEntity getProcessor(String processGroupId, String processorId) throws NifiComponentNotFoundException {
        try {
            return get("/controller/process-groups/" + processGroupId + "/processors/" + processorId, null, ProcessorEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processorId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESSOR, e);
        }
    }

    /**
     * Saves the Processor
     */
    public ProcessorEntity updateProcessor(ProcessorEntity processorEntity) {
        updateEntityForSave(processorEntity);
        return put(
            "/controller/process-groups/" + processorEntity.getProcessor().getParentGroupId() + "/processors/" + processorEntity
                .getProcessor().getId(), processorEntity, ProcessorEntity.class);
    }

    public ProcessorEntity updateProcessor(ProcessorDTO processorDTO) {
        ProcessorEntity processorEntity = new ProcessorEntity();
        processorEntity.setProcessor(processorDTO);
        updateEntityForSave(processorEntity);
        return put(
            "/controller/process-groups/" + processorEntity.getProcessor().getParentGroupId() + "/processors/" + processorEntity
                .getProcessor().getId(), processorEntity, ProcessorEntity.class);
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
        ProcessorEntity processor = getProcessor(processGroupId, processorId);
        //iterate through and update the properties
        for (Map.Entry<String, NifiProperty> property : propertyMap.entrySet()) {
            processor.getProcessor().getConfig().getProperties().put(property.getKey(), property.getValue().getValue());
        }
        updateProcessor(processor);
    }

    public void updateProcessorProperty(String processGroupId, String processorId, NifiProperty property) {
        // fetch the processor
        ProcessorEntity processor = getProcessor(processGroupId, processorId);
        //iterate through and update the properties
        processor.getProcessor().getConfig().getProperties().put(property.getKey(), property.getValue());
        updateProcessor(processor);
    }

    @Deprecated
    public Set<ControllerServiceDTO> getControllerServices() {
        return client.controllerServices().findAll();
    }

    @Deprecated
    public Set<ControllerServiceDTO> getControllerServices(String type) {
        return getControllerServices();
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

        Set<ControllerServiceDTO> entity = getControllerServices(type);
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
    public ControllerServiceDTO enableControllerServiceAndSetProperties(String id, Map<String, String> properties) throws NifiClientRuntimeException {
        ControllerServiceDTO entity = getControllerService(null, id);
        ControllerServiceDTO dto = entity;
        //only need to do this if it is not enabled
        if (!dto.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())) {
            if (properties != null) {
                boolean changed = false;
                Map<String, String> resolvedProperties = NifiEnvironmentProperties.getEnvironmentControllerServiceProperties(properties, dto.getName());
                if (resolvedProperties != null && !resolvedProperties.isEmpty()) {
                    changed = ConfigurationPropertyReplacer.replaceControllerServiceProperties(dto, resolvedProperties);
                } else {
                    changed = ConfigurationPropertyReplacer.replaceControllerServiceProperties(dto, properties);
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


    public LineageEntity postLineageQuery(LineageEntity lineageEntity) {

        return post("/controller/provenance/lineage", lineageEntity, LineageEntity.class);
    }


    public ProvenanceEntity getProvenanceEntity(String provenanceId) {

        ProvenanceEntity entity = get("/controller/provenance/" + provenanceId, null, ProvenanceEntity.class);
        if (entity != null) {
            if (!entity.getProvenance().isFinished()) {
                return getProvenanceEntity(provenanceId);
            } else {
                //if it is finished you must delete the provenance entity
                try {
                    delete("/controller/provenance/" + provenanceId, null, ProvenanceEntity.class);
                } catch (ClientErrorException e) {
                    //swallow the exception.  Nothing we can do about it
                }
                return entity;
            }
        }
        return null;
    }

    public ProvenanceEventEntity getProvenanceEvent(String eventId) {
        ProvenanceEventEntity eventEntity = get("/controller/provenance/events/" + eventId, null, ProvenanceEventEntity.class);
        return eventEntity;
    }

    public AboutEntity getNifiVersion() {
        return get("/controller/about", null, AboutEntity.class);
    }


    public boolean isConnected(){
        AboutEntity aboutEntity = getNifiVersion();
        return aboutEntity != null;
    }

    public BulletinBoardEntity getBulletins(Map<String, Object> params) {

        BulletinBoardEntity entity = get("/controller/bulletin-board", params, BulletinBoardEntity.class);
        return entity;
    }

    public BulletinBoardEntity getProcessGroupBulletins(String processGroupId) {
        Map<String, Object> params = new HashMap<>();
        if (processGroupId != null) {
            params.put("groupId", processGroupId);
        }
        BulletinBoardEntity entity = get("/controller/bulletin-board", params, BulletinBoardEntity.class);
        return entity;
    }

    public BulletinBoardEntity getProcessorBulletins(String processorId) {
        Map<String, Object> params = new HashMap<>();
        if (processorId != null) {
            params.put("sourceId", processorId);
        }
        BulletinBoardEntity entity = get("/controller/bulletin-board", params, BulletinBoardEntity.class);
        return entity;
    }


    public InputPortEntity getInputPort(String groupId, String portId) throws NifiComponentNotFoundException {
        try {
            return get("/controller/process-groups/" + groupId + "/input-ports/" + portId, null, InputPortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("Unable to find Input Port " + portId + " Under Process Group " + groupId, NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
        }
    }

    @Deprecated
    public Set<PortDTO> getInputPorts(String groupId) throws NifiComponentNotFoundException {
        return client.processGroups().getInputPorts(groupId);

    }

    public OutputPortEntity getOutputPort(String groupId, String portId) {
        try {
            return get("/controller/process-groups/" + groupId + "/output-ports/" + portId, null, OutputPortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("Unable to find Output Port " + portId + " Under Process Group " + groupId, NifiConstants.NIFI_COMPONENT_TYPE.OUTPUT_PORT, e);
        }
    }

    @Deprecated
    public Set<PortDTO> getOutputPorts(String groupId) throws NifiComponentNotFoundException {
        return client.processGroups().getOutputPorts(groupId);
    }

    public List<ConnectionDTO> findAllConnectionsMatchingDestinationId(String parentGroupId, String destinationId) throws NifiComponentNotFoundException {
        //get this parentGroup and find all connections under this parent that relate to this inputPortId
        //1. get this processgroup
        ProcessGroupDTO parentGroup = getProcessGroup(parentGroupId, false, false);
        //2 get the parent
        String parent = parentGroup.getParentGroupId();
        Set<ConnectionDTO> connectionDTOs = findConnectionsForParent(parent);
        List<ConnectionDTO> matchingConnections = NifiConnectionUtil.findConnectionsMatchingDestinationId(connectionDTOs,
                                                                                                          destinationId);

        return matchingConnections;

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
            InputPortEntity inputPortEntity = new InputPortEntity();
            PortDTO portDTO = new PortDTO();
            portDTO.setParentGroupId(reusableTemplateCategoryGroupId);
            portDTO.setName(inputPortName);
            inputPortEntity.setInputPort(portDTO);
            updateEntityForSave(inputPortEntity);
            inputPortEntity =
                post("/controller/process-groups/" + reusableTemplateCategoryGroupId + "/input-ports", inputPortEntity,
                     InputPortEntity.class);
            inputPort = inputPortEntity.getInputPort();
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
            //  dest.setName(reusableTemplateGroup.getProcessGroup().getName());
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
            OutputPortEntity outputPortEntity = new OutputPortEntity();
            PortDTO portDTO = new PortDTO();
            portDTO.setParentGroupId(categoryProcessGroupId);
            portDTO.setName(categoryOutputPortName);
            outputPortEntity.setOutputPort(portDTO);
            updateEntityForSave(outputPortEntity);
            outputPortEntity =
                post("/controller/process-groups/" + categoryProcessGroupId + "/output-ports", outputPortEntity,
                     OutputPortEntity.class);
            categoryOutputPort = outputPortEntity.getOutputPort();
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


    public ConnectionDTO findConnection(String parentProcessGroupId, final String sourceProcessGroupId,
                                        final String destProcessGroupId) throws NifiComponentNotFoundException {
        return NifiConnectionUtil.findConnection(findConnectionsForParent(parentProcessGroupId), sourceProcessGroupId,
                                                 parentProcessGroupId);
    }

    @Deprecated
    public ConnectionDTO createConnection(String processGroupId, ConnectableDTO source, ConnectableDTO dest) {
        return client.processGroups().createConnection(processGroupId, source, dest);
    }


    public NifiVisitableProcessGroup getFlowOrder(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup group = null;
        ProcessGroupDTO processGroupEntity = getProcessGroup(processGroupId, true, true);
        if (processGroupEntity != null) {
            group = new NifiVisitableProcessGroup(processGroupEntity);
            NifiConnectionOrderVisitor orderVisitor = new NifiConnectionOrderVisitor(this, group);
            try {
                //find the parent just to get hte names andids
                ProcessGroupDTO parent = getProcessGroup(processGroupEntity.getParentGroupId(), false, false);
                group.setParentProcessGroup(parent);
            } catch (NifiComponentNotFoundException e) {
                //cant find the parent
            }

            group.accept(orderVisitor);
           //  orderVisitor.printOrder();
            //orderVisitor.printOrder();
        }
        return group;
    }

    public NifiFlowProcessGroup getFlowForProcessGroup(String processGroupId) {
        NifiFlowProcessGroup group = getFeedFlow(processGroupId);
        log.info("********************** getFlowForProcessGroup  ({})", group);
        NifiFlowDeserializer.constructGraph(group);
        return group;
    }



    public List<NifiFlowProcessGroup> getAllFlows() {
        log.info("********************** STARTING getAllFlows  ");
        System.out.println("********************** STARTING getAllFlows  ");
        List<NifiFlowProcessGroup> groups = getFeedFlows();
        if (groups != null) {
            System.out.println("********************** finished getAllFlows .. construct graph   " + groups.size());
            log.info("********************** getAllFlows  ({})", groups.size());
            groups.stream().forEach(group -> NifiFlowDeserializer.constructGraph(group));
        } else {
            log.info("********************** getAllFlows  (NULL!!!!)");
        }
        return groups;
    }

    public SearchResultsEntity search(String query) {
        Map<String,Object> map = new HashMap<>();
        map.put("q", query);
        return get("/controller/search-results", map, SearchResultsEntity.class);
    }

    public ProcessorDTO findProcessorById(String processorId){
        SearchResultsEntity results = search(processorId);
        //log this
        if(results != null && results.getSearchResultsDTO() != null && results.getSearchResultsDTO().getProcessorResults() != null && !results.getSearchResultsDTO().getProcessorResults().isEmpty()){
            log.info("Attempt to find processor by id {}. Processors Found: {} ",processorId,results.getSearchResultsDTO().getProcessorResults().size());
            ComponentSearchResultDTO processorResult =  results.getSearchResultsDTO().getProcessorResults().get(0);
            String id = processorResult.getId();
            String groupId = processorResult.getGroupId();
            ProcessorEntity processorEntity = getProcessor(groupId,id);

            if(processorEntity != null){
                return processorEntity.getProcessor();
            }
        }
        else {
            log.info("Unable to find Processor in Nifi for id: {}",processorId);
        }
        return null;
    }


    public NifiFlowProcessGroup getFeedFlow(String processGroupId) throws NifiComponentNotFoundException {
        NifiFlowProcessGroup group = null;
        NifiVisitableProcessGroup visitableGroup = getFlowOrder(processGroupId);
        NifiFlowProcessGroup flow = new NifiFlowBuilder().build(visitableGroup);
        String categoryName = flow.getParentGroupName();
        String feedName = flow.getName();
        feedName = FeedNameUtil.fullName(categoryName, feedName);
        //if it is a versioned feed then strip the version to get the correct feed name
        feedName = TemplateCreationHelper.parseVersionedProcessGroupName(feedName);
        flow.setFeedName(feedName);
        return flow;
    }

    public Set<ProcessorDTO> getProcessorsForFlow(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup group = getFlowOrder(processGroupId);
        Set<ProcessorDTO> processors = new HashSet<>();
        for (NifiVisitableProcessor p : group.getStartingProcessors()) {
            processors.addAll(p.getProcessors());
        }
        return processors;
    }


    public NifiFlowProcessGroup getFeedFlowForCategoryAndFeed(String categoryAndFeedName) {
        NifiFlowProcessGroup flow = null;
        String category = FeedNameUtil.category(categoryAndFeedName);
        String feed = FeedNameUtil.feed(categoryAndFeedName);
        //1 find the ProcessGroup under "root" matching the name category
        ProcessGroupDTO processGroupEntity = getRootProcessGroup();
        ProcessGroupDTO root = processGroupEntity;
        ProcessGroupDTO categoryGroup = root.getContents().getProcessGroups().stream().filter(group -> category.equalsIgnoreCase(group.getName())).findAny().orElse(null);
        if (categoryGroup != null) {
            ProcessGroupDTO feedGroup = categoryGroup.getContents().getProcessGroups().stream().filter(group -> feed.equalsIgnoreCase(group.getName())).findAny().orElse(null);
            if (feedGroup != null) {
                flow = getFeedFlow(feedGroup.getId());
            }
        }
        return flow;
    }


    //walk entire graph
    public List<NifiFlowProcessGroup> getFeedFlows() {
        log.info("get Graph of Nifi Flows");
        List<NifiFlowProcessGroup> feedFlows = new ArrayList<>();
        ProcessGroupDTO processGroupEntity = getRootProcessGroup();
        ProcessGroupDTO root = processGroupEntity;
        //first level is the category
        for (ProcessGroupDTO category : root.getContents().getProcessGroups()) {
            for (ProcessGroupDTO feedProcessGroup : category.getContents().getProcessGroups()) {
                //second level is the feed
                String feedName = FeedNameUtil.fullName(category.getName(), feedProcessGroup.getName());
                //if it is a versioned feed then strip the version to get the correct feed name
                feedName = TemplateCreationHelper.parseVersionedProcessGroupName(feedName);
                NifiFlowProcessGroup feedFlow = getFeedFlow(feedProcessGroup.getId());
                feedFlow.setFeedName(feedName);
                feedFlows.add(feedFlow);
            }
        }
        log.info("finished Graph of Nifi Flows.  Returning {} flows", feedFlows.size());
        return feedFlows;
    }



    /**
     * Wallk the flow for a given Root Process Group and return all those Processors who are marked with a Failure Relationship
     */
    public Set<ProcessorDTO> getFailureProcessors(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup g = getFlowOrder(processGroupId);
        Set<ProcessorDTO> failureProcessors = new HashSet<>();
        for (NifiVisitableProcessor p : g.getStartingProcessors()) {

            failureProcessors.addAll(p.getFailureProcessors());
        }

        return failureProcessors;
    }


    public ProvenanceEventEntity replayProvenanceEvent(Long eventId) {
        Form form = new Form();
        form.param("eventId", eventId.toString());
        try {
            Entity controller = getControllerRevision();
            if (controller != null && controller.getRevision() != null) {
                form.param("clientId", controller.getRevision().getClientId());
            }
        } catch (ClientErrorException e) {

        }

        return postForm("/controller/provenance/replays", form, ProvenanceEventEntity.class);
    }


}


