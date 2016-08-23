package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.StatisticsUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds onto the processor and list of events associated with the processor
 *
 * Created by sr186054 on 8/16/16.
 */
public class ProcessorStats {

    private String processorId;
    private List<ProvenanceEventStats> eventStats;

    //partition it by eventTime for easy removal


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
