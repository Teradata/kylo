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
public class FeedEventStatistics {


    Map<String,String> feedFlowFileIdToFeedProcessorId = new ConcurrentHashMap<>();

    ////Track feedFlowFile relationships to parent/child flow files for lifetime feed job execution
    Set<String> detailedTrackingFeedFlowFileId = new HashSet<>();

    /**
     * Map of all the flow files as they pertain to the starting feed flow file
     * Used to expire EventStatistics
     */
    Map<String, String> allFlowFileToFeedFlowFile = new ConcurrentHashMap<>();


    Map<String, Set<String>> flowFileToParents = new ConcurrentHashMap<>();

    Map<String, Set<String>> flowFileToChildren = new ConcurrentHashMap<>();

    Set<String> activeFlowFiles = new HashSet<>();



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


    Map<String,Set<String>> feedFlowToRelatedFlowFiles = new ConcurrentHashMap<>();



    /// Skipped detail tracking and failure counts

    /**
     * Events not capturing details
     */
    AtomicLong skippedEvents = new AtomicLong(0);


   /**
     * Count of how many failures have been detected for a feedFlowFile execution
     */
    Map<String,AtomicInteger> feedFlowFileFailureCount = new ConcurrentHashMap<>();

    //Map<Long,Long> eventIdEventTime = new ConcurrentHashMap<>();



    private static final FeedEventStatistics instance = new FeedEventStatistics();

    private FeedEventStatistics() {

    }

    public static FeedEventStatistics getInstance(){
        return instance;
    }



    public void checkAndAssignStartingFlowFile(ProvenanceEventRecord event){
        if(ProvenanceEventUtil.isStartingFeedFlow(event)) {
            //startingFlowFiles.add(event.getFlowFileUuid());
            allFlowFileToFeedFlowFile.put(event.getFlowFileUuid(),event.getFlowFileUuid());
            //add the flow to active processing
            feedFlowProcessing.computeIfAbsent(event.getFlowFileUuid(), feedFlowFileId -> new AtomicInteger(0)).incrementAndGet();
            feedFlowFileIdToFeedProcessorId.put(event.getFlowFileUuid(),event.getComponentId());

          //  feedFlowToRelatedFlowFiles.computeIfAbsent(event.getFlowFileUuid(), feedFlowFileId -> new HashSet<>()).add(event.getFlowFileUuid());
        }

    }

    /**
     * attach the event that has parents/children to a tracking feedflowfile (if possible)
     * @param event
     * @return
     */
    private String determineParentFeedFlow(ProvenanceEventRecord event) {
        String feedFlowFile = null;
        String parent = event.getParentUuids().stream().filter(parentFlowFileId -> isTrackingDetails(parentFlowFileId)).findFirst().orElse(null);
        if(parent == null) {
            parent = event.getParentUuids().get(0);
        }
        if(parent != null){
            feedFlowFile = getFeedFlowFileId(parent);
        }

        return feedFlowFile;
    }


    public boolean assignParentsAndChildren(ProvenanceEventRecord event){

        //Assign the Event to one of the Parents

      //  activeFlowFiles.add(event.getFlowFileUuid());
        String startingFlowFile = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        boolean trackingEventFlowFile = false;
        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {

            if(startingFlowFile == null){
                startingFlowFile = determineParentFeedFlow(event);
                if(startingFlowFile != null) {
                    allFlowFileToFeedFlowFile.put(event.getFlowFileUuid(), startingFlowFile);
                    if(feedFlowProcessing.containsKey(startingFlowFile)) {
                        feedFlowProcessing.get(startingFlowFile).incrementAndGet();
                        trackingEventFlowFile = true;
                    }
                }
            }

        }
        if (startingFlowFile != null && event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
            //    activeFlowFiles.add(child);
                allFlowFileToFeedFlowFile.put(child, startingFlowFile);
              //  flowFileToParents.computeIfAbsent(child, flowFile -> new HashSet<>()).add(event.getFlowFileUuid());
                //not sure if needed
             //   feedFlowToRelatedFlowFiles.computeIfAbsent(startingFlowFile, feedFlowFileId -> new HashSet<>()).add(child);

                //Add children flow files to active processing
                //skip this add if we already did it while iterating the parents.
                //NiFi will create a new Flow File for this event (event.getFlowFileId) and it will also be part of the children
                if(feedFlowProcessing.containsKey(startingFlowFile) && (!trackingEventFlowFile || (trackingEventFlowFile && !child.equalsIgnoreCase(event.getFlowFileUuid())))){
                    feedFlowProcessing.get(startingFlowFile).incrementAndGet();
                }
            }
        }

