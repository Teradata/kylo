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

import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventJmsWriter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Send data to Ops Manager
 */
public class JmsSender {

    private static final Logger log = LoggerFactory.getLogger(JmsSender.class);

    List<ProvenanceEventRecordDTO> eventsToSend = null;
    List<AggregatedFeedProcessorStatistics> statsToSend = null;

    public JmsSender(List<ProvenanceEventRecordDTO> eventsToSend, Collection<AggregatedFeedProcessorStatistics> statsToSend) {
        this.eventsToSend = eventsToSend;
        if (statsToSend != null) {
            this.statsToSend = new ArrayList<>(statsToSend);
        }
    }

    public void run() {

        try {
            if (eventsToSend != null && !eventsToSend.isEmpty()) {
                ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
                eventRecordDTOHolder.setEvents(eventsToSend);
                getProvenanceEventActiveMqWriter().writeBatchEvents(eventRecordDTOHolder);
            }

            if (statsToSend != null && !statsToSend.isEmpty()) {
                AggregatedFeedProcessorStatisticsHolder statsHolder = new AggregatedFeedProcessorStatisticsHolder();
                statsHolder.setCollectionId(statsToSend.get(0).getCollectionId());
                statsHolder.setFeedStatistics(statsToSend);
                getProvenanceEventActiveMqWriter().writeStats(statsHolder);
            }


        } catch (Exception e) {
            e.printStackTrace();
            //TODO log
        }
    }

    public ProvenanceEventJmsWriter getProvenanceEventActiveMqWriter() {
        ProvenanceEventJmsWriter provenanceEventJmsWriter = SpringApplicationContext.getInstance().getBean(ProvenanceEventJmsWriter.class);
        if (provenanceEventJmsWriter == null) {
            log.error("!!!!!!!ProvenanceEventJmsWriter is NULL !!!!!!");
        }
        return provenanceEventJmsWriter;
    }
}
