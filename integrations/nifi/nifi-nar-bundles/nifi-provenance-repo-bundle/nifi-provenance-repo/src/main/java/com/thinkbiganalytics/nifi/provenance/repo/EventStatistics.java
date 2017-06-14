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
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 6/11/17.
 */
public class EventStatistics {




    ////Track feedFlowFile relationships to parent/child flow files for lifetime feed job execution

    /**
     * Map of the flow files to the Starting Feed flow file that will be processed (falls within the thresholds)
     */
    Map<String, String> detailedFlowFileToFeedFlowFile = new ConcurrentHashMap<>();


    /**
     * Map of all the flow files as they pertain to the starting feed flow file
     * Used to expire EventStatistics
     */
    Map<String, String> allFlowFileToFeedFlowFile = new ConcurrentHashMap<>();


    ///Track Timing Information for each event


    /**
     * Map of the FlowFile Id to Event Time that is not a drop event
     */
    private Map<String,Long> flowFileLastNonDropEventTime = new ConcurrentHashMap<>();

    /**
     * Map of the processor (componentId) to the starting time for that processor
     */
    private Map<String,Long> processorStartTime = new ConcurrentHashMap<>();

    /**
     * Map of the EventId to the duration in millis
     */
    private Map<Long,Long> eventDuration = new ConcurrentHashMap<>();

    /**
     * feed flowFile Id to startTime
     */
    Map<String,Long> feedFlowFileStartTime = new ConcurrentHashMap<>();

    /**
     * feedFlowFile Id to end time
     */
    Map<String,Long> feedFlowFileEndTime = new ConcurrentHashMap<>();



    //Feed Execution tracking

    /**
     * Set of Event Ids that are events that finish the feed flow execution.  Last Job Event Ids
     */
    Set<Long> eventsThatCompleteFeedFlow = new HashSet<>();

    /**
     * Count of how many flow files are still processing for a given feedFlowFile execution
     */
    Map<String,AtomicInteger> feedFlowProcessing = new ConcurrentHashMap<>();



    /// Skipped detail tracking and failure counts

    /**
     * Events not capturing details
     */
    AtomicLong skippedEvents = new AtomicLong(0);


   /**
     * Count of how many failures have been detected for a feedFlowFile execution
     */
    Map<String,AtomicInteger> feedFlowFileFailureCount = new ConcurrentHashMap<>();

    Map<Long,Long> eventIdEventTime = new ConcurrentHashMap<>();



    private static final EventStatistics instance = new EventStatistics();

    private EventStatistics() {

    }

    public static EventStatistics getInstance(){
        return instance;
    }



    public void checkAndAssignStartingFlowFile(ProvenanceEventRecord event, boolean isCaptureDetails){
        if(ProvenanceEventUtil.isStartingFeedFlow(event)) {
            allFlowFileToFeedFlowFile.put(event.getFlowFileUuid(), event.getFlowFileUuid());
            if (isCaptureDetails) {
                detailedFlowFileToFeedFlowFile.put(event.getFlowFileUuid(), event.getFlowFileUuid());
            }
            //add the flow to active processing
            feedFlowProcessing.computeIfAbsent(event.getFlowFileUuid(), feedFlowFileId -> new AtomicInteger(0)).incrementAndGet();
        }

    }


    public boolean isProcessPreCheck(ProvenanceEventRecord event){

        String startingFlowFile = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        String startingFlowToProcess = detailedFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {
            for (String parent : event.getParentUuids()) {
                startingFlowFile = allFlowFileToFeedFlowFile.get(parent);
                if(startingFlowFile != null) {
                    allFlowFileToFeedFlowFile.put(event.getFlowFileUuid(), startingFlowFile);
                }
                startingFlowToProcess = detailedFlowFileToFeedFlowFile.get(parent);
                if(startingFlowToProcess != null) {
                    detailedFlowFileToFeedFlowFile.put(event.getFlowFileUuid(), startingFlowToProcess);
                }
            }

        }
        if (startingFlowFile != null && event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                allFlowFileToFeedFlowFile.put(child, startingFlowFile);
                if(startingFlowToProcess != null){
                    detailedFlowFileToFeedFlowFile.put(child, startingFlowToProcess);
                }
                //Add children flow files to active processing
                if(feedFlowProcessing.containsKey(event.getFlowFileUuid())){
                    feedFlowProcessing.get(event.getFlowFileUuid()).incrementAndGet();
                }
            }
        }

