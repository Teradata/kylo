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

/**
 * This will detect a stream by just looking at the last event and determining if the lastEvent and current event is within a given timeframe. If so the two will be declared a Stream otherwise it will
 * be a batch
 *
 * Created by sr186054 on 8/25/16.
 */
public class SimpleFeedProcessorEventAggregate implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(SimpleFeedProcessorEventAggregate.class);

    private String feedName;
    private String processorId;
    private String processorName;
    private DateTime lastEventTime;
    private DateTime lastSystemTime;


    private ProvenanceEventRecordDTO lastEvent;
    private AggregatedProcessorStatistics stats;
    private Long allowedMillisBetweenEvents;

    private AtomicInteger eventCount = new AtomicInteger(0);
    private AtomicInteger streamingCount = new AtomicInteger(0);
    private AtomicInteger batchCount = new AtomicInteger(0);

    private List<ProvenanceEventRecordDTO> jmsEvents = new ArrayList<>();
    private List<ProvenanceEventRecordDTO> failedEvents;
    private DateTime initTime;

    public SimpleFeedProcessorEventAggregate(String feedName, String processorId, Long allowedMillisBetweenEvents, Integer numberOfEventsThatMakeAStream) {

        this.feedName = feedName;
        this.processorId = processorId;
        this.failedEvents = new LinkedList<>();
        this.allowedMillisBetweenEvents = allowedMillisBetweenEvents;
        this.stats = new AggregatedProcessorStatistics(processorId, feedName);
        this.initTime = DateTime.now();
        this.lastSystemTime = DateTime.now();
        log.info("new FeedProcessorEventAggregate for " + feedName + "," + processorId + " - " + this.initTime);
    }


    public SimpleFeedProcessorEventAggregate addEvent(ProvenanceEventRecordDTO event) {
        //   log.info("aggregate Event {} ",event);

        groupEventAsStreamOrBatch(event);
        lastSystemTime = DateTime.now();
        lastEventTime = event.getEventTime();
        lastEvent = event;
        //if is failure add it
        //failedEvents
        return this;

    }


    private void groupEventAsStreamOrBatch(ProvenanceEventRecordDTO event) {
        //   lock.lock();
        try {
            if (lastEventTime == null) {
                lastEventTime = event.getEventTime();
            }
            if (ProvenanceEventUtil.isCompletionEvent(event)) {
                //if this event time is before the allotted time between events check for streaming case
                eventCount.incrementAndGet();
                if (event.getEventTime().isBefore(lastEventTime.plus(allowedMillisBetweenEvents))) {
                    //STREAM
                    if (lastEvent != null && lastEvent.getProcessed().compareAndSet(false, true)) {
                        streamingCount.incrementAndGet();
                    }
                    if (event.getProcessed().compareAndSet(false, true)) {
                        streamingCount.incrementAndGet();
                    }
                } else {
                    ///BATCH... only add the last event if it has not been processed yet
                    if (lastEvent != null && lastEvent.getProcessed().compareAndSet(false, true)) {
                        jmsEvents.add(lastEvent);
                        batchCount.incrementAndGet();
                    }
                    if (event.getProcessed().compareAndSet(false, true)) {
                        jmsEvents.add(event);
                        batchCount.incrementAndGet();
                    }
                }
            } else {
                //        log.info("Non completion event {} ", event);
            }
        } finally {
            //        lock.unlock();
        }
    }

    public List<ProvenanceEventRecordDTO> collectEventsToBeSentToJmsQueue() {
        //  lock.lock();
        List<ProvenanceEventRecordDTO> events = null;
        try {
            //Move anything that is potential to batch if the time between is greater
            if (DateTime.now().isAfter(lastEventTime.plus(allowedMillisBetweenEvents))) {
                //BATCH
                if (lastEvent != null && lastEvent.getProcessed().compareAndSet(false, true)) {
                    jmsEvents.add(lastEvent);
                    batchCount.incrementAndGet();
                }
            }
            events = new ArrayList<>(jmsEvents);
            jmsEvents.clear();

        } finally {
            //        lock.unlock();
        }
        return events == null ? new ArrayList<>() : events;

    }

    public void add(ProvenanceEventStats stats, ProvenanceEventRecordDTO event) {
        addEvent(event);
        addEventStats(stats);
    }

    public SimpleFeedProcessorEventAggregate addEventStats(ProvenanceEventStats stats) {
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
