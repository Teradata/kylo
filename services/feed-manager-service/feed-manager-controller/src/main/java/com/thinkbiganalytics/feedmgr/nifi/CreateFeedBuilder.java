package com.thinkbiganalytics.feedmgr.nifi;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.nifi.feedmgr.FeedCreationException;
import com.thinkbiganalytics.nifi.feedmgr.FeedRollbackException;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignProcessGroupComponents;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Builds a NiFi feed based on a NiFi template and a Feed Manager Feed. Internally this uses the NiFi REST API.
 */
public class CreateFeedBuilder {

    private static final Logger log = LoggerFactory.getLogger(CreateFeedBuilder.class);

    LegacyNifiRestClient restClient;

    private String templateId;
    private String category;
    private String feedName;
    private boolean enabled = true;
    private FeedMetadata feedMetadata;
    private PropertyExpressionResolver propertyExpressionResolver;
    private String inputProcessorType;
    private String reusableTemplateCategoryName = "reusable_templates";
    private boolean isReusableTemplate;

    /** List of Input / Output Port connections */
    @Nonnull
    private List<InputOutputPort> inputOutputPorts = Lists.newArrayList();

    private NifiProcessGroup newProcessGroup = null;
    private ProcessGroupDTO previousFeedProcessGroup = null;


    private String version;

    private List<NifiProperty> properties;
    private NifiProcessorSchedule feedSchedule;

    private List<NifiProperty> modifiedProperties;

    private List<NifiError> errors = new ArrayList<>();

    TemplateCreationHelper templateCreationHelper;


    protected CreateFeedBuilder(LegacyNifiRestClient restClient, FeedMetadata feedMetadata, String templateId, PropertyExpressionResolver propertyExpressionResolver) {
        this.restClient = restClient;
        this.feedMetadata = feedMetadata;
        this.category = feedMetadata.getCategory().getSystemName();
        this.feedName = feedMetadata.getSystemFeedName();
        this.templateId = templateId;
        this.templateCreationHelper = new TemplateCreationHelper(this.restClient);
        this.propertyExpressionResolver = propertyExpressionResolver;
    }


    public static CreateFeedBuilder newFeed(LegacyNifiRestClient restClient, FeedMetadata feedMetadata, String templateId, PropertyExpressionResolver propertyExpressionResolver) {
        return new CreateFeedBuilder(restClient, feedMetadata, templateId, propertyExpressionResolver);
    }

    public CreateFeedBuilder feedSchedule(NifiProcessorSchedule feedSchedule) {
        this.feedSchedule = feedSchedule;
        return this;
    }

    public CreateFeedBuilder reusableTemplateCategoryName(String reusableTemplateCategoryName) {
        this.reusableTemplateCategoryName = reusableTemplateCategoryName;
        return this;
    }

