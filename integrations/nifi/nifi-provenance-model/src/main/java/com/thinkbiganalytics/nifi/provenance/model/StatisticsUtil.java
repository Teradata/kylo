package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.FeedProcessorStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProcessorStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/16/16.
 */
public class StatisticsUtil {


    /**
     * Take a list of Event Stats objects and return only those that are in between the supplied dates
     */
    public static List<ProvenanceEventStats> getEventStatsBetween(List<ProvenanceEventStats> eventStatsList, DateTime start, DateTime end) {

        return eventStatsList.stream().filter(
            stats -> ((stats.getTime() != null && ((stats.getTime().isAfter(start) || stats.getTime().isEqual(start)) && (stats.getTime().isBefore(end) || stats.getTime().isEqual(end))))))
            //.sorted((stats1, stats2) -> stats1.getTime().compareTo(stats2.getTime()))
            .collect(
                Collectors.toList());
    }


    public static List<ProvenanceEventStats> getEventStatsBeforeOrEqualTo(List<ProvenanceEventStats> eventStatsList, DateTime end) {

        return eventStatsList.stream().filter(stats -> ((stats.getTime() != null && (stats.getTime().isBefore(end) || stats.getTime().isEqual(end)))))
            //.sorted((stats1, stats2) -> stats1.getTime().compareTo(stats2.getTime()))
            .collect(
                Collectors.toList());
    }

    /**
     * Aggregate
     */
    public static GroupedStats aggregateStats(List<ProvenanceEventStats> eventStatsList, String collectionId, DateTime start, DateTime end) {
        List<ProvenanceEventStats> stats = getEventStatsBetween(eventStatsList, start, end);
        return aggregateStats(stats, collectionId);
    }

    public static GroupedStats aggregateStats(List<ProvenanceEventStats> eventStatsList, String collectionId) {
        return new GroupedStats(collectionId, eventStatsList);
    }

    /**
     * For the incoming list of EventStats group them by Feed and then By Processor and sum up the totals.
     * @param eventStatsList
     * @param collectionId
     * @return
     */
    public static List<AggregatedFeedProcessorStatistics> aggregateStatsByFeedAndProcessor(List<ProvenanceEventStats> eventStatsList, String collectionId) {

        List<AggregatedFeedProcessorStatistics> aggregatedFeedProcessorStatisticsList;
        //1 group the List of eventStats first by feed and then by Processor
        DateTime minEventTime = null;
        DateTime maxEventTime = null;

        Map<String, FeedProcessorStats> feedStatistics = new HashMap<>();
        for (ProvenanceEventStats provenanceEventStats : eventStatsList) {
            String feedName = provenanceEventStats.getFeedName();
            if (!feedStatistics.containsKey(feedName)) {
                feedStatistics.put(feedName, new FeedProcessorStats(feedName));
            }
            FeedProcessorStats feedProcessorStatistics = feedStatistics.get(feedName);
            feedProcessorStatistics.addEventStats(provenanceEventStats);
            if (minEventTime == null || (minEventTime != null && provenanceEventStats.getTime().isBefore(minEventTime))) {
                minEventTime = provenanceEventStats.getTime();
            }
            if (maxEventTime == null || (maxEventTime != null && provenanceEventStats.getTime().isAfter(maxEventTime))) {
                maxEventTime = provenanceEventStats.getTime();
            }
        }

        //2 aggregate and sum up the statistics
        final DateTime fMinEventTime = minEventTime;
        final DateTime fMaxEventTime = maxEventTime;

        return feedStatistics.entrySet().stream().map(entry -> {
            String feedName = entry.getKey();
            FeedProcessorStats feedProcessorStats = entry.getValue();
            AggregatedFeedProcessorStatistics aggregatedFeedProcessorStatistics = new AggregatedFeedProcessorStatistics(feedName);
            aggregatedFeedProcessorStatistics.setMinTime(fMinEventTime);
            aggregatedFeedProcessorStatistics.setMaxTime(fMaxEventTime);
            Map<String, AggregatedProcessorStatistics> processorStatisticsMap = new HashMap<>();

            for (Map.Entry<String, ProcessorStats> processorStatsEntry : feedProcessorStats.getProcessorStats().entrySet()) {
                String processorId = processorStatsEntry.getKey();
                ProcessorStats processorStats = processorStatsEntry.getValue();

                AggregatedProcessorStatistics aggregatedProcessorStatistics = new AggregatedProcessorStatistics(processorId, aggregateStats(processorStats.getEventStats(), collectionId));
                processorStatisticsMap.put(processorId, aggregatedProcessorStatistics);

            }
            aggregatedFeedProcessorStatistics.setProcessorStats(processorStatisticsMap);
            aggregatedFeedProcessorStatistics.calculateTotalEvents();
            return aggregatedFeedProcessorStatistics;
        }).collect(Collectors.toList());


    }




}
