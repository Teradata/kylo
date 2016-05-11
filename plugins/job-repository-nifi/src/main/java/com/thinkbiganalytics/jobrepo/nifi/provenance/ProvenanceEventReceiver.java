/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.activemq.Subscriptions;
import com.thinkbiganalytics.nifi.activemq.Topics;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sr186054 on 3/3/16.
 */

@Component
public class ProvenanceEventReceiver implements ProvenanceEventStartupCompleteListener {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiver.class);


    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    @Autowired
    private ProvenanceEventStartupListener provenanceEventStartupListener;


    private AtomicBoolean canProcessJmsMessages = new AtomicBoolean(false);

    @Override
    public void onEventsInitialized() {
        //process anything in the queue
        log.info("Startup is finished... now processign internal Queue size of: {}",unprocessedEvents.size());
        processAllUnprocessedEventsEvent();
        log.info("Startup processing is finished continue on with JMS processing ");
        canProcessJmsMessages.set(true);
    }

    public ProvenanceEventReceiver() {

    }

    @PostConstruct
    public void init(){
        provenanceEventStartupListener.subscribe(this);
    }

    @JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY,
            subscription = Subscriptions.FEED_MANAGER_NIFI_PROVENANCE)
    public void receiveTopic(ProvenanceEventDTO message) {
        Long eventId = message.getEventId();
        log.info("Received ProvenanceEvent with Nifi Event Id of {} <{}>",eventId,message);


        if(canProcessJmsMessages.get()) {
            if(!unprocessedEvents.isEmpty()){
                //process it
                processAllUnprocessedEventsEvent();
            }
            ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO(eventId, message);
            provenanceEventListener.receiveEvent(dto);
        }
        else {
            //wait and hold until
            log.info("Startup is not finished... holding event {} in internal queue", message.getEventId());
            unprocessedEvents.add(message);
        }
    }

    private synchronized void processAllUnprocessedEventsEvent(){
        for(ProvenanceEventDTO event: unprocessedEvents){
            Long eventId = event.getEventId();
            ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO(eventId, event);
            log.info("process event in internal queue {} ",dto);
            provenanceEventListener.receiveEvent(dto);
        }
        unprocessedEvents.clear();
    }



    private TreeSet<ProvenanceEventDTO> unprocessedEvents = new TreeSet<ProvenanceEventDTO>(new Comparator<ProvenanceEventDTO>() {
        @Override
        public int compare(ProvenanceEventDTO o1, ProvenanceEventDTO o2) {
            if (o1 == null && o1 == null) {
                return 0;
            } else if (o1 == null && o2 != null) {
                return 1;
            } else if (o1 != null && o2 == null) {
                return -1;
            } else if (o1.getEventId().equals(o2.getEventId())) {
                return o1.getEventTime().compareTo(o2.getEventTime());
            } else {
                return o1.getEventId().compareTo(o2.getEventId());
            }
        }
    });



}
