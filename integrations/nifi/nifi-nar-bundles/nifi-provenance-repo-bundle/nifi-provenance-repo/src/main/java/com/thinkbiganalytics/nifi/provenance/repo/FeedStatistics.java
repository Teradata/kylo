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

import com.google.common.collect.EvictingQueue;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStatsV2;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds Statistics about a Feed and Processor updated during Nifi execution
 */
public class FeedStatistics {

    private static final Logger log = LoggerFactory.getLogger(FeedStatistics.class);

    private static int limit = ConfigurationProperties.DEFAULT_MAX_EVENTS;

    /**
     * The originating processor id that started this entire execution.  This will mark the feed identity
     */
    private String feedProcessorId;

    /**
     * The Processor ID
     */
    private String processorId;


    /**
     * Records to send off to JMS
     */
    private Map<String, ProvenanceEventRecordDTO> lastRecords = new ConcurrentHashMap<>(limit);


    /**
     * SourceQueueIdentifier to Grouped Stats
     * Stats are grouped by their SourceQueueId so Kylo can detect if it came off a "failure" path
     */
    private Map<String, GroupedStats> stats;

    /**
     * Flag to indicate we are throttling the start Job events that get sent to ops manager
     */
    private AtomicBoolean isThrottled = new AtomicBoolean(false);

    /**
     * The
     */
    private Integer throttleStartingFeedFlowsThreshold = 15;

    /**
     * Rolling queue of the last {throttleStartingFeedFlowsThreshold} items based upon time
     */
    Queue<Long> startingFeedFlowQueue = null;


    /**
     * Time to before the throttle key will rest
     * Rapid events need to be slow for this amount of time before resetting the key
     */
    private Integer throttleStartingFeedFlowsTimePeriod = 1000;


    private String batchKey(ProvenanceEventRecord event, String feedFlowFileId, boolean isStartingFeedFlow) {
        String key = event.getComponentId() + ":" + event.getEventType().name();

        if (isStartingFeedFlow) {
            if(startingFeedFlowQueue == null){
                startingFeedFlowQueue =EvictingQueue.create(throttleStartingFeedFlowsThreshold);
            }

            startingFeedFlowQueue.add(event.getEventTime());
            if(startingFeedFlowQueue.size() >= throttleStartingFeedFlowsThreshold) {
                Long diff = event.getEventTime() - startingFeedFlowQueue.peek();
                if (diff < throttleStartingFeedFlowsTimePeriod) {
                    //we got more than x events within the threshold... throttle
                    key += eventTimeNearestSecond(event);
                    if(isThrottled.compareAndSet(false,true)) {
                        log.info("Detected over {} flows/sec starting within the given window, throttling back starting events ", throttleStartingFeedFlowsThreshold);
                    }
                } else {
                    key +=":" + feedFlowFileId;
                    startingFeedFlowQueue.clear();
                    if(isThrottled.compareAndSet(true,false)) {
                        log.info("Resetting throttle flow rate is slower than threshold {} flows/se for flows starting within the given window.", throttleStartingFeedFlowsThreshold);
                    }
                }

            }
            else {
                key = key + ":" + feedFlowFileId;
            }
        }
        else {
            key +=":" + feedFlowFileId;
        }


        return key;


    }


    private Long eventTimeNearestSecond(ProvenanceEventRecord event) {
        return new DateTime(event.getEventTime()).withMillisOfSecond(0).getMillis();
    }

    private Long nowToNearestSecond(ProvenanceEventRecord event) {
        return DateTime.now().withMillis(0).getMillis();
    }



    public FeedStatistics(String feedProcessorId, String processorId) {
        this.feedProcessorId = feedProcessorId;
        this.processorId = processorId;
        stats = new ConcurrentHashMap<>();
        this.limit = ConfigurationProperties.getInstance().getFeedProcessorMaxEvents();
        this.throttleStartingFeedFlowsThreshold = ConfigurationProperties.getInstance().getThrottleStartingFeedFlowsThreshold();
        this.throttleStartingFeedFlowsTimePeriod = ConfigurationProperties.getInstance().getDefaultThrottleStartingFeedFlowsTimePeriodMillis();
    }

