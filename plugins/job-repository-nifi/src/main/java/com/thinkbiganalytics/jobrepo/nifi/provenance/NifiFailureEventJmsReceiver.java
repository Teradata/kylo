package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.config.OperationalMetadataAccess;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventStatisticsProvider;
import com.thinkbiganalytics.jobrepo.jpa.NifiFailedEvent;
import com.thinkbiganalytics.jobrepo.jpa.NifiFailedProvenanceEventProvider;
import com.thinkbiganalytics.jobrepo.model.FailedProvenanceEvent;
import com.thinkbiganalytics.jobrepo.nifi.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;

import org.springframework.jms.annotation.JmsListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 8/17/16.
 */
public class NifiFailureEventJmsReceiver {

    @Inject
    NifiFlowCache nifiFlowCache;


    @Inject
    private NifiFailedProvenanceEventProvider nifiFailedProvenanceEventProvider;

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;


    @Inject
    private NifiEventStatisticsProvider nifiEventStatisticsProvider;


    @JmsListener(destination = Queues.PROVENANCE_EVENT_FAILURE_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(ProvenanceEventRecordDTOHolder failedEvents) {

        operationalMetadataAccess.commit(() -> {

            return createFailedEvents(failedEvents);

        });

    }

    private List<FailedProvenanceEvent> createFailedEvents(ProvenanceEventRecordDTOHolder failedEvents) {
        if (failedEvents != null && !failedEvents.getEvents().isEmpty()) {
            List<FailedProvenanceEvent> savedFailures = new ArrayList<>();
            for (ProvenanceEventRecordDTO eventRecordDTO : failedEvents.getEvents()) {
                FailedProvenanceEvent failedProvenanceEvent = nifiFailedProvenanceEventProvider.create(toFailedEvent(eventRecordDTO));
                savedFailures.add(failedProvenanceEvent);
            }
            return savedFailures;
        }
        return null;
    }


    private NifiFailedEvent toFailedEvent(ProvenanceEventRecordDTO event) {
        NifiFailedEvent failedEvent = new NifiFailedEvent(event.getEventId(), event.getFlowFileUuid());
        failedEvent.setFeedProcessGroupId(event.getFeedProcessGroupId());
        failedEvent.setProcessorName(event.getProcessorName());
        failedEvent.setFeedName(event.getFeedName());
        failedEvent.setEventDetails(event.getDetails());
        failedEvent.setEventTime(event.getEventTime());
        failedEvent.setProcessorId(event.getComponentId());
        return failedEvent;
    }
}
