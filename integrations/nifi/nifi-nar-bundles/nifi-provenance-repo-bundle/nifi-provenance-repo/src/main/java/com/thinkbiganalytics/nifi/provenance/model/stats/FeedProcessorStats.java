package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 8/16/16.
 */
public class FeedProcessorStats {

    private String feedName;
    private Map<String, ProcessorStats> processorStats;


    public FeedProcessorStats(String feedName) {
        this.feedName = feedName;
        this.processorStats = new HashMap<>();
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public void addEventStats(ProvenanceEventStats stats) {
        String processorId = stats.getProcessorId();
        if (!processorStats.containsKey(processorId)) {
            processorStats.put(processorId, new ProcessorStats(processorId));
        }
        processorStats.get(processorId).addProvenanceEventStats(stats);

    }

    public Map<String, ProcessorStats> getProcessorStats() {
        return processorStats;
    }

    public AggregratedFeedProcessorStatistics getStats(DateTime start, DateTime end) {
        AggregratedFeedProcessorStatistics feedStatistics = new AggregratedFeedProcessorStatistics(feedName);
        feedStatistics.setMinTime(start);
        feedStatistics.setMaxTime(end);
        for (Map.Entry<String, ProcessorStats> entry : processorStats.entrySet()) {
            AggregratedProcessorStatistics statistics = entry.getValue().getStats(start, end);
            feedStatistics.getProcessorStats().put(statistics.getProcessorId(), statistics);
        }
        feedStatistics.calculateTotalEvents();
        return feedStatistics;
    }


}
