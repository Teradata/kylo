package com.thinkbiganalytics.nifi.provenance;

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