    public GroupedStats getStats(ProvenanceEventRecord event) {
        String key = event.getSourceQueueIdentifier();
        if (key == null) {
            key = GroupedStats.DEFAULT_SOURCE_CONNECTION_ID;
        }
        return stats.computeIfAbsent(key, sourceConnectionIdentifier -> new GroupedStatsV2(sourceConnectionIdentifier));
    }


    public void addEvent(ProvenanceEventRecord event, Long eventId) {

        FeedEventStatistics.getInstance().calculateTimes(event, eventId);

        ProvenanceEventRecordDTO eventRecordDTO = null;

        String feedFlowFileId = FeedEventStatistics.getInstance().getFeedFlowFileId(event);

        boolean isStartingFeedFlow = ProvenanceEventUtil.isStartingFeedFlow(event);
        String batchKey = batchKey(event, feedFlowFileId, isStartingFeedFlow);

        //always track drop events if its on a tracked feed
        boolean isDropEvent = ProvenanceEventUtil.isEndingFlowFileEvent(event);
        if (isDropEvent && FeedEventStatistics.getInstance().beforeProcessingIsLastEventForTrackedFeed(event, eventId)) {
            batchKey += UUID.randomUUID().toString();
        }

        if (((!isStartingFeedFlow && FeedEventStatistics.getInstance().isTrackingDetails(event.getFlowFileUuid())) || (isStartingFeedFlow && lastRecords.size() <= limit)) && !lastRecords
            .containsKey(batchKey)) {
            // if we are tracking details send the event off for jms
            if (isStartingFeedFlow) {
                FeedEventStatistics.getInstance().setTrackingDetails(event);
            }

            eventRecordDTO = ProvenanceEventRecordConverter.convert(event);
            eventRecordDTO.setEventId(eventId);
            eventRecordDTO.setIsStartOfJob(ProvenanceEventUtil.isStartingFeedFlow(event));

            eventRecordDTO.setJobFlowFileId(feedFlowFileId);
            eventRecordDTO.setFirstEventProcessorId(feedProcessorId);
            eventRecordDTO.setStartTime(FeedEventStatistics.getInstance().getEventStartTime(eventId));
            eventRecordDTO.setEventDuration(FeedEventStatistics.getInstance().getEventDuration(eventId));

            if (ProvenanceEventUtil.isFlowFileQueueEmptied(event)) {
                // a Drop event component id will be the connection, not the processor id. we will set the name of the component
                eventRecordDTO.setComponentName("FlowFile Queue emptied");
                eventRecordDTO.setIsFailure(true);
            }

            if (ProvenanceEventUtil.isTerminatedByFailureRelationship(event)) {
                eventRecordDTO.setIsFailure(true);
            }

            lastRecords.put(batchKey, eventRecordDTO);

        } else {
            FeedEventStatistics.getInstance().skip(event, eventId);
        }
        FeedEventStatistics.getInstance().finishedEvent(event, eventId);

        boolean isEndingEvent = FeedEventStatistics.getInstance().isEndingFeedFlow(eventId);
        if (eventRecordDTO != null && isEndingEvent) {
            eventRecordDTO.setIsFinalJobEvent(isEndingEvent);
        }
        FeedProcessorStatisticsAggregator.getInstance().add(getStats(event), event, eventId);

        FeedEventStatistics.getInstance().cleanup(event, eventId);


    }

    public boolean hasStats() {
        return getStats().stream().anyMatch(s -> s.getTotalCount() > 0);
    }

    public String getFeedProcessorId() {
        return feedProcessorId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public Collection<ProvenanceEventRecordDTO> getEventsToSend() {
        return lastRecords.values();
    }

    //  public AggregatedProcessorStatistics getFeedProcessorStatistics(){
    //      return feedProcessorStatistics;
    //  }

    public Collection<GroupedStats> getStats() {
        return stats.values();
    }

    public void clear() {
        lastRecords.clear();
        stats.clear();
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}
