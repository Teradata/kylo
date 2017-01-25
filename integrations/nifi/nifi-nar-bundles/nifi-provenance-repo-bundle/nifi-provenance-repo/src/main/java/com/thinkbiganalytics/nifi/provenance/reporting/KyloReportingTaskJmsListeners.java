package com.thinkbiganalytics.nifi.provenance.reporting;

import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.jms.JmsSendListener;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * listeners as to when the JMS sucessfully processes the events. The {@link KyloProvenanceEventReportingTask} subscribes to the JMS events to update the lastEventId {@link
 * KyloProvenanceEventReportingTask#setLastEventId(long)} so it knows what id it should start with next time it runs.
 */
public class KyloReportingTaskJmsListeners {

    private static final Logger log = LoggerFactory.getLogger(KyloReportingTaskBatchJmsListener.class);

    public static class KyloReportingTaskBatchJmsListener implements JmsSendListener<ProvenanceEventRecordDTOHolder> {

        private KyloProvenanceEventReportingTask reportingTask;

        public KyloReportingTaskBatchJmsListener(KyloProvenanceEventReportingTask reportingTask) {
            this.reportingTask = reportingTask;
        }

        @Override
        public String getDestination() {
            return Queues.FEED_MANAGER_QUEUE;
        }

        @Override
        public void successfulJmsMessage(String destination, ProvenanceEventRecordDTOHolder payload) {
            try {
                reportingTask.setLastEventId(payload.getMaxEventId());
            } catch (IOException e) {
                log.error("Unable to set LastEventId from JMS {}.  Tried to set it to {}, but got an error ", destination, payload.getMaxEventId(), e);
            }
        }

        @Override
        public void errorJmsMessage(String destination, ProvenanceEventRecordDTOHolder payload, String message) {

            log.error("Error processing JMS messages for {} with events between {} - {}.  Error : {} ", destination, payload.getMinEventId(), payload.getMaxEventId(), message);
        }
    }


    public static class KyloReportingTaskStatsJmsListener implements JmsSendListener<AggregatedFeedProcessorStatisticsHolder> {


        private KyloProvenanceEventReportingTask reportingTask;

        public KyloReportingTaskStatsJmsListener(KyloProvenanceEventReportingTask reportingTask) {
            this.reportingTask = reportingTask;
        }

        @Override
        public String getDestination() {
            return Queues.PROVENANCE_EVENT_STATS_QUEUE;
        }

        @Override
        public void successfulJmsMessage(String destination, AggregatedFeedProcessorStatisticsHolder payload) {
            try {
                reportingTask.setLastEventId(payload.getMaxEventId());
            } catch (IOException e) {
                log.error("Unable to set LastEventId from JMS {}.  Tried to set it to {}, but got an error ", destination, payload.getMaxEventId(), e);
            }
        }

        @Override
        public void errorJmsMessage(String destination, AggregatedFeedProcessorStatisticsHolder payload, String message) {

            log.error("Error processing JMS messages for {} with events between {} - {}.  Error : {} ", destination, payload.getMinEventId(), payload.getMaxEventId(), message);
        }
    }

}
