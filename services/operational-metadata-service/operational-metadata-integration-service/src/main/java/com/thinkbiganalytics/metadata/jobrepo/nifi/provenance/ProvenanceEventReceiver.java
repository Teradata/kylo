package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.jms.JmsConstants;
import com.thinkbiganalytics.jms.Queues;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.FailedStepExecutionListener;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventProvider;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;

import org.apache.nifi.web.api.dto.BulletinDTO;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * JMS Listener for NiFi Provenance Events.
 */
public class ProvenanceEventReceiver implements FailedStepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiver.class);

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;
    @Inject
    NifiBulletinExceptionExtractor nifiBulletinExceptionExtractor;
    /**
     * Temporary cache of completed events in to check against to ensure we trigger the same event twice
     */
    Cache<String, String> completedJobEvents = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();


    @Value("${kylo.ops.mgr.query.nifi.bulletins:false}")
    private boolean queryForNiFiBulletins;
    @Inject
    private NifiEventProvider nifiEventProvider;
    @Inject
    private BatchJobExecutionProvider batchJobExecutionProvider;
    @Inject
    private BatchStepExecutionProvider batchStepExecutionProvider;
    @Inject
    private LegacyNifiRestClient nifiRestClient;
    @Inject
    private MetadataAccess metadataAccess;
    @Inject
    private MetadataEventService eventService;

    @Inject
    private ProvenanceEventFeedUtil provenanceEventFeedUtil;


    /**
     * The amount of retry attempts the system will do if it gets a LockAcquisitionException
     * MySQL may fail to lock the table when performing inserts into the database resulting in a deadlock exception.
     * When processing each event the LockAcquisitionException is caught and a retry attempt is done, retrying to process the event this amount of times before giving up.
     */
    private int lockAcquisitionRetryAmount = 4;


    /**
     * default constructor creates the feed cache
     */
    public ProvenanceEventReceiver() {

    }

    @PostConstruct
    private void init() {
        batchStepExecutionProvider.subscribeToFailedSteps(this);
    }


    /**
     * Ensure the cache and NiFi are up, or if not ensure the data exists in the NiFi cache to be processed
     *
     * @param events the events.
     */
    public boolean readyToProcess(ProvenanceEventRecordDTOHolder events) {
        return provenanceEventFeedUtil.isNifiFlowCacheAvailable() || (!provenanceEventFeedUtil.isNifiFlowCacheAvailable() && events.getEvents().stream()
            .allMatch(event -> provenanceEventFeedUtil.validateNiFiFeedInformation(event)));
    }


    /**
     * Unique key for the Event in relation to the Job
     *
     * @param event a provenance event
     * @return a unique key representing the event
     */
    private String triggeredEventsKey(ProvenanceEventRecordDTO event) {
        return event.getJobFlowFileId() + "_" + event.getEventId();
    }


    /**
     * Process the Events from Nifi
     * If it is a batch job, write the records to Ops manager.
     * if it is a stream just write to the Nifi_event table.
     * When either are marked as the last event Notify the event bus for the trigger feed mechanism to work.
     *
     * @param events The events obtained from JMS
     */
    @JmsListener(destination = Queues.FEED_MANAGER_QUEUE, containerFactory = JmsConstants.JMS_CONTAINER_FACTORY, concurrency = "3-10")
    public void receiveEvents(ProvenanceEventRecordDTOHolder events) {
        log.info("About to process batch: {},  {} events from the {} queue ", events.getBatchId(), events.getEvents().size(), Queues.FEED_MANAGER_QUEUE);
        if (readyToProcess(events)) {
            events.getEvents().stream().map(event -> provenanceEventFeedUtil.enrichEventWithFeedInformation(event))
                .filter(event -> provenanceEventFeedUtil.isRegisteredWithFeedManager(event))
                .forEach(event -> processEvent(event, 0));
        } else {
            log.info("NiFi is not up yet. Sending batch {} back to JMS for later dequeue ", events.getBatchId());
            throw new JmsProcessingException("Unable to process events.  NiFi is either not up, or there is an error trying to populate the Kylo NiFi Flow Cache. ");
        }
    }

    /**
     * process the event and persist it along with creating the Job and Step.  If there is a lock error it will retry until it hits the {@link #lockAcquisitionRetryAmount}
     *
     * @param event        a provenance event
     * @param retryAttempt the retry number.  If there is a lock error it will retry until it hits the {@link #lockAcquisitionRetryAmount}
     */
    private void processEvent(ProvenanceEventRecordDTO event, int retryAttempt) {
        try {

            log.debug("Process {} for flowfile: {} and processorId: {} ", event, event.getJobFlowFileId(), event.getFirstEventProcessorId());
            //ensure the job is there
            BatchJobExecution jobExecution = metadataAccess.commit(() -> batchJobExecutionProvider.getOrCreateJobExecution(event, provenanceEventFeedUtil.getFeed(event)),
                                                                   MetadataAccess.SERVICE);

            if (jobExecution != null && !event.isStream()) {
                metadataAccess.commit(() -> receiveBatchEvent(jobExecution, event),
                                      MetadataAccess.SERVICE);
            }
            if (jobExecution != null && event.isFinalJobEvent()) {
                notifyJobFinished(jobExecution, event);
            }
        } catch (LockAcquisitionException lae) {
            //safeguard against LockAcquisitionException if MySQL has a problem locking the table during its processing of the Event.

            if (retryAttempt < lockAcquisitionRetryAmount) {
                retryAttempt++;
                log.error("LockAcquisitionException found trying to process Event: {} .  Retry attempt # {} ", event, retryAttempt, lae);
                //wait and re attempt
                try {
                    Thread.sleep(300L);
                } catch (InterruptedException var10) {

                }
                processEvent(event, retryAttempt);
            } else {
                log.error("LockAcquisitionException found.  Unsuccessful after retrying {} times.  This event {} will not be processed. ", retryAttempt, event, lae);
            }
        } catch (Exception e) {
            log.error("Error processing Event ", event, e);
        }

    }


    /**
     * Process this record and record the Job and steps
     *
     * @param jobExecution the job execution
     * @param event        a provenance event
     * @return a persisted nifi event object
     */
    private void receiveBatchEvent(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {
        //   NifiEvent nifiEvent = null;
        log.debug("Received ProvenanceEvent {}.  is ending flowfile:{}", event, event.isEndingFlowFileEvent());
        //query it again
        jobExecution = batchJobExecutionProvider.findByJobExecutionId(jobExecution.getJobExecutionId());
        BatchJobExecution job = batchJobExecutionProvider.save(jobExecution, event);
        if (job == null) {
            log.error(" Detected a Batch event, but could not find related Job record. for event: {}  is end of Job: {}.  is ending flowfile:{}, ", event, event.isEndingFlowFileEvent(),
                      event.isEndingFlowFileEvent());
        }
    }


    /**
     * Notify that the Job is complete either as a successful job or failed Job
     *
     * @param event a provenance event
     */
    private void notifyJobFinished(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {
        if (event.isFinalJobEvent()) {
            String mapKey = triggeredEventsKey(event);
            String alreadyTriggered = completedJobEvents.getIfPresent(mapKey);
            if (alreadyTriggered == null) {
                completedJobEvents.put(mapKey, mapKey);
                /// TRIGGER JOB COMPLETE!!!
                metadataAccess.commit(() -> {
                    BatchJobExecution batchJobExecution = batchJobExecutionProvider.findByJobExecutionId(jobExecution.getJobExecutionId());
                    if (batchJobExecution.isFailed()) {
                        failedJob(batchJobExecution, event);
                    } else {
                        successfulJob(batchJobExecution, event);
                    }

                }, MetadataAccess.SERVICE);
            }
        }
    }

    /**
     * Triggered for both Batch and Streaming Feed Jobs when the Job and any related Jobs (as a result of a Merge of other Jobs are complete but have a failure in the flow<br/> Example: <br/> Job
     * (FlowFile) 1,2,3 are all running<br/> Job 1,2,3 get Merged<br/> Job 1,2 finish<br/> Job 3 finishes <br/>
     *
     * This will fire when Job3 finishes indicating this entire flow is complete<br/>
     *
     * @param jobExecution the job
     * @param event        a provenance event
     */
    private void failedJob(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {
        if (queryForNiFiBulletins && !event.isStream()) {
            queryForNiFiErrorBulletins(event);
        }
        log.debug("Failed JOB for Event {} ", event);
        batchJobExecutionProvider.notifyFailure(jobExecution, event.getFeedName(), event.isStream(), null);
    }

    /**
     * Triggered for both Batch and Streaming Feed Jobs when the Job and any related Jobs (as a result of a Merge of other Jobs are complete<br/> Example: <br/> Job (FlowFile) 1,2,3 are all
     * running<br/> Job 1,2,3 get Merged<br/> Job 1,2 finish<br/> Job 3 finishes <br/>
     *
     * This will fire when Job3 finishes indicating this entire flow is complete<br/>
     *
     * @param jobExecution the job
     * @param event        a provenance event
     */
    private void successfulJob(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {
        log.debug("Success JOB for Event {} ", event);
        batchJobExecutionProvider.notifySuccess(jobExecution,jobExecution.getJobInstance().getFeed(), null);
    }

    /**
     * Make a REST call to NiFi and query for the NiFi Bulletins that have a flowfile id matching for this job execution and write the bulletin message to the {@link
     * BatchJobExecution#setExitMessage(String)}
     *
     * @param event a provenance event
     */
    private void queryForNiFiErrorBulletins(ProvenanceEventRecordDTO event) {
        try {
            metadataAccess.commit(() -> {
                //query for nifi logs
                List<String> relatedFlowFiles = batchJobExecutionProvider.findRelatedFlowFiles(event.getFlowFileUuid());
                if (relatedFlowFiles == null) {
                    relatedFlowFiles = new ArrayList<>();
                }
                if (relatedFlowFiles.isEmpty()) {
                    relatedFlowFiles.add(event.getFlowFileUuid());
                }
                log.debug("Failed Job {}/{}. Found {} related flow files. ", event.getEventId(), event.getFlowFileUuid(), relatedFlowFiles.size());
                List<BulletinDTO> bulletinDTOS = nifiBulletinExceptionExtractor.getErrorBulletinsForFlowFiles(relatedFlowFiles);
                if (bulletinDTOS != null && !bulletinDTOS.isEmpty()) {
                    //write them back to the job
                    BatchJobExecution jobExecution = batchJobExecutionProvider.findJobExecution(event);
                    if (jobExecution != null) {
                        String msg = jobExecution.getExitMessage() != null ? jobExecution.getExitMessage() + "\n" : "";
                        msg += "NiFi exceptions: \n" + bulletinDTOS.stream().map(bulletinDTO -> bulletinDTO.getMessage()).collect(Collectors.joining("\n"));
                        jobExecution.setExitMessage(msg);
                        this.batchJobExecutionProvider.save(jobExecution);
                    }
                }
            }, MetadataAccess.SERVICE);
        } catch (Exception e) {
            log.error("Unable to query NiFi and save exception bulletins for job failure eventid/flowfile : {} / {}. Exception Message:  {}", event.getEventId(), event.getFlowFileUuid(),
                      e.getMessage(), e);
        }
    }


    /**
     * Fails the step identified by the parameters given
     *
     * @param jobExecution  the job execution
     * @param stepExecution the step execution
     * @param flowFileId    the id of the flow file
     * @param componentId   the id of the component
     */
    @Override
    public void failedStep(BatchJobExecution jobExecution, BatchStepExecution stepExecution, String flowFileId, String componentId) {
        nifiBulletinExceptionExtractor.addErrorMessagesToStep(stepExecution, flowFileId, componentId);
    }


}
