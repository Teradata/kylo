package com.thinkbiganalytics.nifi.feedmgr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.Entity;
import org.apache.nifi.web.api.entity.FlowSnippetEntity;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Form;

/**
 * Created by sr186054 on 2/3/16.
 */
public class CreateFeedBuilder {

  NifiRestClient restClient;

  private String templateId;
  private String category;
  private String feedName;
  private String inputProcessorType;
  private String reusableTemplateCategoryName = "reusable_templates";
  private String reusableTemplateFeedName;
  private String feedOutputPortName;
  private String reusableTemplateInputPortName;
  private boolean isReusableTemplate;


  private String version;

  private List<NifiProperty> properties;
  private NifiProcessorSchedule feedSchedule;

  private List<NifiProperty> modifiedProperties;
  private Set<ControllerServiceDTO> snapshotControllerServices;

  private Set<ControllerServiceDTO> newlyCreatedControllerServices;

  private List<NifiError> errors = new ArrayList<>();


  protected CreateFeedBuilder(NifiRestClient restClient, String category, String feedName, String templateId) {
    this.restClient = restClient;

    this.category = category;
    this.feedName = feedName;
    this.templateId = templateId;
  }

  public static CreateFeedBuilder newFeed(NifiRestClient restClient, String category, String feedName, String templateId) {
    return new CreateFeedBuilder(restClient, category, feedName, templateId);
  }

  public CreateFeedBuilder feedSchedule(NifiProcessorSchedule feedSchedule) {
    this.feedSchedule = feedSchedule;
    return this;
  }


  public CreateFeedBuilder reusableTemplateFeedName(String reusableTemplateFeedName) {
    this.reusableTemplateFeedName = reusableTemplateFeedName;
    return this;
  }

  public CreateFeedBuilder reusableTemplateCategoryName(String reusableTemplateCategoryName) {
    this.reusableTemplateCategoryName = reusableTemplateCategoryName;
    return this;
  }

  public CreateFeedBuilder feedOutputPortName(String feedOutputPortName) {
    this.feedOutputPortName = feedOutputPortName;
    return this;
  }

  public CreateFeedBuilder reusableTemplateInputPortName(String reusableTemplateInputPortName) {
    this.reusableTemplateInputPortName = reusableTemplateInputPortName;
    return this;
  }

  public CreateFeedBuilder inputProcessorType(String inputProcessorType) {
    this.inputProcessorType = inputProcessorType;
    return this;
  }

  public CreateFeedBuilder properties(List<NifiProperty> properties) {
    this.properties = properties;
    return this;
  }

  public CreateFeedBuilder version(String version) {
    this.version = version;
    return this;
  }

  public CreateFeedBuilder setReusableTemplate(boolean isReusableTemplate) {
    this.isReusableTemplate = isReusableTemplate;
    return this;
  }

