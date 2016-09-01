package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sr186054 on 8/25/16.
 */
public class FeedProcessorEventAggregate implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(FeedProcessorEventAggregate.class);

    private String feedName;
    private String processorId;
    private String processorName;
    private DateTime lastEventTime;
    private DateTime lastSystemTime;
    private List<ProvenanceEventRecordDTO> events;
    private List<ProvenanceEventRecordDTO> streamEvents;
    private ProvenanceEventRecordDTO lastEvent;
    private AggregatedProcessorStatistics stats;
    private boolean stream;
    private Long delayMillis;
    private AtomicInteger eventCount = new AtomicInteger(0);
    private AtomicInteger streamingCount = new AtomicInteger(0);

    private AtomicInteger possibleStreamCount = new AtomicInteger(0);

    private List<ProvenanceEventRecordDTO> failedEvents;
    private DateTime initTime;

    private Integer numberOfEventsThatMakeAStream;

    public FeedProcessorEventAggregate(String feedName, String processorId, Long delayMillis, Integer numberOfEventsThatMakeAStream) {

        this.feedName = feedName;
        this.processorId = processorId;
        this.events = new LinkedList<>();
        this.failedEvents = new LinkedList<>();
        this.delayMillis = delayMillis;
        this.numberOfEventsThatMakeAStream = numberOfEventsThatMakeAStream;
        this.stats = new AggregatedProcessorStatistics(processorId, feedName);
        this.initTime = DateTime.now();
        this.lastSystemTime = DateTime.now();
        log.info("new FeedProcessorEventAggregate for " + feedName + "," + processorId + " - " + this.initTime);
    }


    public FeedProcessorEventAggregate addEvent(ProvenanceEventRecordDTO event) {
        //   log.info("aggregate Event {} ",event);

        checkStreaming(event);
        lastSystemTime = DateTime.now();
        lastEventTime = event.getEventTime();
        lastEvent = event;

        //if is failure add it
        //failedEvents
        return this;

    }

    private boolean checkStreaming(ProvenanceEventRecordDTO event) {
        if (lastEventTime == null) {
            lastEventTime = event.getEventTime();
        }

        if (ProvenanceEventUtil.isCompletionEvent(event)) {
            //if this event time is before the allotted time between events check for streaming case
            eventCount.incrementAndGet();
            if (event.getEventTime().isBefore(lastEventTime.plus(delayMillis)) && streamingCount.incrementAndGet() >= numberOfEventsThatMakeAStream) {

                if (!stream) {
                    this.stream = true;
                    this.events.clear();
                    //if its a stream mark the root flowfile as such
                    //event.getFlowFile().getRootFlowFile()
                    log.info("Detected a Stream for Feed/Processsor {}/{}.  {} events in the last {} ms ", feedName, processorId, eventCount.get(),
                             (DateTime.now().getMillis() - this.initTime.getMillis()));
                }
            } else {
                /// no longer a stream event
                if (!stream) {
                    events.add(event);
                }

            }
        } else {
            log.info("Non completion event {} ", event);
        }
        return this.stream;
    }

    public void add(ProvenanceEventStats stats, ProvenanceEventRecordDTO event) {
        addEvent(event);
        addEventStats(stats);
    }

    public FeedProcessorEventAggregate addEventStats(ProvenanceEventStats stats) {
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

    public List<ProvenanceEventRecordDTO> getEvents() {
        return events;
    }

    public void setEvents(List<ProvenanceEventRecordDTO> events) {
        this.events = events;
    }

    public AggregatedProcessorStatistics getStats() {
        return stats;
    }

    public void setStats(AggregatedProcessorStatistics stats) {
        this.stats = stats;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public String getSummary() {
        return "Processed " + eventCount.get() + " events within " + (lastSystemTime.getMillis() - this.initTime.getMillis()) + " ms  of which " + (isStream() ? streamingCount.get() : 0)
               + " were streaming and " + (events.size()) + " were batch";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeedProcessorEventAggregate{");
        sb.append("feedName='").append(feedName).append('\'');
        sb.append(", processorId='").append(processorId).append('\'');
        sb.append(", stream=").append(stream);
        sb.append(", summary='").append(getSummary()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
