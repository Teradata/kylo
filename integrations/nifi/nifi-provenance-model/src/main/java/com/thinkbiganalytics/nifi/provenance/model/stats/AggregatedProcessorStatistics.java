package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by sr186054 on 8/16/16. A Aggregrated stats
 */
public class AggregatedProcessorStatistics implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(AggregatedProcessorStatistics.class);

    String processorId;
    String processorName;
    GroupedStats stats;
    private Long totalEvents;

    public AggregatedProcessorStatistics(){
        this.stats = new GroupedStats();
    }

    public AggregatedProcessorStatistics(String processorId, String processorName) {
        this.processorId = processorId;
        this.processorName = processorName;
        this.stats = new GroupedStats();
    }

    public AggregatedProcessorStatistics(String processorId, String processorName, String collectionId) {
        this.processorId = processorId;
        this.processorName = processorName;
        this.stats = new GroupedStats();
        this.stats.setGroupKey(collectionId);

    }

    public AggregatedProcessorStatistics(GroupedStats stats) {
        this.stats = stats;
    }

    public AggregatedProcessorStatistics(String processorId, String processorName, GroupedStats stats) {
        this.processorId = processorId;
        this.processorName = processorName;
        this.stats = stats;
        this.totalEvents = stats.getTotalCount();
    }

    public void add(ProvenanceEventStats stats) {
        this.stats.add(stats);
    }

    public String getCollectionId() {
        return stats.getGroupKey();
    }


    public String getProcessorId() {
        return processorId;
    }

    public GroupedStats getStats() {
        return stats;
    }

    public void setStats(GroupedStats stats) {
        this.stats = stats;
    }

    public String getProcessorName() {
        return processorName;
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
