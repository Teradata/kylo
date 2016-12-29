package com.thinkbiganalytics.nifi.provenance.jms;

import com.thinkbiganalytics.activemq.ObjectMapperSerializer;
import com.thinkbiganalytics.activemq.SendJmsMessage;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.AggregationEventProcessingStats;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 3/3/16.
 */
@Component
public class ProvenanceEventActiveMqWriter {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceEventActiveMqWriter.class);


    @Autowired
    private SendJmsMessage sendJmsMessage;

    @Autowired
    ObjectMapperSerializer objectMapperSerializer;

    private Map<String, Set<JmsSendListener>> listeners = new HashMap<>();

    public void subscribe(JmsSendListener listener) {
        this.listeners.computeIfAbsent(listener.getDestination(), (d) -> new HashSet<JmsSendListener>()).add(listener);
    }


    public ProvenanceEventActiveMqWriter() {

    }

    @PostConstruct
    public void postConstruct() {

    }

    private void notifySuccess(String destination, Object payload) {
        if (this.listeners.containsKey(destination)) {
            this.listeners.get(destination).stream().forEach(listener -> listener.successfulJmsMessage(destination, payload));
        }
    }

    private void notifyError(String destination, Object payload, String errorMessge) {
        if (this.listeners.containsKey(destination)) {
            this.listeners.get(destination).stream().forEach(listener -> listener.errorJmsMessage(destination, payload, errorMessge));
        }
    }


    /**
     * Write to the provenance-event queue This is used for important events such as Start, Fail, End
     */
    public void writeEvents(ProvenanceEventRecordDTOHolder events) {
        logger.info("SENDING {} events to {} ", events, Queues.PROVENANCE_EVENT_QUEUE);
        try {
            sendJmsMessage.sendSerializedObjectToQueue(Queues.PROVENANCE_EVENT_QUEUE, events);
            notifySuccess(Queues.PROVENANCE_EVENT_QUEUE, events);
        } catch (JmsException e) {
            notifyError(Queues.PROVENANCE_EVENT_QUEUE, events, e.getMessage());
        }
    }

    /**
     * Write out stats to JMS
     */
    public void writeStats(AggregatedFeedProcessorStatisticsHolder stats) {
        try {
            logger.info("SENDING AGGREGATED STAT to JMS {} ", stats);
            sendJmsMessage.sendSerializedObjectToQueue(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats);
            AggregationEventProcessingStats.addStreamingEvents(stats.getEventCount().intValue());
            notifySuccess(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats);
        } catch (Exception e) {
            logger.error("JMS Error has occurred sending stats. Temporary queue has been disabled in this current version.", e);
            notifyError(Queues.PROVENANCE_EVENT_STATS_QUEUE, stats, e.getMessage());
        }
    }

    /***
     * Write the batch events to JMS
     */
    public void writeBatchEvents(ProvenanceEventRecordDTOHolder events) {
        try {
            logger.info("SENDING Events to JMS {} ", events);
            logger.info("Processing the JMS message as normal");
            sendJmsMessage.sendSerializedObjectToQueue(Queues.FEED_MANAGER_QUEUE, events);
            AggregationEventProcessingStats.addBatchEvents(events.getEvents().size());
            notifySuccess(Queues.FEED_MANAGER_QUEUE, events);
        } catch (Exception e) {
            logger.error("Error writing sending JMS ", e);
            notifyError(Queues.FEED_MANAGER_QUEUE, events, e.getMessage());

        }
    }

}
