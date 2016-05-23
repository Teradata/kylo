package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.BulletinBoardDTO;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.entity.BulletinBoardEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by sr186054 on 2/26/16.
 */
@Component
public class NifiComponentFlowData {
    private static final Logger LOG = LoggerFactory.getLogger(NifiComponentFlowData.class);


    @Autowired
    private NifiRestClient nifiRestClient;
    Map<String, String> processorToGroupIdMap = new HashMap<>();


    Map<String, String> componentIdFeedNameMap = new HashMap<>();
    Map<String, ProcessGroupDTO> componentIdFeedProcessGroupMap = new HashMap<>();
    Map<ProcessGroupDTO, Map<String, ProcessorDTO>> feedProcessorMap = new HashMap<>();

    Map<ProcessGroupDTO, Set<ProcessorDTO>> feedEndingProcessors = new HashMap<>();

    Map<ProcessGroupDTO, Map<String, ProcessorDTO>> feedFailureProcessors = new HashMap<>();

    Map<String, ProcessorDTO> processorMap = new HashMap<>();


    private void populateFeedFailureProcessorMap(ProcessGroupDTO feedGroup) {
        Map<String, ProcessorDTO> failureMap = new HashMap<>();
        feedFailureProcessors.put(feedGroup, failureMap);
        try {
            LOG.debug("Check for failure processors on group " + feedGroup.getId());
            Set<ProcessorDTO> failureProcessors = nifiRestClient.getFailureProcessors(feedGroup.getId());
            LOG.debug(" Found failure Processors as " + failureProcessors);
            if (failureProcessors != null && !failureProcessors.isEmpty()) {

                for (ProcessorDTO processorDTO : failureProcessors) {
                    failureMap.put(processorDTO.getId(), processorDTO);
                }
            }

        } catch (JerseyClientException e) {
            e.printStackTrace();
        }
    }

    /*
    ExecuteHQLStatement[id=65919315-16a2-4552-9c30-c4aa2b57d088] Unable to execute SQL DDL alter table employees.workers_feed add
    if not exists partition (processing_dttm=20160307225142)
    location '/etl/employees/workers/20160307225142/' for StandardFlowFileRecord[uuid=8de76059-2959-49e6-9cba-a6487132986d,claim=StandardContentClaim [resourceClaim=StandardResourceClaim[id=1457386676814-2, container=default, section=2], offset=430440, length=143480],offset=0,name=userdata1.csv,size=143480] due to org.apache.hive.service.cli.HiveSQLException: Error while compiling statement: FAILED: HiveAccessControlException Permission denied: user [nifi] does not have [READ] privilege on [hdfs://sandbox.hortonworks.com:8020/etl/employees/workers/20160307225142]; routing to failure: org.apache.hive.service.cli.HiveSQLException: Error while compiling statement: FAILED: HiveAccessControlException Permission denied: user [nifi] does not have [READ] privilege on [hdfs://sandbox.hortonworks.com:8020/etl/employees/workers/20160307225142]


    PutFile[id=8d7e559d-78eb-4f8d-99c4-72e20ea9fb16] Penalizing StandardFlowFileRecord[uuid=74bf96ac-f984-45ec-9c47-c53123d5f4e3,claim=StandardContentClaim [resourceClaim=StandardResourceClaim[id=1457386676814-2, container=default, section=2], offset=286960, length=143480],offset=0,name=userdata1.csv,size=143480] and routing to 'failure' because the output directory /tmp/test123 does not exist and Processor is configured not to create missing directories
*/
    public String getFlowFileUUIDFromBulletinMessage(String message) {
        return StringUtils.substringBetween(message, "StandardFlowFileRecord[uuid=", ",");
    }


    public List<BulletinDTO> getBulletinsNotYetProcessed(ProvenanceEventRecordDTO event) {
        NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
        List<BulletinDTO> dtos = new ArrayList<>();
        List<BulletinDTO> bulletinDTOList = getFeedBulletinsForComponentInFlowFile(event);
        for (BulletinDTO dto : bulletinDTOList) {
            if (!jobExecution.isBulletinProcessed(dto)) {
                dtos.add(dto);
            }
        }
        return dtos;
    }

