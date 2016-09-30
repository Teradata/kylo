package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.DateTimeUtil;
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
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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


    private AtomicLong activeJobExecutionThreads = new AtomicLong(0);

    @Value("${thinkbig.opsmgr.provenance.jobexecution.maxthreads:30}")
    private Integer maxThreads = 30;

    /**
     * Service to run threads
     */
    ExecutorService executorService =
        new ThreadPoolExecutor(
            maxThreads, // core thread pool size
            maxThreads, // maximum thread pool size
            10, // time to wait before resizing pool
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(maxThreads, true),
            new ThreadPoolExecutor.CallerRunsPolicy());


    /**
     * Temporary cache of completed events in to check against to ensure we trigger the same event twice
     */
    Cache<String, String> completedJobEvents = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();


    /**
     * small cache to make sure we dont process any event more than once. The refresh/expire interval for this cache can be small since if a event comes in moore than once it would happen within a
     * second
     */
    Cache<String, DateTime> processedEvents = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();

    /**
     * A Map that will be used in the Thread poll grouped by Job Flow file Id
     */
    private Map<String, LinkedBlockingQueue<ProvenanceEventRecordDTO>> jobEventMap = new ConcurrentHashMap<>();

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
     * Process events coming from NiFi that are related to "BATCH Jobs. These will result in new JOB/STEPS to be created in Ops Manager with full provenance data
     */
    @JmsListener(destination = Queues.FEED_MANAGER_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveEvents(ProvenanceEventRecordDTOHolder events) {
        log.info("About to process {} events from the {} queue ", events.getEvents().size(), Queues.FEED_MANAGER_QUEUE);
        addEventsToQueue(events, Queues.FEED_MANAGER_QUEUE);
    }

    /**
     * Process Failure Events or Ending Job Events
     */
    @JmsListener(destination = Queues.PROVENANCE_EVENT_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(ProvenanceEventRecordDTOHolder events) {
        log.info("About to process {} events from the {} queue ", events.getEvents().size(), Queues.PROVENANCE_EVENT_QUEUE);
        addEventsToQueue(events, Queues.PROVENANCE_EVENT_QUEUE);
    }


    private void addEventsToQueue(ProvenanceEventRecordDTOHolder events, String sourceJmsQueue) {
        Set<String> newJobs = new HashSet<>();

        events.getEvents().stream().sorted(ProvenanceEventUtil.provenanceEventRecordDTOComparator()).filter(e -> isRegisteredWithFeedManager(e)).forEach(e -> {
            if (e.isBatchJob() && isProcessBatchEvent(e, sourceJmsQueue)) {
                try {
                    // eventKeys.add(eventKey(e));
                    log.info("Process event {} ", e);

                    LinkedBlockingQueue queue = new LinkedBlockingQueue<ProvenanceEventRecordDTO>();

                    BlockingQueue q = jobEventMap.putIfAbsent(e.getJobFlowFileId(), queue);
                    if (q == null) {
                        q = queue;
                        //we just added it so start the thread
                        log.info("Starting new Job queue ", e);
                        activeJobExecutionThreads.incrementAndGet();
                        executorService.submit(new ProcessBatchJobTask(e.getJobFlowFileId(), q));

                    }
                    q.put(e);
                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            } else {
                //we dont care about order for these events... just process in any order and save
                receiveEvent(e);
            }

        });
    }

    /**
     * persist the event to the NIFI_EVENT table and Notify if its a final job
     *
     * @return the persisted NiFi Event
     */
    public NifiEvent receiveEvent(ProvenanceEventRecordDTO event) {
        NifiEvent nifiEvent = operationalMetadataAccess.commit(() -> nifiEventProvider.create(event));
        if (!event.isBatchJob() && event.isFinalJobEvent()) {
            //if its a Stream notify its complete
            notifyJobFinished(event);
        }
        return nifiEvent;
    }


    /**
     * Process events for a given Root Flow file (this indicates a JobExecution in Ops Manager)
     * This task will block on the queue until the Job has received the "ending event" from NiFi
     *
     */
    private class ProcessBatchJobTask implements Runnable {

        private String jobId;
        private BlockingQueue<ProvenanceEventRecordDTO> queue;
        private List<NifiEvent> eventsProcessed;
        private boolean isActive = true;

        public ProcessBatchJobTask(String jobId, BlockingQueue<ProvenanceEventRecordDTO> queue) {
            this.jobId = jobId;
            this.queue = queue;
            this.eventsProcessed = new ArrayList<>();
            log.debug("processing job {} in new thread ", jobId);

        }

        @Override
        public void run() {

                try {
                     while (isActive) {
                         {
                             final ProvenanceEventRecordDTO event = queue.take();
                             NifiEvent nifiEvent = operationalMetadataAccess.commit(() -> receiveBatchEvent(event));
                             if (nifiEvent != null) {
                                 eventsProcessed.add(nifiEvent);
                             }
                             if (event.isFinalJobEvent()) {
                                 isActive = false;
                                 notifyJobFinished(event);

                             }
                         }
                        }
                        clearJobFromQueue(jobId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log.error("Error processing {} ", ex);
                    clearJobFromQueue(jobId);
                }
        }

        /**
         * Process this record and record the Job Obs
         * @param event
         * @return
         */
        private NifiEvent receiveBatchEvent(ProvenanceEventRecordDTO event) {
            NifiEvent nifiEvent = null;
            log.debug("Received ProvenanceEvent {}.  is end of Job: {}.  is ending flowfile:{}, isBatch: {}", event, event.isEndOfJob(), event.isEndingFlowFileEvent(), event.isBatchJob());
            nifiEvent = nifiEventProvider.create(event);
            if (event.isBatchJob()) {
                BatchJobExecution job = nifiJobExecutionProvider.save(event, nifiEvent);
                if (job == null) {
                    log.error(" Detected a Batch event, but could not find related Job record. for event: {}  is end of Job: {}.  is ending flowfile:{}, isBatch: {}", event, event.isEndOfJob(),
                              event.isEndingFlowFileEvent(), event.isBatchJob());
                }
            }

            return nifiEvent;
        }

    }

    private void clearJobFromQueue(final String jobId) {

            if(jobEventMap.containsKey(jobId) && jobEventMap.get(jobId).isEmpty()) {
                log.debug("clearning JobQueue {} ", jobId);
                jobEventMap.remove(jobId);
                activeJobExecutionThreads.decrementAndGet();
            } else {
                if (jobEventMap.containsKey(jobId)) {
                    log.debug("Dont clear. {} ", jobId);
                }
            }

    }


    /**
     * Enusre this incoming event didnt get processed already
     * @param event
     * @return
     */
    private boolean isProcessBatchEvent(ProvenanceEventRecordDTO event, String sourceJmsQueue) {
        //Skip batch processing for the events coming in as batch events from the Provenance Event Queue.
        // this will be processed in order when the events come in.
        if (Queues.PROVENANCE_EVENT_QUEUE.equalsIgnoreCase(sourceJmsQueue)) {
            //   log.info("Skip processing event {} from Jms Queue: {}. It will be processed later in order.", event,Queues.PROVENANCE_EVENT_QUEUE);
            return false;
        }

        String processingCheckMapKey = event.getEventId() + "_" + event.getFlowFileUuid();

        DateTime timeAddedToQueue = processedEvents.getIfPresent(processingCheckMapKey);
        if (timeAddedToQueue == null) {
            processedEvents.put(processingCheckMapKey, DateTimeUtil.getNowUTCTime());
            return true;
        } else {
            log.debug("Skip processing for id: {}, event {}  at {} since it has already been added to a queue for processing at {} ", processingCheckMapKey, event, DateTimeUtil.getNowUTCTime(),
                      timeAddedToQueue);
            return false;
        }
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
                log.debug("Not processiong operational metadata for feed {} , event {} because it is not registered in feed manager ", feedName, event);
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
        log.info("FAILED JOB for Event {} ", event);
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
        log.info("Success JOB for Event {} ", event);
        this.eventService.notify(new FeedOperationStatusEvent(event.getFeedName(), null, state, "Job Succeeded for feed: "+event.getFeedName()));
    }
}
