package com.thinkbiganalytics.nifi.activemq;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created by sr186054 on 3/3/16.
 */

@Component
public class ProvenanceEventReceiver {

    @Autowired
    ProvenanceEventReceiverDatabaseWriter databaseWriter;

    public ProvenanceEventReceiver() {

    }

    @JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(ProvenanceEventDTO message) {
        System.out.println("Received <" + message + "> ...... databaseWriter == " + databaseWriter);
        databaseWriter.writeEvent(message);
    }


}
