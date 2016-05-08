package com.thinkbiganalytics.nifi.rest.client;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
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
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyClientException;
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
import org.apache.nifi.web.api.entity.*;
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

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

/**
 * Created by sr186054 on 1/9/16.
 *
 * @TODO break up into separate modules for the various units of work (i.e. TemplateRestClient, FeedRestClient,  or out into
 * separate Working classes to make this more readable
 */
@Component
public class NifiRestClient extends JerseyRestClient {

  private String apiPath = "/nifi-api";

  public NifiRestClient(JerseyClientConfig config) {
    super(config);
  }

  protected WebTarget getBaseTarget() {
    WebTarget target = super.getBaseTarget();
    return target.path(apiPath);
  }


  /**
   * Gets Template data, either a quick view or including all its content
   */
  public TemplatesEntity getTemplates(boolean includeFlow) throws JerseyClientException {

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

  public TemplateEntity deleteTemplate(String templateId) throws JerseyClientException {
    return delete("/controller/templates/"+templateId,null,TemplateEntity.class);
  }


  public TemplateDTO importTemplate(String templateXml) throws JerseyClientException, IOException {
    return importTemplate(null,templateXml);
  }

  public TemplateDTO importTemplate(String templateName,String templateXml) throws JerseyClientException, IOException {
  if(templateName == null){
    templateName= "import_template_"+System.currentTimeMillis();
  }

    MultiPart multiPart = new MultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
    File tmpFile = File.createTempFile(templateName,".xml");
    FileUtils.writeStringToFile(tmpFile,templateXml);

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("template",tmpFile,
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    multiPart.bodyPart(fileDataBodyPart);
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

    TemplateEntity templateEntity = postMultiPart("/controller/templates",multiPart, TemplateEntity.class);
     if(templateEntity != null){
       return templateEntity.getTemplate();
     }
    return null;
  }

  /**
   * Populate a Template with the contents of its Flow
   */
  private TemplateDTO populateTemplateDTO(TemplateDTO dto) throws JerseyClientException {
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
  public String getTemplateXml(String templateId) throws JerseyClientException {
    String xml = get("/controller/templates/" + templateId, null, String.class);
    return xml;
  }


  /**
   * Return a template, populated along with its Flow snippet
   */
  public TemplateDTO getTemplateById(String templateId) throws JerseyClientException {
    TemplateDTO dto = get("/controller/templates/" + templateId, null, TemplateDTO.class);
    return dto;
  }

  /**
   * return a template by Name, populated with its Flow snippet
   */
  public TemplateDTO getTemplateByName(String templateName) throws JerseyClientException {
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

  public FlowSnippetEntity instantiateFlowFromTemplate(String processGroupId, String templateId) throws JerseyClientException {
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
  }

  public NifiProcessGroup createNewTemplateInstance(String templateId) throws JerseyClientException {
    TemplateInstanceCreator creator = new TemplateInstanceCreator(this,templateId);
    return creator.createTemplate();
  }

  public ControllerStatusEntity getControllerStatus() throws JerseyClientException {
    return get("/controller/status", null, ControllerStatusEntity.class);
  }

  /**
   * Gets the current Revision and Version of Nifi instance. This is needed when performing an update to pass over the
   * revision.getVersion() for locking purposes
   */
  public Entity getControllerRevision() throws JerseyClientException {
    return get("/controller/revision", null, Entity.class);
  }

  /**
   * Expose all Properties for a given Template as parameters for external use
   */
  public List<NifiProperty> getPropertiesForTemplate(String templateId) throws JerseyClientException {
    TemplateDTO dto = getTemplateById(templateId);
    ProcessGroupEntity rootProcessGroup = getProcessGroup("root", false, false);
    return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup.getProcessGroup(), dto);
  }


  public Set<PortDTO> getPortsForTemplate(String templateId) throws JerseyClientException {
    Set<PortDTO> ports = new HashSet<>();
    TemplateDTO dto = getTemplateById(templateId);
    Set<PortDTO> inputPorts = dto.getSnippet().getInputPorts();
    if(inputPorts != null){
      ports.addAll(inputPorts);
    }
    Set<PortDTO> outputPorts = dto.getSnippet().getOutputPorts();
    if(outputPorts != null){
      ports.addAll(outputPorts);
    }
   return ports;
  }

  public Set<PortDTO> getPortsForProcessGroup(String processGroupId) throws JerseyClientException {
    Set<PortDTO> ports = new HashSet<>();
    ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, false, true);
    Set<PortDTO> inputPorts = processGroupEntity.getProcessGroup().getContents().getInputPorts();
    if(inputPorts != null){
      ports.addAll(inputPorts);
    }
    Set<PortDTO> outputPorts = processGroupEntity.getProcessGroup().getContents().getOutputPorts();
    if(outputPorts != null){
      ports.addAll(outputPorts);
    }
    return ports;
  }

  /**
   * Expose all Properties for a given Template as parameters for external use
   */
  public List<NifiProperty> getPropertiesForTemplateByName(String templateName) throws JerseyClientException {
    TemplateDTO dto = getTemplateByName(templateName);
    ProcessGroupEntity rootProcessGroup = getProcessGroup("root", false, false);
    return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup.getProcessGroup(), dto);
  }

  /**
   * get All properties
   */
  public List<NifiProperty> getAllProperties() throws JerseyClientException {
    ProcessGroupEntity root = getRootProcessGroup();
    return NifiPropertyUtil.getProperties(root.getProcessGroup());
  }


  /**
   * get All properties
   */
  public List<NifiProperty> getPropertiesForProcessGroup(String processGroupId) throws JerseyClientException {
    ProcessGroupEntity processGroup = getProcessGroup(processGroupId, true, true);
    return NifiPropertyUtil.getProperties(processGroup.getProcessGroup());
  }

  private void updateEntityForSave(Entity entity) throws JerseyClientException {
    Entity status = getControllerRevision();
    entity.setRevision(status.getRevision());
  }

  public ProcessGroupEntity createProcessGroup(String name) throws JerseyClientException {

    return createProcessGroup("root", name);
  }

  public ProcessGroupEntity createProcessGroup(String parentGroupId, String name) throws JerseyClientException {

    ProcessGroupEntity entity = new ProcessGroupEntity();
    ProcessGroupDTO group = new ProcessGroupDTO();
    group.setName(name);
    updateEntityForSave(entity);
    entity.setProcessGroup(group);
    ProcessGroupEntity
        returnedGroup =
        post("/controller/process-groups/" + parentGroupId + "/process-group-references", entity, ProcessGroupEntity.class);
    return returnedGroup;
  }

  /**
   * //mark everything as running //http://localhost:8079/nifi-api/controller/process-groups/2f0e55cb-34af-4e5b-8bf9-38909ea3af51/processors/bbef9df7-ff67-49fb-aa2e-3200ece92128
   */
  public List<ProcessorDTO> markProcessorsAsRunning(List<ProcessorDTO> processors) throws JerseyClientException {
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

  public ProcessGroupEntity markProcessorGroupAsRunning(ProcessGroupDTO groupDTO) throws JerseyClientException {
    ProcessGroupEntity entity = new ProcessGroupEntity();
    entity.setProcessGroup(groupDTO);
    entity.getProcessGroup().setRunning(true);
    updateEntityForSave(entity);
    return put("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),
               entity, ProcessGroupEntity.class);


  }


  public NifiProcessGroup createTemplateInstanceAsProcessGroup(String templateId, String category, String feedName,
                                                               String inputProcessorType, List<NifiProperty> properties,
                                                               NifiProcessorSchedule feedSchedule) throws JerseyClientException {
    return CreateFeedBuilder.newFeed(this, category, feedName, templateId).inputProcessorType(inputProcessorType)
        .feedSchedule(feedSchedule).properties(properties).build();
  }

  public CreateFeedBuilder newFeedBuilder(String templateId, String category, String feedName) {
    return CreateFeedBuilder.newFeed(this, category, feedName, templateId);
  }


  private Map<String, Object> getUpdateParams() throws JerseyClientException {
    Entity status = getControllerRevision();
    Map<String, Object> params = new HashMap<>();
    params.put("version", status.getRevision().getVersion().toString());
    params.put("clientId", status.getRevision().getClientId());
    return params;
  }


  public void stopAllProcessors(ProcessGroupDTO groupDTO) throws JerseyClientException {
   stopAllProcessors(groupDTO.getId(), groupDTO.getParentGroupId());
  }

  public ProcessGroupEntity stopAllProcessors(String processGroupId, String parentProcessGroupId) throws JerseyClientException {
    ProcessGroupEntity entity = getProcessGroup(processGroupId,false,false);
    entity.getProcessGroup().setRunning(false);
    updateEntityForSave(entity);
    return put("/controller/process-groups/" + parentProcessGroupId + "/process-group-references/" + processGroupId,
               entity, ProcessGroupEntity.class);
  }


  public ProcessGroupEntity startAll(String processGroupId, String parentProcessGroupId) throws JerseyClientException {
    ProcessGroupEntity entity = getProcessGroup(processGroupId,false,false);
    entity.getProcessGroup().setRunning(true);
    updateEntityForSave(entity);
    return put("/controller/process-groups/" + parentProcessGroupId + "/process-group-references/" + processGroupId,
               entity, ProcessGroupEntity.class);
  }

  public InputPortEntity stopInputPort(String groupId, String portId) throws JerseyClientException {
    InputPortEntity portEntity = getInputPort(groupId, portId);
    portEntity.getInputPort().setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
    updateEntityForSave(portEntity);
    return put("/controller/process-groups/"+groupId+"/input-ports/"+portId,portEntity,InputPortEntity.class);
  }

  public OutputPortEntity stopOutputPort(String groupId, String portId) throws JerseyClientException {
    OutputPortEntity portEntity = getOutputPort(groupId, portId);
    portEntity.getOutputPort().setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
    updateEntityForSave(portEntity);
    return put("/controller/process-groups/"+groupId+"/output-ports/"+portId,portEntity,OutputPortEntity.class);
  }

  public InputPortEntity startInputPort(String groupId, String portId) throws JerseyClientException {
    InputPortEntity portEntity = getInputPort(groupId, portId);
    portEntity.getInputPort().setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
    updateEntityForSave(portEntity);
    return put("/controller/process-groups/"+groupId+"/input-ports/"+portId,portEntity,InputPortEntity.class);
  }

  public OutputPortEntity startOutputPort(String groupId, String portId) throws JerseyClientException {
    OutputPortEntity portEntity = getOutputPort(groupId, portId);
    portEntity.getOutputPort().setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
    updateEntityForSave(portEntity);
    return put("/controller/process-groups/"+groupId+"/output-ports/"+portId,portEntity,OutputPortEntity.class);
  }

  public ProcessGroupEntity deleteProcessGroup(ProcessGroupDTO groupDTO) throws JerseyClientException {
    ProcessGroupEntity deleteEntity = new ProcessGroupEntity();
    deleteEntity.setProcessGroup(groupDTO);
    deleteEntity.getProcessGroup().setRunning(false);
    updateEntityForSave(deleteEntity);
    put("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),
        deleteEntity, ProcessGroupEntity.class);

    //PUT //http://localhost:8079/nifi-api/controller/process-groups/00d886f3-a9ad-4302-9572-5f160d72bd81/process-group-references/d9dbdce4-7d01-498a-962e-f3f8ab924bae
    //running= false
    //http://localhost:8079/nifi-api/controller/process-groups/00d886f3-a9ad-4302-9572-5f160d72bd81/process-group-references/d9dbdce4-7d01-498a-962e-f3f8ab924bae?version=637&clientId=a254dff5-bcb9-4f11-9420-1c065cc15289
    //first stop the processors
    // stopAllProcessors(groupDTO);
    //first all connections need to be deleted before the group is deleted
    //deleteConnections(groupDTO);
    Entity status = getControllerRevision();
    Map<String, Object> params = getUpdateParams();
    ProcessGroupEntity
        entity =
        delete("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),
               params, ProcessGroupEntity.class);

    return entity;

  }

  public List<ProcessGroupEntity> deleteChildProcessGroups(String processGroupId) throws JerseyClientException {
    List<ProcessGroupEntity> deletedEntities = new ArrayList<>();
    ProcessGroupEntity entity = getProcessGroup(processGroupId, true, true);
    if (entity != null && entity.getProcessGroup().getContents().getProcessGroups() != null) {
      for (ProcessGroupDTO groupDTO : entity.getProcessGroup().getContents().getProcessGroups()) {
        deletedEntities.add(deleteProcessGroup(groupDTO));
      }
    }
    return deletedEntities;

  }

  public ProcessGroupEntity deleteProcessGroup(String processGroupId) throws JerseyClientException {
    ProcessGroupEntity entity = getProcessGroup(processGroupId, false, true);
    ProcessGroupEntity deletedEntity = null;
    if (entity != null && entity.getProcessGroup() != null) {
      deletedEntity = deleteProcessGroup(entity.getProcessGroup());
    }

    return deletedEntity;
  }


  private void deleteConnections(ProcessGroupDTO group) throws JerseyClientException {
    if (group != null && group.getContents().getConnections() != null) {
      for (ConnectionDTO connection : group.getContents().getConnections()) {
        Map<String, Object> params = getUpdateParams();
        try {
          deleteConnection(connection);
        } catch (JerseyClientException e) {
          e.printStackTrace();
        }
      }
    }
    if (group != null && group.getContents().getProcessGroups() != null) {
      for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
        deleteConnections(groupDTO);
      }
    }
  }

  public void deleteConnection(ConnectionDTO connection) throws JerseyClientException {

        Map<String, Object> params = getUpdateParams();
        try {
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
             delete("/controller/process-groups/" + connection.getParentGroupId() + "/connections/" + connection.getId(), params,
                 ConnectionEntity.class);
        } catch (JerseyClientException e) {
          e.printStackTrace();
        }

  }

  public void deleteControllerService(String controllerServiceId) throws JerseyClientException {
    //http://localhost:8079/nifi-api/controller/controller-services/node/3c475f44-b038-4cb0-be51-65948de72764?version=1210&clientId=86af0022-9ba6-40b9-ad73-6d757b6f8d25
    Map<String, Object> params = getUpdateParams();
    delete("/controller/controller-services/node/" + controllerServiceId, params, ControllerServiceEntity.class);
  }

  public void deleteControllerServices(Collection<ControllerServiceDTO> services) throws JerseyClientException {
    //http://localhost:8079/nifi-api/controller/controller-services/node/3c475f44-b038-4cb0-be51-65948de72764?version=1210&clientId=86af0022-9ba6-40b9-ad73-6d757b6f8d25
    for (ControllerServiceDTO dto : services) {
      deleteControllerService(dto.getId());
    }
  }

  //get a process and its connections
  //http://localhost:8079/nifi-api/controller/process-groups/e40bfbb2-4377-43e6-b6eb-369e8f39925d/connections

  public ConnectionsEntity getProcessGroupConnections(String processGroupId) throws JerseyClientException {
    return get("/controller/process-groups/" + processGroupId + "/connections", null, ConnectionsEntity.class);
  }


  public ProcessGroupEntity getProcessGroup(String processGroupId, boolean recursive, boolean verbose)
      throws JerseyClientException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("recursive", recursive);
    params.put("verbose", verbose);
    return get("controller/process-groups/" + processGroupId, params, ProcessGroupEntity.class);
  }