    public List<BulletinDTO> getBulletinsNotYetProcessedForComponent(final ProvenanceEventRecordDTO event) {

        List<BulletinDTO> bulletinDTOs = getBulletinsNotYetProcessed(event);

        return Lists.newArrayList(Iterables.filter(bulletinDTOs, new Predicate<BulletinDTO>() {
            @Override
            public boolean apply(BulletinDTO bulletinDTO) {
                if (bulletinDTO == null || bulletinDTO.getSourceId() == null) {
                    return false;
                }
                return event.getFlowFileComponent().getComponentId().equalsIgnoreCase(bulletinDTO.getSourceId());
            }
        }));
    }

    public List<BulletinDTO> getBulletinsNotYetProcessedForOtherComponents(final ProvenanceEventRecordDTO event) {

        List<BulletinDTO> bulletinDTOs = getBulletinsNotYetProcessed(event);

        return Lists.newArrayList(Iterables.filter(bulletinDTOs, new Predicate<BulletinDTO>() {
            @Override
            public boolean apply(BulletinDTO bulletinDTO) {
                if (bulletinDTO == null || bulletinDTO.getSourceId() == null) {
                    return false;
                }
                return !event.getFlowFileComponent().getComponentId().equalsIgnoreCase(bulletinDTO.getSourceId());
            }
        }));
    }

    public String getBulletinDetails(Collection<BulletinDTO> dtos) {
        StringBuffer sb = null;
        if (dtos != null) {
            for (BulletinDTO dto : dtos) {
                if (sb == null) {
                    sb = new StringBuffer();
                } else {
                    sb.append("\n");
                }
                sb.append(dto.getMessage());
            }
        }
        if (sb != null) {
            return sb.toString();
        }
        return null;
    }

    public ProvenanceEventRecordDTO createFailedComponentPriorToEvent(ProvenanceEventRecordDTO event, String componentId, Set<BulletinDTO> dtos) {
        NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
        ProvenanceEventRecordDTO provenanceEventRecordDTO = new ProvenanceEventRecordDTO();
        provenanceEventRecordDTO.setDetails(getBulletinDetails(dtos));
        provenanceEventRecordDTO.markRunning();
        FlowFileComponent component = new FlowFileComponent(componentId);
        component.markRunning();
        component.setJobExecution(jobExecution);
        ProcessorDTO processorDTO = processorMap.get(componentId);
        component.setComponetName(processorDTO.getName());
        provenanceEventRecordDTO.setComponentId(componentId);
        provenanceEventRecordDTO.setFlowFileComponent(component);
        provenanceEventRecordDTO.setComponentType(processorDTO.getType());
        FlowFileEvents flowFile = jobExecution.getFlowFile().findFlowFile(event.getFlowFileUuid());
        provenanceEventRecordDTO.setFlowFile(flowFile);
        provenanceEventRecordDTO.setFlowFileUuid(flowFile.getUuid());
        flowFile.addEventPriorTo(provenanceEventRecordDTO, event);
        LOG.info("Added new Failure Event with id " + provenanceEventRecordDTO.getEventId() + " and time " + provenanceEventRecordDTO.getEventTime() + " with details " + provenanceEventRecordDTO.getDetails());
        //mark the bulletins as processed for the job
        jobExecution.addBulletinErrors(dtos);
        return provenanceEventRecordDTO;
    }


    public void setBulletinsToEventDetails(ProvenanceEventRecordDTO event) {
        LOG.debug(" attempt to setBulletinsToEventDetails for " + event.getFlowFileComponent().getComponetName());
        List<BulletinDTO> bulletinDTOList = getFeedBulletinsForComponentInFlowFile(event);
        List<BulletinDTO> currentComponentErrors = new ArrayList<>();
        Map<String, Set<BulletinDTO>> otherErrors = new HashMap<>();
        NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
        Set<String> otherComponents = new HashSet<String>();
        Map<String, StringBuffer> componentIdMessage = new HashMap<>();
        if (bulletinDTOList != null) {
            for (BulletinDTO dto : bulletinDTOList) {
                if (!jobExecution.isBulletinProcessed(dto)) {
                    if (dto.getSourceId().equalsIgnoreCase(event.getComponentId())) {
                        currentComponentErrors.add(dto);
                    } else {
                        if (!otherErrors.containsKey(dto.getSourceId())) {
                            otherErrors.put(dto.getSourceId(), new HashSet<BulletinDTO>());
                        }
                        otherErrors.get(dto.getSourceId()).add(dto);
                        otherComponents.add(dto.getSourceId());
                    }

                    if (!componentIdMessage.containsKey(dto.getSourceId())) {
                        componentIdMessage.put(dto.getSourceId(), new StringBuffer(dto.getMessage()));
                    } else {
                        componentIdMessage.get(dto.getSourceId()).append("\n").append(dto.getMessage());
                    }
                }
            }
            LOG.debug(" Bulletin Events for currentComponentErrors for " + event.getFlowFileComponent().getComponetName() + " are: " + currentComponentErrors.size());
            LOG.debug(" Found Bulletin Events otherComponents for " + otherComponents + " are: " + otherComponents.size());


            //add in the current component errors
            StringBuffer sb = null;
            for (BulletinDTO dto : currentComponentErrors) {
                if (sb == null) {
                    sb = new StringBuffer();
                } else {
                    sb.append("\n");
                }
                sb.append(dto.getMessage());
                jobExecution.addBulletinError(dto);
            }
            if (sb != null) {
                event.setDetails(sb.toString());
                LOG.info(" Adding current details for   in new Component of " + event.getFlowFileComponent().getComponetName() + " as : " + sb.toString());
            }
        }

    }


