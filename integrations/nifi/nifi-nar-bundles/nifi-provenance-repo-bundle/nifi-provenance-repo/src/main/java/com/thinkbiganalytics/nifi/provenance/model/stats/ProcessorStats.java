package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.StatisticsUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/16/16. Object that holds onto the processor and list of events associated with the processor
 */
public class ProcessorStats {

    private String processorId;
    private List<ProvenanceEventStats> eventStats;


    public ProcessorStats(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public List<ProvenanceEventStats> getEventStats() {
        if (eventStats == null) {
            eventStats = new ArrayList<>();
        }
        return eventStats;
    }

    public void addProvenanceEventStats(ProvenanceEventStats stats) {
        getEventStats().add(stats);
    }

    public AggregatedProcessorStatistics getStats(String collectionId,DateTime start, DateTime end) {
        return new AggregatedProcessorStatistics(processorId, StatisticsUtil.aggregateStats(eventStats, collectionId,start, end));
    }
}
