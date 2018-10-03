package com.thinkbiganalytics.nifi.provenance.repo;
/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.thinkbiganalytics.nifi.provenance.RemoteMessageResponseWithRelatedFlowFiles;
import com.thinkbiganalytics.nifi.provenance.model.RemoteEventMessageResponse;
import com.thinkbiganalytics.nifi.provenance.model.RemoteEventeMessageResponseHolder;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class RemoteProvenanceEventService {

    private static final Logger log = LoggerFactory.getLogger(RemoteProvenanceEventService.class);

    /**
     * Map the event flowFileId to the remote (sourceFlowFileId)
     * flowfile id to remote flowfile id
     */
    protected Map<String,String> flowFileIdToRemoteFlowFileId = new ConcurrentHashMap<>();

    /**
     * Map of the source flowfile and all those flow files on this node related to id
     */
    private LoadingCache<String,Set<String>> sourceFlowFileToFlowFileIds;

    /**
     * Queue of all those incoming flow files that need more information
     * This will be used to make a request via JMS to get more information from other nodes about the flowfiles in the payload
     * this will be drained during the standard Sending Provenance Data (defaulted to every 3 seconds)
     */
    private LinkedBlockingQueue<ProvenanceEventRecordWithId> sourceRemoteInputPortEvents = new LinkedBlockingQueue<>();

    /**
     * Map of incoming starting remote flow file to list of other events that are related
     */
    private LoadingCache<String,Set<ProvenanceEventRecordWithId>> waitingRemoteEvents;

    /**
     * Remote Input Port SEND events mapping of flowfile -> RemoteSourceFlowFile tracking info
     */
    private LoadingCache<String,RemoteSourceFlowFile> remoteSourceFlowFileInformation;

    /**
     * The Map of flowFile -> RemoteSourceFlowFiles that can be removed from this nodes FeedStatistcs Mapping
     */
    private Map<String,RemoteSourceFlowFile> remoteFilesAvailableToBeRemoved = new ConcurrentHashMap<>();


    /**
     * Lock used when adding events to the Queue for waiting events
     * and when events are pulled off the queue
     */
    private ReentrantLock lock = new ReentrantLock();


    private static final RemoteProvenanceEventService instance = new RemoteProvenanceEventService();

    private RemoteProvenanceEventService() {

        Integer expireTime = ConfigurationProperties.getInstance().getRemoteInputPortExpireTimeSeconds();

        this.sourceFlowFileToFlowFileIds = CacheBuilder.newBuilder()
            .expireAfterAccess(expireTime,TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, Set<String>>() {
                @Override
                public void onRemoval(RemovalNotification<String, Set<String>> removalNotification) {
                    log.debug("KYLO-DEBUG: SourceFlowFileToFlowFile Cache Removal Listener. Removing entry from sourceFlowFileToFlowFileIds {}",removalNotification.getKey());
                }
            })
            .build(new CacheLoader<String, Set<String>>() {
                @Override
                public Set<String> load(String s) throws Exception {
                    return new HashSet<>();
                }
            });

        this.waitingRemoteEvents = CacheBuilder.newBuilder()
            .expireAfterAccess(expireTime,TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, Set<ProvenanceEventRecordWithId>>() {
                @Override
                public void onRemoval(RemovalNotification<String, Set<ProvenanceEventRecordWithId>> removalNotification) {
                    log.debug("KYLO-DEBUG: WaitingRemoteEvents Cache Removal Listener. Removing entry from waitingRemoteEvents {}",removalNotification.getKey());
                }
            })
            .build(new CacheLoader<String, Set<ProvenanceEventRecordWithId>>() {
                @Override
                public Set<ProvenanceEventRecordWithId> load(String s) throws Exception {
                    return new HashSet<>();
                }
            });

        remoteSourceFlowFileInformation = CacheBuilder.newBuilder()
            .expireAfterAccess(expireTime,TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, RemoteSourceFlowFile>() {
                @Override
                public void onRemoval(RemovalNotification<String, RemoteSourceFlowFile> removalNotification) {
                    log.debug("KYLO-DEBUG: RemoteSourceFlowFileInfo Cache Removal Listener. Removing entry from remoteSourceFlowFileInformation {}",removalNotification.getKey());
                }
            })
            .build(new CacheLoader<String, RemoteSourceFlowFile>() {
                @Override
                public RemoteSourceFlowFile load(String flowFileId) throws Exception {
                    return new RemoteSourceFlowFile(flowFileId);
                }
            });
    }




    public static RemoteProvenanceEventService getInstance() {
        return instance;
    }

    /**
     * Is the current event on this node waiting for additional RemoteEventResponse data
     * before processing
     * @param incomingFlowFileId the current flow file for this node
     * @return true if waiting, false if not
     */
    public boolean isWaitingRemoteFlowFile(String incomingFlowFileId){
        return flowFileIdToRemoteFlowFileId.containsKey(incomingFlowFileId);
    }



    /**
     * Detects the first incoming port event coming from a Remote Process Group where the incoming event doesnt have a parent feed flow file
     * relationship.  If in this state more information is needed from the remote system to complete the feed flow.
     *
     * Add the Source Flow file ( the ff that came from the other NiFi node and relate it to this event and this flow file id)
     * This data will be sent to JMS to gather more information
     *
     * @param sourceSystemFlowFileIdentifier the flow file from the other nifi node
     * @param event the event
     * @param eventId the event id
     */
    public void addRemoteSourceEventToQueue(String sourceSystemFlowFileIdentifier, ProvenanceEventRecord event, Long eventId)
    {
        //relate the source ff back to itself
        addRemoteFlowFile(sourceSystemFlowFileIdentifier,sourceSystemFlowFileIdentifier);
        //relate this event to the source
        addRemoteFlowFile(event.getFlowFileUuid(),sourceSystemFlowFileIdentifier);
       // flowfileIdsRequestingData.add(sourceSystemFlowFileIdentifier);
        assignParentsAndChildren(event);
        //queue up the events.  wait on processing we havent received the source feed flowfile data yet
        addWaitingOnRemoteFlowFile(event,eventId);
    }

    /**
     * Is the event part of a 'Remote Input Port'
     * @param event the event to check
     * @return true if its from a Remote Input Port, false if not
     */
    public boolean isRemoteInputPortEvent(ProvenanceEventRecord event) {
        return isRemoteInputPortEvent(event.getComponentType());
    }

    /**
     * Is the event part of a 'Remote Input Port'
     * @param event the event to check
     * @return true if its from a Remote Input Port, false if not
     */
    public boolean isRemoteInputPortEvent(String componentType) {
        return "Remote Input Port".equalsIgnoreCase(componentType);
    }

    /**
     * If this event is a 'Remote Input Port' event and a type of SEND, track that we have sent out the event flowfile data to another NiFi node
     * @param event the event
     * @param eventId the id of the event
     */
    public void checkAndAddRemoteInputPortSendEvent(ProvenanceEventRecord event, Long eventId){
        if(isRemoteInputPortEvent(event) && event.getEventType().name().equalsIgnoreCase("SEND")) {
            log.debug("KYLO-DEBUG: Register the 'Remote Input Port' SEND event.  eventId: {}, FlowFile: {}, componentId: {}, componentType: {}, Waiting on Remote Events: {} ",eventId,event.getFlowFileUuid(),event.getComponentId(), event.getComponentType());
            sourceRemoteInputPortEvents.add(new ProvenanceEventRecordWithId(eventId, event));
        }
    }

    /**
     * If we are waiting for more data we need to queue up those related events
     * @param event the event to process on this node
     * @param eventId the event id related to this event
     * @return true if we are waiting and have queued up the event, false if we can proceed and process this event
     */
    public boolean checkAndQueueRemoteEvent(ProvenanceEventRecord event, Long eventId){

        //TODO can this be replaced with a simple get flowFileIdToRemoteFlowFileId.containsKey(event.getFlowFileUuid());
        boolean isWaiting = isWaitingRemoteFlowFile(event.getFlowFileUuid());
        if(isWaiting) {
            lock.lock();
            try {
                String sourceSystemFlowFileIdentifier = flowFileIdToRemoteFlowFileId.get(event.getFlowFileUuid());
                addRemoteFlowFile(event.getFlowFileUuid(), sourceSystemFlowFileIdentifier);
                assignParentsAndChildren(event);
                addWaitingOnRemoteFlowFile(event, eventId);
            }
            finally {
                lock.unlock();
            }
        }
        return isWaiting;
    }




    /**
     * can this flowfile be removed from the remote mappings
     * Qualifications needed for removal.
     *  1. data needs to be collected for notification
     *  2. data needs to have been marked as Dropped
     * @param flowFileId the remote flowfile Id
     * @return true if it can be deleted, false if not
     */
    public boolean canRemoteEventDataBeDeleted(String flowFileId){
        if(remoteFilesAvailableToBeRemoved.containsKey(flowFileId)){
            remoteFilesAvailableToBeRemoved.remove(flowFileId);
            return true;
        }
        //if its not in the removal set, only allow it to be removed if its not present in the remoteSource Info
        return remoteSourceFlowFileInformation.getIfPresent(flowFileId) == null;
    }

    /**
     * if the event is a DROP and it is a 'Remote Input Port' event then it needs to update the tracking data to make sure this object can be removed from this nodes mapping
     * @param event the event being dropped
     * @param eventId the eventId
     */
    public void registerRemoteInputPortDropEvent(ProvenanceEventRecord event, Long eventId){
        addRemoteSourceFlowFile(event.getFlowFileUuid(),eventId);
    }

    /**
     * if the event is a DROP and it is a 'Remote Input Port' event then it needs to update the tracking data to make sure this object can be removed from this nodes mapping
     * @param eventFlowFileId the event flowfile id being dropped
     * @param eventId the eventId
     */
    public void registerRemoteInputPortDropEvent(String eventFlowFileId, Long eventId){
        addRemoteSourceFlowFile(eventFlowFileId,eventId);
    }

    /**
     * Take all those "Remote Input Port" SEND events and map them into a Remote object that will be used to notify other nodes about this data.
     * @return
     */
    public List<RemoteEventMessageResponse> processRemoteInputPortSendEvents() {
      //  log.debug(this.toString());
        Set<ProvenanceEventRecordWithId>events = new HashSet<>();
        sourceRemoteInputPortEvents.drainTo(events);
        List<RemoteEventMessageResponse> responseMessages = events.stream()
            .filter(event -> FeedEventStatistics.getInstance().getFeedFlowFileId(event.getEvent().getFlowFileUuid()) != null)
            .map(event -> {
                RemoteEventMessageResponse response = new RemoteEventMessageResponse();
                String feedFlowFile = FeedEventStatistics.getInstance().getFeedFlowFileId(event.getEvent().getFlowFileUuid());
                boolean trackingDetails = FeedEventStatistics.getInstance().isTrackingDetails(feedFlowFile);
                response.setTrackingDetails(trackingDetails);
                response.setSourceFlowFileId(event.getEvent().getFlowFileUuid());
                response.setFeedFlowFileId(feedFlowFile);
                response.setFeedFlowFileStartTime(FeedEventStatistics.getInstance().getFeedFlowStartTime(feedFlowFile));
                response.setFeedProcessorId(FeedEventStatistics.getInstance().getFeedProcessorId(feedFlowFile));
                addRemoteSourceFlowFile(event.getEvent().getFlowFileUuid(),null);
                log.debug("KYLO-DEBUG: About to send message to notify other nodes about the 'Remote Input Port' SEND events.  isTrackingDetails: {}, Source FF: {}, Feed FF: {}, Feed FF start time: {}, Feed Processor Id: {}",trackingDetails,response.getSourceFlowFileId(),response.getFeedFlowFileId(),response.getFeedFlowFileStartTime(),response.getFeedProcessorId());
                return response;
                //  response.setFeedFlowRunningCount(FeedEventStatistics.getInstance().getRunningFeedFlows());
            }).collect(Collectors.toList());

        //Notify this nodes maps that they can safely clear any of these remote items that have 'DROP' events related to them
        List<RemoteSourceFlowFile> removeList = new ArrayList<>();
        remoteFilesAvailableToBeRemoved.keySet().forEach(k -> {
            RemoteSourceFlowFile remoteSourceFlowFile = remoteFilesAvailableToBeRemoved.remove(k);
            if(remoteSourceFlowFile != null){
                removeList.add(remoteSourceFlowFile);
                log.debug("KYLO-DEBUG: Allow the following Remote source files to be removed and processed as completed. FlowFile: {}, isCollected: {}, Drop Event Id:{}",remoteSourceFlowFile.getFlowFileId(),remoteSourceFlowFile.isCollected(),remoteSourceFlowFile.getDropEventId());
            }
        });


        FeedEventStatistics.getInstance().checkAndClearRemoteEvents(removeList);

        return responseMessages;
    }


    /**
     * 1. load the remote feed flowfile data obtained from JMS into this nodes event mapping
     * 2. pull any waiting events related to this remote flow data and then add the Waiting Events to be processed downstream
     * @param remoteEventeMessages remote event information
     */
    public void loadRemoteEventFlowData(RemoteEventeMessageResponseHolder remoteEventeMessages){
        lock.lock();
        try {
            Set<String> sourceFlowFiles = remoteEventeMessages.getMessages().stream()
                .filter(message -> sourceFlowFileToFlowFileIds.getIfPresent(message.getSourceFlowFileId()) != null)
                .map(message -> {
                    //populate the stats maps for this data
                    Set<String> relatedFlowFiles =sourceFlowFileToFlowFileIds.asMap().remove(message.getSourceFlowFileId());
                    FeedEventStatistics.getInstance().load(new RemoteMessageResponseWithRelatedFlowFiles(message,relatedFlowFiles));
                    return message.getSourceFlowFileId();
                }).collect(Collectors.toSet());

            //TODO queue up all waitingProvenanceEventRecord.getEvent().getFlowFileUuid() and remove them all at once after they are added to the stats manager

            Set<String> waitingFlowFilesThatCanBeRemoved = new HashSet<>();
            sourceFlowFiles.stream().flatMap(sourceFlowFile -> removeAndGetWaitingEvents(sourceFlowFile).stream())
                .forEach(waitingProvenanceEventRecord -> {
                    FeedStatisticsManager.getInstance().addEvent(waitingProvenanceEventRecord.getEvent(),waitingProvenanceEventRecord.getEventId());
                    log.debug("Loaded the remote event data.  Adding waiting event and removing from waiting queue.  event id:{}, flow file id: {}, component id: {}, component type: {} ",waitingProvenanceEventRecord.getEventId(),waitingProvenanceEventRecord.getEvent().getFlowFileUuid(), waitingProvenanceEventRecord.getEvent().getComponentId(), waitingProvenanceEventRecord.getEvent().getComponentType());
                    waitingFlowFilesThatCanBeRemoved.add(waitingProvenanceEventRecord.getEvent().getFlowFileUuid());
                });


            if(!waitingFlowFilesThatCanBeRemoved.isEmpty()){
                log.debug("About to remove waiting flowfiles: {} ",waitingFlowFilesThatCanBeRemoved.stream().collect(Collectors.joining(",")));
            }
            waitingFlowFilesThatCanBeRemoved.stream().forEach(ff -> flowFileIdToRemoteFlowFileId.remove(ff));

            //TODO Need to  flowFileIdToRemoteFlowFileId for all the sourceFlowFiles ... is there a more efficient way?
            sourceFlowFiles.stream().forEach(s -> flowFileIdToRemoteFlowFileId.remove(s));


        }
        finally {
            lock.unlock();
        }
    }

    /**
     * relate the current flow file to a remote flow file
     * @param incomingFlowFileId the flowfile from the event in this NiFi instance
     * @param sourceFlowFileId the flowfile from the other remote machine
     */
    private void addRemoteFlowFile(String incomingFlowFileId, String sourceFlowFileId) {
        flowFileIdToRemoteFlowFileId.put(incomingFlowFileId, sourceFlowFileId);
        sourceFlowFileToFlowFileIds.getUnchecked(sourceFlowFileId).add(incomingFlowFileId);

    }

    /**
     * Events that cannont find the parent feed flow file and are remote events
     * will add themeselves to the queue of Waiting for the remote response data before processing
     *
     * @param event the event on this node
     * @param eventId the event id
     */
    private void addWaitingOnRemoteFlowFile(ProvenanceEventRecord event, Long eventId){
        String remoteSource = flowFileIdToRemoteFlowFileId.get(event.getFlowFileUuid());
        if(remoteSource != null) {
            log.debug("KYLO-DEBUG: Add waiting event for  flowFile: {}, eventId: {}",event.getFlowFileUuid(),eventId);
            waitingRemoteEvents.getUnchecked(remoteSource).add(new ProvenanceEventRecordWithId(eventId, event));
        }
    }



    /**
     * Add the remote event data and all its children back to the related remote 'sourceFlowFile'
     * @param event
     */
    private void assignParentsAndChildren(ProvenanceEventRecord event) {
        String sourceFlowFile = flowFileIdToRemoteFlowFileId.get(event.getFlowFileUuid());
        if (sourceFlowFile != null && event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                flowFileIdToRemoteFlowFileId.put(child, sourceFlowFile);
            }
        }
    }

    /**
     * Register the flowfile as a remoteSource flowfile and track if its been collected for notification to other nodes and if it has been dropped
     * Remote flowfile cannot be dropped from this nodes mapping until they have been collected and shipped off for notifications
     * @param flowfileId the remote (sourceFlowFileId)
     * @param dropEventId the event related to the DROP event Type for this flowfile
     * @return the Object in queue that is tracking drop and collection
     */
    private RemoteSourceFlowFile addRemoteSourceFlowFile(String flowfileId, Long dropEventId) {
        RemoteSourceFlowFile remoteSourceFlowFile =  remoteSourceFlowFileInformation.getUnchecked(flowfileId);
        log.debug("");
        if(dropEventId != null) {
            remoteSourceFlowFile.setDropEventId(dropEventId);
        }
        else {
            remoteSourceFlowFile.setCollected(true);
        }
        if(remoteSourceFlowFile.canDelete()){
            remoteSourceFlowFileInformation.asMap().remove(remoteSourceFlowFile.getFlowFileId());
            remoteFilesAvailableToBeRemoved.put(remoteSourceFlowFile.getFlowFileId(), remoteSourceFlowFile);
        }
        return remoteSourceFlowFile;
    }

    /**
     * pull all ProvenanceEvents from the Waiting queue and return them
     * @param sourceFlowFileId the initial remote source flowfile
     * @return a list of events related to the source flowfile
     */
    private Set<ProvenanceEventRecordWithId> removeAndGetWaitingEvents(String sourceFlowFileId){
        Set<ProvenanceEventRecordWithId> list = waitingRemoteEvents.asMap().remove(sourceFlowFileId);
        if(list == null){
            return Collections.emptySet();
        }
        else {
            return list;
        }
    }

    /**
     * The class to track if the Remote Input Port SEND event has been collected, and dropped to ensure that this node doesnt remove this event flow file from this nodes Statistics map
      */
    public static class RemoteSourceFlowFile {
        private String flowFileId;
        private boolean isCollected;
        private Long dropEventId;

        public RemoteSourceFlowFile(String flowFileId) {
            this.flowFileId = flowFileId;
        }

        public boolean isDropped() {
            return dropEventId != null;
        }

        public String getFlowFileId() {
            return flowFileId;
        }

        public boolean isCollected() {
            return isCollected;
        }

        public void setCollected(boolean collected) {
            isCollected = collected;
        }

        public boolean canDelete() {
            return isCollected && isDropped();
        }

        public Long getDropEventId() {
            return dropEventId;
        }

        public void setDropEventId(Long dropEventId) {
            this.dropEventId = dropEventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RemoteSourceFlowFile)) {
                return false;
            }

            RemoteSourceFlowFile that = (RemoteSourceFlowFile) o;

            if (flowFileId != null ? !flowFileId.equals(that.flowFileId) : that.flowFileId != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return flowFileId != null ? flowFileId.hashCode() : 0;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RemoteProvenanceEventService{");
        sb.append("flowFileIdToRemoteFlowFileId=").append(flowFileIdToRemoteFlowFileId.size());
        sb.append(", sourceFlowFileToFlowFileIds=").append(sourceFlowFileToFlowFileIds.size());
        sb.append(", sourceRemoteInputPortEvents=").append(sourceRemoteInputPortEvents.size());
        sb.append(", waitingRemoteEvents=").append(waitingRemoteEvents.size());
        sb.append('}');
        return sb.toString();
    }
}
