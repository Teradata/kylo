package com.thinkbiganalytics.nifi.provenance.jms;

import com.thinkbiganalytics.activemq.SendJmsMessage;
import com.thinkbiganalytics.nifi.activemq.Queues;
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
 *
 */
public class ProvenanceEventActiveMqWriter {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceEventActiveMqWriter.class);


    @Autowired
    private SendJmsMessage sendJmsMessage;


    private Map<String, Set<JmsSendListener>> listeners = new HashMap<>();

    public void subscribe(JmsSendListener listener) {
        this.listeners.computeIfAbsent(listener.getDestination(), (d) -> new HashSet<JmsSendListener>()).add(listener);
    }


    public ProvenanceEventActiveMqWriter() {

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
     * Send the Statistics to JMS using the JMS Queue {@link Queues.PROVENANCE_EVENT_STATS_QUEUE}
     *
     * @param stats that statistics to send to JMS
     */
    public void writeStats(AggregatedFeedProcessorStatisticsHolder stats) {
        try {
            if (stats.getEventCount().get() > 0) {
                logger.info("SENDING AGGREGATED STAT to JMS {} ", stats);
                sendJmsMessage.sendSerializedObjectToQueue(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats);
                AggregationEventProcessingStats.addStreamingEvents(stats.getEventCount().intValue());
                notifySuccess(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats);
            }
        } catch (Exception e) {
            logger.error("JMS Error has occurred sending stats. Temporary queue has been disabled in this current version.", e);
            notifyError(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats, e.getMessage());
        }
    }

    /**
     * Send the Batched Events to the JMS Queue {@link Queues.FEED_MANAGER_QUEUE}
     *
     * @param events the events to send to JMS
     */
    public void writeBatchEvents(ProvenanceEventRecordDTOHolder events) {
        try {
            logger.info("SENDING Events to JMS {} ", events);
            sendJmsMessage.sendSerializedObjectToQueue(Queues.FEED_MANAGER_QUEUE, events);
            AggregationEventProcessingStats.addBatchEvents(events.getEvents().size());
            notifySuccess(Queues.FEED_MANAGER_QUEUE, events);
        } catch (Exception e) {
            logger.error("Error writing sending JMS ", e);
            notifyError(Queues.FEED_MANAGER_QUEUE, events, e.getMessage());

        }
    }

}
