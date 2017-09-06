package com.thinkbiganalytics.feedmgr.nifi;

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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.nifi.feedmgr.FeedCreationException;
import com.thinkbiganalytics.nifi.feedmgr.FeedRollbackException;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignProcessGroupComponents;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Builds/updates a NiFi feed flow based on a NiFi template and a Feed Manager Feed.
 */
public class CreateFeedBuilder {

    private static final Logger log = LoggerFactory.getLogger(CreateFeedBuilder.class);

    LegacyNifiRestClient restClient;
    TemplateCreationHelper templateCreationHelper;
    private NifiFlowCache nifiFlowCache;
    private String templateId;
    private String category;
    private String feedName;
    private boolean enabled = true;
    private FeedMetadata feedMetadata;
    private PropertyExpressionResolver propertyExpressionResolver;
    private String inputProcessorType;
    private String reusableTemplateCategoryName = TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME;
    private boolean isReusableTemplate;
    /**
     * if true it will remove the versioned process group with the <feed> - timestamp
     * if false it will keep thhe versioned process group
     * These can be cleaned up later through the {@code CleanupStaleFeedRevisions} class
     */
    private boolean removeInactiveVersionedProcessGroup;
    /**
     * List of Input / Output Port connections
     */
    @Nonnull
    private List<InputOutputPort> inputOutputPorts = Lists.newArrayList();
    private NifiProcessGroup newProcessGroup = null;
    private ProcessGroupDTO previousFeedProcessGroup = null;
    private String version;
    private List<NifiProperty> properties;
    private NifiProcessorSchedule feedSchedule;
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;
    private List<NifiProperty> modifiedProperties;
    private List<NifiError> errors = new ArrayList<>();
    /**
     * the category group in NiFi where this feed resides
     **/
    private ProcessGroupDTO categoryGroup;

    private boolean autoAlign = true;


    protected CreateFeedBuilder(LegacyNifiRestClient restClient, NifiFlowCache nifiFlowCache, FeedMetadata feedMetadata, String templateId, PropertyExpressionResolver propertyExpressionResolver,
                                NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        this.restClient = restClient;
        this.nifiFlowCache = nifiFlowCache;
        this.feedMetadata = feedMetadata;
        this.category = feedMetadata.getCategory().getSystemName();
        this.feedName = feedMetadata.getSystemFeedName();
        this.templateId = templateId;
        this.templateCreationHelper = new TemplateCreationHelper(this.restClient);
        this.templateCreationHelper.setTemplateProperties(feedMetadata.getRegisteredTemplate().getProperties());
        this.propertyExpressionResolver = propertyExpressionResolver;
        this.propertyDescriptorTransform = propertyDescriptorTransform;
    }


