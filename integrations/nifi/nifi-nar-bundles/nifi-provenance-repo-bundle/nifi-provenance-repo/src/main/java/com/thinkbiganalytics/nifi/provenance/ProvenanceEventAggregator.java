package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.GroupedFeedProcessorEventAggregate;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.stats.ProvenanceStatsCalculator;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.util.SpringApplicationContext;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This will take a ProvenanceEvent, prepare it by building the FlowFile graph of its relationship to the running flow, generate Statistics about the Event and then set it in the DelayQueue for the
 * system to process as either a Batch or a Strem Statistics are calculated by the (ProvenanceStatsCalculator) where the events are grouped by Feed and Processor and then aggregated during a given
 * interval before being sent off to a JMS queue for processing.
 *
 *
 * Created by sr186054 on 8/14/16.
 */
@Component
public class ProvenanceEventAggregator {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventAggregator.class);

    @Autowired
    private StreamConfiguration configuration;

    // Creates an instance of blocking queue using the DelayQueue.
    private BlockingQueue<ProvenanceEventRecordDTO> jmsProcessingQueue;


    private BlockingQueue<ProvenanceEventRecordDTO> failedEventsJmsQueue;

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


    Map<String, GroupedFeedProcessorEventAggregate> eventsToAggregate = new ConcurrentHashMap<>();

    @Autowired
    public ProvenanceEventAggregator(@Qualifier("streamConfiguration") StreamConfiguration configuration,
                                     @Qualifier("provenanceEventActiveMqWriter") ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter) {
        super();
        this.jmsProcessingQueue = new LinkedBlockingQueue<>();
        //batchup and send failed events off
        this.failedEventsJmsQueue = new LinkedBlockingQueue<>();
        log.info("************** NEW ProvenanceEventAggregator for {} and activemq: {} ", configuration, provenanceEventActiveMqWriter);
        this.configuration = configuration;
        this.provenanceEventActiveMqWriter = provenanceEventActiveMqWriter;
        this.lastCollectionTime = DateTime.now();

        Thread t = new Thread(new JmsBatchedProvenanceEventFeedConsumer(configuration, provenanceEventActiveMqWriter, this.jmsProcessingQueue));
        t.start();

        Thread t2 = new Thread(new JmsBatchedFailedProvenanceEventConsumer(configuration, provenanceEventActiveMqWriter, this.failedEventsJmsQueue));
        t2.start();

        initCheckAndSendTimer();
/*
        int maxThreads = 1;
        eventStatisticsExecutor =
            new ThreadPoolExecutor(
                maxThreads, // core thread pool size
                maxThreads, // maximum thread pool size
                1, // time to wait before resizing pool
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(maxThreads, true),
                new ThreadPoolExecutor.CallerRunsPolicy());
                */

    }

    private String mapKey(ProvenanceEventRecordDTO event) {
        return event.getFeedName() + ":" + event.getComponentId();
    }

    /**
     * - Create/update the flowfile graph for this new event - Calculate statistics on the event and add them to the aggregated set to be processed - Add the event to the DelayQueue for further
     * processing as a Batch or Stream
     */
    public void prepareAndAdd(ProvenanceEventRecordDTO event) {
        try {
            CacheUtil cacheUtil = (CacheUtil) SpringApplicationContext.getInstance().getBean("cacheUtil");
            cacheUtil.cacheAndBuildFlowFileGraph(event);

            //send the event off for stats processing to the threadpool.  order does not matter thus they can be in an number of threads
            // eventStatisticsExecutor.execute(new StatisticsProvenanceEventConsumer(event));
            // ProvenanceStatsCalculator statsCalculator = (ProvenanceStatsCalculator) SpringApplicationContext.getInstance().getBean("provenanceStatsCalculator");
            ProvenanceEventStats stats = statsCalculator.calculateStats(event);
            //add to delayed queue for processing
            aggregateEvent(event, stats);

            //if failure detected group and send off to separate queue
            collectFailureEvents(event);


        } catch (Exception e) {
            log.error("ERROR PROCESSING EVENT! {}.  ERROR: {} ", event, e.getMessage(), e);
        }

    }

    public void collectFailureEvents(ProvenanceEventRecordDTO event) {
        Set<ProvenanceEventRecordDTO> failedEvents = provenanceFeedLookup.getFailureEvents(event);
        if (failedEvents != null && !failedEvents.isEmpty()) {
            failedEvents.forEach(failedEvent ->
                                 {
                                     if (!failedEvent.isFailure()) {
                                         failedEvent.setIsFailure(true);
                                         log.info("Failed Event Detected for {} ", failedEvent);
                                         failedEventsJmsQueue.offer(failedEvent);
                                         ProvenanceEventStats failedEventStats = provenanceFeedLookup.failureEventStats(failedEvent);
                                         statsCalculator.addFailureStats(failedEventStats);
                                     }
                                 });
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

        List<ProvenanceEventRecordDTO> eventsSentToJms = eventsToAggregate.values().stream().flatMap(rollingFeedProcessorEventAggregate -> {
            return rollingFeedProcessorEventAggregate.collectEventsToBeSentToJmsQueue().stream();
        }).collect(Collectors.toList());
        log.info("collecting {} events from {} - {} ", eventsSentToJms.size(), lastCollectionTime, DateTime.now());
        jmsProcessingQueue.addAll(eventsSentToJms);
        lastCollectionTime = DateTime.now();
    }


    /**
     * Start a timer to run and get any leftover events and send to JMS This is where the events are not calculated because there is a long delay in provenacne events and they are still waiting in the
     * caches
     */
    private void initCheckAndSendTimer() {
        log.info("*** STARTING PRODUCER timer");
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
