package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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


import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileCacheListener;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventExtendedAttributes;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.nifi.provenance.ProvenanceEventType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provenance events for Feeds not marked as "streaming" will be processed by this class in the KyloReportingTask of NiFi
 */
public class BatchFeedProcessorEventsV2 implements  Serializable {

    private static final Logger log = LoggerFactory.getLogger(BatchFeedProcessorEventsV2.class);

    /**
     * Map of Time -> FeedFlowFileId, # events per sec received
     */
    Map<DateTime, Map<String, AtomicInteger>> jobEventsBySecond = new HashMap<>();


    /**
     * Time and a Set of those starting flow file ids that start a flow
     */
    Map<DateTime, Set<String>> startingBatchJobFlowFileIdsPerSecond = new HashMap<>();

    Map<DateTime, String> primaryStartingBatchJobFlowFileIdsPerSecond = new HashMap<>();

    AtomicInteger suppressedEventCount = new AtomicInteger(0);

    Set<String> suppressedStartingFlowFiles = new HashSet<>();

    Set<String> uniqueFlowFileCount = new HashSet<>();

    /**
     * Pointer to the first event that came through that is a starting job event
     */
    ProvenanceEventRecordDTO firstStartingJobEvent = null;
    /**
     * The name of the feed.  Derived from the process group {category}.{feed}
     */
    private String feedFlowFileId;

    /**
     * the last DateTime stored in the jobEventsBySecond
     */
    private DateTime lastEventTimeBySecond;

    /**
     * The Processor Id
     */
    private String processorId;
    /**
     * The time for the Last Event that has been processed
     */
    private DateTime lastEventTime;

    /**
     * Collection of events that will be sent to jms
     */
    private Set<ProvenanceEventRecordDTO> jmsEvents = new LinkedHashSet<>();

    private Set<String> uniqueBatchEvents = new HashSet<>();
    /**
     * The max number of starting job events for the given feed and processor allowed to pass through per second This parameter is passed in via the constructor
     */
    private Integer maxEventsPerSecond = 10;


    /**
     * Time when this group first got created
     */
    private DateTime initTime;



    public BatchFeedProcessorEventsV2(String feedFlowFileId, String processorId, Integer maxEventsPerSecond) {

        this.feedFlowFileId = feedFlowFileId;
        this.processorId = processorId;
        this.initTime = DateTime.now();
        this.maxEventsPerSecond = maxEventsPerSecond;
        log.debug("new BatchFeedProcessorEvents for " + feedFlowFileId + "," + processorId + " - " + this.initTime);
    }


    /**
     * Add the event to be processed
     *
     * @param event the event to add to the batch
     * @return true if added, false if not
     */
    public boolean add(ProvenanceEventRecordDTO event) {
       return addEvent(event);
    }

    /**
     * Unique key describing the event for ops manager
     *
     * @param event the provenance event
     * @return the unique key
     */
    private String batchEventKey(ProvenanceEventRecordDTO event) {
        return feedFlowFileId + "-" + processorId + "-" + event.getJobFlowFileId() + event.isStartOfJob() + event.isEndOfJob();
    }

    private DateTime eventTimeToNearestSecond(ProvenanceEventRecordDTO event){
        return event.getEventTime().withMillisOfSecond(0);
    }

    private void trackJoins(ProvenanceEventRecordDTO event){
        if(ProvenanceEventType.JOIN.name().equalsIgnoreCase(event.getEventType())){
            //joining parents -> producing Children
            //when child comes through, remove the parents

        }
    }

    /**
     * Check to see if we are getting events too fast to be considered a batch.  If so suppress the events so just a few go through and the rest generate statistics.
     *
     * @param event the event to check
     * @return true if suppressing the event (not adding it to the batch of events), false if it will be added
     */
    private boolean isSuppressEvent(ProvenanceEventRecordDTO event) {
            String jobEventsBySecondKey = event.getJobFlowFileId();
            //if we ware the starting event of the job change the key to be the processorId
            if (event.isStartOfJob()) {
                jobEventsBySecondKey = processorId;
            }
            uniqueFlowFileCount.add(event.getFlowFileUuid());



            if(event.getFeedFlowFile().isStream()){
                jobEventsBySecondKey = event.getFeedFlowFile().getFirstEventProcessorId();
            }

            DateTime time = eventTimeToNearestSecond(event);
            lastEventTimeBySecond = time;
            jobEventsBySecond.computeIfAbsent(time, key -> new HashMap<String, AtomicInteger>()).computeIfAbsent(jobEventsBySecondKey, flowFileId -> new AtomicInteger(0)).incrementAndGet();
            //suppress if its not the ending event for a batch flow
            if (event.isFinalJobEvent() && !event.getFeedFlowFile().isStream()) {
                return false;
            }
            else if (jobEventsBySecond.get(time).get(jobEventsBySecondKey).get() > maxEventsPerSecond) {
                if (event.isStartOfJob()) {
                    event.getFeedFlowFile().setStream(true);
                    suppressedStartingFlowFiles.add(event.getFeedFlowFile().getId());
                    event.getFeedFlowFile().setRelatedBatchFeedFlows(startingBatchJobFlowFileIdsPerSecond.get(time));
                    event.getFeedFlowFile().setPrimaryRelatedBatchFeedFlow( primaryStartingBatchJobFlowFileIdsPerSecond.get(time));
                }
                event.setStream(true);
                suppressedEventCount.incrementAndGet();
                return true;
            }
            else {
                if(event.isStartOfJob()){
                    if(firstStartingJobEvent == null){
                        firstStartingJobEvent = event;
                    }
                    startingBatchJobFlowFileIdsPerSecond.computeIfAbsent(time,key -> new HashSet<String>()).add(event.getFeedFlowFile().getId());
                    //mark the first one coming in for this time based batch as the primary flowfile
                    if(!primaryStartingBatchJobFlowFileIdsPerSecond.containsKey(time)){
                        primaryStartingBatchJobFlowFileIdsPerSecond.put(time,event.getFeedFlowFile().getId());
                    }
                }

            }

        return false;
    }