  private void cleanupControllerServices() {
    //only delete the services that were created if none of them with that type existed in the system before
    // only keep them if they are the first of their kind
    if (snapshotControllerServices != null && !snapshotControllerServices.isEmpty()) {
      final Set<String> serviceTypes = new HashSet<>();
      for (ControllerServiceDTO dto : snapshotControllerServices) {
        serviceTypes.add(dto.getType());
      }

      List<ControllerServiceDTO>
          servicesToDelete =
          Lists.newArrayList(Iterables.filter(newlyCreatedControllerServices, new Predicate<ControllerServiceDTO>() {
            @Override
            public boolean apply(ControllerServiceDTO controllerServiceDTO) {
              return serviceTypes.contains(controllerServiceDTO.getType());
            }

          }));
      if (servicesToDelete != null && !servicesToDelete.isEmpty()) {
        try {
          restClient.deleteControllerServices(servicesToDelete);
        } catch (JerseyClientException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void connectFeedToReusableTemplate(String feedGroupId) throws JerseyClientException {
    ProcessGroupDTO reusableTemplateCategory = restClient.getProcessGroupByName("root", reusableTemplateCategoryName);
    ProcessGroupDTO reusableTemplate = restClient.getProcessGroupByName(reusableTemplateCategory.getId(),
                                                                        reusableTemplateFeedName);
    ProcessGroupEntity feedProcessGroup = restClient.getProcessGroup(feedGroupId, false, false);
    String feedCategoryId = feedProcessGroup.getProcessGroup().getParentGroupId();
    String reusableTemplateCategoryGroupId = reusableTemplateCategory.getId();
    String templateGroupId = reusableTemplate.getId();
    String inputPortName = reusableTemplateInputPortName;
    restClient
        .connectFeedToGlobalTemplate(feedGroupId, feedOutputPortName, feedCategoryId, reusableTemplateCategoryGroupId,
                                     templateGroupId, inputPortName);
  }

  private void ensureInputPortsForReuseableTemplate(String feedGroupId) throws JerseyClientException {
    ProcessGroupEntity template = restClient.getProcessGroup(feedGroupId, false, false);
    String categoryId = template.getProcessGroup().getParentGroupId();
    restClient.createReusableTemplateInputPort(categoryId, feedGroupId);

  }


  public NifiProcessGroup build() throws JerseyClientException {
    NifiProcessGroup newProcessGroup = null;
    TemplateDTO template = restClient.getTemplateById(templateId);

    if (template != null) {
      //create the encompassing process group
      String processGroupId = createProcessGroupForFeed();
      if (StringUtils.isNotBlank(processGroupId)) {
        //snapshot the existing controller services
        snapshotControllerServiceReferences();
        //create the flow from the template
        instantiateFlowFromTemplate(processGroupId);

        //if the feed has an outputPort that should go to a reusable Flow then make those connections
        if (reusableTemplateFeedName != null) {
          connectFeedToReusableTemplate(processGroupId);

        }
        if (isReusableTemplate) {
          ensureInputPortsForReuseableTemplate(processGroupId);
        }

        //mark the new services that were created as a result of creating the new flow from the template
        identifyNewlyCreatedControllerServiceReferences();

        //match the properties incoming to the defined properties
        updateProcessGroupProperties(processGroupId);

        //Fetch the Feed Group now that it has the flow in it
        ProcessGroupEntity entity = restClient.getProcessGroup(processGroupId, true, true);

        //identify the various processors (first level initial processors)
        List<ProcessorDTO> inputProcessors = NifiProcessUtil.getInputProcessors(entity.getProcessGroup());

        ProcessorDTO input = NifiProcessUtil.findFirstProcessorsByType(inputProcessors, inputProcessorType);
        List<ProcessorDTO> nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity.getProcessGroup());

        //if the input is null attempt to get the first input available on the template
        if (input == null && inputProcessors != null && !inputProcessors.isEmpty()) {
          input = inputProcessors.get(0);
        }

        newProcessGroup = new NifiProcessGroup(entity, input, nonInputProcessors);

        //Validate and if invalid Delete the process group
        if (newProcessGroup.hasFatalErrors()) {
          restClient.deleteProcessGroup(entity.getProcessGroup());
          // cleanupControllerServices();
          newProcessGroup.setSuccess(false);
        } else {

          //update the input schedule
          updateFeedSchedule(newProcessGroup, input);
          //update any references to the controller services and try to assign the value to an enabled service if it is not already
          if (input != null) {
            updateControllerServiceReferences(Lists.newArrayList(input));
          }
          updateControllerServiceReferences(nonInputProcessors);
          if (input != null) {
            markInputAsRunning(newProcessGroup, input);
          }

          markProcessorsAsRunning(newProcessGroup, nonInputProcessors);

          ///make the input/output ports in the category group as running
          //TODO


          if (newProcessGroup.hasFatalErrors()) {
            restClient.deleteProcessGroup(entity.getProcessGroup());
            //  cleanupControllerServices();
            newProcessGroup.setSuccess(false);
          }

          //add any global errors to the object
          if (errors != null && !errors.isEmpty()) {
            for (NifiError error : errors) {
              newProcessGroup.addError(error);
              if (error.isFatal()) {
                newProcessGroup.setSuccess(false);
              }
            }
          }
        }
        cleanupControllerServices();

      }
    }
    return newProcessGroup;
  }

  private void versionFeed(ProcessGroupDTO feedGroup) throws JerseyClientException {
    restClient.disableAllInputProcessors(feedGroup.getId());
    //attempt to stop all processors
    try {
      restClient.stopAllProcessors(feedGroup);
    }catch (JerseyClientException e)
    {

    }
    //delete all connections
    if (reusableTemplateFeedName != null || isReusableTemplate) {
      ConnectionsEntity connectionsEntity = restClient.getProcessGroupConnections(feedGroup.getParentGroupId());
      if(connectionsEntity != null) {
        List<ConnectionDTO> connections = null;
        if(reusableTemplateFeedName != null) {
          //delete the output Port connections (matching this source)
          connections =
              NifiConnectionUtil.findConnectionsMatchingSourceGroupId(connectionsEntity.getConnections(), feedGroup.getId());

          if(connections != null) {
            for(ConnectionDTO connection: connections){
              String type = connection.getDestination().getType();
              if(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(type)){
                //stop the port
                restClient.stopOutputPort(connection.getParentGroupId(), connection.getDestination().getId());
              }
              restClient.deleteConnection(connection);
            }
          }
        }
        else if(isReusableTemplate){
          //delete the input port connections (matching this dest)
          connections = NifiConnectionUtil.findConnectionsMatchingDestinationGroupId(connectionsEntity.getConnections(),feedGroup.getId());
          if(connections != null) {
            for(ConnectionDTO connection: connections){
              String type = connection.getSource().getType();
              if(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)){
                //stop the port
                restClient.stopInputPort(connection.getParentGroupId(),connection.getSource().getId());
              }
              restClient.deleteConnection(connection);
            }
          }
        }

      }
    }


    //rename the feedGroup to be name+timestamp
    //TODO change to work with known version passed in (get the rename to current version -1 or something.
    feedGroup.setName(feedName + new Date().getTime());
    ProcessGroupEntity entity = new ProcessGroupEntity();
    entity.setProcessGroup(feedGroup);
    restClient.updateProcessGroup(entity);
  }

  private String createProcessGroupForFeed() throws JerseyClientException {
    //create Category Process group
    String processGroupId = null;
    ProcessGroupDTO categoryGroup = restClient.getProcessGroupByName("root", category);

    if (categoryGroup == null) {
      ProcessGroupEntity group = restClient.createProcessGroup(category);
      categoryGroup = group.getProcessGroup();
    }
    if (categoryGroup == null) {
      throw new JerseyClientException("Unable to get or create the Process group for the Category " + category
                                      + ". Error occurred while creating instance of template " + templateId + " for Feed "
                                      + feedName);
    }

    //1 create the processGroup
    //check to see if the feed exists... if so version off the old group and create a new group with this feed
    ProcessGroupDTO feedGroup = restClient.getProcessGroupByName(categoryGroup.getId(), feedName);
    if (feedGroup != null) {
      try {
        versionFeed(feedGroup);
      } catch (JerseyClientException e) {
        throw new JerseyClientException("Previous version of the feed " + feedName
                                        + " was found.  Error in attempting to version the previous feed.  Please go into Nifi and address any issues with the Feeds Process Group");
      }
    }

    ProcessGroupEntity group = restClient.createProcessGroup(categoryGroup.getId(), feedName);
    if (group != null) {
      processGroupId = group.getProcessGroup().getId();
    }
    return processGroupId;
  }

  private FlowSnippetEntity instantiateFlowFromTemplate(String processGroupId) throws JerseyClientException {
    Entity status = restClient.getControllerRevision();
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
        restClient.postForm("/controller/process-groups/" + processGroupId + "/template-instance", form, FlowSnippetEntity.class);
    return response;
  }

  /**
   * Updates a process groups properties
   */
  private void updateProcessGroupProperties(String processGroupId) throws JerseyClientException {
    List<NifiProperty> propertiesToUpdate = restClient.getPropertiesForProcessGroup(processGroupId);
    //get the Root processGroup
    ProcessGroupEntity rootProcessGroup = restClient.getRootProcessGroup();
    //get this process group
    ProcessGroupEntity activeProcessGroupName = restClient.getProcessGroup(processGroupId, false, false);

    modifiedProperties = NifiPropertyUtil.matchAndSetPropertyValues(rootProcessGroup.getProcessGroup().getName(),
                                                                    activeProcessGroupName.getProcessGroup().getName(),
                                                                    propertiesToUpdate, properties);

    restClient.updateProcessGroupProperties(modifiedProperties);


  }

  private void snapshotControllerServiceReferences() throws JerseyClientException {
    ControllerServicesEntity controllerServiceEntity = restClient.getControllerServices();
    if (controllerServiceEntity != null) {
      snapshotControllerServices = controllerServiceEntity.getControllerServices();
    }
  }

  /**
   * Compare the services in Nifi with the ones from the snapshot and return any that are not in the snapshot
   */
  private Set<ControllerServiceDTO> identifyNewlyCreatedControllerServiceReferences() throws JerseyClientException {
    Set<ControllerServiceDTO> newServices = new HashSet<>();
    ControllerServicesEntity controllerServiceEntity = restClient.getControllerServices();
    if (controllerServiceEntity != null) {
      if (snapshotControllerServices != null) {
        for (ControllerServiceDTO dto : controllerServiceEntity.getControllerServices()) {
          if (!snapshotControllerServices.contains(dto)) {
            newServices.add(dto);
          }
        }
      } else {
        newServices = controllerServiceEntity.getControllerServices();
      }
    }
    newlyCreatedControllerServices = newServices;
    return newServices;
  }


  private void updateControllerServiceReferences(List<ProcessorDTO> processors) {

    try {
      ControllerServicesEntity controllerServiceEntity = restClient.getControllerServices();
      final Map<String, ControllerServiceDTO> enabledServices = new HashMap<>();
      Map<String, ControllerServiceDTO> allServices = new HashMap<>();
      for (ControllerServiceDTO dto : controllerServiceEntity.getControllerServices()) {
        if (NifiProcessUtil.SERVICE_STATE.ENABLED.name().equals(dto.getState())) {
          enabledServices.put(dto.getId(), dto);
        }
        allServices.put(dto.getId(), dto);

      }
      List<NifiProperty> properties = new ArrayList<>();
      Map<String, ProcessGroupDTO> processGroupDTOMap = new HashMap<>();

      for (ProcessorDTO dto : processors) {
        ProcessGroupDTO groupDTO = processGroupDTOMap.get(dto.getParentGroupId());
        if (groupDTO == null) {
          //we can create a tmp group dto here as all we need is the id
          groupDTO = new ProcessGroupDTO();
          groupDTO.setId(dto.getParentGroupId());
          groupDTO.setName(dto.getParentGroupId());
          processGroupDTOMap.put(dto.getParentGroupId(), groupDTO);
        }
        properties.addAll(NifiPropertyUtil.getPropertiesForProcessor(groupDTO, dto));
        //properties.addAll(NifiPropertyUtil.get.getPropertiesForProcessor(modifiedProperties, dto.getId()));
        //properties.addAll(NifiPropertyUtil.getPropertiesForProcessor(modifiedProperties,dto.getId()));
      }

      for (final NifiProperty property : properties) {
        String controllerService = property.getPropertyDescriptor().getIdentifiesControllerService();
        if (StringUtils.isNotBlank(controllerService)) {
          boolean set = false;

          //if the service is not enabled, but it exists then try to enable that
          if (!enabledServices.containsKey(property.getValue()) && allServices.containsKey(property.getValue())) {
            ControllerServiceDTO dto = allServices.get(property.getValue());
            try {
              restClient.enableControllerService(dto.getId());
              set = true;
            } catch (JerseyClientException e) {
              //errors.add(new NifiError(NifiError.SEVERITY.WARN,"Error trying to enable Controller Service " + dto.getName() +" on referencing Processor: " + property.getProcessorName() + " and field " + property.getKey() + ". Please go to Nifi and configure and enable this Service before creating this feed.", "Controller Services"));

            }
          }
          if (!set) {
            boolean controllerServiceSet = false;
            String controllerServiceName = "";
            // match a allowable service and enable it
            List<PropertyDescriptorDTO.AllowableValueDTO>
                allowableValueDTOs =
                property.getPropertyDescriptor().getAllowableValues();
            //if any of the allowable values are enabled already use that and continue
            List<PropertyDescriptorDTO.AllowableValueDTO>
                enabledValues =
                Lists.newArrayList(Iterables.filter(allowableValueDTOs, new Predicate<PropertyDescriptorDTO.AllowableValueDTO>() {
                  @Override
                  public boolean apply(PropertyDescriptorDTO.AllowableValueDTO allowableValueDTO) {
                    return enabledServices.containsKey(allowableValueDTO.getValue());
                  }
                }));
            if (enabledValues != null && !enabledValues.isEmpty()) {
              PropertyDescriptorDTO.AllowableValueDTO enabledService = enabledValues.get(0);
              ControllerServiceDTO dto = enabledServices.get(enabledService.getValue());
              controllerServiceName = dto.getName();
              property.setValue(enabledService.getValue());
              controllerServiceSet = true;
            } else {
              //try to enable the service

              for (PropertyDescriptorDTO.AllowableValueDTO allowableValueDTO : allowableValueDTOs) {
                ControllerServiceDTO dto = allServices.get(allowableValueDTO.getValue());
                if (StringUtils.isBlank(controllerServiceName)) {
                  controllerServiceName = dto.getName();
                }
                if (allServices.containsKey(allowableValueDTO.getValue())) {
                  property.setValue(allowableValueDTO.getValue());
                  try {
                    ControllerServiceEntity
                        entity = restClient.enableControllerService(allowableValueDTO.getValue());
                    if (entity != null && NifiProcessUtil.SERVICE_STATE.ENABLED.name()
                        .equalsIgnoreCase(entity.getControllerService().getState())) {
                      controllerServiceSet = true;
                      break;
                    }
                  } catch (JerseyClientException e) {
                    //errors will be handled downstream

                  }
                }
              }
            }
            if (controllerServiceSet) {
              //update the processor
              restClient.updateProcessorProperty(property.getProcessGroupId(), property.getProcessorId(), property);
            }
            if (!controllerServiceSet) {
              errors.add(new NifiError(NifiError.SEVERITY.WARN,
                                       "Error trying to enable Controller Service " + controllerServiceName
                                       + " on referencing Processor: " + property.getProcessorName() + " and field " + property
                                           .getKey()
                                       + ". Please go to Nifi and configure and enable this Service before creating this feed.",
                                       "Controller Services"));
            }

          }
        }
      }

    } catch (JerseyClientException e) {
      errors.add(new NifiError(NifiError.SEVERITY.FATAL, "Error trying to identify Controller Services. " + e.getMessage(),
                               "Controller Services"));
    }
  }

  private void markProcessorsAsRunning(NifiProcessGroup newProcessGroup, List<ProcessorDTO> nonInputProcessors) {
    if (newProcessGroup.isSuccess()) {
      try {
        restClient.markProcessorGroupAsRunning(newProcessGroup.getProcessGroupEntity().getProcessGroup());
      } catch (JerseyClientException e) {
        String errorMsg = "Unable to mark feed as " + NifiProcessUtil.PROCESS_STATE.RUNNING + ".";
        newProcessGroup
            .addError(newProcessGroup.getProcessGroupEntity().getProcessGroup().getId(), "", NifiError.SEVERITY.WARN, errorMsg,
                      "Process State");
        newProcessGroup.setSuccess(false);
      }
    }
  }

  private void markInputAsRunning(NifiProcessGroup newProcessGroup, ProcessorDTO input) {
    try {
      restClient.setInputAsRunningByProcessorMatchingType(newProcessGroup.getProcessGroupEntity().getProcessGroup().getId(),
                                                          inputProcessorType);
    } catch (JerseyClientException error) {
      String
          errorMsg =
          "Unable to mark group as " + NifiProcessUtil.PROCESS_STATE.RUNNING + " for " + input.getName() + "("
          + inputProcessorType + ").";
      newProcessGroup
          .addError(newProcessGroup.getProcessGroupEntity().getProcessGroup().getId(), input.getId(), NifiError.SEVERITY.WARN,
                    errorMsg, "Process State");
      newProcessGroup.setSuccess(false);

    }
  }

  private void updateFeedSchedule(NifiProcessGroup newProcessGroup, ProcessorDTO input) {
    if (feedSchedule != null) {
      input.getConfig().setSchedulingPeriod(feedSchedule.getSchedulingPeriod());
      input.getConfig().setSchedulingStrategy(feedSchedule.getSchedulingStrategy());
      input.getConfig().setConcurrentlySchedulableTaskCount(feedSchedule.getConcurrentTasks());
      try {
        restClient.updateProcessor(input);
      } catch (JerseyClientException e) {
        String
            errorMsg =
            "Unable set Scheduling Information for feed " + input.getName() + " on " + input.getType() + ". " + e.getMessage();
        newProcessGroup.addError(input.getParentGroupId(), input.getId(), NifiError.SEVERITY.WARN, errorMsg, "Schedule");
        newProcessGroup.setSuccess(false);
      }
    }
  }
}



