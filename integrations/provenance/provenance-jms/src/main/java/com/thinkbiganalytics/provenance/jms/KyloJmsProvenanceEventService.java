package com.thinkbiganalytics.provenance.jms;
/*-
 * #%L
 * kylo-provenance-jms
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
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.GroupedStatsUtil;
import com.thinkbiganalytics.provenance.api.ProvenanceEventService;
import com.thinkbiganalytics.provenance.api.ProvenanceException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Send provenance data to JMS
 */
public class KyloJmsProvenanceEventService implements ProvenanceEventService {

    private static final Logger log = LoggerFactory.getLogger(KyloJmsProvenanceEventService.class);

    private static final String KYLO_BATCH_EVENT_QUEUE = "thinkbig.feed-manager";

    private static final String KYLO_EVENT_STATS_QUEUE = "thinkbig.provenance-event-stats";
    public static final String JMS_URL_CONFIG = "jms.url";

    private KyloJmsService kyloJmsService;

    private String jmsUrl;

    public KyloJmsProvenanceEventService() {
        kyloJmsService = new KyloJmsService();
    }


    @Override
    public void configure(Map<String, String> params) {
        jmsUrl = params.get(JMS_URL_CONFIG);
        if (StringUtils.isBlank(jmsUrl)) {
            jmsUrl = "tcp://localhost:61616";
        }
    }

    @Override
    public void sendEvents(List<ProvenanceEventRecordDTO> events) throws ProvenanceException {
        try {
            sendEvents(jmsUrl, events);
        } catch (Exception e) {
            throw new ProvenanceException(e);
        }
    }

    @Override
    public void closeConnection() {
        kyloJmsService.closeConnection();
    }


    private void sendEvents(String jmsUrl, List<ProvenanceEventRecordDTO> events) throws Exception {
        ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
        List<ProvenanceEventRecordDTO> batchEvents = new ArrayList<>();
        for(ProvenanceEventRecordDTO event : events){
            if(!event.isStream()){
                batchEvents.add(event);
            }
        }
        eventRecordDTOHolder.setEvents(batchEvents);
        AggregatedFeedProcessorStatisticsHolder stats = GroupedStatsUtil.gatherStats(events);
        log.info("Sending {} events to JMS ", eventRecordDTOHolder);
        sendKyloBatchEventMessage(jmsUrl, eventRecordDTOHolder);
        sendKyloEventStatisticsMessage(jmsUrl, stats);
        log.info("Events successfully sent to JMS");
    }


    private void sendKyloBatchEventMessage(String url, ProvenanceEventRecordDTOHolder msg) throws Exception {
        kyloJmsService.sendMessage(url, KYLO_BATCH_EVENT_QUEUE, msg);
    }

    private void sendKyloEventStatisticsMessage(String url, AggregatedFeedProcessorStatisticsHolder msg) throws Exception {
        kyloJmsService.sendMessage(url, KYLO_EVENT_STATS_QUEUE, msg);
    }


}