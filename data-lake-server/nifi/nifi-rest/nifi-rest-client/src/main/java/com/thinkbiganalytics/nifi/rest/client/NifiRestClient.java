package com.thinkbiganalytics.nifi.rest.client;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.feedmgr.CreateFeedBuilder;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiConnectionOrderVisitor;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateUtil;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyClientException;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.BulletinBoardEntity;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.ControllerStatusEntity;
import org.apache.nifi.web.api.entity.DropRequestEntity;
import org.apache.nifi.web.api.entity.Entity;
import org.apache.nifi.web.api.entity.LineageEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupsEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ProvenanceEntity;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.WebTarget;

/**
 * Created by sr186054 on 1/9/16.
 * @TODO break up into separate modules for the various units of work (i.e. TemplateRestClient, FeedRestClient,  or out into separate Working classes to make this more readable
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
     * @param includeFlow
     * @return
     * @throws JerseyClientException
     */
    public TemplatesEntity getTemplates(boolean includeFlow) throws JerseyClientException {

        TemplatesEntity nifiTemplatesEntity = get("/controller/templates", null, TemplatesEntity.class);

            //get the contents and update the returned DTO with the populated snippetDTO
            for (TemplateDTO dto : ImmutableSet.copyOf(nifiTemplatesEntity.getTemplates())) {
               if( includeFlow) {
                nifiTemplatesEntity.getTemplates().remove(dto);
                TemplateDTO populatedDto = populateTemplateDTO(dto);
                nifiTemplatesEntity.getTemplates().add(populatedDto);
            }
        }
        return nifiTemplatesEntity;

    }

    /**
     * Populate a Template with the contents of its Flow
     * @param dto
     * @return
     * @throws JerseyClientException
     */
    private TemplateDTO populateTemplateDTO(TemplateDTO dto) throws JerseyClientException{
        if(dto.getSnippet() == null) {
            TemplateDTO populatedDto = getTemplateById(dto.getId());
            populatedDto.setId(dto.getId());
            populatedDto.setUri(dto.getUri());
            populatedDto.setDescription(dto.getDescription());
            return populatedDto;
        }
        else {
            return dto;
        }
    }


    /**
     * return the Template as an XML string
     * @param templateId
     * @return
     * @throws JerseyClientException
     */
    public String getTemplateXml(String templateId) throws JerseyClientException {
        String xml = get("/controller/templates/" + templateId, null, String.class);
        return xml;
    }


    /**
     * Return a template, populated along with its Flow snippet
     * @param templateId
     * @return
     * @throws JerseyClientException
     */
    public TemplateDTO getTemplateById(String templateId) throws JerseyClientException {
      TemplateDTO dto = get("/controller/templates/" + templateId, null, TemplateDTO.class);
      return dto;
    }

    /**
     * return a template by Name, populated with its Flow snippet
     * @param templateName
     * @return
     * @throws JerseyClientException
     */
    public TemplateDTO getTemplateByName(String templateName) throws JerseyClientException {
        TemplatesEntity templatesEntity = getTemplates(false);
        TemplateDTO templateDTO = null;
        if(templatesEntity.getTemplates() != null && !templatesEntity.getTemplates().isEmpty()) {
            for(TemplateDTO dto : templatesEntity.getTemplates()){
                if(dto.getName().equalsIgnoreCase(templateName)) {
                    templateDTO = populateTemplateDTO(dto);
                    break;
                }
            }
        }
      return templateDTO;
    }


    public ControllerStatusEntity getControllerStatus()  throws JerseyClientException{
        return get("/controller/status", null, ControllerStatusEntity.class);
    }

    /**
     * Gets the current Revision and Version of Nifi instance.
     * This is needed when performing an update to pass over the revision.getVersion() for locking purposes
     * @return
     * @throws JerseyClientException
     */
    public Entity getControllerRevision() throws JerseyClientException{
        return get("/controller/revision", null, Entity.class);
    }

    /**
     * Expose all Properties for a given Template as parameters for external use
     * @param templateId
     * @return
     * @throws JerseyClientException
     */
    public List<NifiProperty> getPropertiesForTemplate(String templateId)throws JerseyClientException {
        TemplateDTO dto = getTemplateById(templateId);
        ProcessGroupEntity rootProcessGroup = getProcessGroup("root", false, false);
        return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup.getProcessGroup(), dto);
    }


    /**
     * Expose all Properties for a given Template as parameters for external use
     * @param templateName
     * @return
     * @throws JerseyClientException
     */
    public List<NifiProperty> getPropertiesForTemplateByName(String templateName)throws JerseyClientException {
        TemplateDTO dto = getTemplateByName(templateName);
        ProcessGroupEntity rootProcessGroup = getProcessGroup("root", false, false);
        return NifiPropertyUtil.getPropertiesForTemplate(rootProcessGroup.getProcessGroup(), dto);
    }

    /**
     * get All properties
     * @return
     * @throws JerseyClientException
     */
    public List<NifiProperty> getAllProperties() throws JerseyClientException {
        ProcessGroupEntity root = getRootProcessGroup();
        return NifiPropertyUtil.getProperties(root.getProcessGroup());
    }


    /**
     * get All properties
     * @return
     * @throws JerseyClientException
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

     return createProcessGroup("root",name);
    }

    public ProcessGroupEntity createProcessGroup(String parentGroupId, String name) throws JerseyClientException {

        ProcessGroupEntity entity = new ProcessGroupEntity();
        ProcessGroupDTO group = new ProcessGroupDTO();
        group.setName(name);
        updateEntityForSave(entity);
        entity.setProcessGroup(group);
        ProcessGroupEntity returnedGroup = post("/controller/process-groups/"+parentGroupId+"/process-group-references",entity,ProcessGroupEntity.class);
        return returnedGroup;
    }

    /**
     * //mark everything as running
     //http://localhost:8079/nifi-api/controller/process-groups/2f0e55cb-34af-4e5b-8bf9-38909ea3af51/processors/bbef9df7-ff67-49fb-aa2e-3200ece92128
     * @param processors
     * @return
     */
