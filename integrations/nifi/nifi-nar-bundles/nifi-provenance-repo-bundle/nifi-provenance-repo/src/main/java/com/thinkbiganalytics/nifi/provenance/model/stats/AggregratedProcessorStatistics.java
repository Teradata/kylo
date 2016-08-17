package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 8/16/16. A Aggregrated stats
 */
public class AggregratedProcessorStatistics {

    String processorId;
    GroupedStats stats;
    private Long totalEvents;

    public AggregratedProcessorStatistics(GroupedStats stats) {
        this.stats = stats;
    }

    public AggregratedProcessorStatistics(String processorId, GroupedStats stats) {
        this.processorId = processorId;
        this.stats = stats;
        this.totalEvents = stats.getTotalCount();
    }

    public DateTime getIntervalTime() {
        return stats.getGroupKey();
    }


    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public GroupedStats getStats() {
        return stats;
    }

    public void setStats(GroupedStats stats) {
        this.stats = stats;
    }

    public DateTime getMinTime() {
        return stats.getMinTime();
    }

    public DateTime getMaxTime() {
        return stats.getMinTime();
    }

    public Long getTotalEvents() {
        return totalEvents;
    }
}
