package com.thinkbiganalytics.nifi.provenance;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.GroupedFeedProcessorEventAggregate;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.StatsModel;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiRestConnectionListener;
import com.thinkbiganalytics.nifi.provenance.v2.cache.stats.ProvenanceStatsCalculator;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventActiveMqWriter;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * This will take a ProvenanceEvent, prepare it by building the FlowFile graph of its relationship to the running flow, generate Statistics about the Event and then set it in the DelayQueue for the
 * system to process as either a Batch or a Strem Statistics are calculated by the (ProvenanceStatsCalculator) where the events are grouped by Feed and Processor and then aggregated during a given
 * interval before being sent off to a JMS queue for processing.
 *
 *
 * Created by sr186054 on 8/14/16.
 */
@Component
public class ProvenanceEventAggregator implements NifiRestConnectionListener {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventAggregator.class);

    @Autowired
    private StreamConfiguration configuration;

    /**
     * This is the batch event jms queue
     */
    private BlockingQueue<ProvenanceEventRecordDTO> jmsProcessingQueue;

    /**
     * other queue for recording Failures and job completion regardless of batch or stream Needed for triggering when a feed is complete
     */
    private BlockingQueue<ProvenanceEventRecordDTO> eventsJmsQueue;

    /**
     * Reference to when to send the aggregated events found in the  statsByTime map.
     */
    private DateTime lastCollectionTime = null;

    @Autowired
    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    @Autowired
    ProvenanceFeedLookup provenanceFeedLookup;

    @Autowired
    ProvenanceStatsCalculator statsCalculator;

    @Autowired
    CacheUtil cacheUtil;

    @Autowired
    NifiFlowCache nifiFlowCache;

    /**
     * The Map of Objects that determine if the events for a Feed/processor are stream or batch
     */
    Map<String, GroupedFeedProcessorEventAggregate> eventsToAggregate = new ConcurrentHashMap<>();

    List<ProvenanceEventRecordDTO> eventsPriorToNifiConnection = new LinkedList<>();
    CountDownLatch onNifiConnectedLatch = new CountDownLatch(1);

    private AtomicBoolean isProcessingConnectionEvents = new AtomicBoolean(false);


    @PostConstruct
    private void init() {
        this.nifiFlowCache.subscribeConnectionListener(this);
    }

    //sometimes events come in before their root flow file.  Root Flow files are needed for processing.
    //if the events come in out of order, queue them in this map
    //@see processEarlyChildren method
    Cache<String, ConcurrentLinkedQueue<ProvenanceEventRecordDTO>> jobFlowFileIdEarlyChildrenMap = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

    @Autowired
    public ProvenanceEventAggregator(@Qualifier("streamConfiguration") StreamConfiguration configuration,
                                     @Qualifier("provenanceEventActiveMqWriter") ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter) {
        super();
        this.jmsProcessingQueue = new LinkedBlockingQueue<>();

        this.eventsJmsQueue = new LinkedBlockingQueue<>();
        log.debug("************** NEW ProvenanceEventAggregator for {} and activemq: {} ", configuration, provenanceEventActiveMqWriter);
        this.configuration = configuration;
        this.provenanceEventActiveMqWriter = provenanceEventActiveMqWriter;
        this.lastCollectionTime = DateTime.now();

        Thread t = new Thread(new JmsBatchProvenanceEventFeedConsumer(configuration, provenanceEventActiveMqWriter, this.jmsProcessingQueue));
        t.start();

        Thread t2 = new Thread(new JmsImportantProvenanceEventConsumer(configuration, provenanceEventActiveMqWriter, this.eventsJmsQueue));
        t2.start();

        initCheckAndSendTimer();

    }


    private String mapKey(ProvenanceEventRecordDTO event) {
        return event.getFeedName() + ":" + event.getComponentId();
    }

    private String mapKey(ProvenanceEventStats stats) {
        return stats.getFeedName() + ":" + stats.getProcessorId();
    }

    private void processEarlyChildren(String jobFlowFileId) {
        //if this is the start of the job the check and determine if there are any children that were initialized for this flowfile before receiving the start event and then reprocess them
        ConcurrentLinkedQueue<ProvenanceEventRecordDTO> queue = jobFlowFileIdEarlyChildrenMap.getIfPresent(jobFlowFileId);
        if (queue != null) {
            ProvenanceEventRecordDTO nextEvent = null;
            while ((nextEvent = queue.poll()) != null) {
                log.debug("Processing early child {} since the root flowfile {} has been processed.", nextEvent, jobFlowFileId);
                prepareAndAdd(nextEvent);
            }
            jobFlowFileIdEarlyChildrenMap.invalidate(jobFlowFileId);
        }
    }

    @Override
    public void onConnected() {
        ProvenanceEventRecordDTO nextEvent = null;
        isProcessingConnectionEvents.set(true);
        if (!eventsPriorToNifiConnection.isEmpty()) {
            log.info("About to process {} events that were stored prior to making the NiFi rest connection ", eventsPriorToNifiConnection.size());
        }
        eventsPriorToNifiConnection.forEach(e -> {
            log.debug("Processing onConnected {}", e);
            process(e);

        });
        eventsPriorToNifiConnection.clear();
        isProcessingConnectionEvents.set(false);
        log.info("Finished processing events prior to nifi rest connection.  Allow for flow to continue processing");
        onNifiConnectedLatch.countDown();

    }

    public void process(ProvenanceEventRecordDTO event) {
        try {
            if (event != null) {
                if (ProvenanceEventUtil.isDropFlowFilesEvent(event)) {
                    log.info("DROPPING FLOW FILES Event: {}", event);
                    return;
                }
                log.info("Process Event {} ", event);
                try {
                    cacheUtil.cacheAndBuildFlowFileGraph(event);

                    //send the event off for stats processing to the threadpool.  order does not matter thus they can be in an number of threads
                    // eventStatisticsExecutor.execute(new StatisticsProvenanceEventConsumer(event));
                    ProvenanceEventStats stats = statsCalculator.calculateStats(event);
                    //if failure detected group and send off to separate queue
                    collectFailureEvents(event);
                    //add to delayed queue for processing
                    aggregateEvent(event, stats);

                    if (event.isStartOfJob()) {
                        processEarlyChildren(event.getJobFlowFileId());
                    }

                    // check to see if the parents should be finished
                    if (event.isEndingFlowFileEvent()) {
                        Set<ProvenanceEventRecordDTO> completedRootFlowFileEvents = completeStatsForParentFlowFiles(event);
                        if (completedRootFlowFileEvents != null) {
                            completedRootFlowFileEvents(completedRootFlowFileEvents);
                        }

                        if (event.isEndOfJob() && event.getFlowFile().getRootFlowFile().isFlowAndRelatedRootFlowFilesComplete()) {
                            //trigger Complete event for event and flowfile
                            collectCompletionEvents(event);
                        }
                    }
                } catch (RootFlowFileNotFoundException e) {
                    //add to a holding bin
                    log.info("Holding on to {} since the root flow file {} has yet to be processed.", event, event.getFlowFileUuid());
                    jobFlowFileIdEarlyChildrenMap.asMap().computeIfAbsent(event.getFlowFileUuid(), (flowFileId) -> new ConcurrentLinkedQueue<ProvenanceEventRecordDTO>()).add(event);

                }
            }
        } catch (Exception e) {
            log.error("ERROR PROCESSING EVENT! {}.  ERROR: {} ", event, e.getMessage(), e);
        }
    }

    /**
     * - Create/update the flowfile graph for this new event - Calculate statistics on the event and add them to the aggregated set to be processed - Add the event to the DelayQueue for further
     * processing as a Batch or Stream
     */
    public void prepareAndAdd(ProvenanceEventRecordDTO event) {

        if (event != null) {

            if (!nifiFlowCache.isConnected() && !isProcessingConnectionEvents.get()) {
                eventsPriorToNifiConnection.add(event);
                log.debug("Adding {} prior to nifi connection size: {}", event, eventsPriorToNifiConnection.size());
                return;
            } else {
                //if(isProcessingConnectionEvents.get() == true) {
                //wait
                if (onNifiConnectedLatch.getCount() == 1) {
                    log.debug("Awaiting on Nifi Connection to finish processing event {} ", event);
                }
                try {
                    onNifiConnectedLatch.await();
                } catch (Exception e) {

                }
                // }
            }

            process(event);
        }
    }

    private String eventsToString(Collection<ProvenanceEventRecordDTO> events) {
        return StringUtils.join(events.stream().map(e -> e.getEventId()).collect(Collectors.toList()), ",");
    }

    /**
     * get list of EventStats marking the flowfile as complete for the direct parent flowfiles if the child is complete and the parent is complete
     */
    public Set<ProvenanceEventRecordDTO> completeStatsForParentFlowFiles(ProvenanceEventRecordDTO event) {

        ActiveFlowFile rootFlowFile = event.getFlowFile().getRootFlowFile();
        //  log.info("try to complete parents for {}, root: {},  parents: {} ", event.getFlowFile().getId(), rootFlowFile.getId(), event.getFlowFile().getParents().size());

        if (event.isEndingFlowFileEvent() && event.getFlowFile().hasParents()) {
            log.debug("Attempt to complete all {} parent Job flow files that are complete", event.getFlowFile().getParents().size());
            List<ProvenanceEventStats> list = new ArrayList<>();
            Set<ProvenanceEventRecordDTO> eventList = new HashSet<>();
            event.getFlowFile().getParents().stream().filter(parent -> parent.isCurrentFlowFileComplete()).forEach(parent -> {
                ProvenanceEventRecordDTO lastFlowFileEvent = parent.getLastEvent();
                ProvenanceEventStats stats = StatsModel.newJobCompletionProvenanceEventStats(event.getFeedName(), lastFlowFileEvent);
                if (stats != null) {
                    list.add(stats);
                    eventList.add(lastFlowFileEvent);

                }
            });
            if (!list.isEmpty()) {
                statsCalculator.addStats(list);
            }
            return eventList;
        }
        return null;

    }

    private void completedRootFlowFileEvents(Set<ProvenanceEventRecordDTO> completedEvents) {
        for (ProvenanceEventRecordDTO event : completedEvents) {

            if (event != null) {
                eventsToAggregate.computeIfAbsent(mapKey(event), mapKey -> new GroupedFeedProcessorEventAggregate(event.getFeedName(),
                                                                                                                  event
                                                                                                                      .getComponentId(), configuration.getMaxTimeBetweenEventsMillis(),
                                                                                                                  configuration.getNumberOfEventsToConsiderAStream())).addRootFlowFileCompletionEvent(
                    event);
            }


        }
    }

    /**
     * send off all failures to ops mgr for recording
     * @param event
     */
    public void collectFailureEvents(ProvenanceEventRecordDTO event) {
        Set<ProvenanceEventRecordDTO> failedEvents = provenanceFeedLookup.getFailureEvents(event);
        if (failedEvents != null && !failedEvents.isEmpty()) {
            failedEvents.forEach(failedEvent ->
                                 {
                                     if (!failedEvent.isFailure()) {
                                         failedEvent.setIsFailure(true);
                                         failedEvent.getFlowFile().getRootFlowFile().addFailedEvent(failedEvent);
                                         eventsJmsQueue.offer(failedEvent);
                                         ProvenanceEventStats failedEventStats = provenanceFeedLookup.failureEventStats(failedEvent);
                                         statsCalculator.addFailureStats(failedEventStats);
                                     }
                                 });
        }
    }


    /**
     * Send off all final completion job events to ops manager for recording
     * @param event
     */
    public void collectCompletionEvents(ProvenanceEventRecordDTO event) {
        if (event.isEndOfJob()) {
            if (event.getFlowFile() != null && event.getFlowFile().getRootFlowFile() != null) {
                event.setIsBatchJob(event.getFlowFile().getRootFlowFile().isBatch());
            }

            //  if(event.getFlowFile().getRootFlowFile().isFlowComplete() && event.getFlowFile().getRootFlowFile().areRelatedRootFlowFilesComplete()) {
            event.setIsFinalJobEvent(true);
            if (event.getFlowFile().getRootFlowFile().hasAnyFailures()) {
                event.setIsFailure(true);
                event.setHasFailedEvents(true);
            }
            //mark flow as able to be expired from cache
            // }
            log.info("Sending Final Flowfile completion event for event: {} isFailure: {} ", event, event.isFailure());
            eventsJmsQueue.offer(event);
        }
    }


    /**
     * Stats are grouped by Feed and Processor
     */
    private void aggregateEvent(ProvenanceEventRecordDTO event, ProvenanceEventStats stats) {
        //if now is after the next time to send update the nextTime to send
        //   checkAndProcess();
        if (event != null) {
            eventsToAggregate.computeIfAbsent(mapKey(event), mapKey -> new GroupedFeedProcessorEventAggregate(event.getFeedName(),
                                                                                                              event
                                                                                                                  .getComponentId(), configuration.getMaxTimeBetweenEventsMillis(),
                                                                                                              configuration.getNumberOfEventsToConsiderAStream())).add(
                stats, event);
        }
    }


    private void checkAndProcess() {
        //update the collection time

        List<ProvenanceEventRecordDTO> eventsSentToJms = eventsToAggregate.values().stream().flatMap(feedProcessorEventAggregate -> {
            return feedProcessorEventAggregate.collectEventsToBeSentToJmsQueue().stream();
        }).collect(Collectors.toList());
        log.debug("collecting {} events from {} - {} ", eventsSentToJms.size(), lastCollectionTime, DateTime.now());
        jmsProcessingQueue.addAll(eventsSentToJms);
        lastCollectionTime = DateTime.now();
    }


    /**
     * Start a timer to run and get any leftover events and send to JMS This is where the events are not calculated because there is a long delay in provenacne events and they are still waiting in the
     * caches
     */
    private void initCheckAndSendTimer() {
        long millis = this.configuration.getProcessDelay();
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            checkAndProcess();
        }, millis, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventAggregator{");
        sb.append("configuration=").append(configuration);
        sb.append(", provenanceEventActiveMqWriter=").append(provenanceEventActiveMqWriter);
        sb.append(", lastCollectionTime=").append(lastCollectionTime);
        sb.append('}');
        return sb.toString();
    }
}
