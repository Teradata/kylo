package com.thinkbiganalytics.nifi.provenance;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This will take a ProvenanceEvent, prepare it by building the FlowFile graph of its relationship to the running flow, generate Statistics about the Event and then set it in the DelayQueue for the
 * system to process as either a Batch or a Strem Statistics are calculated by the (ProvenanceStatsCalculator) where the events are grouped by Feed and Processor and then aggregated during a given
 * interval before being sent off to a JMS queue for processing.
 *
 *
 * Created by sr186054 on 8/14/16.
 */
public class TestMaps {

    private StreamConfiguration configuration;

    private static final Logger log = LoggerFactory.getLogger(TestMaps.class);
    /**
     * Reference to when to send the aggregated events found in the  statsByTime map.
     */
    private final AtomicReference<DateTime> nextCollectionTime;

    private DateTime previousCollectionTime = null;

    /**
     * flag to lock if we are updating the nextSendTime
     */
    private final AtomicBoolean isUpdatingNextCollectionTime = new AtomicBoolean(false);

    Map<Long, Map<String, List<Long>>> eventsToAggregate = new ConcurrentHashMap<>();

    public TestMaps() {
        super();
        this.configuration = new StreamConfiguration.Builder().processDelay(10000).jmsBatchDelay(10000).build();
        this.previousCollectionTime = DateTime.now();
        this.nextCollectionTime = new AtomicReference<>(previousCollectionTime.plus(configuration.getProcessDelay()));
        // initCheckAndSendTimer();
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

    private String mapKey(Long event) {
        return event + "";
    }

    /**
     * - Create/update the flowfile graph for this new event - Calculate statistics on the event and add them to the aggregated set to be processed - Add the event to the DelayQueue for further
     * processing as a Batch or Stream
     */
    public void prepareAndAdd(Long event) {
        try {
            aggregateEvent(event);

        } catch (Exception e) {
            log.error("ERROR PROCESSING EVENT! {}.  ERROR: {} ", event, e.getMessage(), e);
        }

    }

    private void process(Collection<List<Long>> events) {
        //further batching
        log.info("Processing Events within Window {} - {}.", previousCollectionTime, nextCollectionTime);
        LongSummaryStatistics summaryStatistics = events.stream().flatMapToLong(list -> list.stream().mapToLong(l -> l)).summaryStatistics();
        System.out.println(summaryStatistics);

    }

    /**
     * Stats are grouped by the nextSendTime and then by Feed and Processor
     */
    private void aggregateEvent(Long event) {
        //if now is after the next time to send update the nextTime to send
        checkAndProcess();
        if (event != null) {
            eventsToAggregate.computeIfAbsent(nextCollectionTime.get().getMillis(), time -> new ConcurrentHashMap<>()).computeIfAbsent(mapKey(event),
                                                                                                                                       mapKey -> new ArrayList<>()).add(
                event);
        }
    }


    private void checkAndProcess() {
        if (DateTime.now().isAfter(nextCollectionTime.get())) {
            if (isUpdatingNextCollectionTime.compareAndSet(false, true)) {
                try {
                    //update the collection time
                    previousCollectionTime = nextCollectionTime.get();
                    nextCollectionTime.set(nextCollectionTime.get().plus(configuration.getProcessDelay()));
                    Collection<List<Long>> events = eventsToAggregate.getOrDefault(previousCollectionTime.getMillis(), new HashMap<>()).values();
                    process(events);
                    eventsToAggregate.remove(previousCollectionTime.getMillis());
                    log.info("Updated the nextCollectionTime {}.  Removed {} from map ", nextCollectionTime.get(), events.size());
                } finally {
                    isUpdatingNextCollectionTime.set(false);

                }

            }
        }
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


}
