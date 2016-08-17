package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/17/16.
 */
public class AggregatedFeedProcessorStatisticsHolder {

    DateTime minTime;
    DateTime maxTime;
    Integer collectionInterval;
    String collectionId;

    public AggregatedFeedProcessorStatisticsHolder() {

    }

    List<AggregatedFeedProcessorStatistics> statistics;

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

    public List<AggregatedFeedProcessorStatistics> getStatistics() {
        if (statistics == null) {
            statistics = new ArrayList<>();
        }
        return statistics;
    }

    public void setStatistics(List<AggregatedFeedProcessorStatistics> statistics) {
        this.statistics = statistics;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AggregatedFeedProcessorStatisticsHolder{");
        sb.append("minTime=").append(minTime);
        sb.append(", maxTime=").append(maxTime);
        sb.append(", collectionInterval=").append(collectionInterval);
        sb.append(", collectionId='").append(collectionId).append('\'');
        sb.append(", statistics=").append(getStatistics().size());
        sb.append('}');
        return sb.toString();
    }
}
