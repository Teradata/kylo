package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventProvider;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * JMS Listener for NiFi Provenance Events.
 */
@Component
public class ProvenanceEventReceiver {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventReceiver.class);

    @Autowired
    private NifiEventProvider nifiEventProvider;

    @Autowired
    private BatchJobExecutionProvider nifiJobExecutionProvider;

    @Autowired
    private NifiRestClient nifiRestClient;

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    private MetadataEventService eventService;




    /**
     * Temporary cache of completed events in to check against to ensure we trigger the same event twice
     */
    Cache<String, String> completedJobEvents = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

    /**
     * Cache of the Ops Manager Feed Object to ensure that we only process and create Job Executions for feeds that have been registered in Feed Manager
     */
    LoadingCache<String, OpsManagerFeed> opsManagerFeedCache = null;

    /**
     * Empty feed object for Loading Cache
     */
    static OpsManagerFeed NULL_FEED = new OpsManagerFeed() {
        @Override
        public ID getId() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    };


    public ProvenanceEventReceiver(){
        // create the loading Cache to get the Feed Manager Feeds.  If its not in the cache, query the JCR store for the Feed object otherwise return the NULL_FEED object
        opsManagerFeedCache = CacheBuilder.newBuilder().build(new CacheLoader<String, OpsManagerFeed>() {
                                                                  @Override
                                                                  public OpsManagerFeed load(String feedName) throws Exception {
                                                                      OpsManagerFeed feed =null;
                                                                      try {
                                                                          feed = operationalMetadataAccess.commit(() -> {
                                                                                return opsManagerFeedProvider.findByName(feedName);
                                                                            });
                                                                      }catch (Exception e){

                                                                      }
                                                                      return feed == null ? NULL_FEED : feed;
                                                                  }

                                                              }
        );
    }


    /**
     * Unique key for the Event in relation to the Job
     * @param event
     * @return
     */
    private String triggeredEventsKey(ProvenanceEventRecordDTO event) {
        return event.getJobFlowFileId() + "_" + event.getEventId();
    }


    /**
     * Process the Events from Nifi
     * If its a batch job, write the records to Ops manager
     * if its a stream just write to the Nifi_event table
     * When either are marked as the last event Notify the eventbus for the trigger feed mechanism to work.
     * @param events
     */
    @JmsListener(destination = Queues.FEED_MANAGER_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY, concurrency = "10-50")
    public void receiveEvents(ProvenanceEventRecordDTOHolder events) {
        log.info("About to process {} events from the {} queue ", events.getEvents().size(), Queues.FEED_MANAGER_QUEUE);
        events.getEvents().stream().filter(e -> isRegisteredWithFeedManager(e)).forEach(event -> {

            if (event.isBatchJob()) {
                //ensure the job is there
                BatchJobExecution jobExecution = operationalMetadataAccess.commit(() -> nifiJobExecutionProvider.getOrCreateJobExecution(event));
                NifiEvent nifiEvent = operationalMetadataAccess.commit(() -> receiveBatchEvent(jobExecution, event));
            } else {
                NifiEvent nifiEvent = operationalMetadataAccess.commit(() -> nifiEventProvider.create(event));
            }
            if (event.isFinalJobEvent()) {
                notifyJobFinished(event);
            }
        });
    }


    /**
     * Process this record and record the Job Obs
     */
    private NifiEvent receiveBatchEvent(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {
        NifiEvent nifiEvent = null;
        log.debug("Received ProvenanceEvent {}.  is end of Job: {}.  is ending flowfile:{}, isBatch: {}", event, event.isEndOfJob(), event.isEndingFlowFileEvent(), event.isBatchJob());
        nifiEvent = nifiEventProvider.create(event);
        if (event.isBatchJob()) {
            BatchJobExecution job = nifiJobExecutionProvider.save(jobExecution, event, nifiEvent);
            if (job == null) {
                log.error(" Detected a Batch event, but could not find related Job record. for event: {}  is end of Job: {}.  is ending flowfile:{}, isBatch: {}", event, event.isEndOfJob(),
                          event.isEndingFlowFileEvent(), event.isBatchJob());
            }
        }

        return nifiEvent;
    }

    /**
     * Check to see if the event has a relationship to Feed Manager
     * In cases where a user is experimenting in NiFi and not using Feed Manager the event would not be registered
     * @param event
     * @return
     */
    private boolean isRegisteredWithFeedManager(ProvenanceEventRecordDTO event) {

        String feedName = event.getFeedName();
        if(StringUtils.isNotBlank(feedName)) {
            OpsManagerFeed feed = opsManagerFeedCache.getUnchecked(feedName);
            if(feed == null || NULL_FEED.equals(feed)) {
                log.debug("Not processing operational metadata for feed {} , event {} because it is not registered in feed manager ", feedName, event);
                opsManagerFeedCache.invalidate(feedName);
              return false;
            } else {
                return true;
            }
        }
        return false;
    }


    /**
     * Notify that the Job is complete either as a successful job or failed Job
     * @param event
     */
    private void notifyJobFinished(ProvenanceEventRecordDTO event) {
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
    }

    /**
     * Triggered for both Batch and Streaming Feed Jobs when the Job and any related Jobs (as a result of a Merge of other Jobs are complete but have a failure in the flow<br/> Example: <br/> Job
     * (FlowFile) 1,2,3 are all running<br/> Job 1,2,3 get Merged<br/> Job 1,2 finish<br/> Job 3 finishes <br/>
     *
     * This will fire when Job3 finishes indicating this entire flow is complete<br/>
     */
    private void failedJob(ProvenanceEventRecordDTO event) {
        FeedOperation.State state = FeedOperation.State.FAILURE;
        log.debug("FAILED JOB for Event {} ", event);
        this.eventService.notify(new FeedOperationStatusEvent(event.getFeedName(), null, state, "Failed Job"));
    }

    /**
     * Triggered for both Batch and Streaming Feed Jobs when the Job and any related Jobs (as a result of a Merge of other Jobs are complete<br/> Example: <br/> Job (FlowFile) 1,2,3 are all
     * running<br/> Job 1,2,3 get Merged<br/> Job 1,2 finish<br/> Job 3 finishes <br/>
     *
     * This will fire when Job3 finishes indicating this entire flow is complete<br/>
     */
    private void successfulJob(ProvenanceEventRecordDTO event) {

        FeedOperation.State state = FeedOperation.State.SUCCESS;
        log.debug("Success JOB for Event {} ", event);
        this.eventService.notify(new FeedOperationStatusEvent(event.getFeedName(), null, state, "Job Succeeded for feed: " + event.getFeedName()));
    }
}