  public ProcessGroupDTO getProcessGroupByName(String parentGroupId, final String groupName) throws JerseyClientException {
    ProcessGroupsEntity
        groups =
        get("/controller/process-groups/" + parentGroupId + "/process-group-references", null, ProcessGroupsEntity.class);
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


  public ProcessGroupEntity getRootProcessGroup() throws JerseyClientException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("recursive", true);
    params.put("verbose", true);
    return get("controller/process-groups/root", params, ProcessGroupEntity.class);
  }


  /**
   * Disables all inputs for a given process group
   */
  public void disableAllInputProcessors(String processGroupId) throws JerseyClientException {
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
  }

  /**
   * Marks the source processor as Running based upon the type DISABLEs the other source processors
   */
  public void setInputAsRunningByProcessorMatchingType(String processGroupId, String type) throws JerseyClientException {
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
        dto.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
      }
      if (update) {
        ProcessorEntity entity = getProcessor(processGroupId, dto.getId());
        entity.getProcessor().setState(dto.getState());
        updateProcessor(entity);
      }
    }
  }

  /**
   * return the Source Processors for a given group
   */
  public List<ProcessorDTO> getInputProcessors(String processGroupId) throws JerseyClientException {
    //get the group
    ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, false, true);
    //get the Source Processors
    List<String> sourceIds = getInputProcessorIds(processGroupId);

    return NifiProcessUtil.findProcessorsByIds(processGroupEntity.getProcessGroup().getContents().getProcessors(), sourceIds);
  }

  /**
   *
   * @param template
   * @return
   */
  public List<ProcessorDTO> getInputProcessorsForTemplate(TemplateDTO template) {
    return NifiTemplateUtil.getInputProcessorsForTemplate(template);
  }


  /**
   * get a set of all ProcessorDTOs in a template and optionally remove the initial input ones
   */
  public Set<ProcessorDTO> getProcessorsForTemplate(String templateId, boolean excludeInputs) throws JerseyClientException {
    TemplateDTO dto = getTemplateById(templateId);
    Set<ProcessorDTO> processors = NifiProcessUtil.getProcessors(dto);

    return processors;

  }

  /**
   * returns a list of Processors in a group that dont have any connection destinations (1st in the flow)
   */
  public List<String> getInputProcessorIds(String processGroupId) throws JerseyClientException {
    List<String> processorIds = new ArrayList<>();
    ConnectionsEntity connections = get("/controller/process-groups/" + processGroupId + "/connections", null,
                                        ConnectionsEntity.class);
    if (connections != null) {
      processorIds = NifiConnectionUtil.getInputProcessorIds(connections.getConnections());
    }
    return processorIds;
  }

  /**
   * returns a list of Processors in a group that dont have any connection destinations (1st in the flow)
   */
  public List<String> getEndingProcessorIds(String processGroupId) throws JerseyClientException {
    List<String> processorIds = new ArrayList<>();
    ConnectionsEntity
        connections =
        get("/controller/process-groups/" + processGroupId + "/connections", null, ConnectionsEntity.class);
    if (connections != null) {
      processorIds = NifiConnectionUtil.getEndingProcessorIds(connections.getConnections());
    }
    return processorIds;
  }

  /**
   * return the Source Processors for a given group
   */
  public List<ProcessorDTO> getEndingProcessors(String processGroupId) throws JerseyClientException {
    //get the group
    ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, false, true);
    //get the Source Processors
    List<String> sourceIds = getEndingProcessorIds(processGroupId);

    return NifiProcessUtil.findProcessorsByIds(processGroupEntity.getProcessGroup().getContents().getProcessors(), sourceIds);
  }


  /**
   * gets a Processor
   */
  public ProcessorEntity getProcessor(String processGroupId, String processorId) throws JerseyClientException {
    return get("/controller/process-groups/" + processGroupId + "/processors/" + processorId, null, ProcessorEntity.class);
  }

  /**
   * Saves the Processor
   */
  public ProcessorEntity updateProcessor(ProcessorEntity processorEntity) throws JerseyClientException {
    updateEntityForSave(processorEntity);
    return put(
        "/controller/process-groups/" + processorEntity.getProcessor().getParentGroupId() + "/processors/" + processorEntity
            .getProcessor().getId(), processorEntity, ProcessorEntity.class);
  }

  public ProcessorEntity updateProcessor(ProcessorDTO processorDTO) throws JerseyClientException {
    ProcessorEntity processorEntity = new ProcessorEntity();
    processorEntity.setProcessor(processorDTO);
    updateEntityForSave(processorEntity);
    return put(
        "/controller/process-groups/" + processorEntity.getProcessor().getParentGroupId() + "/processors/" + processorEntity
            .getProcessor().getId(), processorEntity, ProcessorEntity.class);
  }


  public ProcessGroupEntity updateProcessGroup(ProcessGroupEntity processGroupEntity) throws JerseyClientException {
    updateEntityForSave(processGroupEntity);
    return put("/controller/process-groups/" + processGroupEntity.getProcessGroup().getId(), processGroupEntity,
               ProcessGroupEntity.class);
  }


  /**
   * Update the properties
   */
  public void updateProcessGroupProperties(List<NifiProperty> properties) throws JerseyClientException {

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

  public void updateProcessorProperties(String processGroupId, String processorId, List<NifiProperty> properties)
      throws JerseyClientException {
    Map<String, NifiProperty> propertyMap = NifiPropertyUtil.propertiesAsMap(properties);
    // fetch the processor
    ProcessorEntity processor = getProcessor(processGroupId, processorId);
    //iterate through and update the properties
    for (Map.Entry<String, NifiProperty> property : propertyMap.entrySet()) {
      processor.getProcessor().getConfig().getProperties().put(property.getKey(), property.getValue().getValue());
    }
    updateProcessor(processor);
  }

  public void updateProcessorProperty(String processGroupId, String processorId, NifiProperty property)
      throws JerseyClientException {
    // fetch the processor
    ProcessorEntity processor = getProcessor(processGroupId, processorId);
    //iterate through and update the properties
    processor.getProcessor().getConfig().getProperties().put(property.getKey(), property.getValue());
    updateProcessor(processor);
  }

  public ControllerServicesEntity getControllerServices() throws JerseyClientException {
    return getControllerServices(null);
  }

  public ControllerServicesEntity getControllerServices(String type) throws JerseyClientException {

    if (StringUtils.isBlank(type)) {
      type = "NODE";
    }
    return get("/controller/controller-services/" + type, null, ControllerServicesEntity.class);
  }

  public ControllerServiceEntity getControllerService(String type, String id) throws JerseyClientException {

    if (StringUtils.isBlank(type)) {
      type = "NODE";
    }
    return get("/controller/controller-services/" + type + "/" + id, null, ControllerServiceEntity.class);
  }

  public ControllerServiceDTO getControllerServiceByName(String type, final String serviceName) throws JerseyClientException {
    ControllerServiceDTO controllerService = null;

   ControllerServicesEntity entity = getControllerServices(type);
    if(entity != null) {
     List<ControllerServiceDTO> services = Lists.newArrayList(Iterables.filter(entity.getControllerServices(), new Predicate<ControllerServiceDTO>() {
        @Override
        public boolean apply(ControllerServiceDTO controllerServiceDTO) {
          return controllerServiceDTO.getName().equalsIgnoreCase(serviceName);
        }
      }));

      if(services != null) {

        for(ControllerServiceDTO controllerServiceDTO : services) {
          if(controllerService == null){
            controllerService = controllerServiceDTO;
          }
          if(controllerServiceDTO.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())){
            controllerService = controllerServiceDTO;
            break;
          }
        }
      }
    }
    return controllerService;
  }

  //http://localhost:8079/nifi-api/controller/controller-services/node/edfe9a53-4fde-4437-a798-1305830c15ac
  public ControllerServiceEntity enableControllerService(String id) throws JerseyClientException {
    ControllerServiceEntity entity = new ControllerServiceEntity();
    ControllerServiceDTO dto = new ControllerServiceDTO();
    dto.setState(NifiProcessUtil.SERVICE_STATE.ENABLED.name());
    entity.setControllerService(dto);
    updateEntityForSave(entity);
    return put("/controller/controller-services/node/" + id, entity, ControllerServiceEntity.class);
  }

  public ControllerServiceEntity disableControllerService(String id) throws JerseyClientException {
    ControllerServiceEntity entity = new ControllerServiceEntity();
    ControllerServiceDTO dto = new ControllerServiceDTO();
    dto.setState(NifiProcessUtil.SERVICE_STATE.DISABLED.name());
    entity.setControllerService(dto);
    updateEntityForSave(entity);
    return put("/controller/controller-services/node/" + id, entity, ControllerServiceEntity.class);
  }


  public ControllerServiceTypesEntity getControllerServiceTypes() throws JerseyClientException {
    return get("/controller/controller-service-types", null, ControllerServiceTypesEntity.class);
  }


  public LineageEntity postLineageQuery(LineageEntity lineageEntity) throws JerseyClientException {

    return post("/controller/provenance/lineage", lineageEntity, LineageEntity.class);
  }


  public ProvenanceEntity getProvenanceEntity(String provenanceId) throws JerseyClientException {

    ProvenanceEntity entity = get("/controller/provenance/" + provenanceId, null, ProvenanceEntity.class);
    if (entity != null) {
      if (!entity.getProvenance().isFinished()) {
        return getProvenanceEntity(provenanceId);
      } else {
        //if it is finished you must delete the provenance entity
        try {
          delete("/controller/provenance/" + provenanceId, null, ProvenanceEntity.class);
        } catch (JerseyClientException e) {

        }
        return entity;
      }
    }
    return null;
  }


  public AboutEntity getNifiVersion() throws JerseyClientException {
    return get("/controller/about", null, AboutEntity.class);
  }

  public BulletinBoardEntity getBulletins(Map<String, Object> params) throws JerseyClientException {

    BulletinBoardEntity entity = get("/controller/bulletin-board", params, BulletinBoardEntity.class);
    return entity;
  }

  public BulletinBoardEntity getProcessGroupBulletins(String processGroupId) throws JerseyClientException {
    Map<String, Object> params = new HashMap<>();
    if (processGroupId != null) {
      params.put("groupId", processGroupId);
    }
    BulletinBoardEntity entity = get("/controller/bulletin-board", params, BulletinBoardEntity.class);
    return entity;
  }

  public BulletinBoardEntity getProcessorBulletins(String processorId) throws JerseyClientException {
    Map<String, Object> params = new HashMap<>();
    if (processorId != null) {
      params.put("sourceId", processorId);
    }
    BulletinBoardEntity entity = get("/controller/bulletin-board", params, BulletinBoardEntity.class);
    return entity;
  }

