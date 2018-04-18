package com.thinkbiganalytics.nifi.provenance.model.util;
/*-
 * #%L
 * thinkbig-nifi-provenance-model
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

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolderV3;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsV2;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatisticsV2;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStatsIdentity;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStatsV2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GroupedStatsUtil {


    /**
     * Add stats2 to stats1 and modify stats1
     *
     * @param stats1 stats to modify
     * @param stats2 stats to add
     */
    public static void addStats1(GroupedStats stats1, GroupedStats stats2) {
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
        if (stats1 instanceof GroupedStatsV2 && stats2 instanceof GroupedStatsV2) {
            if (((GroupedStatsV2) stats1).getLatestFlowFileId() == null && ((GroupedStatsV2) stats2).getLatestFlowFileId() != null) {
                ((GroupedStatsV2) stats1).setLatestFlowFileId(((GroupedStatsV2) stats2).getLatestFlowFileId());
            }
        }
    }


    /**
     * Add 2 stats together and return a new stats object
     *
     * @param stats1 stats object
     * @param stats2 stats object
     * @return a new stats object
     */
    public static GroupedStats add(GroupedStats stats1, GroupedStats stats2) {
        GroupedStats stats = new GroupedStats();
        addStats1(stats, stats1);
        addStats1(stats, stats2);
        return stats;
    }

    /**
     * Add a list of stats together and return a new object
     *
     * @param groupedStats stats
     * @return new stats
     */
    public static GroupedStats add(GroupedStats... groupedStats) {
        GroupedStats allStats = new GroupedStats();
        for (GroupedStats stats : groupedStats) {
            addStats1(allStats, stats);
        }
        return allStats;
    }

    /**
     * Add a list of stats together and return a new object
     *
     * @param groupedStats stats to add
     * @return a new stats object
     */
    public static GroupedStats add(List<GroupedStats> groupedStats) {
        return add(groupedStats.toArray(new GroupedStats[groupedStats.size()]));
    }


    public static GroupedStats add(GroupedStats stats, ProvenanceEventRecordDTO event) {
        Long eventId = event.getEventId();
        if (stats instanceof GroupedStatsV2) {
            ((GroupedStatsV2) stats).setLatestFlowFileId(event.getFlowFileUuid());
        }
        stats.addTotalCount(1L);
        stats.addBytesIn(event.getInputContentClaimFileSizeBytes() != null ? event.getInputContentClaimFileSizeBytes() : 0L);
        stats.addBytesOut(event.getOutputContentClaimFileSizeBytes() != null ? event.getOutputContentClaimFileSizeBytes() : 0L);
        //reset the duration
        stats.addDuration(event.getEventDuration());
        stats.setSourceConnectionIdentifier(event.getSourceConnectionIdentifier());

        if (event.isFailure()) {
            stats.addProcessorsFailed(1L);

        }
        if (event.isStartOfJob()) {
            stats.addJobsStarted(1L);
        }
     /*
        if (event.isFinalJobEvent()) {
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
        */
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

        if (Math.abs(stats.getMaxEventId()) < Math.abs(eventId)) {
            stats.setMaxEventId(eventId);
        }

        return stats;

    }


    /**
     * Group stats together by processor for a given feed
     *
     * @param feedName     feed name
     * @param groupedStats Map of processorIdentity to list of stats for that processor
     */
    public static AggregatedFeedProcessorStatistics groupStatsByProcessor(String feedName, Map<GroupedStatsIdentity, List<GroupedStats>> groupedStats) {
        Long sendJmsTimeMillis = 3000L;
        String collectionId = UUID.randomUUID().toString();

        //Create a random id for the starting processor.
        //the starting feed processor id is not needed as we already know the feed name associated with these stats
        String startingProcessorId = UUID.randomUUID().toString();
        AggregatedFeedProcessorStatisticsV2
            feedProcessorStatistics = new AggregatedFeedProcessorStatisticsV2(startingProcessorId, collectionId, sendJmsTimeMillis);
       feedProcessorStatistics.setFeedName(feedName);

        for (Map.Entry<GroupedStatsIdentity, List<GroupedStats>> stats : groupedStats.entrySet()) {
            String processorName = stats.getKey().getProcessorName();
            String processorId = stats.getKey().getProcessorId();


            Map<String, AggregatedProcessorStatistics> processorStatsMap = feedProcessorStatistics.getProcessorStats();
            if (!processorStatsMap.containsKey(processorName)) {
                processorStatsMap.put(processorName, new AggregatedProcessorStatisticsV2(processorId, processorName, collectionId));
            }
            AggregatedProcessorStatistics
                processorStatistics = processorStatsMap.get(processorName);

            for (GroupedStats s : stats.getValue()) {
                GroupedStatsUtil.addStats1(processorStatistics.getStats(GroupedStats.DEFAULT_SOURCE_CONNECTION_ID), s);
            }
           /*
            AggregatedProcessorStatistics
                processorStatistics =
                feedProcessorStatistics.getProcessorStats()
                    .computeIfAbsent(processorName, k -> new AggregatedProcessorStatisticsV2(processorId, k, collectionId));


            stats.getValue().stream().forEach( s ->  GroupedStatsUtil.add(processorStatistics.getStats(GroupedStats.DEFAULT_SOURCE_CONNECTION_ID), s));
            */

        }
        return feedProcessorStatistics;

    }


    /**
     * Gather feed stats for a list of events
     */
    public static AggregatedFeedProcessorStatisticsHolder gatherStats(final List<ProvenanceEventRecordDTO> events) {
        Map<String, Map<GroupedStatsIdentity, List<GroupedStats>>> feedStatsByProcessor = new ConcurrentHashMap<>();

        //events.stream().forEach(e -> {
        for (ProvenanceEventRecordDTO e : events) {

            if (!feedStatsByProcessor.containsKey(e.getFeedName())) {
                feedStatsByProcessor.put(e.getFeedName(), new ConcurrentHashMap<GroupedStatsIdentity, List<GroupedStats>>());
            }
            // feedStatsByProcessor.putIfAbsent(e.getFeedName(), );

            Map<GroupedStatsIdentity, List<GroupedStats>> feedStats = feedStatsByProcessor.get(e.getFeedName());

            GroupedStatsIdentity identity = new GroupedStatsIdentity(e.getComponentId(), e.getComponentName());
            if (!feedStats.containsKey(identity)) {
                feedStats.put(identity, new ArrayList<GroupedStats>());
            }
            //feedStats.putIfAbsent(identity, new ArrayList<>());

            List<GroupedStats> feedProcessorStats = feedStats.get(identity);

            //Add the new stats
            GroupedStats statsV2 = GroupedStatsUtil.add(new GroupedStatsV2(), e);
            feedProcessorStats.add(statsV2);


        }
        //);

        List<AggregatedFeedProcessorStatistics> statsList = new ArrayList<>();

        for (Map.Entry<String, Map<GroupedStatsIdentity, List<GroupedStats>>> feedStats : feedStatsByProcessor.entrySet()) {
            AggregatedFeedProcessorStatistics feedProcessorStatistics = GroupedStatsUtil.groupStatsByProcessor(feedStats.getKey(), feedStats.getValue());
            statsList.add(feedProcessorStatistics);
        }
       /* feedStatsByProcessor.entrySet().stream().forEach(feedStats -> {
            AggregatedFeedProcessorStatistics feedProcessorStatistics = GroupedStatsUtil.groupStatsByProcessor(feedStats.getKey(), feedStats.getValue());
            statsList.add(feedProcessorStatistics);
        });
        */
        AggregatedFeedProcessorStatisticsHolderV3 feedProcessorStatisticsHolderV3 = new AggregatedFeedProcessorStatisticsHolderV3();
        feedProcessorStatisticsHolderV3.setFeedStatistics(statsList);
        return feedProcessorStatisticsHolderV3;
    }

}
