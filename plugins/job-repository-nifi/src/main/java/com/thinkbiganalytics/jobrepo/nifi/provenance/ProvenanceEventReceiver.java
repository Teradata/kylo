/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.jobrepo.nifi.provenance.db.ProvenanceEventReceiverDatabaseWriter;

import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.activemq.Subscriptions;
import com.thinkbiganalytics.nifi.activemq.Topics;

/**
 * Created by sr186054 on 3/3/16.
 */

@Component
public class ProvenanceEventReceiver {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceEventReceiver.class);


    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    @Autowired
    private ProvenanceEventReceiverDatabaseWriter databaseWriter;

    public ProvenanceEventReceiver() {

    }

    @JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY,
                 subscription = Subscriptions.FEED_MANAGER_NIFI_PROVENANCE)
    public void receiveTopic(ProvenanceEventDTO message) {
        logger.info("Received ProvenanceEvent with Nifi Event Id of " + message.getEventId() + " <" + message + ">");
        Long eventId = message.getEventId();
        try {
            eventId = databaseWriter.writeEvent(message);
        } catch (Exception e) {
            logger.error("Error writing database event");
        }
        ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO(eventId, message);
        provenanceEventListener.receiveEvent(dto);
    }


}
