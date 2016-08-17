package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;

import org.joda.time.DateTime;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/16/16.
 */
public class StatisticsUtil {


    /**
     * Take a list of Event Stats objects and return only those that are in between the supplied dates
     */
    public static List<ProvenanceEventStats> getEventStatsBetween(List<ProvenanceEventStats> eventStatsList, DateTime start, DateTime end) {

        return eventStatsList.stream().filter(stats -> ((stats.getTime().isAfter(start) || stats.getTime().isEqual(start)) && (stats.getTime().isBefore(end) || stats.getTime().isEqual(end))))
            .sorted((stats1, stats2) -> stats1.getTime().compareTo(stats2.getTime())).collect(
                Collectors.toList());
    }

    /**
     * Aggregrate
     */
    public static GroupedStats aggregateStats(List<ProvenanceEventStats> eventStatsList, String collectionId, DateTime start, DateTime end) {
        List<ProvenanceEventStats> stats = getEventStatsBetween(eventStatsList, start, end);
        return new GroupedStats(collectionId, stats);
    }


}