    /**
     * Add an event from Nifi to be processed
     *
     * @param event the event to add for batch processing
     * @return returns true if successfully added, false if not.  It may return false if the event is suppressed
     * @see this#isSuppressEvent(ProvenanceEventRecordDTO)
     */
    public boolean addEvent(ProvenanceEventRecordDTO event) {

        event.getFeedFlowFile().getFeedFlowFileJobTrackingStats().trackExtendedAttributes(event);

        if (!isSuppressEvent(event)) {
            if (lastEventTime == null) {
                lastEventTime = event.getEventTime();
            }
            event.setIsBatchJob(true);

            String batchKey = batchEventKey(event);
            if (!uniqueBatchEvents.contains(batchKey)) {
                event.getFeedFlowFile().getFeedFlowFileJobTrackingStats().markExtendedAttributesAsSent(event);
                //reassign the flowfile to a batch one
                if(event.getFeedFlowFile().hasRelatedBatchFlows()) {
                    String ffId = event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow();
                    if(ffId != null) {
                        event.setStreamingBatchFeedFlowFileId(ffId);
                    }
                }
                uniqueBatchEvents.add(batchKey);
                jmsEvents.add(event);
            }
            lastEventTime = event.getEventTime();
            return true;
        }
        return false;
    }


    private void reset(){

        uniqueBatchEvents.clear();
        uniqueFlowFileCount.clear();
        suppressedStartingFlowFiles.clear();
        suppressedEventCount.set(0);
        firstStartingJobEvent = null;

        if (lastEventTimeBySecond != null) {
            jobEventsBySecond.entrySet().removeIf(entry -> entry.getKey().isBefore(lastEventTimeBySecond));
            startingBatchJobFlowFileIdsPerSecond.entrySet().removeIf(entry -> entry.getKey().isBefore(lastEventTimeBySecond));
            primaryStartingBatchJobFlowFileIdsPerSecond.entrySet().removeIf(entry -> new DateTime(entry.getKey()).isBefore(lastEventTimeBySecond));
        }


    }


    /**
     * for all the events that have been processed, send them off to the JMS queue
     *
     * @return the list of events that have been sent
     */
    public List<ProvenanceEventRecordDTO> collectEventsToBeSentToJmsQueue() {
        List<ProvenanceEventRecordDTO> events = null;
        try {
            events = new ArrayList<>(jmsEvents);
            jmsEvents.clear();
        } finally {

        }
        if(uniqueFlowFileCount.size() >0) {
            events.stream().forEach(e -> {
                if(uniqueFlowFileCount.size() >0) {
                    e.setUpdatedAttribute("flow files processed", uniqueFlowFileCount.size() + "");
                }
            });
        }



        reset();
        return events == null ? Collections.emptyList() : events;
    }


    /**
     * Gets the processorId
     *
     * @return the processor Id
     */
    public String getProcessorId() {
        return processorId;
    }

    /**
     * Sets the processor id
     *
     * @param processorId the processor id
     */
    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }


    /**
     * @param maxEventsPerSecond the max number of events allowed per sec to be considered a batch job
     * @return this class
     */
    public BatchFeedProcessorEventsV2 setMaxEventsPerSecond(Integer maxEventsPerSecond) {
        this.maxEventsPerSecond = maxEventsPerSecond;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BatchFeedProcessorEventAggregate{");
        sb.append("feedFlowFile='").append(feedFlowFileId).append('\'');
        sb.append(", processorId='").append(processorId).append('\'');
        sb.append('}');
        return sb.toString();
    }



}
