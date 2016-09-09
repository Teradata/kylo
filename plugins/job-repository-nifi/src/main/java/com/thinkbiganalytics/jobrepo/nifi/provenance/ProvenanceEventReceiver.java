/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.config.OperationalMetadataAccess;
import com.thinkbiganalytics.jobrepo.jpa.NifiEvent;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventProvider;
import com.thinkbiganalytics.jobrepo.jpa.NifiJobExecutionProvider;
import com.thinkbiganalytics.jobrepo.jpa.NifiStepExecution;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;

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
import javax.inject.Inject;

/**
 * Created by sr186054 on 3/3/16.
 */


/**
 * JMS Listener for NIFI Provenance Events.
 */
@Component
public class ProvenanceEventReceiver implements ProvenanceEventJobExecutionStartupListener {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiver.class);

    private ConcurrentLinkedQueue<ProvenanceEventRecordDTO> unprocessedEventsQueue = new ConcurrentLinkedQueue<>();


    private ConcurrentLinkedQueue<ProvenanceEventRecordDTO> erroredEventsQueue = new ConcurrentLinkedQueue<>();


    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    @Autowired
    private ProvenanceEventApplicationStartupListener provenanceEventStartupListener;


    @Autowired
    private NifiEventProvider nifiEventProvider;

    @Autowired
    private NifiJobExecutionProvider nifiJobExecutionProvider;

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;




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


    @JmsListener(destination = Queues.FEED_MANAGER_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveEvents(ProvenanceEventRecordDTOHolder events) {
        //Group by Job flow file id and then multithread it?

        events.getEvents().stream().sorted(ProvenanceEventUtil.provenanceEventRecordDTOComparator()).forEach(dto -> receiveEvent(dto));
    }


    public void receiveEvent(ProvenanceEventRecordDTO dto) {
        Long eventId = dto.getEventId();
        log.info("Received ProvenanceEvent {}.  is end of Job: {}.  is ending flowfile:{}", dto, dto.isEndOfJob(), dto.isEndingFlowFileEvent());

        NifiStepExecution stepExecution = operationalMetadataAccess.commit(() -> {
            NifiEvent e = nifiEventProvider.create(dto);
            return nifiJobExecutionProvider.save(dto);
        });
        //if Startup is complete and we are not in process of syncronizing Jobs on startup of Ops Manager then process the message
        if (canProcessJmsMessages.get() && !processingConnectionErrorStartupJobs.get() && !nifiStartupConnectionError.get()) {
            processAllUnprocessedEventsEvent(unprocessedEventsQueue);
            try {
                //    provenanceEventListener.receiveEvent(dto);
            } catch (NifiConnectionException e) {
                canProcessJmsMessages.set(false);
                startNifiConnectionTimer();
                log.error("Nifi Connection Error while processing JMS Event.... Adding Event back to JMS QUEUE for processing");
                erroredEventsQueue.add(dto);
            }
        } else {
            //wait and hold until
            log.info("JMS unable to process... holding event {} in internal queue", dto.getEventId());
            unprocessedEventsQueue.add(dto);

            // unprocessedEvents.add(message);
        }
    }


    private void processAllUnprocessedEventsEvent(ConcurrentLinkedQueue<ProvenanceEventRecordDTO> queue) throws NifiConnectionException {
        ProvenanceEventRecordDTO event = null;

        while ((event = queue.poll()) != null) {
            Long eventId = event.getEventId();
            log.info("process event in internal queue {} ", event);
            try {
                //      provenanceEventListener.receiveEvent(event);
            } catch (Exception e) {
                log.error("ERROR PROCESSING EVENT (Nifi Processor Id: {} ) for job that was running prior to Pipeline Controller going down. {}. {} ", event.getComponentId(), e.getMessage(),
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
