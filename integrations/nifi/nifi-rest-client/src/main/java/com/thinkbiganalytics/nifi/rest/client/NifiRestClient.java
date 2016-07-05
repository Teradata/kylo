package com.thinkbiganalytics.nifi.rest.client;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.feedmgr.CreateFeedBuilder;
import com.thinkbiganalytics.nifi.feedmgr.TemplateInstanceCreator;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateUtil;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.BulletinBoardEntity;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.ControllerStatusEntity;
import org.apache.nifi.web.api.entity.DropRequestEntity;
import org.apache.nifi.web.api.entity.Entity;
import org.apache.nifi.web.api.entity.FlowSnippetEntity;
import org.apache.nifi.web.api.entity.InputPortEntity;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.LineageEntity;
import org.apache.nifi.web.api.entity.ListingRequestEntity;
import org.apache.nifi.web.api.entity.OutputPortEntity;
import org.apache.nifi.web.api.entity.OutputPortsEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupsEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ProvenanceEntity;
import org.apache.nifi.web.api.entity.ProvenanceEventEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

/**
 * Created by sr186054 on 1/9/16.
 *
 * @TODO break up into separate modules for the various units of work (i.e. TemplateRestClient, FeedRestClient,  or out into separate Working classes to make this more readable
 */
@Component
public class NifiRestClient extends JerseyRestClient {

    private String apiPath = "/nifi-api";

    private NifiRestClientConfig clientConfig;

    public NifiRestClient(NifiRestClientConfig config) {
        super(config);
        this.clientConfig = config;
    }

    protected WebTarget getBaseTarget() {
        WebTarget target = super.getBaseTarget();
        return target.path(apiPath);
    }

    public String getClusterType() {
        return clientConfig.getClusterType();
    }


    /**
     * Gets Template data, either a quick view or including all its content
     */
    public TemplatesEntity getTemplates(boolean includeFlow) {

        TemplatesEntity nifiTemplatesEntity = get("/controller/templates", null, TemplatesEntity.class);

        //get the contents and update the returned DTO with the populated snippetDTO
        for (TemplateDTO dto : ImmutableSet.copyOf(nifiTemplatesEntity.getTemplates())) {
            if (includeFlow) {
                nifiTemplatesEntity.getTemplates().remove(dto);
                TemplateDTO populatedDto = populateTemplateDTO(dto);
                nifiTemplatesEntity.getTemplates().add(populatedDto);
            }
        }
        return nifiTemplatesEntity;

    }

    public TemplateEntity deleteTemplate(String templateId) {
        return delete("/controller/templates/" + templateId, null, TemplateEntity.class);
    }


    public TemplateDTO importTemplate(String templateXml) throws IOException {
        return importTemplate(null, templateXml);
    }

    public TemplateDTO importTemplate(String templateName, String templateXml) throws IOException {
        if (templateName == null) {
            templateName = "import_template_" + System.currentTimeMillis();
        }

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        File tmpFile = File.createTempFile(templateName, ".xml");
        FileUtils.writeStringToFile(tmpFile, templateXml);

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("template", tmpFile,
                                                                 MediaType.APPLICATION_OCTET_STREAM_TYPE);
        multiPart.bodyPart(fileDataBodyPart);
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        TemplateEntity templateEntity = postMultiPart("/controller/templates", multiPart, TemplateEntity.class);
        if (templateEntity != null) {
            return templateEntity.getTemplate();
        }
        return null;
    }

    /**
     * Populate a Template with the contents of its Flow
     */
    private TemplateDTO populateTemplateDTO(TemplateDTO dto) {
        if (dto.getSnippet() == null) {
            TemplateDTO populatedDto = getTemplateById(dto.getId());
            populatedDto.setId(dto.getId());
            populatedDto.setUri(dto.getUri());
            populatedDto.setDescription(dto.getDescription());
            return populatedDto;
        } else {
            return dto;
        }
    }


    /**
     * return the Template as an XML string
     */
    public String getTemplateXml(String templateId) throws NifiComponentNotFoundException {
        try {
            String xml = get("/controller/templates/" + templateId, null, String.class);
            return xml;
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(templateId, NifiConstants.NIFI_COMPONENT_TYPE.TEMPLATE, e);
        }
    }


