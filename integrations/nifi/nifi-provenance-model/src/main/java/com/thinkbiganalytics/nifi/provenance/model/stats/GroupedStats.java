package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/16/16.
 */
public class GroupedStats extends BaseStatistics {

    private String groupKey;
    private DateTime minTime;
    private DateTime maxTime;

    private List<ProvenanceEventStats> eventStatsList;

    public GroupedStats() {
    }

    public GroupedStats(String groupKey, List<ProvenanceEventStats> eventStats) {
        this.groupKey = groupKey;
        this.eventStatsList = new ArrayList<>(eventStats);

        this.totalCount = new Long(eventStats.size());

        eventStatsList.stream().forEach(stats -> {
            this.bytesIn += stats.getBytesIn();
            this.bytesOut += stats.getBytesOut();
            this.duration += stats.getDuration();
            this.processorsFailed += stats.getProcessorsFailed();
            this.flowFilesStarted += stats.getFlowFilesStarted();
            this.flowFilesFinished += stats.getFlowFilesFinished();
            this.jobsStarted += stats.getJobsStarted();
            this.jobsFinished += stats.getJobsFinished();
            if (this.time == null) {
                this.time = stats.getTime();
            }
            if (this.minTime == null) {
                this.minTime = stats.getTime();
            }

            if (this.maxTime == null) {
                this.maxTime = stats.getTime();
            }
            this.maxTime = (stats.getTime()).isAfter(this.maxTime) ? stats.getTime() : this.maxTime;
            this.minTime = (stats.getTime()).isBefore(this.minTime) ? stats.getTime() : this.minTime;
            this.time = this.minTime;
        });
        //reassign as collection time
        this.time = DateTime.now();
    }

    public DateTime getMinTime() {
        return minTime;
    }

    public DateTime getMaxTime() {
        return maxTime;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public List<ProvenanceEventStats> getEventStatsList() {
        return eventStatsList;
    }
}
