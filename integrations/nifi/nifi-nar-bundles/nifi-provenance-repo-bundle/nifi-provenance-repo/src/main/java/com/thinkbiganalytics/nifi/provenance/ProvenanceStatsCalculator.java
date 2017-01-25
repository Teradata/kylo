package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Calculate Statistics pertaining to each Feed and Processor and send them off to JMS
 */

public class ProvenanceStatsCalculator {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceStatsCalculator.class);

    /**
     * Object to hold onto the {@link com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics} for each feed and send them onto Kylo via JMS ({@link this#sendStats()}
     */
    private AggregatedFeedProcessorStatisticsHolder statsHolder;


    /**
     * Helper class to send the data off to JMS
     */
    @Autowired
    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    public ProvenanceStatsCalculator() {
        statsHolder = new AggregatedFeedProcessorStatisticsHolder();
    }


    /**
     * Send the stats to JMS
     */
    public void sendStats() {
        if (statsHolder != null) {
            if (provenanceEventActiveMqWriter != null) {
                provenanceEventActiveMqWriter.writeStats(statsHolder);
                //reset the statsHolder to get new Statistics
                statsHolder = new AggregatedFeedProcessorStatisticsHolder();
            }
        }
    }


    /**
     * Group the incoming provenance event and gather statistics from it.
     *
     * @param event a ProvenanceEvent to be processed for statistics
     */
    public void calculateStats(ProvenanceEventRecordDTO event) {
        String feedName = event.getFeedName();
        if (feedName != null) {
            try {
                statsHolder.addStat(event);
            } catch (Exception e) {
                log.error("Unable to add Statistics for Event {}.  Exception: {} ", event, e.getMessage(), e);
            }
        } else {
            log.error("Unable to add Statistics for Event {}.  Unable to find feed for event ", event);
        }
    }


}