    /**
     * Return a template, populated along with its Flow snippet
     */
    public TemplateDTO getTemplateById(String templateId) throws NifiComponentNotFoundException {
        try {
            TemplateDTO dto = get("/controller/templates/" + templateId, null, TemplateDTO.class);
            return dto;
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(templateId, NifiConstants.NIFI_COMPONENT_TYPE.TEMPLATE, e);
        }
    }

    /**
     * return a template by Name, populated with its Flow snippet If not found it returns null
     */
    public TemplateDTO getTemplateByName(String templateName) {
        TemplatesEntity templatesEntity = getTemplates(false);
        TemplateDTO templateDTO = null;
        if (templatesEntity.getTemplates() != null && !templatesEntity.getTemplates().isEmpty()) {
            for (TemplateDTO dto : templatesEntity.getTemplates()) {
                if (dto.getName().equalsIgnoreCase(templateName)) {
                    templateDTO = populateTemplateDTO(dto);
                    break;
                }
            }
        }

        return templateDTO;
    }

    public FlowSnippetEntity instantiateFlowFromTemplate(String processGroupId, String templateId) throws NifiComponentNotFoundException {
        try {
            Entity status = getControllerRevision();
            String clientId = status.getRevision().getClientId();
            String originX = "10";
            String originY = "10";
            Form form = new Form();
            form.param("templateId", templateId);
            form.param("clientId", clientId);
            form.param("originX", originX);
            form.param("originY", originY);
            form.param("version", status.getRevision().getVersion().toString());
            FlowSnippetEntity
                response =
                postForm("/controller/process-groups/" + processGroupId + "/template-instance", form, FlowSnippetEntity.class);
            return response;
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(
                "Unable to create Template instance for templateId: " + templateId + " under Process group " + processGroupId + ".  Unable find the processGroup or template");
        }
    }

    public NifiProcessGroup createNewTemplateInstance(String templateId, Map<String, Object> staticConfigProperties, boolean createReusableFlow) {
        TemplateInstanceCreator creator = new TemplateInstanceCreator(this, templateId, staticConfigProperties, createReusableFlow);
        return creator.createTemplate();
    }

