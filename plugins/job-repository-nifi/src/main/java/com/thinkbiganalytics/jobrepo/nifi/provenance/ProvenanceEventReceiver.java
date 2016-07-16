/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.activemq.Subscriptions;
import com.thinkbiganalytics.nifi.activemq.Topics;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;

import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 3/3/16.
 */


/**
 * JMS Listener for NIFI Provenance Events.
 */
@Component
public class ProvenanceEventReceiver implements ProvenanceEventJobExecutionStartupListener {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiver.class);

    private ConcurrentLinkedQueue<ProvenanceEventDTO> unprocessedEventsQueue = new ConcurrentLinkedQueue<>();


    private ConcurrentLinkedQueue<ProvenanceEventDTO> erroredEventsQueue = new ConcurrentLinkedQueue<>();


    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    @Autowired
    private ProvenanceEventApplicationStartupListener provenanceEventStartupListener;


    private AtomicBoolean canProcessJmsMessages = new AtomicBoolean(false);

    private AtomicBoolean nifiStartupConnectionError = new AtomicBoolean(false);

    private AtomicBoolean processingConnectionErrorStartupJobs = new AtomicBoolean(false);

    Timer processStartupEventsOnNifiConnectionErrorTimer;


    Timer nifiConnectionErrorTimer;


    @Override
    public void onEventsInitialized() {
        //process anything in the queue
        processEventsOnStartup();
    }

    @Override
    public void onStartupConnectionError() {
        //on connection errors for startup
        //1. flag receiver so it cannot process any more jms events
        canProcessJmsMessages.set(false);
        nifiStartupConnectionError.set(true);
        //start a timer to poll for nifi availablity and then call the  processStartupEventsOnNifiConnectionError() on connection
        startNifiConnectionStartupTimer();
    }

    public void startNifiConnectionStartupTimer() {
        processStartupEventsOnNifiConnectionErrorTimer = new Timer();
        //run the timer for every 5 seconds checking to see if nifi is up
        int timerSeconds = 5;
        log.info("Starting new Timer for Nifi Connection Check running every {} seconds ", timerSeconds);

        processStartupEventsOnNifiConnectionErrorTimer.schedule(new ProcessStartupEventsOnNifiConnectionErrorTask(), 0, timerSeconds * 1000);
    }


    public void startNifiConnectionTimer() {
        nifiConnectionErrorTimer = new Timer();
        //run the timer for every 5 seconds checking to see if nifi is up
        int timerSeconds = 5;
        log.info("Starting new Timer for Nifi Connection Check running every {} seconds ", timerSeconds);

        nifiConnectionErrorTimer.schedule(new NifiConnectionErrorTask(), 0, timerSeconds * 1000);
    }


    public ProvenanceEventReceiver() {

    }

    private void processEventsOnStartup() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Startup is finished... now processing internal Queue size of: {} in a new Thread", unprocessedEventsQueue.size());
                processAllUnprocessedEventsEvent(unprocessedEventsQueue);
                if (nifiStartupConnectionError.get()) {
                    log.info("Startup processing received Nifi Connection issues. Events will be resync'd once nifi is back up.  Please ensure NIFI is up and running ");
                } else {
                    log.info("Startup processing is finished continue on with JMS processing ");
                    canProcessJmsMessages.set(true);
                }
            }
        });
        thread.start();
    }

    private void processStartupEventsOnNifiConnectionError() {
        if (!processingConnectionErrorStartupJobs.get()) {
            processingConnectionErrorStartupJobs.set(true);
            provenanceEventStartupListener.processEventsFromStartTime();
            processingConnectionErrorStartupJobs.set(false);
        }
    }


    @PostConstruct
    public void init() {
        provenanceEventStartupListener.subscribe(this);
    }

    @JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY,
                 subscription = Subscriptions.FEED_MANAGER_NIFI_PROVENANCE)
    public void receiveTopic(ProvenanceEventDTO message) {
        Long eventId = message.getEventId();
        log.info("Received ProvenanceEvent with Nifi Event Id of {}", eventId);


        //if Startup is complete and we are not in process of syncronizing Jobs on startup of Ops Manager then process the message
        if (canProcessJmsMessages.get() && !processingConnectionErrorStartupJobs.get() && !nifiStartupConnectionError.get()) {
            processAllUnprocessedEventsEvent(unprocessedEventsQueue);
            ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO(eventId, message);
            try {
                provenanceEventListener.receiveEvent(dto);
            } catch (NifiConnectionException e) {
                canProcessJmsMessages.set(false);
                startNifiConnectionTimer();
                log.error("Nifi Connection Error while processing JMS Event.... Adding Event back to JMS QUEUE for processing");
                erroredEventsQueue.add(message);
            }
        } else {
            //wait and hold until
            log.info("JMS unable to process... holding event {} in internal queue", message.getEventId());
            unprocessedEventsQueue.add(message);

            // unprocessedEvents.add(message);
        }
    }


    private void processAllUnprocessedEventsEvent(ConcurrentLinkedQueue<ProvenanceEventDTO> queue) throws NifiConnectionException {
        ProvenanceEventDTO event = null;

        while ((event = queue.poll()) != null) {
            Long eventId = event.getEventId();
            ProvenanceEventRecordDTO dto = new ProvenanceEventRecordDTO(eventId, event);
            log.info("process event in internal queue {} ", dto);
            try {
                provenanceEventListener.receiveEvent(dto);
            } catch (Exception e) {
                log.error("ERROR PROCESSING EVENT (Nifi Processor Id: {} ) for job that was running prior to Pipeline Controller going down. {}. {} ", dto.getComponentId(), e.getMessage(),
                          e.getStackTrace());
                if (e instanceof NifiConnectionException) {
                    canProcessJmsMessages.set(false);
                }
                if (e instanceof RuntimeException) {
                    throw e;
                }

            }
        }
    }


    public void setProvenanceEventListener(ProvenanceEventListener provenanceEventListener) {
        this.provenanceEventListener = provenanceEventListener;
    }

    public void setProvenanceEventStartupListener(ProvenanceEventApplicationStartupListener provenanceEventStartupListener) {
        this.provenanceEventStartupListener = provenanceEventStartupListener;
    }


    class ProcessStartupEventsOnNifiConnectionErrorTask extends TimerTask {

        public void run() {
            boolean isConnected = provenanceEventListener.isConnectedToNifi();
            if (isConnected) {
                log.info("Established Nifi Connection.  Cancelling Timer");
                processStartupEventsOnNifiConnectionErrorTimer.cancel();
                processStartupEventsOnNifiConnectionError();
                nifiStartupConnectionError.set(false);
                canProcessJmsMessages.set(true);
            }
        }
    }

    class NifiConnectionErrorTask extends TimerTask {

        public void run() {
            boolean isConnected = provenanceEventListener.isConnectedToNifi();
            if (isConnected) {
                log.info("Established Nifi Connection.  Cancelling Timer");
                nifiConnectionErrorTimer.cancel();
                processAllUnprocessedEventsEvent(erroredEventsQueue);
                canProcessJmsMessages.set(true);
            }
        }
    }


}