    public List<BulletinDTO> getFeedBulletinsForComponentInFlowFile(ProvenanceEventRecordDTO event) {
        BulletinBoardEntity entity = null;
        ProcessGroupDTO feedGroup = getFeedProcessGroup(event);
        final String flowFileUUID = event.getFlowFileUuid();
        try {
            entity = nifiRestClient.getProcessGroupBulletins(feedGroup.getId());
            if (entity != null) {
                final BulletinBoardDTO bulletinBoardDTO = entity.getBulletinBoard();

                return Lists.newArrayList(Iterables.filter(bulletinBoardDTO.getBulletins(), new Predicate<BulletinDTO>() {
                    @Override
                    public boolean apply(BulletinDTO bulletinDTO) {
                        return flowFileUUID.equalsIgnoreCase(getFlowFileUUIDFromBulletinMessage(bulletinDTO.getMessage()));
                    }
                }));

            }
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<BulletinDTO> getProcessorBulletinsForComponentInFlowFile(final String flowFileUUID, String componentId) {
        BulletinBoardEntity entity = null;
        try {
            entity = nifiRestClient.getProcessorBulletins(componentId);
            if (entity != null) {
                final BulletinBoardDTO bulletinBoardDTO = entity.getBulletinBoard();

                return Lists.newArrayList(Iterables.filter(bulletinBoardDTO.getBulletins(), new Predicate<BulletinDTO>() {
                    @Override
                    public boolean apply(BulletinDTO bulletinDTO) {
                        return flowFileUUID.equalsIgnoreCase(getFlowFileUUIDFromBulletinMessage(bulletinDTO.getMessage()));
                    }
                }));

            }
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<BulletinDTO> getProcessorBulletinsForComponentInFlowFile(FlowFileEvents rootFlowFile, String componentId) {
        BulletinBoardEntity entity = null;
        try {
            entity = nifiRestClient.getProcessorBulletins(componentId);
            if (entity != null) {
                final BulletinBoardDTO bulletinBoardDTO = entity.getBulletinBoard();
               final Set<String> flowFileIds = rootFlowFile.getAllFlowFileIds();
                final NifiJobExecution jobExecution = rootFlowFile.getNifiJobExecution();

                List<BulletinDTO> bulletins = Lists.newArrayList(Iterables.filter(bulletinBoardDTO.getBulletins(), new Predicate<BulletinDTO>() {
                    @Override
                    public boolean apply(BulletinDTO bulletinDTO) {


                        boolean match = flowFileIds.contains(getFlowFileUUIDFromBulletinMessage(bulletinDTO.getMessage()));
                        if (match && !jobExecution.isBulletinProcessed(bulletinDTO)) {
                            return true;
                        }
                        return false;

                    }
                }));
                return bulletins;

            }
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }
        return null;
    }



    public Set<ProcessorDTO> getEndingProcessors(ProvenanceEventRecordDTO event) {
        ProcessGroupDTO feedGroup = getFeedProcessGroup(event);
        return getEndingProcessors(feedGroup);
    }

    public Set<String> getEndingProcessorIds(ProvenanceEventRecordDTO event) {
        Set<String> ids = new HashSet<>();
        Set<ProcessorDTO> dtos = getEndingProcessors(event);
        if (dtos != null) {
            for (ProcessorDTO dto : dtos) {
                ids.add(dto.getId());
            }
        }
        return ids;
    }


    private Set<ProcessorDTO> getEndingProcessors(ProcessGroupDTO feedGroup) {
        LOG.debug("getEndingProcessors for {} ... feedGroup is null?? ", feedGroup, (feedGroup == null ? true : false));
        if (!feedEndingProcessors.containsKey(feedGroup)) {
            Set<ProcessorDTO> processorDTOs = new HashSet<>();
            try {
                Set<NifiVisitableProcessor> processors = null;
                NifiVisitableProcessGroup group =  nifiRestClient.getFlowOrder(feedGroup.getId());
                if(group != null){
                    processors = group.getEndingProcessors();
                }
                if (processors != null) {
                    for (NifiVisitableProcessor p : processors) {
                        processorDTOs.add(p.getDto());
                    }
                }
                feedEndingProcessors.put(feedGroup, processorDTOs);
            } catch (JerseyClientException e) {
                e.printStackTrace();
            }
        }
        return feedEndingProcessors.get(feedGroup);

    }


    public Integer getEndingProcessorCount(ProvenanceEventRecordDTO event) {
        ProcessGroupDTO feedGroup = getFeedProcessGroup(event);
        Set<ProcessorDTO> endingProcessors = getEndingProcessors(feedGroup);
        if (endingProcessors != null) {
            return endingProcessors.size();
        } else {
            return 0;
        }

    }


    public boolean isFailureProcessor(ProvenanceEventRecordDTO event) {
        ProcessGroupDTO feedGroup = getFeedProcessGroup(event);
        if(feedGroup != null) {
            if (!feedFailureProcessors.containsKey(feedGroup)) {
                LOG.debug("Check for Failure Processor on Feed Group " + feedGroup);
                populateFeedFailureProcessorMap(feedGroup);
            }
            boolean isFailure = feedFailureProcessors.get(feedGroup).containsKey(event.getComponentId());
            if (isFailure) {
                LOG.debug("Failure Processor Found:  {} ", event.getFlowFileComponent());
            }
            return feedFailureProcessors.get(feedGroup).containsKey(event.getComponentId());
        }
        else {
            LOG.warn("Warning unable to find Feed Process Group for Event {} when checkign for isFailureProcessor",event);
            return false;
        }
    }

    public void populateGroupIdForEvent(ProvenanceEventRecordDTO event) {
        if (event.getGroupId() == null) {
            String componentId = event.getComponentId();
            String groupId = getGroupIdForComponent(event.getComponentId());
            LOG.debug("FOUND GROUP ID FOR COMPONENT " + event.getComponentId() + " AS " + groupId);
            event.setGroupId(groupId);
        }
    }

    public String getGroupIdForComponent(String componentId) {
        String groupId = processorToGroupIdMap.get(componentId);
        if (groupId == null) {
            populateProcessorGroupIdMap(null);
        }
        return processorToGroupIdMap.get(componentId);
    }


    private void populateProcessorGroupIdMap(ProcessGroupDTO root) {

        try {
            if (root == null) {
                ProcessGroupEntity processGroupEntity = nifiRestClient.getRootProcessGroup();
                root = processGroupEntity.getProcessGroup();
            }
            //first level is the category

            Map<String, ProcessorDTO> processorDTOMap = NifiProcessUtil.getProcessorsMap(root);
            processorMap.putAll(processorDTOMap);
            for (ProcessorDTO processorDTO : processorDTOMap.values()) {
                processorToGroupIdMap.put(processorDTO.getId(), processorDTO.getParentGroupId());
            }

        } catch (JerseyClientException e) {
            e.printStackTrace();
        }

    }


    private void populateComponentIdFeedNameMap() {

        try {
            ProcessGroupEntity processGroupEntity = nifiRestClient.getRootProcessGroup();
            ProcessGroupDTO root = processGroupEntity.getProcessGroup();
            //first level is the category
            for (ProcessGroupDTO category : root.getContents().getProcessGroups()) {
                for (ProcessGroupDTO feedProcessGroup : category.getContents().getProcessGroups()) {
                    //second level is the feed
                    String feedName = category.getName() + "." + feedProcessGroup.getName();
                    Map<String, ProcessorDTO> feedProcessors = getFeedProcessorsUsingFlowOrder(feedName, feedProcessGroup.getId());//NifiProcessUtil.getProcessorsMap(feed);
                    //    LOG.info("populate ComponentId Feed Name Map for "+feedName+ " with "+feedProcessors.size()+" processors");
                    processorMap.putAll(feedProcessors);
                    //map the feed to the set of processors
                    //this will be used for the other processors to lookup the correct component name
                    feedProcessorMap.put(feedProcessGroup, feedProcessors);
                    for (ProcessorDTO processor : feedProcessors.values()) {
                        String key = componentIdFeedNameMapKey(feedProcessGroup.getId(), processor.getId());
                        //  LOG.info("KEY "+key+" group: "+feedProcessGroup.getId()+" Feed: "+feedName);
                        componentIdFeedNameMap.put(key, feedName);
                        componentIdFeedProcessGroupMap.put(key, feedProcessGroup);

                    }
                }
            }
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }

    }

    public ProcessorDTO getProcessor(String id) {
        return processorMap.get(id);
    }

    private String componentIdFeedNameMapKey(String feedProcessGroupId, String componentId) {
        return feedProcessGroupId + "_" + componentId;
    }

    public String componentIdFeedNameMapKey(ProvenanceEventRecordDTO event) {
        ///   LOG.info("componentIdFeedNameMapKey for Component: "+event.getComponentId()+" and FirstEvent: "+event.getFlowFile().getFirstEvent()+" and First Group: "+event.getFlowFile().getFirstEvent().getGroupId());
        return componentIdFeedNameMapKey(event.getFlowFile().getRoot().getFirstEvent().getGroupId(), event.getComponentId());
    }

    public String getFeedNameForEvent(ProvenanceEventRecordDTO event) {
        return componentIdFeedNameMap.get(componentIdFeedNameMapKey(event));
    }

    public ProcessGroupDTO getFeedProcessGroupForEvent(ProvenanceEventRecordDTO event) {
        return componentIdFeedProcessGroupMap.get(componentIdFeedNameMapKey(event));
    }

    public Map<String, ProcessorDTO> getFeedProcessorsUsingFlowOrder(String feedName, String feedProcessGroupId) throws JerseyClientException {
        //  LOG.info("getProcessorsForFlow for Feed "+feedName+" ID: "+feedProcessGroupId);
        Set<ProcessorDTO> processorDTOs = nifiRestClient.getProcessorsForFlow(feedProcessGroupId);
        Map<String, ProcessorDTO> feedProcessors = new HashMap<>();

        if (processorDTOs != null) {

            for (ProcessorDTO p : processorDTOs) {
                feedProcessors.put(p.getId(), p);

            }
        }
        return feedProcessors;
    }


    public String getFeedNameForComponentId(ProvenanceEventRecordDTO event) {
        String feedName = getFeedNameForEvent(event);
        boolean found = true;
        if (feedName == null) {
            found = false;
            LOG.debug("Unable to find feed name for Component .. populate the map " + event.getComponentId());
            populateComponentIdFeedNameMap();

        }
        feedName = getFeedNameForEvent(event);
        if (!found && feedName != null) {
            LOG.debug("Found feed name  for Component " + event.getComponentId() + " as " + feedName);

        }
        return feedName;
    }


    public ProcessGroupDTO getFeedProcessGroup(ProvenanceEventRecordDTO event) {
        ProcessGroupDTO feedGroup = getFeedProcessGroupForEvent(event);
        boolean found = true;
        if (feedGroup == null) {
            found = false;
            LOG.debug("Unable to find feed group for Component " + event.getComponentId() + " id: " + componentIdFeedNameMapKey(event));
            populateComponentIdFeedNameMap();
            //  LOG.info("componentIdFeedNameMap {} ", componentIdFeedNameMap);
        }
        feedGroup = getFeedProcessGroupForEvent(event);
        if (!found && feedGroup != null) {
            LOG.debug("Found feed group  for Component " + event.getComponentId() + " as " + feedGroup.getName() + "(" + feedGroup.getId() + ")");

        }
        return feedGroup;
    }


    public ProcessorDTO getFeedProcessor(ProvenanceEventRecordDTO event) {
        String componentId = event.getComponentId();
        ProcessGroupDTO feedGroup = getFeedProcessGroup(event);
        if (feedGroup != null) {
            Map<String, ProcessorDTO> feedProcessors = feedProcessorMap.get(feedGroup);
            if (feedProcessors != null) {
                return feedProcessors.get(componentId);
            }
        }
        return null;
    }


}
