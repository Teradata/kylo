package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Group Stats by Feed and Processor
 */
public class AggregatedFeedProcessorStatistics implements Serializable {

    String feedName;
    String processGroup;
    private String collectionId;
    private Long totalEvents = 0L;
    private Long minEventId = 0L;
    private Long maxEventId = 0L;

    Map<String, AggregatedProcessorStatistics> processorStats = new ConcurrentHashMap<>();

    public AggregatedFeedProcessorStatistics() {
    }

    public AggregatedFeedProcessorStatistics(String feedName, String collectionId) {
        this.feedName = feedName;
        this.collectionId = collectionId;
    }

    /**
     * Add the event to compute statistics
     */
    public void addEventStats(ProvenanceEventRecordDTO event) {
        processorStats.computeIfAbsent(event.getComponentId(), processorId -> new AggregatedProcessorStatistics(processorId, event.getComponentName(), collectionId)).add(event);
        totalEvents++;
        if (event.getEventId() < minEventId) {
            minEventId = event.getEventId();
        }
        if (event.getEventId() > maxEventId) {
            maxEventId = event.getEventId();
        }
        if (StringUtils.isBlank(processGroup) && StringUtils.isNotBlank(event.getFeedProcessGroupId())) {
            processGroup = event.getFeedProcessGroupId();
        }
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

    public Long getMaxEventId() {
        return maxEventId;
    }

    public Map<String, AggregatedProcessorStatistics> getProcessorStats() {
        return processorStats;
    }


    public void clear(String newCollectionId) {
        this.collectionId = newCollectionId;
        processorStats.entrySet().forEach(e -> e.getValue().clear());
    }
}