    public ControllerStatusEntity getControllerStatus() {
        return get("/controller/status", null, ControllerStatusEntity.class);
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
        ProcessGroupEntity rootProcessGroup = getProcessGroup("root", false, false);
        return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup.getProcessGroup(), dto);
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
        ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, false, true);
        Set<PortDTO> inputPorts = processGroupEntity.getProcessGroup().getContents().getInputPorts();
        if (inputPorts != null) {
            ports.addAll(inputPorts);
        }
        Set<PortDTO> outputPorts = processGroupEntity.getProcessGroup().getContents().getOutputPorts();
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
        ProcessGroupEntity rootProcessGroup = getProcessGroup("root", false, false);
        return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup.getProcessGroup(), dto);
    }

    /**
     * Returns an Empty ArrayList of nothing is found
     */
    public List<NifiProperty> getAllProperties() throws NifiComponentNotFoundException {
        ProcessGroupEntity root = getRootProcessGroup();
        return NifiPropertyUtil.getProperties(root.getProcessGroup());
    }


    public List<NifiProperty> getPropertiesForProcessGroup(String processGroupId) throws NifiComponentNotFoundException {
        ProcessGroupEntity processGroup = getProcessGroup(processGroupId, true, true);
        return NifiPropertyUtil.getProperties(processGroup.getProcessGroup());
    }

    private void updateEntityForSave(Entity entity) {
        Entity status = getControllerRevision();
        entity.setRevision(status.getRevision());
    }

    public ProcessGroupEntity createProcessGroup(String name) {

        return createProcessGroup("root", name);
    }

    public ProcessGroupEntity createProcessGroup(String parentGroupId, String name) throws NifiComponentNotFoundException {

        ProcessGroupEntity entity = new ProcessGroupEntity();
        ProcessGroupDTO group = new ProcessGroupDTO();
        group.setName(name);
        updateEntityForSave(entity);
        try {
            entity.setProcessGroup(group);
            ProcessGroupEntity
                returnedGroup =
                post("/controller/process-groups/" + parentGroupId + "/process-group-references", entity, ProcessGroupEntity.class);
            return returnedGroup;
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }

    }

    /**
     * //mark everything as running //http://localhost:8079/nifi-api/controller/process-groups/2f0e55cb-34af-4e5b-8bf9-38909ea3af51/processors/bbef9df7-ff67-49fb-aa2e-3200ece92128
     */
    public List<ProcessorDTO> markProcessorsAsRunning(List<ProcessorDTO> processors) {
        Entity status = getControllerRevision();
        List<ProcessorDTO> dtos = new ArrayList<>();
        for (ProcessorDTO dto : processors) {
            if (NifiProcessUtil.PROCESS_STATE.STOPPED.name().equalsIgnoreCase(dto.getState())) {
                //start it
                ProcessorEntity entity = new ProcessorEntity();
                dto.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                entity.setProcessor(dto);
                entity.setRevision(status.getRevision());

                updateEntityForSave(entity);
                ProcessorEntity
                    processorEntity =
                    put("/controller/process-groups/" + dto.getParentGroupId() + "/processors/" + dto.getId(), entity,
                        ProcessorEntity.class);
                if (processorEntity != null) {
                    dtos.add(processorEntity.getProcessor());
                }
            }
        }
        return dtos;

    }

    public ProcessGroupEntity markProcessorGroupAsRunning(ProcessGroupDTO groupDTO) {
        ProcessGroupEntity entity = new ProcessGroupEntity();
        entity.setProcessGroup(groupDTO);
        entity.getProcessGroup().setRunning(true);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),
                   entity, ProcessGroupEntity.class);


    }


    public NifiProcessGroup createTemplateInstanceAsProcessGroup(String templateId, String category, String feedName,
                                                                 String inputProcessorType, List<NifiProperty> properties,
                                                                 NifiProcessorSchedule feedSchedule) {
        return CreateFeedBuilder.newFeed(this, category, feedName, templateId).inputProcessorType(inputProcessorType)
            .feedSchedule(feedSchedule).properties(properties).build();
    }

    public CreateFeedBuilder newFeedBuilder(String templateId, String category, String feedName) {
        return CreateFeedBuilder.newFeed(this, category, feedName, templateId);
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
        InputPortsEntity inputPorts = getInputPorts(groupDTO.getId());
        if (inputPorts != null) {
            for (PortDTO port : inputPorts.getInputPorts()) {
                stopInputPort(groupDTO.getId(), port.getId());
            }
        }
    }

    public ProcessGroupEntity stopInputs(String processGroupId) {
        ProcessGroupEntity entity = getProcessGroup(processGroupId, false, true);
        if (entity != null && entity.getProcessGroup() != null) {
            stopInputs(entity.getProcessGroup());
            return entity;
        }
        return null;
    }

    public ProcessGroupEntity stopAllProcessors(String processGroupId, String parentProcessGroupId) throws NifiClientRuntimeException {
        ProcessGroupEntity entity = getProcessGroup(processGroupId, false, false);
        entity.getProcessGroup().setRunning(false);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + parentProcessGroupId + "/process-group-references/" + processGroupId,
                   entity, ProcessGroupEntity.class);
    }


    public ProcessGroupEntity startAll(String processGroupId, String parentProcessGroupId) throws NifiClientRuntimeException {
        ProcessGroupEntity entity = getProcessGroup(processGroupId, false, false);
        entity.getProcessGroup().setRunning(true);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + parentProcessGroupId + "/process-group-references/" + processGroupId,
                   entity, ProcessGroupEntity.class);
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

    public ProcessGroupEntity deleteProcessGroup(ProcessGroupDTO groupDTO) throws NifiClientRuntimeException {
        return deleteProcessGroup(groupDTO, null);
    }


    private ProcessGroupEntity deleteProcessGroup(ProcessGroupDTO groupDTO, Integer retryAttempt) throws NifiClientRuntimeException {

        if (retryAttempt == null) {
            retryAttempt = 0;
        }
        ProcessGroupEntity entity = stopProcessGroup(groupDTO);
        try {
            entity = doDeleteProcessGroup(entity.getProcessGroup());

        } catch (WebApplicationException e) {
            NifiClientRuntimeException clientException = new NifiClientRuntimeException(e);
            if (clientException.is409Error() && retryAttempt < 2) {
                //wait and retry?
                retryAttempt++;
                try {
                    Thread.sleep(300);
                    deleteProcessGroup(groupDTO, retryAttempt);
                } catch (InterruptedException e2) {
                    throw new NifiClientRuntimeException("Unable to delete Process Group " + groupDTO.getName(), e2);
                }
            } else {
                throw clientException;
            }
        }
        return entity;


    }

    public ProcessGroupEntity stopProcessGroup(ProcessGroupDTO groupDTO) throws NifiClientRuntimeException {
        ProcessGroupEntity entity = new ProcessGroupEntity();
        entity.setProcessGroup(groupDTO);
        entity.getProcessGroup().setRunning(false);
        entity.getProcessGroup().setRunning(false);
        updateEntityForSave(entity);
        return put("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),
                   entity, ProcessGroupEntity.class);
    }

    private ProcessGroupEntity doDeleteProcessGroup(ProcessGroupDTO groupDTO) throws NifiClientRuntimeException {
        Entity status = getControllerRevision();
        Map<String, Object> params = getUpdateParams();
        ProcessGroupEntity
            entity =
            delete("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),
                   params, ProcessGroupEntity.class);
        return entity;

    }


    public List<ProcessGroupEntity> deleteChildProcessGroups(String processGroupId) throws NifiClientRuntimeException {
        List<ProcessGroupEntity> deletedEntities = new ArrayList<>();
        ProcessGroupEntity entity = getProcessGroup(processGroupId, true, true);
        if (entity != null && entity.getProcessGroup().getContents().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : entity.getProcessGroup().getContents().getProcessGroups()) {
                deletedEntities.add(deleteProcessGroup(groupDTO));
            }
        }
        return deletedEntities;

    }

    public ProcessGroupEntity deleteProcessGroup(String processGroupId) throws NifiClientRuntimeException {
        ProcessGroupEntity entity = getProcessGroup(processGroupId, false, true);
        ProcessGroupEntity deletedEntity = null;
        if (entity != null && entity.getProcessGroup() != null) {
            deletedEntity = deleteProcessGroup(entity.getProcessGroup());
        }

        return deletedEntity;
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

    public void deleteControllerService(String controllerServiceId) throws NifiClientRuntimeException {

        try { //http://localhost:8079/nifi-api/controller/controller-services/node/3c475f44-b038-4cb0-be51-65948de72764?version=1210&clientId=86af0022-9ba6-40b9-ad73-6d757b6f8d25
            Map<String, Object> params = getUpdateParams();
            delete("/controller/controller-services/" + getClusterType() + "/" + controllerServiceId, params, ControllerServiceEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(controllerServiceId, NifiConstants.NIFI_COMPONENT_TYPE.CONTROLLER_SERVICE, e);
        }
    }

    public void deleteControllerServices(Collection<ControllerServiceDTO> services) throws NifiClientRuntimeException {
        //http://localhost:8079/nifi-api/controller/controller-services/node/3c475f44-b038-4cb0-be51-65948de72764?version=1210&clientId=86af0022-9ba6-40b9-ad73-6d757b6f8d25
        Set<String> unableToDelete = new HashSet<>();
        for (ControllerServiceDTO dto : services) {
            try {
                deleteControllerService(dto.getId());
            } catch (NifiClientRuntimeException e) {
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

    public ConnectionsEntity getProcessGroupConnections(String processGroupId) throws NifiComponentNotFoundException {
        try {
            return get("/controller/process-groups/" + processGroupId + "/connections", null, ConnectionsEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    public void removeConnectionsToProcessGroup(String parentProcessGroupId, final String processGroupId) {
        ConnectionsEntity connectionsEntity = getProcessGroupConnections(parentProcessGroupId);
        if (connectionsEntity != null && connectionsEntity.getConnections() != null) {
            List<ConnectionDTO> connections = Lists.newArrayList(Iterables.filter(connectionsEntity.getConnections(), new Predicate<ConnectionDTO>() {
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

    public ProcessGroupEntity getProcessGroup(String processGroupId, boolean recursive, boolean verbose) throws NifiComponentNotFoundException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("recursive", recursive);
            params.put("verbose", verbose);
            return get("controller/process-groups/" + processGroupId, params, ProcessGroupEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    /**
     * if the parentGroup is found but it cannot find the group by Name then it will return null
     */
    public ProcessGroupDTO getProcessGroupByName(String parentGroupId, final String groupName) throws NifiComponentNotFoundException {

        ProcessGroupsEntity
            groups = null;
        try {
            groups =

                get("/controller/process-groups/" + parentGroupId + "/process-group-references", null, ProcessGroupsEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(groupName, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
        if (groups != null) {
            List<ProcessGroupDTO>
                list =
                Lists.newArrayList(Iterables.filter(groups.getProcessGroups(), new Predicate<ProcessGroupDTO>() {
                    @Override
                    public boolean apply(ProcessGroupDTO groupDTO) {
                        return groupDTO.getName().equalsIgnoreCase(groupName);
                    }
                }));
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;

    }


    public ProcessGroupEntity getRootProcessGroup() throws NifiComponentNotFoundException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("recursive", true);
            params.put("verbose", true);
            return get("controller/process-groups/root", params, ProcessGroupEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("root", NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
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
     * Marks the source processor as Running based upon the type DISABLEs the other source processors
     */
    public void setInputAsRunningByProcessorMatchingType(String processGroupId, String type) throws NifiComponentNotFoundException {
        //get the Source Processors
        List<ProcessorDTO> processorDTOs = getInputProcessors(processGroupId);
        if (StringUtils.isBlank(type)) {
            //start the first one it finds
            type = processorDTOs.get(0).getType();
        }
        ProcessorDTO processorDTO = NifiProcessUtil.findFirstProcessorsByType(processorDTOs, type);
        //Mark all that dont match type as DISABLED.  Start the other one
        boolean update = false;
        for (ProcessorDTO dto : processorDTOs) {
            update = false;
            //fetch the processor and update it
            if (!dto.equals(processorDTO) && !NifiProcessUtil.PROCESS_STATE.DISABLED.name().equals(dto.getState())) {
                dto.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                update = true;
            } else if (dto.equals(processorDTO) && !NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(dto.getState())) {
                update = true;
                if (NifiProcessUtil.PROCESS_STATE.DISABLED.name().equals(dto.getState())) {
                    //if its disabled you need to stop it first before making it running
                    //this is needed on rollback
                    stopProcessor(dto.getParentGroupId(), dto.getId());
                }
                dto.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
            }
            if (update) {
                ProcessorEntity entity = new ProcessorEntity();
                ProcessorDTO updateDto = new ProcessorDTO();
                updateDto.setId(dto.getId());
                updateDto.setParentGroupId(dto.getParentGroupId());
                updateDto.setState(dto.getState());
                entity.setProcessor(updateDto);
                updateProcessor(entity);
            }
        }

    }

    /**
     * return the Source Processors for a given group
     */
    public List<ProcessorDTO> getInputProcessors(String processGroupId) throws NifiComponentNotFoundException {
        //get the group
        ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, false, true);
        //get the Source Processors
        List<String> sourceIds = getInputProcessorIds(processGroupId);
        return NifiProcessUtil.findProcessorsByIds(processGroupEntity.getProcessGroup().getContents().getProcessors(), sourceIds);
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
        ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, false, true);
        //get the Source Processors
        List<String> sourceIds = getEndingProcessorIds(processGroupId);

        return NifiProcessUtil.findProcessorsByIds(processGroupEntity.getProcessGroup().getContents().getProcessors(), sourceIds);
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


    public ProcessGroupEntity updateProcessGroup(ProcessGroupEntity processGroupEntity) {
        updateEntityForSave(processGroupEntity);
        return put("/controller/process-groups/" + processGroupEntity.getProcessGroup().getId(), processGroupEntity,
                   ProcessGroupEntity.class);
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

    public ControllerServicesEntity getControllerServices() {
        return getControllerServices(null);
    }

    public ControllerServicesEntity getControllerServices(String type) {

        if (StringUtils.isBlank(type)) {
            type = getClusterType();
        }
        return get("/controller/controller-services/" + type, null, ControllerServicesEntity.class);
    }

    public ControllerServiceEntity getControllerService(String type, String id) throws NifiComponentNotFoundException {
        try {
            if (StringUtils.isBlank(type)) {
                type = getClusterType();
            }
            return get("/controller/controller-services/" + type + "/" + id, null, ControllerServiceEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(id, NifiConstants.NIFI_COMPONENT_TYPE.CONTROLLER_SERVICE, e);
        }
    }

    /**
     * Returns Null if cant find it
     */
    public ControllerServiceDTO getControllerServiceByName(String type, final String serviceName) {
        ControllerServiceDTO controllerService = null;

        ControllerServicesEntity entity = getControllerServices(type);
        if (entity != null) {
            List<ControllerServiceDTO> services = Lists.newArrayList(Iterables.filter(entity.getControllerServices(), new Predicate<ControllerServiceDTO>() {
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

    //http://localhost:8079/nifi-api/controller/controller-services/node/edfe9a53-4fde-4437-a798-1305830c15ac
    public ControllerServiceEntity enableControllerService(String id) throws NifiClientRuntimeException {
        ControllerServiceEntity entity = getControllerService(null, id);
        ControllerServiceDTO dto = entity.getControllerService();
        if (!dto.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())) {
            dto.setState(NifiProcessUtil.SERVICE_STATE.ENABLED.name());

            entity.setControllerService(dto);
            updateEntityForSave(entity);
            return put("/controller/controller-services/" + getClusterType() + "/" + id, entity, ControllerServiceEntity.class);
        } else {
            return entity;
        }
    }

    public ControllerServiceEntity disableControllerService(String id) throws NifiComponentNotFoundException {
        ControllerServiceEntity entity = new ControllerServiceEntity();
        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setState(NifiProcessUtil.SERVICE_STATE.DISABLED.name());
        entity.setControllerService(dto);
        updateEntityForSave(entity);
        return put("/controller/controller-services/" + getClusterType() + "/" + id, entity, ControllerServiceEntity.class);
    }


    public ControllerServiceTypesEntity getControllerServiceTypes() {
        return get("/controller/controller-service-types", null, ControllerServiceTypesEntity.class);
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

    public InputPortsEntity getInputPorts(String groupId) throws NifiComponentNotFoundException {
        try {
            return get("/controller/process-groups/" + groupId + "/input-ports/", null, InputPortsEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(groupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    public OutputPortEntity getOutputPort(String groupId, String portId) {
        try {
            return get("/controller/process-groups/" + groupId + "/output-ports/" + portId, null, OutputPortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("Unable to find Output Port " + portId + " Under Process Group " + groupId, NifiConstants.NIFI_COMPONENT_TYPE.OUTPUT_PORT, e);
        }
    }

    public OutputPortsEntity getOutputPorts(String groupId) throws NifiComponentNotFoundException {
        try {
            return get("/controller/process-groups/" + groupId + "/output-ports", null, OutputPortsEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(groupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    public List<ConnectionDTO> findAllConnectionsMatchingDestinationId(String parentGroupId, String destinationId) throws NifiComponentNotFoundException {
        //get this parentGroup and find all connections under this parent that relate to this inputPortId
        //1. get this processgroup
        ProcessGroupEntity parentGroup = getProcessGroup(parentGroupId, false, false);
        //2 get the parent
        String parent = parentGroup.getProcessGroup().getParentGroupId();
        Set<ConnectionDTO> connectionDTOs = findConnectionsForParent(parent);
        List<ConnectionDTO> matchingConnections = NifiConnectionUtil.findConnectionsMatchingDestinationId(connectionDTOs,
                                                                                                          destinationId);

        return matchingConnections;

    }

    public void createReusableTemplateInputPort(String reusableTemplateCategoryGroupId, String reusableTemplateGroupId) throws NifiComponentNotFoundException {
        ProcessGroupEntity reusableTemplateGroup = getProcessGroup(reusableTemplateGroupId, false, false);
        ProcessGroupEntity reusableTemplateCategoryGroup = getProcessGroup(reusableTemplateCategoryGroupId, false, false);
        InputPortsEntity inputPortsEntity = getInputPorts(reusableTemplateGroupId);
        if (inputPortsEntity != null) {
            for (PortDTO inputPort : inputPortsEntity.getInputPorts()) {
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
        InputPortsEntity inputPortsEntity = getInputPorts(reusableTemplateCategoryGroupId);
        PortDTO inputPort = NifiConnectionUtil.findPortMatchingName(inputPortsEntity.getInputPorts(), inputPortName);
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
        InputPortsEntity templatePorts = getInputPorts(reusableTemplateGroupId);
        templateInputPort = NifiConnectionUtil.findPortMatchingName(templatePorts.getInputPorts(), inputPortName);

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
        ProcessGroupEntity categoryProcessGroup = getProcessGroup(categoryProcessGroupId, false, false);
        ProcessGroupEntity feedProcessGroup = getProcessGroup(feedProcessGroupId, false, false);
        ProcessGroupEntity categoryParent = getProcessGroup(categoryProcessGroup.getProcessGroup().getParentGroupId(), false, false);
        ProcessGroupEntity reusableTemplateCategoryGroup = getProcessGroup(reusableTemplateCategoryGroupId, false, false);

        //Go into the Feed and find the output port that is to be associated with the global template
        OutputPortsEntity outputPortsEntity = getOutputPorts(feedProcessGroupId);
        PortDTO feedOutputPort = null;
        if (outputPortsEntity != null) {
            feedOutputPort = NifiConnectionUtil.findPortMatchingName(outputPortsEntity.getOutputPorts(), feedOutputName);
        }
        if (feedOutputPort == null) {
            //ERROR  This feed needs to have an output port assigned on it to make the connection
        }

        InputPortsEntity inputPortsEntity = getInputPorts(reusableTemplateCategoryGroupId);
        PortDTO inputPort = NifiConnectionUtil.findPortMatchingName(inputPortsEntity.getInputPorts(), inputPortName);
        String inputPortId = inputPort.getId();

        final String categoryOutputPortName = categoryProcessGroup.getProcessGroup().getName() + " to " + inputPort.getName();

        //Find or create the output port that will join to the globabl template at the Category Level

        OutputPortsEntity categoryOutputPortsEntity = getOutputPorts(categoryProcessGroupId);
        PortDTO categoryOutputPort = null;
        if (categoryOutputPortsEntity != null) {
            categoryOutputPort =
                NifiConnectionUtil.findPortMatchingName(categoryOutputPortsEntity.getOutputPorts(), categoryOutputPortName);
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
            source.setName(feedProcessGroup.getProcessGroup().getName());
            source.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
            ConnectableDTO dest = new ConnectableDTO();
            dest.setGroupId(categoryProcessGroupId);
            dest.setName(categoryOutputPort.getName());
            dest.setId(categoryOutputPort.getId());
            dest.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
            createConnection(categoryProcessGroup.getProcessGroup().getId(), source, dest);
        }

        ConnectionDTO
            categoryToReusableTemplateConnection =
            NifiConnectionUtil
                .findConnection(findConnectionsForParent(categoryParent.getProcessGroup().getId()), categoryOutputPort.getId(),
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
            createConnection(categoryParent.getProcessGroup().getId(), categorySource, categoryToGlobalTemplate);
        }

    }


    public Set<ConnectionDTO> findConnectionsForParent(String parentProcessGroupId) throws NifiComponentNotFoundException {
        Set<ConnectionDTO> connections = new HashSet<>();
        //get all connections under parent group
        ConnectionsEntity connectionsEntity = getProcessGroupConnections(parentProcessGroupId);
        if (connectionsEntity != null) {
            connections = connectionsEntity.getConnections();
        }
        return connections;
    }


    public ConnectionDTO findConnection(String parentProcessGroupId, final String sourceProcessGroupId,
                                        final String destProcessGroupId) throws NifiComponentNotFoundException {
        return NifiConnectionUtil.findConnection(findConnectionsForParent(parentProcessGroupId), sourceProcessGroupId,
                                                 parentProcessGroupId);
    }


    public ConnectionEntity createConnection(String processGroupId, ConnectableDTO source, ConnectableDTO dest) {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setSource(source);
        connectionDTO.setDestination(dest);
        connectionDTO.setName(source.getName() + " - " + dest.getName());
        ConnectionEntity connectionEntity = new ConnectionEntity();
        connectionEntity.setConnection(connectionDTO);
        updateEntityForSave(connectionEntity);
        return post("/controller/process-groups/" + processGroupId + "/connections", connectionEntity, ConnectionEntity.class);
    }


    public NifiVisitableProcessGroup getFlowOrder(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup group = null;
        ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, true, true);
        if (processGroupEntity != null) {
            group = new NifiVisitableProcessGroup(processGroupEntity.getProcessGroup());
            NifiConnectionOrderVisitor orderVisitor = new NifiConnectionOrderVisitor(this, group);
            group.accept(orderVisitor);
            //orderVisitor.printOrder();
        }
        return group;
    }

    public Set<ProcessorDTO> getProcessorsForFlow(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup group = getFlowOrder(processGroupId);
        Set<ProcessorDTO> processors = new HashSet<>();
        for (NifiVisitableProcessor p : group.getStartingProcessors()) {
            processors.addAll(p.getProcessors());
        }
        return processors;
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


