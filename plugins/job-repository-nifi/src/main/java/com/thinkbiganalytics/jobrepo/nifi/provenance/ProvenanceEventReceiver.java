/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.config.OperationalMetadataAccess;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventProvider;
import com.thinkbiganalytics.jobrepo.jpa.NifiJobExecutionProvider;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiEvent;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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


    @Inject
    private MetadataEventService eventService;


    Cache<String, String> completedJobEvents = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

    private String triggeredEventsKey(ProvenanceEventRecordDTO event) {
        return event.getJobFlowFileId() + "_" + event.getEventId();
    }


    public ProvenanceEventReceiver() {

    }


    /**
     * Process events coming from NiFi that are related to "BATCH Jobs. These will result in new JOB/STEPS to be created in Ops Manager with full provenance data
     */
    @JmsListener(destination = Queues.FEED_MANAGER_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveEvents(ProvenanceEventRecordDTOHolder events) {
        addEventsToQueue(events);

    }


    /**
     * Process Failure Events or Ending Job Events
     * @param events
     */
    @JmsListener(destination = Queues.PROVENANCE_EVENT_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(ProvenanceEventRecordDTOHolder events) {
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
                executorService.submit(new ProcessJobEventsTask(jobId));
            }
        }

    }


    private class ProcessJobEventsTask implements Runnable {

        private String jobId;

        public ProcessJobEventsTask(String jobId) {
            this.jobId = jobId;
        }

        @Override
        public void run() {
            operationalMetadataAccess.commit(() -> {
                List<NifiEvent> nifiEvents = new ArrayList<NifiEvent>();
                try {
                    ConcurrentLinkedQueue<ProvenanceEventRecordDTO> queue = jobEventMap.get(jobId);
                    if (queue == null) {
                        jobEventMap.remove(jobId);
                    } else {
                        ProvenanceEventRecordDTO event = null;
                        while ((event = queue.poll()) != null) {
                            nifiEvents.add(receiveEvent(event));
                        }
                        jobEventMap.remove(jobId);
                        //  ((ThreadPoolExecutor)executorService).getQueue()
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log.error("Error processing {} ", ex);
                }
                return nifiEvents;
            });
        }
    }


    public NifiEvent receiveEvent(ProvenanceEventRecordDTO event) {
        log.info("Received ProvenanceEvent {}.  is end of Job: {}.  is ending flowfile:{}", event, event.isEndOfJob(), event.isEndingFlowFileEvent());

            NifiEvent nifiEvent = nifiEventProvider.create(event);
           if(event.isBatchJob()) {
               nifiJobExecutionProvider.save(event, nifiEvent);
           }
        if (event.isFinalJobEvent()) {
            String mapKey = triggeredEventsKey(event);
            String alreadyTriggered = completedJobEvents.getIfPresent(mapKey);
            if (alreadyTriggered == null) {
                completedJobEvents.put(mapKey, mapKey);
                /// TRIGGER JOB COMPLETE!!!
                if (event.isHasFailedEvents()) {
                    failedJob(event);
                } else {
                    successfulJob(event);
                }
            }
        }

        return nifiEvent;


    }


    /**
     * Triggered for both Batch and Streaming Feed Jobs when the Job and any related Jobs (as a result of a Merge of other Jobs are complete but have a failure in the flow<br/>
     * Example: <br/>
     *  Job (FlowFile) 1,2,3 are all running<br/>
     *  Job 1,2,3 get Merged<br/>
     *  Job 1,2 finish<br/>
     *  Job 3 finishes <br/>
     *
     *  This will fire when Job3 finishes indicating this entire flow is complete<br/>
     * @param event
     */
    private void failedJob(ProvenanceEventRecordDTO event) {
        FeedOperation.State state = FeedOperation.State.FAILURE;
        log.info("FAILED JOB for Event {} ", event);
        //this.eventService.notify(new FeedOperationStatusEvent(event.getFeedName(), null, state, "Failed Job"));
    }

    /**
     * Triggered for both Batch and Streaming Feed Jobs when the Job and any related Jobs (as a result of a Merge of other Jobs are complete<br/>
     * Example: <br/>
     *  Job (FlowFile) 1,2,3 are all running<br/>
     *  Job 1,2,3 get Merged<br/>
     *  Job 1,2 finish<br/>
     *  Job 3 finishes <br/>
     *
     *  This will fire when Job3 finishes indicating this entire flow is complete<br/>
     * @param event
     */
    private void successfulJob(ProvenanceEventRecordDTO event) {
        FeedOperation.State state = FeedOperation.State.SUCCESS;
        log.info("Success JOB for Event {} ", event);
        // this.eventService.notify(new FeedOperationStatusEvent(event.getFeedName(), null, state, ""));
    }


}
