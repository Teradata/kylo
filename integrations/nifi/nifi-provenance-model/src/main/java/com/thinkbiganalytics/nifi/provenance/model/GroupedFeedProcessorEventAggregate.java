package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Determines a Stream by looking at the last {n} events and determines if the time between each of the events is within a given streaming threshold.
 *
 * If the event is detected as being a stream but the originating flow file event ( the one that started the job) was declared a Batch event then it will also process it
 *
 * Created by sr186054 on 8/25/16.
 */
public class GroupedFeedProcessorEventAggregate implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(GroupedFeedProcessorEventAggregate.class);

    private String feedName;
    private String processorId;
    private String processorName;
    private DateTime lastEventTime;
    private DateTime lastSystemTime;


    private List<ProvenanceEventRecordDTO> potentialStreamEvents = new ArrayList<>();
    private List<ProvenanceEventRecordDTO> jmsEvents = new ArrayList<>();
    private List<ProvenanceEventRecordDTO> streamEvents = new ArrayList<>();


    private ProvenanceEventRecordDTO lastEvent;
    private AggregatedProcessorStatistics stats;

    private Long allowedMillisBetweenEvents;

    private AtomicInteger eventCount = new AtomicInteger(0);
    private AtomicInteger batchCount = new AtomicInteger(0);
    private AtomicInteger streamingCount = new AtomicInteger(0);

    private AtomicInteger tempStreamingCount = new AtomicInteger(0);

    private List<ProvenanceEventRecordDTO> failedEvents;
    private DateTime initTime;

    private Integer numberOfEventsThatMakeAStream;

    private final ReentrantLock lock = new ReentrantLock(true);

    public GroupedFeedProcessorEventAggregate(String feedName, String processorId, Long allowedMillisBetweenEvents, Integer numberOfEventsThatMakeAStream) {

        this.feedName = feedName;
        this.processorId = processorId;
        this.failedEvents = new LinkedList<>();
        this.allowedMillisBetweenEvents = allowedMillisBetweenEvents;
        this.numberOfEventsThatMakeAStream = numberOfEventsThatMakeAStream;
        this.stats = new AggregatedProcessorStatistics(processorId, feedName);
        this.initTime = DateTime.now();
        this.lastSystemTime = DateTime.now();
        log.info("new FeedProcessorEventAggregate for " + feedName + "," + processorId + " - " + this.initTime);
    }


    public GroupedFeedProcessorEventAggregate addEvent(ProvenanceEventRecordDTO event) {
        groupEventAsStreamOrBatch(event);
        lastSystemTime = DateTime.now();
        lastEventTime = event.getEventTime();
        lastEvent = event;
        return this;
    }


    private void movePotentialStreamToBatch() {
        markFirstEventsAsBatch(potentialStreamEvents);
        jmsEvents.addAll(potentialStreamEvents);
        batchCount.addAndGet(potentialStreamEvents.size());
        potentialStreamEvents.clear();
    }

    private void movePotentialStreamToStream() {
        streamEvents.addAll(potentialStreamEvents);
        streamingCount.addAndGet(potentialStreamEvents.size());
        potentialStreamEvents.clear();
    }

    private void groupEventAsStreamOrBatch(ProvenanceEventRecordDTO event) {
        lock.lock();
        try {
            if (lastEventTime == null) {
                lastEventTime = event.getEventTime();
            }
            if (ProvenanceEventUtil.isCompletionEvent(event)) {

                //if this event time is before the allotted time between events check for streaming case
                eventCount.incrementAndGet();
                if (event.getEventTime().isBefore(lastEventTime.plus(allowedMillisBetweenEvents))) {
                    if (tempStreamingCount.incrementAndGet() >= numberOfEventsThatMakeAStream) {
                        //  log.info("Detected a Stream for Feed/Processsor {}/{}.  {} event ", feedName, processorId, event);
                        //move potential stream events to stream
                        movePotentialStreamToStream();
                        //do anythign with streaming???
                        streamEvents.add(event);

                    } else {
                        potentialStreamEvents.add(event);
                    }
                } else {
                    potentialStreamEvents.add(event);
                    /// no longer a stream event
                    movePotentialStreamToBatch();
                    tempStreamingCount.set(0);
                }
            } else {
                log.info("Non completion event {} ", event);
            }
        } finally {
            lock.unlock();
        }
    }

    private List<ProvenanceEventRecordDTO> addStreamingEventsWhoseFirstEventWasABatchToQueue() {
        List<ProvenanceEventRecordDTO> events = streamEvents.stream().filter(e -> e.getFlowFile().getRootFlowFile().isFirstEventBatch()).collect(Collectors.toList());
        if (events != null && !events.isEmpty()) {
            jmsEvents.addAll(events);
        }
        log.info("Adding {} events since they originated from a BATCH event ", events.size());
        return events;
    }

    private void markFirstEventsAsBatch(List<ProvenanceEventRecordDTO> events) {
        events.stream().filter(e -> e.isStartOfJob()).map(e -> e.getFlowFile().getRootFlowFile()).forEach(ff -> ff.setIsFirstEventBatch(true));
    }

    private void printList(List<ProvenanceEventRecordDTO> list, String title) {
        log.info("Print {} ", title);
        for (ProvenanceEventRecordDTO e : list) {
            log.info("Event {} - {} ", e.getEventId(), e.getEventTime());
        }
    }

    public List<ProvenanceEventRecordDTO> collectEventsToBeSentToJmsQueue() {
        lock.lock();
        List<ProvenanceEventRecordDTO> events = null;
        try {
            //Move anything that is potential to batch if the time between is greater
            if (DateTime.now().isAfter(lastEventTime.plus(allowedMillisBetweenEvents))) {
                movePotentialStreamToBatch();
            }
            //if the First Event was a Batch event we should pass this event through so it gets reconciled in the Ops Manager
            addStreamingEventsWhoseFirstEventWasABatchToQueue();
            //copy and clear
            events = new ArrayList<>(jmsEvents);
            jmsEvents.clear();
            //add in any Streaming events to the copied events array
            streamEvents.clear();
        } finally {
            lock.unlock();
        }

        return events == null ? new ArrayList<>() : events;

    }


    public void add(ProvenanceEventStats stats, ProvenanceEventRecordDTO event) {
        addEvent(event);
        addEventStats(stats);
    }

    public GroupedFeedProcessorEventAggregate addEventStats(ProvenanceEventStats stats) {
        if (stats != null) {
            this.stats.add(stats);
        }
        return this;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public DateTime getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(DateTime lastEventTime) {
        this.lastEventTime = lastEventTime;
    }


    public AggregatedProcessorStatistics getStats() {
        return stats;
    }

    public void setStats(AggregatedProcessorStatistics stats) {
        this.stats = stats;
    }


    public String getSummary() {
        return "";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeedProcessorEventAggregate{");
        sb.append("feedName='").append(feedName).append('\'');
        sb.append(", processorId='").append(processorId).append('\'');
        sb.append(", summary='").append(getSummary()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
