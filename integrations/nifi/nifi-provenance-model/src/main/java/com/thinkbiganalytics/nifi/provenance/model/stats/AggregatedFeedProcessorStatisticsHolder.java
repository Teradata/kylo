package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/17/16.
 */
public class AggregatedFeedProcessorStatisticsHolder implements Serializable {

    DateTime minTime;
    DateTime maxTime;
    String collectionId;
    AtomicLong eventCount = new AtomicLong(0L);

    private Long minEventId = 0L;
    private Long maxEventId = 0L;

    public AggregatedFeedProcessorStatisticsHolder() {

        this.collectionId = UUID.randomUUID().toString();
    }

    Map<String, AggregatedFeedProcessorStatistics> feedStatistics = new ConcurrentHashMap<>();

    /**
     * Add an event to generate statistics
     */
    public void addStat(ProvenanceEventRecordDTO event) {
        if (minTime == null || event.getEventTime().isBefore(minTime)) {
            minTime = event.getEventTime();
        }
        if (maxTime == null || event.getEventTime().isAfter(maxTime)) {
            maxTime = event.getEventTime();
        }
        feedStatistics.computeIfAbsent(event.getFeedName(), (feedName) -> new AggregatedFeedProcessorStatistics(feedName, collectionId)).addEventStats(
            event);

        if (event.getEventId() < minEventId) {
            minEventId = event.getEventId();
        }
        if (event.getEventId() > maxEventId) {
            maxEventId = event.getEventId();
        }

        eventCount.incrementAndGet();
    }



    public AtomicLong getEventCount() {
        return eventCount;
    }

    public Long getMinEventId() {
        return minEventId;
    }

    public Long getMaxEventId() {
        return maxEventId;
    }

    public Map<String, AggregatedFeedProcessorStatistics> getFeedStatistics() {
        return feedStatistics;
    }

    public void clear() {
        this.collectionId = UUID.randomUUID().toString();

        feedStatistics.entrySet().forEach(e -> e.getValue().clear(collectionId));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AggregatedFeedProcessorStatisticsHolder{");
        sb.append("minTime=").append(minTime);
        sb.append(", maxTime=").append(maxTime);
        sb.append(", collectionId='").append(collectionId).append('\'');
        sb.append(", eventCount=").append(eventCount.get());
        sb.append('}');
        return sb.toString();
    }
}
