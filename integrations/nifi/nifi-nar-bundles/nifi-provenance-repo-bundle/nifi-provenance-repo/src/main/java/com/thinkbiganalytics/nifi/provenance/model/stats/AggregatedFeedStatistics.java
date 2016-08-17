package com.thinkbiganalytics.nifi.provenance.model.stats;

/**
 * Created by sr186054 on 8/16/16.
 */
public class AggregatedFeedStatistics {

    String feedName;
    String processGroup;

    GroupedStats stats;

    public AggregatedFeedStatistics() {
    }

    public AggregatedFeedStatistics(String feedName, GroupedStats stats) {
        this.feedName = feedName;
        this.stats = stats;
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


}
