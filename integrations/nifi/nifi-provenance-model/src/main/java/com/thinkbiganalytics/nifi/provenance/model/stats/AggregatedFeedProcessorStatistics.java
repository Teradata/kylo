package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 8/16/16.
 */
public class AggregatedFeedProcessorStatistics {

    String feedName;
    String processGroup;
    DateTime minTime;
    DateTime maxTime;
    private Long totalEvents = 0L;

    Map<String, AggregatedProcessorStatistics> processorStats;

    public AggregatedFeedProcessorStatistics() {
    }

    public AggregatedFeedProcessorStatistics(String feedName) {
        this.feedName = feedName;
    }


    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getProcessGroup() {
        return processGroup;
    }

    public void setProcessGroup(String processGroup) {
        this.processGroup = processGroup;
    }

    public Map<String, AggregatedProcessorStatistics> getProcessorStats() {
        if (processorStats == null) {
            processorStats = new HashMap<>();
        }
        return processorStats;
    }

    public void setProcessorStats(Map<String, AggregatedProcessorStatistics> processorStats) {
        this.processorStats = processorStats;
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

    public void calculateTotalEvents() {
        Long total = 0L;
        for (AggregatedProcessorStatistics statistics : getProcessorStats().values()) {
            total += statistics.getStats().getTotalCount();
        }
        this.totalEvents = total;
    }

}
