/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.provenance;

import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.activemq.Topics;

/**
 * Created by sr186054 on 3/3/16.
 */

@Component
public class ProvenanceEventReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(ProvenanceEventReceiver.class);


    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    public ProvenanceEventReceiver(){

    }

    @JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(ProvenanceEventDTO message){
        LOG.info("Received ProvenanceEvent with eventId of " + message.getEventId() + " <" + message + ">");
        ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO((message));
        provenanceEventListener.receiveEvent(dto);
    }



}
