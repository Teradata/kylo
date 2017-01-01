package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.StatsModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by sr186054 on 8/15/16.
 */
@Component
public class ProvenanceStatsCalculator {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceStatsCalculator.class);

    private AggregatedFeedProcessorStatisticsHolder statsHolder;


    @Autowired
    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    public ProvenanceStatsCalculator() {

        statsHolder = new AggregatedFeedProcessorStatisticsHolder();
    }


    /**
     * Stats are grouped by the nextSendTime and then by Feed and Processor
     */
    private void addStats(ProvenanceEventStats stats) {
        if (stats != null) {
            statsHolder.addStat(stats);
        }
    }

    /**
     * Send the stats in the map matching the Key to JMS then remove the elements from the statsByTime map
     */
    public void sendStats() {
        if (statsHolder != null) {
            if (provenanceEventActiveMqWriter != null) {
                provenanceEventActiveMqWriter.writeStats(statsHolder);
                statsHolder = new AggregatedFeedProcessorStatisticsHolder();
            }
        }
    }

    public void addFailureStats(ProvenanceEventStats failureStats) {
        addStats(failureStats);
    }


    /**
     * Converts the incoming ProvenanceEvent into an object that can be used to gather statistics (ProvenanceEventStats)
     */
    public ProvenanceEventStats calculateStats(ProvenanceEventRecordDTO event) {
        String feedName = event.getFeedName() == null ? event.getFlowFile().getFeedName() : event.getFeedName();
        if (feedName != null) {
            try {
                ProvenanceEventStats eventStats = StatsModel.toProvenanceEventStats(feedName, event);
                addStats(eventStats);

                return eventStats;
            } catch (Exception e) {
                log.error("Unable to add Statistics for Event {}.  Exception: {} ", event, e.getMessage(), e);
            }
        } else {
            log.error("Unable to add Statistics for Event {}.  Unable to find feed for event ", event);
        }
        return null;
    }

    /**
     * Stores just the stats
     */
    public void addStats(List<ProvenanceEventStats> statsList) {
        if (statsList != null) {
            statsList.stream().forEach(stats -> {
                addStats(stats);
            });
        }
    }


}
