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
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.Entity;
import org.apache.nifi.web.api.entity.FlowSnippetEntity;
import org.apache.nifi.web.api.entity.InputPortEntity;
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
  private String feedOutputPortName;
  private String reusableTemplateInputPortName;
  private boolean isReusableTemplate;


  private String version;

  private List<NifiProperty> properties;
  private NifiProcessorSchedule feedSchedule;

  private List<NifiProperty> modifiedProperties;

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



  private void connectFeedToReusableTemplate(String feedGroupId) throws JerseyClientException {
    ProcessGroupDTO reusableTemplateCategory = restClient.getProcessGroupByName("root", reusableTemplateCategoryName);
     ProcessGroupEntity feedProcessGroup = restClient.getProcessGroup(feedGroupId, false, false);
    String feedCategoryId = feedProcessGroup.getProcessGroup().getParentGroupId();
    String reusableTemplateCategoryGroupId = reusableTemplateCategory.getId();
    String inputPortName = reusableTemplateInputPortName;
    restClient
        .connectFeedToGlobalTemplate(feedGroupId, feedOutputPortName, feedCategoryId, reusableTemplateCategoryGroupId,
                inputPortName);
  }

  private void ensureInputPortsForReuseableTemplate(String feedGroupId) throws JerseyClientException {
    ProcessGroupEntity template = restClient.getProcessGroup(feedGroupId, false, false);
    String categoryId = template.getProcessGroup().getParentGroupId();
    restClient.createReusableTemplateInputPort(categoryId, feedGroupId);

  }

  private void markConnectionPortsAsRunning(ProcessGroupEntity feedProcessGroup){
    //1 startAll
    try {
      restClient.startAll(feedProcessGroup.getProcessGroup().getId(),feedProcessGroup.getProcessGroup().getParentGroupId());
    } catch (JerseyClientException e) {
      e.printStackTrace();
    }

    Set<PortDTO> ports = null;
    try {
      ports = restClient.getPortsForProcessGroup(feedProcessGroup.getProcessGroup().getParentGroupId());
    } catch (JerseyClientException e) {
      e.printStackTrace();
    }
    if(ports != null && !ports.isEmpty()) {
     for(PortDTO port: ports){
       port.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
       if(port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name())) {
         try {
           restClient.startInputPort(feedProcessGroup.getProcessGroup().getParentGroupId(),port.getId());
         } catch (JerseyClientException e) {
           e.printStackTrace();
         }
       }
       else if(port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name())) {
         try {
           restClient.startOutputPort(feedProcessGroup.getProcessGroup().getParentGroupId(), port.getId());
         } catch (JerseyClientException e) {
           e.printStackTrace();
         }
       }
     }

    }

  }

  private boolean hasConnectionPorts(){
    return reusableTemplateInputPortName != null || isReusableTemplate;
  }


  public NifiProcessGroup build() throws JerseyClientException {
    NifiProcessGroup newProcessGroup = null;
    TemplateDTO template = restClient.getTemplateById(templateId);

    if (template != null) {
      TemplateCreationHelper templateCreationHelper = new TemplateCreationHelper(this.restClient);
      //create the encompassing process group
      String processGroupId = createProcessGroupForFeed();
      if (StringUtils.isNotBlank(processGroupId)) {
        //snapshot the existing controller services
        templateCreationHelper.snapshotControllerServiceReferences();
        //create the flow from the template
        templateCreationHelper.instantiateFlowFromTemplate(processGroupId, templateId);

        //if the feed has an outputPort that should go to a reusable Flow then make those connections
        if (reusableTemplateInputPortName != null) {
          connectFeedToReusableTemplate(processGroupId);

        }
        if (isReusableTemplate) {
          ensureInputPortsForReuseableTemplate(processGroupId);
        }

        //mark the new services that were created as a result of creating the new flow from the template
        templateCreationHelper.identifyNewlyCreatedControllerServiceReferences();

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

        //update any references to the controller services and try to assign the value to an enabled service if it is not already
        if (input != null) {
          templateCreationHelper.updateControllerServiceReferences(Lists.newArrayList(input));
        }
        templateCreationHelper.updateControllerServiceReferences(nonInputProcessors);
        //refetch processors for updated errors
        entity = restClient.getProcessGroup(processGroupId, true, true);
        input = NifiProcessUtil.findFirstProcessorsByType(inputProcessors, inputProcessorType);
        nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity.getProcessGroup());

        newProcessGroup = new NifiProcessGroup(entity, input, nonInputProcessors);

        //Validate and if invalid Delete the process group
        if (newProcessGroup.hasFatalErrors()) {
          restClient.deleteProcessGroup(entity.getProcessGroup());
          // cleanupControllerServices();
          newProcessGroup.setSuccess(false);
        } else {

          //update the input schedule
          updateFeedSchedule(newProcessGroup, input);

          if (input != null) {
            markInputAsRunning(newProcessGroup, input);
          }

          markProcessorsAsRunning(newProcessGroup, nonInputProcessors);

          ///make the input/output ports in the category group as running
          if(hasConnectionPorts())
          {
            markConnectionPortsAsRunning(entity);
          }

          if (newProcessGroup.hasFatalErrors()) {
            restClient.deleteProcessGroup(entity.getProcessGroup());
            //  cleanupControllerServices();
            newProcessGroup.setSuccess(false);
          }
          List<NifiError> helperErrors = templateCreationHelper.getErrors();
          if(helperErrors != null){
            errors.addAll(helperErrors);
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
        templateCreationHelper.cleanupControllerServices();

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
    if (hasConnectionPorts()) {
      ConnectionsEntity connectionsEntity = restClient.getProcessGroupConnections(feedGroup.getParentGroupId());
      if(connectionsEntity != null) {
        List<ConnectionDTO> connections = null;
        if(reusableTemplateInputPortName != null) {
          //delete the output Port connections (matching this source)
          connections =
              NifiConnectionUtil.findConnectionsMatchingSourceGroupId(connectionsEntity.getConnections(), feedGroup.getId());

          if(connections != null) {
            for(ConnectionDTO connection: connections){
              String type = connection.getDestination().getType();
              if(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(type)){
                //stop the port
                try {
                  restClient.stopOutputPort(connection.getParentGroupId(), connection.getDestination().getId());
                }catch (JerseyClientException e) {
                  e.printStackTrace();
                }
              }
              try {
              restClient.deleteConnection(connection);
              }catch (JerseyClientException e) {
                e.printStackTrace();
              }
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
                try {
                  restClient.stopInputPort(connection.getParentGroupId(), connection.getSource().getId());
              }catch (JerseyClientException e) {
                e.printStackTrace();
              }
              }
              try {
              restClient.deleteConnection(connection);
              }catch (JerseyClientException e) {
                e.printStackTrace();
              }
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