public List<ProcessorDTO> markProcessorsAsRunning(List<ProcessorDTO> processors) throws JerseyClientException {
    Entity status = getControllerRevision();
    List<ProcessorDTO> dtos = new ArrayList<>();
    for(ProcessorDTO dto : processors){
        if(NifiProcessUtil.PROCESS_STATE.STOPPED.name().equalsIgnoreCase(dto.getState())) {
               //start it
            ProcessorEntity entity = new ProcessorEntity();
            dto.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
            entity.setProcessor(dto);
            entity.setRevision(status.getRevision());

            updateEntityForSave(entity);
            ProcessorEntity processorEntity = put("/controller/process-groups/" + dto.getParentGroupId() + "/processors/" + dto.getId(),entity,ProcessorEntity.class );
            if(processorEntity != null){
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
        return put("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),entity,ProcessGroupEntity.class );


    }


    public NifiProcessGroup createTemplateInstanceAsProcessGroup(String templateId, String category,String feedName,String inputProcessorType, List<NifiProperty> properties, NifiProcessorSchedule feedSchedule)throws JerseyClientException {
        return CreateFeedBuilder.newFeed(this, category, feedName, templateId).inputProcessorType(inputProcessorType).feedSchedule(feedSchedule).properties(properties).build();
    }



    private Map<String,Object> getUpdateParams() throws JerseyClientException {
        Entity status = getControllerRevision();
        Map<String,Object> params = new HashMap<>();
        params.put("version",status.getRevision().getVersion().toString());
        params.put("clientId",status.getRevision().getClientId());
        return params;
    }


    public void stopAllProcessors(ProcessGroupDTO groupDTO) throws JerseyClientException {
        for(ProcessorDTO dto: groupDTO.getContents().getProcessors()){
            dto.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
            ProcessorEntity entity = new ProcessorEntity();
            entity.setProcessor(dto);
            updateEntityForSave(entity);
            try {
                entity = updateProcessor(entity);
            }catch(JerseyClientException e){
                e.printStackTrace();
            }
        }
        if(groupDTO.getContents().getProcessGroups() != null){
            for(ProcessGroupDTO dto:groupDTO.getContents().getProcessGroups()) {
                stopAllProcessors(dto);
            }
        }
    }

    public ProcessGroupEntity deleteProcessGroup(ProcessGroupDTO groupDTO) throws JerseyClientException {
        ProcessGroupEntity deleteEntity = new ProcessGroupEntity();
        deleteEntity.setProcessGroup(groupDTO);
        deleteEntity.getProcessGroup().setRunning(false);
        updateEntityForSave(deleteEntity);
        put("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(),deleteEntity,ProcessGroupEntity.class );



        //PUT //http://localhost:8079/nifi-api/controller/process-groups/00d886f3-a9ad-4302-9572-5f160d72bd81/process-group-references/d9dbdce4-7d01-498a-962e-f3f8ab924bae
        //running= false
        //http://localhost:8079/nifi-api/controller/process-groups/00d886f3-a9ad-4302-9572-5f160d72bd81/process-group-references/d9dbdce4-7d01-498a-962e-f3f8ab924bae?version=637&clientId=a254dff5-bcb9-4f11-9420-1c065cc15289
        //first stop the processors
       // stopAllProcessors(groupDTO);
        //first all connections need to be deleted before the group is deleted
        //deleteConnections(groupDTO);
        Entity status = getControllerRevision();
        Map<String,Object> params = getUpdateParams();
        ProcessGroupEntity entity = delete("/controller/process-groups/" + groupDTO.getParentGroupId() + "/process-group-references/" + groupDTO.getId(), params, ProcessGroupEntity.class);

       return entity;

    }

    public List<ProcessGroupEntity> deleteChildProcessGroups(String processGroupId) throws JerseyClientException {
        List<ProcessGroupEntity> deletedEntities = new ArrayList<>();
        ProcessGroupEntity entity = getProcessGroup(processGroupId, true, true);
        if(entity != null && entity.getProcessGroup().getContents().getProcessGroups() != null){
            for(ProcessGroupDTO groupDTO: entity.getProcessGroup().getContents().getProcessGroups()){
                deletedEntities.add(deleteProcessGroup(groupDTO));
            }
        }
        return deletedEntities;

    }

    public ProcessGroupEntity deleteProcessGroup(String processGroupId) throws JerseyClientException {
        ProcessGroupEntity entity = getProcessGroup(processGroupId, false, true);
        ProcessGroupEntity deletedEntity = null;
        if(entity != null && entity.getProcessGroup() != null) {
            deletedEntity =deleteProcessGroup(entity.getProcessGroup());
        }

        return deletedEntity;
    }


    private void deleteConnections(ProcessGroupDTO group) throws JerseyClientException {
        if(group != null && group.getContents().getConnections() != null) {
            for (ConnectionDTO connection : group.getContents().getConnections()) {
                Map<String, Object> params = getUpdateParams();
                try {
                    //empty connection Queue
                    DropRequestEntity
                        dropRequestEntity =  delete("/controller/process-groups/" + group.getId() + "/connections/" + connection.getId()+"/contents", params, DropRequestEntity.class);
                    if(dropRequestEntity != null && dropRequestEntity.getDropRequest() != null) {
                        params = getUpdateParams();
                        delete("/controller/process-groups/" + group.getId() + "/connections/" + connection.getId()+"/drop-requests/"+dropRequestEntity.getDropRequest().getId(), params, DropRequestEntity.class);
                    }
                    //http://localhost:8079/nifi-api/controller/process-groups/39014aee-5556-481d-91a1-d5052e0e332a/connections/296b80d4-ee1c-4878-ab29-888f7514952a/contents
                    //http://localhost:8079/nifi-api/controller/process-groups/39014aee-5556-481d-91a1-d5052e0e332a/connections/296b80d4-ee1c-4878-ab29-888f7514952a/drop-requests/1fddf3ac-9337-475a-876a-cf7df2037c18
                    delete("/controller/process-groups/" + group.getId() + "/connections/" + connection.getId(), params, ConnectionEntity.class);
                }catch(JerseyClientException e){
                    e.printStackTrace();
                }
            }
        }
        if(group != null && group.getContents().getProcessGroups() != null){
            for(ProcessGroupDTO groupDTO: group.getContents().getProcessGroups()){
                deleteConnections(groupDTO);
            }
        }
    }

    public void deleteControllerService(String controllerServiceId) throws JerseyClientException {
        //http://localhost:8079/nifi-api/controller/controller-services/node/3c475f44-b038-4cb0-be51-65948de72764?version=1210&clientId=86af0022-9ba6-40b9-ad73-6d757b6f8d25
        Map<String,Object> params =getUpdateParams();
       delete("/controller/controller-services/node/"+controllerServiceId,params,ControllerServiceEntity.class);
    }

    public void deleteControllerServices(Collection<ControllerServiceDTO> services) throws JerseyClientException {
        //http://localhost:8079/nifi-api/controller/controller-services/node/3c475f44-b038-4cb0-be51-65948de72764?version=1210&clientId=86af0022-9ba6-40b9-ad73-6d757b6f8d25
       for(ControllerServiceDTO dto: services){
           deleteControllerService(dto.getId());
       }
    }


    //get a process and its connections
    //http://localhost:8079/nifi-api/controller/process-groups/e40bfbb2-4377-43e6-b6eb-369e8f39925d/connections

    public ConnectionsEntity getProcessGroupConnections(String processGroupId) throws JerseyClientException  {
        return get("/controller/process-groups/"+processGroupId+"/connections", null,ConnectionsEntity.class);
    }



    public ProcessGroupEntity getProcessGroup(String processGroupId, boolean recursive, boolean verbose) throws JerseyClientException{
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("recursive", recursive);
        params.put("verbose", verbose);
        return get("controller/process-groups/" + processGroupId, params, ProcessGroupEntity.class);
    }

    public ProcessGroupDTO getProcessGroupByName(String parentGroupId, final String groupName) throws JerseyClientException {
        ProcessGroupsEntity
            groups = get("/controller/process-groups/"+parentGroupId+"/process-group-references",null,ProcessGroupsEntity.class);
        if(groups != null){
          List<ProcessGroupDTO> list =  Lists.newArrayList(Iterables.filter(groups.getProcessGroups(), new Predicate<ProcessGroupDTO>() {
                @Override
                public boolean apply(ProcessGroupDTO groupDTO) {
                    return groupDTO.getName().equalsIgnoreCase(groupName);
                }
            }));
            if(list != null && !list.isEmpty()){
                return list.get(0);
            }
        }
        return null;

    }





    public ProcessGroupEntity getRootProcessGroup() throws JerseyClientException{
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("recursive",true);
        params.put("verbose",true);
        return get("controller/process-groups/root", params, ProcessGroupEntity.class);
    }


    /**
     * Disables all inputs for a given process group
     * @param processGroupId
     * @throws JerseyClientException
     */
    public void disableAllInputProcessors(String processGroupId) throws JerseyClientException {
        List<ProcessorDTO> processorDTOs = getInputProcessors(processGroupId);
        ProcessorDTO updateDto = new ProcessorDTO();
        if(processorDTOs != null) {
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
     * Marks the source processor as Running based upon the type
     * DISABLEs the other source processors
     * @param processGroupId
     * @param type
     * @return
     * @throws JerseyClientException
     */
    public void setInputAsRunningByProcessorMatchingType(String processGroupId, String type) throws JerseyClientException {
        //get the Source Processors
        List<ProcessorDTO> processorDTOs = getInputProcessors(processGroupId);
        if(StringUtils.isBlank(type)) {
            //start the first one it finds
            type = processorDTOs.get(0).getType();
        }
        ProcessorDTO processorDTO = NifiProcessUtil.findFirstProcessorsByType(processorDTOs, type);
        //Mark all that dont match type as DISABLED.  Start the other one
        boolean update = false;
        for(ProcessorDTO dto : processorDTOs){
             update = false;
            //fetch the processor and update it
            if(!dto.equals(processorDTO) && ! NifiProcessUtil.PROCESS_STATE.DISABLED.name().equals(dto.getState())){
                dto.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                update = true;
            }
            else if(dto.equals(processorDTO) && !NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(dto.getState())) {
                update = true;
                dto.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
            }
            if(update) {
                ProcessorEntity entity = getProcessor(processGroupId, dto.getId());
                entity.getProcessor().setState(dto.getState());
                updateProcessor(entity);
            }
        }
    }

    /**
     * return the Source Processors for a given group
     * @param processGroupId
     * @return
     * @throws JerseyClientException
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
    public List<ProcessorDTO>getInputProcessorsForTemplate(TemplateDTO template ){
      return NifiTemplateUtil.getInputProcessorsForTemplate(template);
    }


    /**
     * get a set of all ProcessorDTOs in a template and optionally remove the initial input ones
     * @param templateId
     * @param excludeInputs
     * @return
     * @throws JerseyClientException
     */
    public Set<ProcessorDTO> getProcessorsForTemplate(String templateId, boolean excludeInputs) throws JerseyClientException {
        TemplateDTO dto = getTemplateById(templateId);
        Set<ProcessorDTO> processors = NifiProcessUtil.getProcessors(dto);

        return processors;

    }

    /**
     * returns a list of Processors in a group that dont have any connection destinations (1st in the flow)
     * @param processGroupId
     * @return
     */
    public List<String> getInputProcessorIds(String processGroupId) throws JerseyClientException {
        List<String> processorIds = new ArrayList<>();
        ConnectionsEntity connections = get("/controller/process-groups/" + processGroupId + "/connections", null, ConnectionsEntity.class);
        if(connections != null){
            processorIds= NifiConnectionUtil.getInputProcessorIds(connections.getConnections());
        }
        return processorIds;
    }

    /**
     * returns a list of Processors in a group that dont have any connection destinations (1st in the flow)
     * @param processGroupId
     * @return
     */
    public List<String> getEndingProcessorIds(String processGroupId) throws JerseyClientException {
        List<String> processorIds = new ArrayList<>();
        ConnectionsEntity connections = get("/controller/process-groups/" + processGroupId + "/connections", null, ConnectionsEntity.class);
        if(connections != null){
            processorIds= NifiConnectionUtil.getEndingProcessorIds(connections.getConnections());
        }
        return processorIds;
    }

    /**
     * return the Source Processors for a given group
     * @param processGroupId
     * @return
     * @throws JerseyClientException
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
     * @param processGroupId
     * @param processorId
     * @return
     * @throws JerseyClientException
     */
    public ProcessorEntity getProcessor(String processGroupId,String processorId) throws JerseyClientException {
        return get("/controller/process-groups/" + processGroupId + "/processors/" + processorId, null, ProcessorEntity.class);
    }

    /**
     * Saves the Processor
     * @param processorEntity
     * @return
     * @throws JerseyClientException
     */
    public ProcessorEntity updateProcessor(ProcessorEntity processorEntity) throws JerseyClientException {
        updateEntityForSave(processorEntity);
        return put("/controller/process-groups/" + processorEntity.getProcessor().getParentGroupId() + "/processors/" + processorEntity.getProcessor().getId(), processorEntity, ProcessorEntity.class);
    }

    public ProcessorEntity updateProcessor(ProcessorDTO processorDTO) throws JerseyClientException {
        ProcessorEntity processorEntity = new ProcessorEntity();
        processorEntity.setProcessor(processorDTO);
        updateEntityForSave(processorEntity);
        return put("/controller/process-groups/" + processorEntity.getProcessor().getParentGroupId() + "/processors/" + processorEntity.getProcessor().getId(), processorEntity, ProcessorEntity.class);
    }


    public ProcessGroupEntity updateProcessGroup(ProcessGroupEntity processGroupEntity) throws JerseyClientException {
        updateEntityForSave(processGroupEntity);
        return put("/controller/process-groups/" + processGroupEntity.getProcessGroup().getId(), processGroupEntity, ProcessGroupEntity.class);
    }




    /**
     * Update the properties
     * @param properties
     * @throws JerseyClientException
     */
    public void updateProcessGroupProperties(List<NifiProperty> properties) throws JerseyClientException {

        Map<String,Map<String,List<NifiProperty>>> processGroupProperties = NifiPropertyUtil.groupPropertiesByProcessGroupAndProcessor(properties);

        for(Map.Entry<String,Map<String,List<NifiProperty>>> processGroupEntry : processGroupProperties.entrySet()){

            String processGroupId = processGroupEntry.getKey();
            for(Map.Entry<String,List<NifiProperty>> propertyEntry : processGroupEntry.getValue().entrySet()) {
                String processorId = propertyEntry.getKey();
                updateProcessorProperties(processGroupId,processorId,propertyEntry.getValue());
            }

        }
    }

    public void updateProcessorProperties(String processGroupId, String processorId,List<NifiProperty> properties) throws JerseyClientException {
        Map<String,NifiProperty> propertyMap = NifiPropertyUtil.propertiesAsMap(properties);
        // fetch the processor
        ProcessorEntity processor = getProcessor(processGroupId, processorId);
        //iterate through and update the properties
        for(Map.Entry<String,NifiProperty> property : propertyMap.entrySet()){
            processor.getProcessor().getConfig().getProperties().put(property.getKey(), property.getValue().getValue());
        }
        updateProcessor(processor);
    }

    public void updateProcessorProperty(String processGroupId, String processorId,NifiProperty property) throws JerseyClientException {
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

        if(StringUtils.isBlank(type)){
            type = "NODE";
        }
        return get("/controller/controller-services/" + type, null, ControllerServicesEntity.class);
    }

    public ControllerServiceEntity getControllerService(String type, String id) throws JerseyClientException {

        if(StringUtils.isBlank(type)){
            type = "NODE";
        }
        return get("/controller/controller-services/" + type + "/" + id, null, ControllerServiceEntity.class);
    }

    //http://localhost:8079/nifi-api/controller/controller-services/node/edfe9a53-4fde-4437-a798-1305830c15ac
    public ControllerServiceEntity enableControllerService(String id) throws JerseyClientException {
        ControllerServiceEntity entity = new ControllerServiceEntity();
        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setState(NifiProcessUtil.SERVICE_STATE.ENABLED.name());
        entity.setControllerService(dto);
        updateEntityForSave(entity);
           return  put("/controller/controller-services/node/" + id, entity, ControllerServiceEntity.class);
    }

    public ControllerServiceEntity disableControllerService(String id) throws JerseyClientException {
        ControllerServiceEntity entity = new ControllerServiceEntity();
        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setState(NifiProcessUtil.SERVICE_STATE.DISABLED.name());
        entity.setControllerService(dto);
        updateEntityForSave(entity);
        return  put("/controller/controller-services/node/" + id, entity, ControllerServiceEntity.class);
    }


    public ControllerServiceTypesEntity getControllerServiceTypes() throws JerseyClientException {
        return get("/controller/controller-service-types", null, ControllerServiceTypesEntity.class);
    }



public LineageEntity postLineageQuery(LineageEntity lineageEntity) throws JerseyClientException {


    return post("/controller/provenance/lineage", lineageEntity, LineageEntity.class);
}


    public ProvenanceEntity getProvenanceEntity(String provenanceId) throws JerseyClientException {

        ProvenanceEntity entity = get("/controller/provenance/" + provenanceId, null, ProvenanceEntity.class);
        if(entity != null) {
            if(!entity.getProvenance().isFinished()) {
                return getProvenanceEntity(provenanceId);
            }
            else {
                //if it is finished you must delete the provenance entity
                try {
                    delete("/controller/provenance/" + provenanceId, null, ProvenanceEntity.class);
                }catch(JerseyClientException e){

                }
                return entity;
            }
        }
        return null;
    }

    public BulletinBoardEntity getBulletins(Map<String,Object>params) throws JerseyClientException {

        BulletinBoardEntity entity = get("/controller/bulletin-board",params,BulletinBoardEntity.class);
        return entity;
    }

    public BulletinBoardEntity getProcessGroupBulletins(String processGroupId) throws JerseyClientException {
        Map<String, Object> params = new HashMap<>();
        if (processGroupId != null) {
            params.put("groupId", processGroupId);
         }
        BulletinBoardEntity entity = get("/controller/bulletin-board",params,BulletinBoardEntity.class);
        return entity;
    }

       public BulletinBoardEntity getProcessorBulletins(String processorId) throws JerseyClientException {
        Map<String, Object> params = new HashMap<>();
        if (processorId != null) {
            params.put("sourceId", processorId);
        }
        BulletinBoardEntity entity = get("/controller/bulletin-board",params,BulletinBoardEntity.class);
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

    public NifiVisitableProcessGroup getFlowOrder(String processGroupId) throws JerseyClientException {
        NifiVisitableProcessGroup group = null;
        ProcessGroupEntity processGroupEntity = getProcessGroup(processGroupId,true,true);
        if(processGroupEntity != null)
        {
            group = new NifiVisitableProcessGroup(processGroupEntity.getProcessGroup());
            NifiConnectionOrderVisitor orderVisitor = new NifiConnectionOrderVisitor(group);
            group.accept(orderVisitor);
        }
        return group;

    }

    /**
     * Wallk the flow for a given Root Process Group and return all those Processors who are marked with a Failure Relationship
     * @param processGroupId
     * @return
     * @throws JerseyClientException
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