        return startingFlowFile != null;

    }




    public void calculateTimes(ProvenanceEventRecord event,Long eventId){
      //  eventIdEventTime.put(eventId,event.getEventTime());
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

      //  if(!processorStartTime.containsKey(event.getComponentId())){
      //      processorStartTime.put(event.getComponentId(),startTime);
      //  }

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
        String feedFlowFile = allFlowFileToFeedFlowFile.get(eventFlowFileId);
        if(feedFlowFile != null){
            return detailedTrackingFeedFlowFileId.contains(feedFlowFile);
        }
        return false;
    }

    public void setTrackingDetails(ProvenanceEventRecord event){
            detailedTrackingFeedFlowFileId.add(event.getFlowFileUuid());
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

    public String getFeedFlowFileId(ProvenanceEventRecord event){
        return allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
    }

    public String getFeedFlowFileId(String eventFlowFileId){
        return allFlowFileToFeedFlowFile.get(eventFlowFileId);
    }


    public String getFeedProcessorId(ProvenanceEventRecord event){
        String feedFlowFileId = getFeedFlowFileId(event);
        return feedFlowFileId != null ? feedFlowFileIdToFeedProcessorId.get(feedFlowFileId) : null;
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



    public void checkAndClear(String eventFlowFileId, String eventType, Long eventId, boolean removeFeedFlowData){
        if(ProvenanceEventType.DROP.name().equals(eventType) && removeFeedFlowData) {
                if(isEndingFeedFlow(eventId)) {
                    String feedFlowFile = getFeedFlowFileId(eventFlowFileId);
                    if(feedFlowFile != null) {
                        feedFlowFileFailureCount.remove(feedFlowFile);
                        feedFlowFileEndTime.remove(feedFlowFile);
                        feedFlowFileStartTime.remove(feedFlowFile);


                                //feedFlowProcessing.remove(feedFlowFile);
                                feedFlowFileIdToFeedProcessorId.remove(feedFlowFile);
                                detailedTrackingFeedFlowFileId.remove(feedFlowFile);

                        }
                    }

                flowFileLastNonDropEventTime.remove(eventFlowFileId);
                eventsThatCompleteFeedFlow.remove(eventId);
               // detailedFlowFileToFeedFlowFile.remove(eventFlowFileId);
                allFlowFileToFeedFlowFile.remove(eventFlowFileId);


            }
            if(ProvenanceEventType.DROP.name().equals(eventType)){
                flowFileLastNonDropEventTime.remove(eventFlowFileId);
            }
        eventDuration.remove(eventId);
    }


    public boolean beforeProcessingIsLastEventForTrackedFeed(ProvenanceEventRecord event, Long eventId){
        String feedFlowFileId = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        if(isTrackingDetails(event.getFlowFileUuid()) && feedFlowFileId != null && ProvenanceEventType.DROP.equals(event.getEventType())) {
            //get the feed flow fileId for this event
            AtomicInteger activeCounts = feedFlowProcessing.get(feedFlowFileId);
            if(activeCounts != null){
               return activeCounts.get() == 1;
            }
        }
        return false;
    }

    public void finishedEvent(ProvenanceEventRecord event, Long eventId){

        String feedFlowFileId = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        if(feedFlowFileId != null && ProvenanceEventType.DROP.equals(event.getEventType())) {
            //get the feed flow fileId for this event
            AtomicInteger activeCounts = feedFlowProcessing.get(feedFlowFileId);
            if(activeCounts != null){
                feedFlowProcessing.get(feedFlowFileId).decrementAndGet();
                if(activeCounts.get() == 0) {
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

    public void cleanup(ProvenanceEventRecord event, Long eventId){
        boolean isTrackDetails = isTrackingDetails(event.getFlowFileUuid());
        checkAndClear(event.getFlowFileUuid(),event.getEventType().name(),eventId,!isTrackDetails);
    }

    public void cleanup(ProvenanceEventRecordDTO event){
       if(event != null && event.isFinalJobEvent()) {
           checkAndClear(event.getFlowFileUuid(), event.getEventType(), event.getEventId(), true);
       }
    }


}
