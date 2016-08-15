package com.thinkbiganalytics.nifi.provenance.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/15/16.
 */
public class ProvenanceEventFeedProcessorStats {

    private String feed;
    private Set<String> feedProcessGroupIds;
    private Map<String, ProcessorStats> processorStats;

    public ProvenanceEventFeedProcessorStats(String feedName) {
        this.feed = feedName;
        this.processorStats = new ConcurrentHashMap<>();
        this.feedProcessGroupIds = new HashSet<>();
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }


    public Map<String, ProcessorStats> getProcessorStats() {
        return processorStats;
    }

    public void setProcessorStats(Map<String, ProcessorStats> processorStats) {
        this.processorStats = processorStats;
    }

    public void add(String feedProcessGroup, ProvenanceEventRecordDTO event) {
        if (feedProcessGroup != null) {
            feedProcessGroupIds.add(feedProcessGroup);
        }
        String processorId = event.getComponentId();
        if (!processorStats.containsKey(processorId)) {

            processorStats.put(processorId, new ProcessorStats(processorId));
        }

        ProcessorStats stats = processorStats.get(processorId);
        stats.addEvent(event);
    }

    public Double getAverageDuration() {
        return processorStats.values().stream().collect(Collectors.averagingDouble(ProcessorStats::getAvgProcessTime));
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventFeedProcessorStats{");
        sb.append("feed='").append(feed).append('\'');
        sb.append("avgDuration=").append(getAverageDuration()).append(" ms");
        sb.append('}');
        return sb.toString();
    }
}