    public CreateFeedBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Adds the specified Input Port and Output Port connection to this feed.
     *
     * @param inputOutputPort the port connection
     * @return this feed builder
     */
    public CreateFeedBuilder addInputOutputPort(@Nonnull final InputOutputPort inputOutputPort) {
        inputOutputPorts.add(inputOutputPort);
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


    private void connectFeedToReusableTemplate(String feedGroupId) throws NifiComponentNotFoundException {
        ProcessGroupDTO reusableTemplateCategory = restClient.getProcessGroupByName("root", reusableTemplateCategoryName);
        ProcessGroupDTO feedProcessGroup = restClient.getProcessGroup(feedGroupId, false, false);
        String feedCategoryId = feedProcessGroup.getParentGroupId();
        if(reusableTemplateCategory == null){
            throw new NifiClientRuntimeException("Unable to find the Reusable Template Group. Please ensure NiFi has the 'reusable_templates' processgroup and appropriate reusable flow for this feed."
                                                 + " You may need to import the base reusable template for this feed.");
        }
        String reusableTemplateCategoryGroupId = reusableTemplateCategory.getId();
        for (InputOutputPort port : inputOutputPorts) {
            restClient.connectFeedToGlobalTemplate(feedGroupId, port.getOutputPortName(), feedCategoryId, reusableTemplateCategoryGroupId, port.getInputPortName());
        }
    }

    private void ensureInputPortsForReuseableTemplate(String feedGroupId) throws NifiComponentNotFoundException {
        ProcessGroupDTO template = restClient.getProcessGroup(feedGroupId, false, false);
        String categoryId = template.getParentGroupId();
        restClient.createReusableTemplateInputPort(categoryId, feedGroupId);

    }


    private boolean hasConnectionPorts() {
        return !inputOutputPorts.isEmpty() || isReusableTemplate;
    }


    public NifiProcessGroup build() throws FeedCreationException {
        try {
            newProcessGroup = null;
            TemplateDTO template = restClient.getTemplateById(templateId);

            if (template != null) {

                //create the encompassing process group
                String processGroupId = createProcessGroupForFeed();
                if (StringUtils.isNotBlank(processGroupId)) {
                    //snapshot the existing controller services
                    templateCreationHelper.snapshotControllerServiceReferences();
                    //create the flow from the template
                    templateCreationHelper.instantiateFlowFromTemplate(processGroupId, templateId);

                    updatePortConnectionsForProcessGroup(processGroupId);

                    //mark the new services that were created as a result of creating the new flow from the template
                    templateCreationHelper.identifyNewlyCreatedControllerServiceReferences();

                    //match the properties incoming to the defined properties
                    updateProcessGroupProperties(processGroupId);

                    //Fetch the Feed Group now that it has the flow in it
                    ProcessGroupDTO entity = restClient.getProcessGroup(processGroupId, true, true);

                    ProcessorDTO input = fetchInputProcessorForProcessGroup(entity);
                    ProcessorDTO cleanupProcessor = NifiProcessUtil.findFirstProcessorsByType(NifiProcessUtil.getInputProcessors(entity),
                                                                                              "com.thinkbiganalytics.nifi.v2.metadata.TriggerCleanup");
                    List<ProcessorDTO> nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity);

                    //update any references to the controller services and try to assign the value to an enabled service if it is not already
                    if (input != null) {
                        templateCreationHelper.updateControllerServiceReferences(Lists.newArrayList(input));
                    }
                    if (cleanupProcessor != null) {
                        templateCreationHelper.updateControllerServiceReferences(Collections.singletonList(cleanupProcessor));
                    }
                    templateCreationHelper.updateControllerServiceReferences(nonInputProcessors);
                    //refetch processors for updated errors
                    entity = restClient.getProcessGroup(processGroupId, true, true);
                    input = fetchInputProcessorForProcessGroup(entity);
                    nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity);

                    newProcessGroup = new NifiProcessGroup(entity, input, nonInputProcessors);
                    //align items
                    AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(restClient.getNiFiRestClient(), entity.getParentGroupId());
                    alignProcessGroupComponents.autoLayout();

                    //Validate and if invalid Delete the process group
                    if (newProcessGroup.hasFatalErrors()) {
                        restClient.deleteProcessGroup(entity);
                        // cleanupControllerServices();
                        newProcessGroup.setSuccess(false);
                    } else {

                        //update the input schedule
                        updateFeedSchedule(newProcessGroup, input);

                        //disable all inputs
                       restClient.disableInputProcessors(newProcessGroup.getProcessGroupEntity().getId());
                       //mark everything else as running
                        templateCreationHelper.markProcessorsAsRunning(newProcessGroup);
                       //if desired start the input processor
                        if (input != null) {
                            if(enabled) {
                                markInputAsRunning(newProcessGroup, input);
                                ///make the input/output ports in the category group as running
                                if (hasConnectionPorts()) {
                                    templateCreationHelper.markConnectionPortsAsRunning(entity);
                                }
                            }
                            else {
                                ///make the input/output ports in the category group as running
                                if (hasConnectionPorts()) {
                                    templateCreationHelper.markConnectionPortsAsRunning(entity);
                                }
                                markInputAsStopped(newProcessGroup,input);
                            }
                        }

                        if (newProcessGroup.hasFatalErrors()) {
                            rollback();
                            newProcessGroup.setRolledBack(true);
                            //  cleanupControllerServices();
                            newProcessGroup.setSuccess(false);
                        }
                        List<NifiError> helperErrors = templateCreationHelper.getErrors();
                        if (helperErrors != null) {
                            errors.addAll(helperErrors);
                        }

                        //add any global errors to the object
                        if (errors != null && !errors.isEmpty()) {
                            for (NifiError error : errors) {
                                newProcessGroup.addError(error);
                                if (error.isFatal()) {
                                    newProcessGroup.setSuccess(false);
                                    if (!newProcessGroup.isRolledBack()) {
                                        rollback();
                                        newProcessGroup.setRolledBack(true);
                                    }
                                }
                            }
                        }
                    }
                    templateCreationHelper.cleanupControllerServices();

                }
            }
            return newProcessGroup;
        } catch (NifiClientRuntimeException e) {
            throw new FeedCreationException("Unable to create the feed [" + feedName + "]. " + e.getMessage(), e);
        }
    }

    private ProcessorDTO fetchInputProcessorForProcessGroup(ProcessGroupDTO entity) {
        // Find first processor by type
        final List<ProcessorDTO> inputProcessors = NifiProcessUtil.getInputProcessors(entity);
        final ProcessorDTO input = Optional.ofNullable(NifiProcessUtil.findFirstProcessorsByType(inputProcessors, inputProcessorType))
                .orElseGet(() -> inputProcessors.stream()
                        .filter(processor -> !processor.getType().equals(NifiProcessUtil.CLEANUP_TYPE))
                        .findFirst()
                        .orElse(null)
                );

        // Update cached type
        if (input != null) {
            inputProcessorType = input.getType();
        }

        return input;
    }

    private void updatePortConnectionsForProcessGroup(String processGroupId) throws NifiComponentNotFoundException {
        //if the feed has an outputPort that should go to a reusable Flow then make those connections
        if (!inputOutputPorts.isEmpty()) {
            connectFeedToReusableTemplate(processGroupId);
        }
        if (isReusableTemplate) {
            ensureInputPortsForReuseableTemplate(processGroupId);
        }
    }

    public ProcessGroupDTO rollback() throws FeedRollbackException {
        if (newProcessGroup != null) {
            try {
                restClient.deleteProcessGroup(newProcessGroup.getProcessGroupEntity());
            } catch (NifiClientRuntimeException e) {
                log.error("Unable to delete the ProcessGroup on rollback {} ", e.getMessage());
            }
        }

        String
            parentGroupId =
            newProcessGroup != null ? newProcessGroup.getProcessGroupEntity().getParentGroupId()
                                    : (previousFeedProcessGroup != null ? previousFeedProcessGroup.getParentGroupId() : null);
        try {
            if (StringUtils.isNotBlank(parentGroupId)) {
                ProcessGroupDTO feedGroup = restClient.getProcessGroupByName(parentGroupId, feedName);
                //rename this group to be something else if for some reason we were not able to delete it
                if (feedGroup != null) {
                    feedGroup.setName(feedGroup.getName() + ".rollback - " + new Date().getTime());
                    restClient.updateProcessGroup(feedGroup);
                    feedGroup = restClient.getProcessGroupByName(parentGroupId, feedName);
                }

                //attempt to reset the last version back to this feed process group... do so only if there is no feed group with this name
                //there shouldt be as we should have deleted it above
                if (feedGroup == null) {
                    if (previousFeedProcessGroup != null) {

                        ProcessGroupDTO entity = restClient.getProcessGroup(previousFeedProcessGroup.getId(), false, false);
                        if (entity != null) {
                            entity.setName(feedName);
                            entity = restClient.updateProcessGroup(entity);

                            updatePortConnectionsForProcessGroup(entity.getId());

                            //disable all inputs
                            restClient.disableInputProcessors(entity.getId());

                            //mark everything else as running
                            restClient.markProcessorGroupAsRunning(entity);
                            if (hasConnectionPorts()) {
                                templateCreationHelper.markConnectionPortsAsRunning(entity);
                            }

                            //Set the state correctly for the inputs
                            if(enabled) {
                                restClient.setInputProcessorState(entity.getId(),
                                                                  inputProcessorType, NifiProcessUtil.PROCESS_STATE.RUNNING);
                            }
                            else {
                                restClient.setInputProcessorState(entity.getId(),
                                                                  inputProcessorType, NifiProcessUtil.PROCESS_STATE.STOPPED);
                            }
                            return entity;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new FeedRollbackException("Unable to rollback feed [" + feedName + "] with Parent Group Id of [" + parentGroupId + "] " + e.getMessage(), e);

        }
        return null;
    }


    private String createProcessGroupForFeed() throws FeedCreationException {
        //create Category Process group
        String processGroupId = null;
        ProcessGroupDTO categoryGroup = restClient.getProcessGroupByName("root", category);

        if (categoryGroup == null) {
            try {
                ProcessGroupDTO group = restClient.createProcessGroup(category);
                categoryGroup = group;
            } catch (Exception e) {
                //Swallow exception... it will be handled later
            }
        }
        if (categoryGroup == null) {
            throw new FeedCreationException("Unable to get or create the Process group for the Category " + category
                                            + ". Error occurred while creating instance of template " + templateId + " for Feed "
                                            + feedName);
        }

        //1 create the processGroup
        //check to see if the feed exists... if so version off the old group and create a new group with this feed
        ProcessGroupDTO feedGroup = restClient.getProcessGroupByName(categoryGroup.getId(), feedName);
        if (feedGroup != null) {
            try {
                previousFeedProcessGroup = feedGroup;
                templateCreationHelper.versionProcessGroup(feedGroup);
            } catch (Exception e) {
                throw new FeedCreationException("Previous version of the feed " + feedName
                                                + " was found.  Error in attempting to version the previous feed.  Please go into Nifi and address any issues with the Feeds Process Group", e);
            }
        }

        ProcessGroupDTO group = restClient.createProcessGroup(categoryGroup.getId(), feedName);
        if (group != null) {
            processGroupId = group.getId();
        }
        return processGroupId;
    }


    /**
     * Updates a process groups properties
     */
    private void updateProcessGroupProperties(String processGroupId) throws FeedCreationException {
        List<NifiProperty> propertiesToUpdate = restClient.getPropertiesForProcessGroup(processGroupId);
        //get the Root processGroup
        ProcessGroupDTO rootProcessGroup = restClient.getRootProcessGroup();
        //get this process group
        ProcessGroupDTO activeProcessGroupName = restClient.getProcessGroup(processGroupId, false, false);

        modifiedProperties = new ArrayList<>();
        //resolve the static properties
        //first fill in any properties with static references
        List<NifiProperty> modifiedStaticProperties = propertyExpressionResolver.resolveStaticProperties(propertiesToUpdate);
        // now apply any of the incoming metadata properties to this

        List<NifiProperty> modifiedFeedMetadataProperties = NifiPropertyUtil.matchAndSetPropertyValues(rootProcessGroup.getName(),
                                                                        activeProcessGroupName.getName(),
                                                                        propertiesToUpdate, properties);
        modifiedProperties.addAll(modifiedStaticProperties);
        modifiedProperties.addAll(modifiedFeedMetadataProperties);
        restClient.updateProcessGroupProperties(modifiedProperties);


    }


    private void markInputAsRunning(NifiProcessGroup newProcessGroup, ProcessorDTO input) {
        setInputProcessorState(newProcessGroup,input, NifiProcessUtil.PROCESS_STATE.RUNNING);
    }
    private void markInputAsStopped(NifiProcessGroup newProcessGroup, ProcessorDTO input) {
        setInputProcessorState(newProcessGroup,input, NifiProcessUtil.PROCESS_STATE.STOPPED);
    }

    private void setInputProcessorState(NifiProcessGroup newProcessGroup, ProcessorDTO input, NifiProcessUtil.PROCESS_STATE state) {

            setInputProcessorState(newProcessGroup.getProcessGroupEntity(),
                                              input,state);
    }

    /**
     * Sets the First processors in the {@code processGroup} matching the passed in {@code input} ProcessorType to the passed in {@code state}
     * If the input ins null it will use the default {@code inputType} supplied from the builder
     * @param processGroup the group which should be inspected for the input processors
     * @param input the processor type to match when finding the correct input
     * @param state the state to set the matched input processor
     */
    private void setInputProcessorState(ProcessGroupDTO processGroup, ProcessorDTO input, NifiProcessUtil.PROCESS_STATE state) {
        try {
            if(input != null && (StringUtils.isBlank(inputProcessorType) || !inputProcessorType.equalsIgnoreCase(input.getType()))){
                inputProcessorType = input.getType();
            }

            restClient.setInputProcessorState(processGroup.getId(),
                                              inputProcessorType,state);
        } catch (Exception error) {
            String
                errorMsg =
                "Unable to mark group as " + state + " for " + input.getName() + "("
                + inputProcessorType + ").";
            newProcessGroup
                .addError(newProcessGroup.getProcessGroupEntity().getId(), input.getId(), NifiError.SEVERITY.WARN,
                          errorMsg, "Process State");
            newProcessGroup.setSuccess(false);
        }
    }





    private void updateFeedSchedule(NifiProcessGroup newProcessGroup, ProcessorDTO input) {
        if (feedSchedule != null && input != null) {
            String strategy = feedSchedule.getSchedulingStrategy();
            String schedule = feedSchedule.getSchedulingPeriod();
            //if the input is of type TriggerFeed then make the schedule for that processor Timer Driven in the flow
            if (inputProcessorType.equalsIgnoreCase(NifiFeedConstants.TRIGGER_FEED_PROCESSOR_CLASS)) {
                strategy = NifiFeedConstants.SCHEDULE_STRATEGIES.TIMER_DRIVEN.name();
                schedule = NifiFeedConstants.DEFAULT_TIGGER_FEED_PROCESSOR_SCHEDULE;
            }
            input.getConfig().setSchedulingPeriod(schedule);
            input.getConfig().setSchedulingStrategy(strategy);
            input.getConfig().setConcurrentlySchedulableTaskCount(feedSchedule.getConcurrentTasks());
            try {
                restClient.updateProcessor(input);
            } catch (Exception e) {
                String
                    errorMsg =
                    "Unable set Scheduling Information for feed " + input.getName() + " on " + input.getType() + ". Please check to make sure you set the Timer or Cron Expression correctly";
                newProcessGroup.addError(input.getParentGroupId(), input.getId(), NifiError.SEVERITY.WARN, errorMsg, "Schedule");
                newProcessGroup.setSuccess(false);
            }
        }
    }
}