/*
    public Map<String,List<ProvenanceEventDTO>> getLineage(ProvenanceRequestDTO request) throws JerseyClientException {

        Map<String,List<ProvenanceEventDTO>> flowFeedEvents = new HashMap<String,List<ProvenanceEventDTO>>();

        //1 query the provenance for data in a time period


        ProvenanceEntity entity = new ProvenanceEntity();
        ProvenanceDTO dto = new ProvenanceDTO();
        dto.setRequest(request);
        entity.setProvenance(dto);



        ProvenanceEntity provenanceQuery = post("/controller/provenance",entity,ProvenanceEntity.class);

        /// clean up the provenance get and delete the query
        if(provenanceQuery != null) {

            ProvenanceEntity provenanceEntity = getProvenanceEntity(provenanceQuery.getProvenance().getId());
          if(provenanceEntity != null) {

             List<ProvenanceEventDTO> unmatchedEvents= new ArrayList<>();
              Map<String,String> flowFileUUIDtoFlowUUIDMap = new HashMap<>();

              ProvenanceResultsDTO resultsDTO = provenanceEntity.getProvenance().getResults();
              List<ProvenanceEventDTO> events = resultsDTO.getProvenanceEvents();
              for (ProvenanceEventDTO event : events) {
                  //Find if the attributes for this event contain the FLOW_RUN_UUID_PARAMETER


                  AttributeDTO match = Iterables.tryFind(event.getAttributes(), new Predicate<AttributeDTO>() {
                      @Override
                      public boolean apply(AttributeDTO attributeDTO) {
                          return attributeDTO.getName().equalsIgnoreCase(FLOW_RUN_UUID_PARAMETER);
                      }
                  }).orNull();

                  //if there is a match add it to the list of events that pertain to that given flow/feed run
                  if (match != null) {
                      if (!flowFeedEvents.containsKey(match.getValue())) {
                          flowFeedEvents.put(match.getValue(), new ArrayList<ProvenanceEventDTO>());
                      }
                      flowFeedEvents.get(match.getValue()).add(event);
                      //add the flowfie uuid to the map
                     flowFileUUIDtoFlowUUIDMap.put(event.getFlowFileUuid(),match.getValue());
                  }
                  else {
                      unmatchedEvents.add(event);
                  }
              }
           //add in the unmatched events
            for(ProvenanceEventDTO unmatchedEvent : unmatchedEvents){
                  String matchingFlowUUID = flowFileUUIDtoFlowUUIDMap.get(unmatchedEvent.getFlowFileUuid());
                  if(matchingFlowUUID != null){
                      flowFeedEvents.get(matchingFlowUUID).add(unmatchedEvent);
                  }
              }


              //now that we have the list of events grouped by the given flow run sort it either by eventId or eventDate
              for (List<ProvenanceEventDTO> allEvents : flowFeedEvents.values()) {
                  Collections.sort(allEvents, new Comparator<ProvenanceEventDTO>() {
                      @Override
                      public int compare(ProvenanceEventDTO o1, ProvenanceEventDTO o2) {
                          return o1.getEventId().compareTo(o2.getEventId());
                      }
                  });
              }


          }

        }
        return flowFeedEvents;
    }
    */
