package com.thinkbiganalytics.nifi.provenance.model.stats;

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
    Integer collectionInterval;
    String collectionId;
    AtomicLong eventCount = new AtomicLong(0L);

    public AggregatedFeedProcessorStatisticsHolder(Integer collectionInterval) {
        this.collectionId = UUID.randomUUID().toString();
        this.collectionInterval = collectionInterval;
    }

    Map<String, AggregatedFeedProcessorStatistics> feedStatistics = new ConcurrentHashMap<>();

    public void addStat(ProvenanceEventStats stats) {
        if (minTime == null || stats.getTime().isBefore(minTime)) {
            minTime = stats.getTime();
        }
        if (maxTime == null || stats.getTime().isAfter(maxTime)) {
            maxTime = stats.getTime();
        }
        feedStatistics.computeIfAbsent(stats.getFeedName(), (feedName) -> new AggregatedFeedProcessorStatistics(feedName, collectionId)).addEventStats(
            stats);

        eventCount.incrementAndGet();
    }


    public DateTime getMinTime() {
        return minTime;
    }

    public void setMinTime(DateTime minTime) {
        this.minTime = minTime;
    }

    public DateTime getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(DateTime maxTime) {
        this.maxTime = maxTime;
    }

    public Integer getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(Integer collectionInterval) {
        this.collectionInterval = collectionInterval;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public AtomicLong getEventCount() {
        return eventCount;
    }

    public Map<String, AggregatedFeedProcessorStatistics> getFeedStatistics() {
        return feedStatistics;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AggregatedFeedProcessorStatisticsHolder{");
        sb.append("minTime=").append(minTime);
        sb.append(", maxTime=").append(maxTime);
        sb.append(", collectionInterval=").append(collectionInterval);
        sb.append(", collectionId='").append(collectionId).append('\'');
        sb.append(", eventCount=").append(eventCount.get());
        sb.append('}');
        return sb.toString();
    }
}
