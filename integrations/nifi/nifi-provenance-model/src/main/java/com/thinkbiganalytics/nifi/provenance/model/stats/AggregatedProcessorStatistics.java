package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Group Statistics by Processor
 *
 */
public class AggregatedProcessorStatistics implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(AggregatedProcessorStatistics.class);

    String processorId;
    String processorName;
    GroupedStats stats;



    public AggregatedProcessorStatistics(String processorId, String processorName, String collectionId) {
        this.processorId = processorId;
        this.processorName = processorName;
        this.stats = new GroupedStats();
        this.stats.setGroupKey(collectionId);

    }


    public void add(ProvenanceEventRecordDTO event) {
        this.stats.add(event);
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


    public void clear() {
        this.stats.clear();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AggregatedProcessorStatistics{");
        sb.append("processorId='").append(processorId).append('\'');
        sb.append(", processorName='").append(processorName).append('\'');
        sb.append(", stats=").append(stats);
        sb.append('}');
        return sb.toString();
    }
}