/*
    public void getFlowStatus(String processGroupId) throws JerseyClientException {

        //http://localhost:8079/nifi-api/controller/process-groups/85e5be3c-698c-4a1c-853a-7d70c5c82b5c/status?recursive=true
        Map<String,Object> params = new HashMap<>();
        params.put("recursive",true);
        ProcessGroupStatusEntity statusEntity = get("controller/process-groups/"+processGroupId+"/status",params,ProcessGroupStatusEntity.class);

       ProcessGroupStatusDTO statusdto = statusEntity.getProcessGroupStatus().getProcessGroupStatus();
        for(ProcessGroupDTOsta)

    }
*/

  public InputPortEntity getInputPort(String groupId, String portId) throws JerseyClientException {
    return get("/controller/process-groups/" + groupId + "/input-ports/" + portId, null, InputPortEntity.class);
  }

  public InputPortsEntity getInputPorts(String groupId) throws JerseyClientException {
    return get("/controller/process-groups/" + groupId + "/input-ports/", null, InputPortsEntity.class);
  }

  public OutputPortEntity getOutputPort(String groupId, String portId) throws JerseyClientException {
    return get("/controller/process-groups/" + groupId + "/output-ports/" + portId, null, OutputPortEntity.class);
  }

  public OutputPortsEntity getOutputPorts(String groupId) throws JerseyClientException {
    return get("/controller/process-groups/" + groupId + "/output-ports", null, OutputPortsEntity.class);
  }

  public List<ConnectionDTO> findAllConnectionsMatchingDestinationId(String parentGroupId, String destinationId)
      throws JerseyClientException {
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

  public void createReusableTemplateInputPort(String reusableTemplateCategoryGroupId, String reusableTemplateGroupId)
      throws JerseyClientException {
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
                                              String inputPortName)
      throws JerseyClientException {
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
                                           String inputPortName) throws JerseyClientException {
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


  public Set<ConnectionDTO> findConnectionsForParent(String parentProcessGroupId) throws JerseyClientException {
    Set<ConnectionDTO> connections = new HashSet<>();
    //get all connections under parent group
    ConnectionsEntity connectionsEntity = getProcessGroupConnections(parentProcessGroupId);
    if (connectionsEntity != null) {
      connections = connectionsEntity.getConnections();
    }
    return connections;
  }


  public ConnectionDTO findConnection(String parentProcessGroupId, final String sourceProcessGroupId,
                                      final String destProcessGroupId)
      throws JerseyClientException {
    return NifiConnectionUtil.findConnection(findConnectionsForParent(parentProcessGroupId), sourceProcessGroupId,
                                             parentProcessGroupId);
  }


  public ConnectionEntity createConnection(String processGroupId, ConnectableDTO source, ConnectableDTO dest)
      throws JerseyClientException {
    ConnectionDTO connectionDTO = new ConnectionDTO();
    connectionDTO.setSource(source);
    connectionDTO.setDestination(dest);
    connectionDTO.setName(source.getName() + " - " + dest.getName());
    ConnectionEntity connectionEntity = new ConnectionEntity();
    connectionEntity.setConnection(connectionDTO);
    updateEntityForSave(connectionEntity);
    return post("/controller/process-groups/" + processGroupId + "/connections", connectionEntity, ConnectionEntity.class);
  }


  public NifiVisitableProcessGroup getFlowOrder(String processGroupId) throws JerseyClientException {
    NifiVisitableProcessGroup group = null;
    ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId, true, true);
    if (processGroupEntity != null) {
      group = new NifiVisitableProcessGroup(processGroupEntity.getProcessGroup());
      NifiConnectionOrderVisitor orderVisitor = new NifiConnectionOrderVisitor(this, group);
      group.accept(orderVisitor);
      orderVisitor.printOrder();
      ;
    }
    return group;
  }

  public Set<ProcessorDTO> getProcessorsForFlow(String processGroupId) throws JerseyClientException {
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
  public Set<ProcessorDTO> getFailureProcessors(String processGroupId) throws JerseyClientException {
    NifiVisitableProcessGroup g = getFlowOrder(processGroupId);
    Set<ProcessorDTO> failureProcessors = new HashSet<>();
    for (NifiVisitableProcessor p : g.getStartingProcessors()) {

      failureProcessors.addAll(p.getFailureProcessors());
    }

    return failureProcessors;
  }


}