    public static CreateFeedBuilder newFeed(LegacyNifiRestClient restClient, NifiFlowCache nifiFlowCache, FeedMetadata feedMetadata, String templateId,
                                            PropertyExpressionResolver propertyExpressionResolver, NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        return new CreateFeedBuilder(restClient, nifiFlowCache, feedMetadata, templateId, propertyExpressionResolver, propertyDescriptorTransform);
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

    public CreateFeedBuilder removeInactiveVersionedProcessGroup(boolean removeInactiveVersionedProcessGroup) {
        this.removeInactiveVersionedProcessGroup = removeInactiveVersionedProcessGroup;
        return this;
    }
    public CreateFeedBuilder autoAlign(boolean autoAlign){
        this.autoAlign = autoAlign;
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


    /**
     * Build the NiFi flow instance
     *
     * @return an object indicating if the feed flow was successfully built or not
     */
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


                    List<NifiProperty> updatedControllerServiceProperties = new ArrayList<>();
                    //update any references to the controller services and try to assign the value to an enabled service if it is not already
                    if (input != null) {
                        updatedControllerServiceProperties.addAll(templateCreationHelper.updateControllerServiceReferences(Lists.newArrayList(input)));
                    }
                    if (cleanupProcessor != null) {
                        updatedControllerServiceProperties.addAll(templateCreationHelper.updateControllerServiceReferences(Collections.singletonList(cleanupProcessor)));
                    }
                    updatedControllerServiceProperties.addAll(templateCreationHelper.updateControllerServiceReferences(nonInputProcessors));
                    //refetch processors for updated errors
                    entity = restClient.getProcessGroup(processGroupId, true, true);
                    input = fetchInputProcessorForProcessGroup(entity);
                    nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity);

                    newProcessGroup = new NifiProcessGroup(entity, input, nonInputProcessors);

                    //Validate and if invalid Delete the process group
                    if (newProcessGroup.hasFatalErrors()) {
                        removeProcessGroup(entity);
                        // cleanupControllerServices();
                        newProcessGroup.setSuccess(false);
                    } else {

                        //update the input schedule
                        updateFeedSchedule(newProcessGroup, input);

                        //Cache the processorIds to the respective flowIds for availability in the ProvenanceReportingTask
                        NifiVisitableProcessGroup group = restClient.getFlowOrder(newProcessGroup.getProcessGroupEntity(), null);
                        NifiFlowProcessGroup
                            flow =
                            new NifiFlowBuilder().build(
                                group);
                        nifiFlowCache.updateFlow(feedMetadata, flow);

                        //disable all inputs
                        restClient.disableInputProcessors(newProcessGroup.getProcessGroupEntity().getId());
                        //mark everything else as running
                        templateCreationHelper.markProcessorsAsRunning(newProcessGroup);
                        //if desired start the input processor
                        if (input != null) {
                            if (enabled) {
                                markInputAsRunning(newProcessGroup, input);
                                ///make the input/output ports in the category group as running
                                if (hasConnectionPorts()) {
                                    templateCreationHelper.markConnectionPortsAsRunning(entity);
                                }
                            } else {
                                ///make the input/output ports in the category group as running
                                if (hasConnectionPorts()) {
                                    templateCreationHelper.markConnectionPortsAsRunning(entity);
                                }
                                markInputAsStopped(newProcessGroup, input);
                            }
                        }

                        if (newProcessGroup.hasFatalErrors()) {
                            rollback();
                            newProcessGroup.setRolledBack(true);
                            //  cleanupControllerServices();
                            newProcessGroup.setSuccess(false);
                        }
                        List<NifiError> templateCreationErrors = templateCreationHelper.getErrors();
                        if (templateCreationErrors != null) {
                            errors.addAll(templateCreationErrors);
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
                    //fix the feed metadata controller service references
                    updateFeedMetadataControllerServiceReferences(updatedControllerServiceProperties);

                    //align items
                    if(this.autoAlign) {
                        log.info("Aligning Feed flows in NiFi ");
                        AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(restClient.getNiFiRestClient(), entity.getParentGroupId());
                        alignProcessGroupComponents.autoLayout();
                        //if this is a new feedProcessGroup (i.e. new category), align the root level items also
                        //fetch the parent to get that id to align
                        if (previousFeedProcessGroup == null) {
                            log.info("This is the first feed created in the category {}.  Aligning the categories. ", feedMetadata.getCategory().getSystemName());
                            new AlignProcessGroupComponents(restClient.getNiFiRestClient(), this.categoryGroup.getParentGroupId()).autoLayout();
                        }
                    }
                    else {
                        log.info("Skipping auto alignment in NiFi. You can always manually align this category and all of its feeds by using the rest api: /v1/feedmgr/nifi/auto-align/{}",entity.getParentGroupId());
                        if (previousFeedProcessGroup == null) {
                            log.info("To re align the categories: /v1/feedmgr/nifi/auto-align/{}",this.categoryGroup.getParentGroupId());
                        }
                    }


                }
            }
            return newProcessGroup;
        } catch (NifiClientRuntimeException e) {
            throw new FeedCreationException("Unable to create the feed [" + feedName + "]. " + e.getMessage(), e);
        }
    }


    /**
     * update feed metadata to point to the valid controller services
     * @param updatedControllerServiceProperties
     */
    private void updateFeedMetadataControllerServiceReferences(List<NifiProperty>updatedControllerServiceProperties){
        //map of the previous to new service values
        Map<String,String> controllerServiceChangeMap =updatedControllerServiceProperties.stream().collect(Collectors.toMap(p-> p.getProcessorNameTypeKey(), p->p.getValue(), (service1, service2) -> service1));
        if(!updatedControllerServiceProperties.isEmpty()){
            feedMetadata.getProperties().stream().filter(property -> controllerServiceChangeMap.containsKey(property.getProcessorNameTypeKey())).forEach(
                (NifiProperty p) -> p.setValue(controllerServiceChangeMap.get(p.getProcessorNameTypeKey())));
        }
    }

    public ProcessGroupDTO rollback() throws FeedRollbackException {
        if (newProcessGroup != null) {
            try {
                removeProcessGroup(newProcessGroup.getProcessGroupEntity());
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
                //there shouldn't be as we should have deleted it above
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
                            if (enabled) {
                                restClient.setInputProcessorState(entity.getId(),
                                                                  inputProcessorType, NifiProcessUtil.PROCESS_STATE.RUNNING);
                            } else {
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


    private void connectFeedToReusableTemplate(String feedGroupId) throws NifiComponentNotFoundException {
        ProcessGroupDTO reusableTemplateCategory = restClient.getProcessGroupByName("root", reusableTemplateCategoryName);
        ProcessGroupDTO feedProcessGroup = restClient.getProcessGroup(feedGroupId, false, false);
        String feedCategoryId = feedProcessGroup.getParentGroupId();
        if (reusableTemplateCategory == null) {
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


    private String createProcessGroupForFeed() throws FeedCreationException {
        //create Category Process group
        String processGroupId = null;
        this.categoryGroup = restClient.getProcessGroupByName("root", category);

        if (categoryGroup == null) {
            try {
                ProcessGroupDTO group = restClient.createProcessGroup(category);
                this.categoryGroup = group;
            } catch (Exception e) {
                //Swallow exception... it will be handled later
            }
        }
        if (this.categoryGroup == null) {
            throw new FeedCreationException("Unable to get or create the Process group for the Category " + category
                                            + ". Error occurred while creating instance of template " + templateId + " for Feed "
                                            + feedName);
        }

        //1 create the processGroup
        //check to see if the feed exists... if so version off the old group and create a new group with this feed
        ProcessGroupDTO feedGroup = restClient.getProcessGroupByName(this.categoryGroup.getId(), feedName);
        if (feedGroup != null) {
            try {
                previousFeedProcessGroup = feedGroup;
                templateCreationHelper.versionProcessGroup(feedGroup);
            } catch (Exception e) {
                throw new FeedCreationException("Previous version of the feed " + feedName
                                                + " was found.  Error in attempting to version the previous feed.  Please go into Nifi and address any issues with the Feeds Process Group", e);
            }
        }

        ProcessGroupDTO group = restClient.createProcessGroup(this.categoryGroup.getId(), feedName);
        if (group != null) {
            processGroupId = group.getId();
        }
        return processGroupId;
    }


    /**
     * removes the {@code previousFeedProcessGroup} from nifi
     */
    public void checkAndRemoveVersionedProcessGroup() {
        if (this.removeInactiveVersionedProcessGroup && previousFeedProcessGroup != null) {
            removeProcessGroup(previousFeedProcessGroup);
        }
    }


    /**
     * Removes a given processGroup from NiFi if nothing is in its queue
     */
    private void removeProcessGroup(ProcessGroupDTO processGroupDTO) {
        if (processGroupDTO != null) {
            try {
                //validate if nothing is in the queue then remove it
                Optional<ProcessGroupStatusDTO> statusDTO = restClient.getNiFiRestClient().processGroups().getStatus(processGroupDTO.getId());
                if (statusDTO.isPresent() && propertyDescriptorTransform.getQueuedCount(statusDTO.get()).equalsIgnoreCase("0")) {
                    //get connections linking to this group, delete them
                    Set<ConnectionDTO> connectionDTOs = restClient.getProcessGroupConnections(processGroupDTO.getParentGroupId());
                    if (connectionDTOs == null) {
                        connectionDTOs = new HashSet<>();
                    }
                    Set<ConnectionDTO>
                        versionedConnections =
                        connectionDTOs.stream().filter(connectionDTO -> connectionDTO.getDestination().getGroupId().equalsIgnoreCase(processGroupDTO.getId()) || connectionDTO.getSource().getGroupId()
                            .equalsIgnoreCase(processGroupDTO.getId()))
                            .collect(Collectors.toSet());
                    restClient.deleteProcessGroupAndConnections(processGroupDTO, versionedConnections);
                    log.info("removed the versioned processgroup {} ", processGroupDTO.getName());
                } else {
                    log.info("Unable to remove the versioned processgroup {} ", processGroupDTO.getName());
                }
            } catch (Exception e) {
                log.error("Unable to remove the versioned processgroup {} ", processGroupDTO.getName(), e);
            }
        }
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
        setInputProcessorState(newProcessGroup, input, NifiProcessUtil.PROCESS_STATE.RUNNING);
    }

    private void markInputAsStopped(NifiProcessGroup newProcessGroup, ProcessorDTO input) {
        setInputProcessorState(newProcessGroup, input, NifiProcessUtil.PROCESS_STATE.STOPPED);
    }

    private void setInputProcessorState(NifiProcessGroup newProcessGroup, ProcessorDTO input, NifiProcessUtil.PROCESS_STATE state) {

        setInputProcessorState(newProcessGroup.getProcessGroupEntity(),
                               input, state);
    }

    /**
     * Sets the First processors in the {@code processGroup} matching the passed in {@code input} ProcessorType to the passed in {@code state}
     * If the input ins null it will use the default {@code inputType} supplied from the builder
     *
     * @param processGroup the group which should be inspected for the input processors
     * @param input        the processor type to match when finding the correct input
     * @param state        the state to set the matched input processor
     */
    private void setInputProcessorState(ProcessGroupDTO processGroup, ProcessorDTO input, NifiProcessUtil.PROCESS_STATE state) {
        try {
            if (input != null && (StringUtils.isBlank(inputProcessorType) || !inputProcessorType.equalsIgnoreCase(input.getType()))) {
                inputProcessorType = input.getType();
            }

            restClient.setInputProcessorState(processGroup.getId(),
                                              inputProcessorType, state);
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
            NifiProcessorSchedule scheduleCopy = new NifiProcessorSchedule(feedSchedule);
            scheduleCopy.setProcessorId(input.getId());
            scheduleCopy.setSchedulingPeriod(schedule);
            scheduleCopy.setSchedulingStrategy(strategy);
            try {
                restClient.getNiFiRestClient().processors().schedule(scheduleCopy);
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



