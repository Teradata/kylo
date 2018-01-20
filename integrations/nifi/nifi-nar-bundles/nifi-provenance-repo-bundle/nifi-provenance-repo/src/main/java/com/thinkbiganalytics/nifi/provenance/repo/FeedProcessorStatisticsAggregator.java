package com.thinkbiganalytics.nifi.provenance.repo;


/*-
 * #%L
 * thinkbig-nifi-provenance-repo
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStatsV2;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;

import org.apache.nifi.provenance.ProvenanceEventRecord;

import java.util.List;


/**
 * Aggregate stats together
 */
public class FeedProcessorStatisticsAggregator {


    private static final FeedProcessorStatisticsAggregator instance = new FeedProcessorStatisticsAggregator();

    private FeedProcessorStatisticsAggregator() {

    }

    public static FeedProcessorStatisticsAggregator getInstance() {
        return instance;
    }


    public void add(GroupedStats stats, ProvenanceEventRecord event, Long eventId) {
        if(stats instanceof GroupedStatsV2) {
            ((GroupedStatsV2)stats).setLatestFlowFileId(event.getFlowFileUuid());
        }
        stats.addTotalCount(1L);
        stats.addBytesIn(event.getPreviousFileSize() != null ? event.getPreviousFileSize() : 0L);
        stats.addBytesOut(event.getFileSize());
        stats.addDuration(FeedEventStatistics.getInstance().getEventDuration(eventId));
        stats.setSourceConnectionIdentifier(event.getSourceQueueIdentifier());

        if (ProvenanceEventUtil.isTerminatedByFailureRelationship(event)) {
            stats.addProcessorsFailed(1L);

        }
        //   this.flowFilesStarted += event.isStartOfFlowFile() ? 1L : 0L;
        //  this.flowFilesFinished += event.isEndingFlowFileEvent() ? 1L : 0L;
        if (ProvenanceEventUtil.isStartingFeedFlow(event)) {
            stats.addJobsStarted(1L);
        }

        if (FeedEventStatistics.getInstance().isEndingFeedFlow(eventId)) {
            stats.addJobsFinished(1L);
            Long jobTime = FeedEventStatistics.getInstance().getFeedFlowFileDuration(event);
            stats.addJobDuration(jobTime);
            if (FeedEventStatistics.getInstance().hasFailures(event)) {
                stats.addJobsFailed(1L);
            } else {
                stats.addSuccessfulJobDuration(jobTime);
                //count successful jobs?
            }

        }
        stats.setTime(event.getEventTime());

        if (stats.getMinTime() == null) {
            stats.setMinTime(event.getEventTime());
        }

        if (stats.getMaxTime() == null) {
            stats.setMaxTime(event.getEventTime());
        }
        if (event.getEventTime() > stats.getMaxTime()) {
            stats.setMaxTime(event.getEventTime());
        }
        if (event.getEventTime() < stats.getMinTime()) {
            stats.setMinTime(event.getEventTime());
        }

        if (stats.getMaxEventId() < eventId) {
            stats.setMaxEventId(eventId);
        }


    }

    public void addStats1(GroupedStats stats1, GroupedStats stats2) {
        stats1.addTotalCount(stats2.getTotalCount());
        stats1.addBytesIn(stats2.getBytesIn());
        stats1.addBytesOut(stats2.getBytesOut());
        stats1.addDuration(stats2.getDuration());
        stats1.addProcessorsFailed(stats2.getProcessorsFailed());
        stats1.addJobsStarted(stats2.getJobsStarted());
        stats1.addJobsFinished(stats2.getJobsFinished());
        stats1.addJobDuration(stats2.getJobDuration());
        stats1.addJobsFailed(stats2.getJobsFailed());
        stats1.addSuccessfulJobDuration(stats2.getSuccessfulJobDuration());
        stats1.setMaxTime(stats1.getMaxTime() == null || stats1.getMaxTime() < stats2.getMaxTime() ? stats2.getMaxTime() : stats1.getMaxTime());
        stats1.setMinTime(stats1.getMinTime() == null || stats1.getMinTime() > stats2.getMinTime() ? stats2.getMinTime() : stats1.getMinTime());
        stats1.setTime(stats1.getMinTime());
        if(stats1 instanceof GroupedStatsV2 && stats2 instanceof GroupedStatsV2)
        if(((GroupedStatsV2) stats1).getLatestFlowFileId() == null && ((GroupedStatsV2) stats2).getLatestFlowFileId() != null){
            ((GroupedStatsV2) stats1).setLatestFlowFileId(((GroupedStatsV2) stats2).getLatestFlowFileId());
        }
    }


    public GroupedStats add(GroupedStats stats1, GroupedStats stats2) {
        GroupedStats stats = new GroupedStats();
        addStats1(stats, stats1);
        addStats1(stats, stats2);
        return stats;
    }

    public GroupedStats add(GroupedStats... groupedStatss) {
        GroupedStats allStats = new GroupedStats();
        for (GroupedStats stats : groupedStatss) {
            addStats1(allStats, stats);
        }
        return allStats;
    }

    public GroupedStats add(List<GroupedStats> groupedStats) {
        return add(groupedStats.toArray(new GroupedStats[groupedStats.size()]));
    }
}
