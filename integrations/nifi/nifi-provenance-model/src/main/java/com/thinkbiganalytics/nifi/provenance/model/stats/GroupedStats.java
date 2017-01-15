package com.thinkbiganalytics.nifi.provenance.model.stats;


import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/16/16.
 */
public class GroupedStats extends BaseStatistics implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(GroupedStats.class);
    private String groupKey;
    private DateTime minTime;
    private DateTime maxTime;

    //  @JsonIgnore
    /// private  transient List<ProvenanceEventStats> eventStatsList;

    public GroupedStats() {
        //      this.eventStatsList = new ArrayList<>();
    }

    public void add(ProvenanceEventStats stats) {
        this.bytesIn += stats.getBytesIn();
        this.bytesOut += stats.getBytesOut();
        this.duration += stats.getDuration();
        this.processorsFailed += stats.getProcessorsFailed();
        this.flowFilesStarted += stats.getFlowFilesStarted();
        this.flowFilesFinished += stats.getFlowFilesFinished();
        this.jobsStarted += stats.getJobsStarted();
        this.jobsFinished += stats.getJobsFinished();
        this.jobsFailed += stats.getJobsFailed();
        this.jobDuration += stats.getJobDuration();
        this.successfulJobDuration += stats.getSuccessfulJobDuration();

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
        if (this.maxEventId < stats.getEventId()) {
            this.maxEventId = stats.getEventId();
        }

        if (StringUtils.isBlank(this.clusterNodeAddress)) {
            this.clusterNodeAddress = stats.getClusterNodeAddress();
        }

        if (StringUtils.isBlank(this.clusterNodeId)) {
            this.clusterNodeId = stats.getClusterNodeId();
        }

        // this.eventStatsList.add(stats);
        this.totalCount++;
    }


    public GroupedStats(String groupKey, List<ProvenanceEventStats> eventStats) {
        this.groupKey = groupKey;
        if (eventStats != null && !eventStats.isEmpty()) {
            List<ProvenanceEventStats> eventStatsList = new ArrayList<>(eventStats);

            eventStatsList.stream().forEach(stats -> {
                add(stats);
            });
            //reassign as collection time
        }
        this.time = DateTime.now();
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupedStats{");
        sb.append("jobsFinished=").append(getJobsFinished());
        sb.append('}');
        return sb.toString();
    }

    // public List<ProvenanceEventStats> getEventStatsList() {
    //      return eventStatsList;
    //  }
}