        return startingFlowFile != null;

    }

    public void postCheck(ProvenanceEventRecord event, Long eventId){
        String feedFlowFileId = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        if(feedFlowFileId != null &&ProvenanceEventType.DROP.equals(event.getEventType())) {
            //get the feed flow fileId for this event


            if(feedFlowProcessing.containsKey(feedFlowFileId)){
               Integer remainingFlows = feedFlowProcessing.get(feedFlowFileId).decrementAndGet();
               if(remainingFlows == 0){
                   //Feed is finished
                   eventsThatCompleteFeedFlow.add(eventId);
                   feedFlowFileEndTime.put(feedFlowFileId, event.getEventTime());
               }
            }
        }

        if(feedFlowFileId != null && ProvenanceEventUtil.isTerminatedByFailureRelationship(event)){
            //add to failureMap
            feedFlowFileFailureCount.computeIfAbsent(feedFlowFileId, flowFileId -> new AtomicInteger(0)).incrementAndGet();
        }

    }

    public void attachTimingInfo(ProvenanceEventRecordDTO eventRecordDTO) {
        Long startTime = getProcessorStartTime(eventRecordDTO.getComponentId());
            if(startTime != null) {
                eventRecordDTO.setStartTime(startTime);
            }
        Long eventDuration = getEventDuration(eventRecordDTO.getEventId());
            if(eventDuration != null){
                eventRecordDTO.setEventDuration(eventDuration);
            }
    }


    public void calculateTimes(ProvenanceEventRecord event,Long eventId){
        eventIdEventTime.put(eventId,event.getEventTime());
        Long startTime = null;
        if(hasParents(event)){
            startTime = lastEventTimeForParent(event.getParentUuids());
        }
        else {
            startTime = lastEventTimeForFlowFile(event.getFlowFileUuid());
        }
        if(startTime == null) {
            startTime = event.getFlowFileEntryDate();
        }
        if(ProvenanceEventUtil.isStartingFeedFlow(event)){
            feedFlowFileStartTime.put(event.getFlowFileUuid(), startTime);
        }

        Long duration = event.getEventTime() - startTime;
        eventDuration.put(eventId,duration);

        if(!processorStartTime.containsKey(event.getComponentId())){
            processorStartTime.put(event.getComponentId(),startTime);
        }

        if(!ProvenanceEventType.DROP.equals(event.getEventType())) {
            flowFileLastNonDropEventTime.put(event.getFlowFileUuid(), event.getEventTime());
        }

    }

    public void skip(ProvenanceEventRecord event, Long eventId){
        skippedEvents.incrementAndGet();
    }


    private Long lastEventTimeForFlowFile(String flowFile){
        return flowFileLastNonDropEventTime.get(flowFile);
    }


    private Long lastEventTimeForParent(Collection<String> parentIds){
        return parentIds.stream().filter(flowFileId -> flowFileLastNonDropEventTime.containsKey(flowFileId)).findFirst().map(flowFileId -> flowFileLastNonDropEventTime.get(flowFileId)).orElse(null);
    }



    public boolean isEndingFeedFlow(Long eventId){
        return eventsThatCompleteFeedFlow.contains(eventId);
    }


    /**
     * are we tracking details for this feed
     * @param eventFlowFileId
     * @return
     */
    public boolean isTrackingDetails(String eventFlowFileId){
        return detailedFlowFileToFeedFlowFile.containsKey(eventFlowFileId);
    }

    private boolean hasParents(ProvenanceEventRecord event){
        return event.getParentUuids() != null && !event.getParentUuids().isEmpty();
    }

    public Long getProcessorStartTime(String processorId){
        return processorStartTime.get(processorId);
    }

    public Long getEventDuration(Long eventId){
        return eventDuration.get(eventId);
    }



    public boolean hasFailures(ProvenanceEventRecord event){
        String feedFlowFile = getFeedFlowFileId(event);
        if(feedFlowFile != null) {
            return feedFlowFileFailureCount.getOrDefault(feedFlowFile, new AtomicInteger(0)).get() >0;
        }
        return false;
    }

    public Long getSkippedEvents(){
        return skippedEvents.get();
    }

    public Integer getFlowFileToStartingFlowCacheSize(){
        return detailedFlowFileToFeedFlowFile.size();
    }

    public String getFeedFlowFileId(ProvenanceEventRecord event){
        return allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
    }

    public String getFeedFlowFileId(String eventFlowFileId){
        return allFlowFileToFeedFlowFile.get(eventFlowFileId);
    }

    public Long getFeedFlowStartTime(ProvenanceEventRecord event){
        String feedFlowFile = getFeedFlowFileId(event);
        if(feedFlowFile != null) {
            return feedFlowFileStartTime.getOrDefault(feedFlowFile, null);
        }
        return null;
    }

    public Long getFeedFlowEndTime(ProvenanceEventRecord event){
        String feedFlowFile = getFeedFlowFileId(event);
        if(feedFlowFile != null) {
            return feedFlowFileEndTime.getOrDefault(feedFlowFile, null);
        }
        return null;
    }

    public Long getFeedFlowFileDuration(ProvenanceEventRecord event){
        Long start = getFeedFlowStartTime(event);
        Long end = getFeedFlowEndTime(event);
        if(start != null && end != null){
            return end - start;
        }
        return null;
    }



    public void checkAndClear(String eventFlowFileId, String eventType, Long eventId, boolean removeDuration){
        if(ProvenanceEventType.DROP.name().equals(eventType) && removeDuration) {
                if(isEndingFeedFlow(eventId)) {
                    String feedFlowFile = getFeedFlowFileId(eventFlowFileId);
                    if(feedFlowFile != null) {
                        feedFlowFileFailureCount.remove(feedFlowFile);
                        feedFlowFileEndTime.remove(feedFlowFile);
                        feedFlowFileStartTime.remove(feedFlowFile);

                        if(feedFlowProcessing.getOrDefault(feedFlowFile, new AtomicInteger(0)).get() == 0){
                            feedFlowProcessing.remove(feedFlowFile);
                        }
                    }
                }
                flowFileLastNonDropEventTime.remove(eventFlowFileId);
                eventsThatCompleteFeedFlow.remove(eventId);
                detailedFlowFileToFeedFlowFile.remove(eventFlowFileId);
                allFlowFileToFeedFlowFile.remove(eventFlowFileId);


            }
            if (removeDuration){
                eventDuration.remove(eventId);
            }

    }


    public void completedEvent(ProvenanceEventRecord event, Long eventId){
boolean isTrackDetails = isTrackingDetails(event.getFlowFileUuid());
checkAndClear(event.getFlowFileUuid(),event.getEventType().name(),eventId,!isTrackDetails);
    }

    public void completedEvent(ProvenanceEventRecordDTO event){
        checkAndClear(event.getFlowFileUuid(),event.getEventType(),event.getEventId(),true);
    }

}
