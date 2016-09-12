/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.config.OperationalMetadataAccess;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventProvider;
import com.thinkbiganalytics.jobrepo.jpa.NifiJobExecutionProvider;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiEvent;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiStepExecution;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.nifi.web.api.entity.AboutEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Created by sr186054 on 3/3/16.
 */


/**
 * JMS Listener for NIFI Provenance Events.
 */
@Component
public class ProvenanceEventReceiver {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiver.class);

    @Autowired
    private NifiEventProvider nifiEventProvider;

    @Autowired
    private NifiJobExecutionProvider nifiJobExecutionProvider;

    @Autowired
    private NifiRestClient nifiRestClient;

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;


    public ProvenanceEventReceiver() {

    }

    @JmsListener(destination = Queues.FEED_MANAGER_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveEvents(ProvenanceEventRecordDTOHolder events) {
        //Group by Job flow file id and then multithread it?

        //  events.getEvents().stream().sorted(ProvenanceEventUtil.provenanceEventRecordDTOComparator()).forEach(dto -> receiveEvent(dto));

        addEventsToQueue(events);


    }

    int maxThreads = 10;
    ExecutorService executorService =
        new ThreadPoolExecutor(
            maxThreads, // core thread pool size
            maxThreads, // maximum thread pool size
            10, // time to wait before resizing pool
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(maxThreads, true),
            new ThreadPoolExecutor.CallerRunsPolicy());


    private Map<String, ConcurrentLinkedQueue<ProvenanceEventRecordDTO>> jobEventMap = new ConcurrentHashMap<>();


    private void addEventsToQueue(ProvenanceEventRecordDTOHolder events) {
        Set<String> newJobs = new HashSet<>();

        events.getEvents().stream().sorted(ProvenanceEventUtil.provenanceEventRecordDTOComparator()).forEach(e -> {
            if (!jobEventMap.containsKey(e.getJobFlowFileId())) {
                newJobs.add(e.getJobFlowFileId());
            }
            jobEventMap.computeIfAbsent(e.getJobFlowFileId(), (id) -> new ConcurrentLinkedQueue()).add(e);
        });

        if (newJobs != null) {
            log.info("Submitting {} threads to process jobs ", newJobs.size());
            for (String jobId : newJobs) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ConcurrentLinkedQueue<ProvenanceEventRecordDTO> queue = jobEventMap.get(jobId);
                            if (queue == null) {
                                jobEventMap.remove(jobId);
                            } else {
                                ProvenanceEventRecordDTO event = null;
                                while ((event = queue.poll()) != null) {
                                    log.info("Running via thread {}, job: {} ", Thread.currentThread().getName(), jobId);
                                    receiveEvent(event);
                                }
                                jobEventMap.remove(jobId);
                                //  ((ThreadPoolExecutor)executorService).getQueue()
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            log.error("Errot processing {} ", ex);
                        }
                    }
                });
            }
        }

    }


    public void receiveEvent(ProvenanceEventRecordDTO event) {
        log.info("Received ProvenanceEvent {}.  is end of Job: {}.  is ending flowfile:{}", event, event.isEndOfJob(), event.isEndingFlowFileEvent());
        NifiStepExecution stepExecution = operationalMetadataAccess.commit(() -> {
            NifiEvent nifiEvent = nifiEventProvider.create(event);
            return nifiJobExecutionProvider.save(event, nifiEvent);
        });
    }


    public boolean isConnectedToNifi() {
        try {
            AboutEntity aboutEntity = nifiRestClient.getNifiVersion();
            log.info("Successful connection to NIFI");
            return true;
        } catch (Exception e) {
            log.info("Not connected to NIFI");
            return false;
        }
    }


}
