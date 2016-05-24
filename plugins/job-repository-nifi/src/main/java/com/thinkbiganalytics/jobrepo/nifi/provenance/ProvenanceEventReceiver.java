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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sr186054 on 3/3/16.
 */

@Component
public class ProvenanceEventReceiver implements ProvenanceEventStartupCompleteListener {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiver.class);

    private ConcurrentLinkedQueue<ProvenanceEventDTO> unprocessedEventsQueue = new ConcurrentLinkedQueue<>();


    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    @Autowired
    private ProvenanceEventStartupListener provenanceEventStartupListener;


    private AtomicBoolean canProcessJmsMessages = new AtomicBoolean(false);

    @Override
    public void onEventsInitialized() {
        //process anything in the queue
        processEventsOnStartup();
    }

    public ProvenanceEventReceiver() {

    }

    private void processEventsOnStartup() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Startup is finished... now processing internal Queue size of: {} in a new Thread", unprocessedEventsQueue.size());
                processAllUnprocessedEventsEvent();
                log.info("Startup processing is finished continue on with JMS processing ");
                canProcessJmsMessages.set(true);
            }
        });
        thread.start();
    }

    @PostConstruct
    public void init() {
        provenanceEventStartupListener.subscribe(this);
    }

    @JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY,
            subscription = Subscriptions.FEED_MANAGER_NIFI_PROVENANCE)
    public void receiveTopic(ProvenanceEventDTO message) {
        Long eventId = message.getEventId();
        log.info("Received ProvenanceEvent with Nifi Event Id of {} <{}>", eventId, message);


        if (canProcessJmsMessages.get()) {
            processAllUnprocessedEventsEvent();
            ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO(eventId, message);
            provenanceEventListener.receiveEvent(dto);
        } else {
            //wait and hold until
            log.info("Startup is not finished... holding event {} in internal queue", message.getEventId());
            unprocessedEventsQueue.add(message);
            // unprocessedEvents.add(message);
        }
    }

    private void processAllUnprocessedEventsEvent() {
        ProvenanceEventDTO event = null;
        while ((event = unprocessedEventsQueue.poll()) != null) {
            Long eventId = event.getEventId();
            ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO(eventId, event);
            log.info("process event in internal queue {} ", dto);
            try {
                provenanceEventListener.receiveEvent(dto);
            } catch (Exception e) {
                log.error("ERROR PROCESSING EVENT (Nifi Processor Id: {} ) for job that was running prior to Pipeline Controller going down. {}. {} ", dto.getComponentId(), e.getMessage(), e.getStackTrace());
            }
        }
    }


}
