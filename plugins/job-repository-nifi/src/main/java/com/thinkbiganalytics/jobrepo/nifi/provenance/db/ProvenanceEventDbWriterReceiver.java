package com.thinkbiganalytics.jobrepo.nifi.provenance.db;

import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;

/**
 * Created by sr186054 on 3/3/16.
 */

//@Component
public class ProvenanceEventDbWriterReceiver {

    //@Autowired
    ProvenanceEventReceiverDatabaseWriter databaseWriter;

    public ProvenanceEventDbWriterReceiver() {

    }

    //@JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(ProvenanceEventDTO message) {
        System.out.println("Received <" + message + "> ...... databaseWriter == " + databaseWriter);
        databaseWriter.writeEvent(message);
    }


}
