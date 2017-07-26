package com.thinkbiganalytics.nifi.provenance.jms;

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

import com.thinkbiganalytics.jms.SendJmsMessage;
import com.thinkbiganalytics.jms.Queues;
import com.thinkbiganalytics.nifi.provenance.AggregationEventProcessingStats;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

/**
 * Send ProvenanceEvent data to JMS queues
 * 2 Queues are used.  The Queue names are constants shared with Kylo Operations Manager found in the {@link Queues} class.
 * Queues.PROVENANCE_EVENT_STATS_QUEUE  is the Statistics Queue name for creating the Summary statistics
 * Queues.FEED_MANAGER_QUEUE is the Batch Provenance Events Queue for creating the Jobs/Steps in Kylo
 */
public class ProvenanceEventJmsWriter {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceEventJmsWriter.class);


    @Autowired
    private SendJmsMessage sendJmsMessage;


    private Map<String, Set<JmsSendListener>> listeners = new HashMap<>();

    public ProvenanceEventJmsWriter() {

    }

    public void subscribe(JmsSendListener listener) {
        this.listeners.computeIfAbsent(listener.getDestination(), (d) -> new HashSet<JmsSendListener>()).add(listener);
    }

    @PostConstruct
    public void postConstruct() {

    }

    /**
     * Notify any listeners of a successful JMS send
     */
    private void notifySuccess(String destination, Object payload) {
        if (this.listeners.containsKey(destination)) {
            this.listeners.get(destination).stream().forEach(listener -> listener.successfulJmsMessage(destination, payload));
        }
    }

    /**
     * Notify any listeners of a JMS error in sending
     */
    private void notifyError(String destination, Object payload, String errorMessge) {
        if (this.listeners.containsKey(destination)) {
            this.listeners.get(destination).stream().forEach(listener -> listener.errorJmsMessage(destination, payload, errorMessge));
        }
    }

    /**
     * Send the Statistics to JMS using the JMS Queue {@link Queues#PROVENANCE_EVENT_STATS_QUEUE}
     *
     * @param stats that statistics to send to JMS
     */
    public void writeStats(AggregatedFeedProcessorStatisticsHolder stats) {
        try {
                sendJmsMessage.sendSerializedObjectToQueue(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats);
              //  AggregationEventProcessingStats.addStreamingEvents(stats.getEventCount().intValue());
                notifySuccess(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats);
        } catch (Exception e) {
            logger.error("JMS Error has occurred sending stats. Temporary queue has been disabled in this current version.", e);
            notifyError(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats, e.getMessage());
        }
    }

    /**
     * Send the Batched Events to the JMS Queue {@link Queues#FEED_MANAGER_QUEUE}
     *
     * @param events the events to send to JMS
     */
    public void writeBatchEvents(ProvenanceEventRecordDTOHolder events) {
        try {
            logger.info("SENDING Batch Events to JMS {} ", events);
            sendJmsMessage.sendSerializedObjectToQueue(Queues.FEED_MANAGER_QUEUE, events);
            AggregationEventProcessingStats.addBatchEvents(events.getEvents().size());
            notifySuccess(Queues.FEED_MANAGER_QUEUE, events);
        } catch (Exception e) {
            logger.error("Error sending Batch Events to JMS ", e);
            notifyError(Queues.FEED_MANAGER_QUEUE, events, e.getMessage());

        }
    }

}
