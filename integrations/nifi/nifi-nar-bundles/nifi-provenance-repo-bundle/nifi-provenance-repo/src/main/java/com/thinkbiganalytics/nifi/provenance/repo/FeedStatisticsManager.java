package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatisticsV2;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Manage the Feed Stats calculation and sending of events to Ops Manager
 */
public class FeedStatisticsManager {

    private static final Logger log = LoggerFactory.getLogger(FeedStatisticsManager.class);

    private Long sendJmsTimeMillis = ConfigurationProperties.DEFAULT_RUN_INTERVAL_MILLIS; //every 3 seconds

    private Lock lock = new ReentrantLock();

    private Map<String, FeedStatistics> feedStatisticsMap = new ConcurrentHashMap<>();

    private static final FeedStatisticsManager instance = new FeedStatisticsManager();

    private FeedStatisticsManager() {
        initTimerThread();
    }

    public static FeedStatisticsManager getInstance() {
        return instance;
    }


    private ThreadFactory gatherStatsThreadFactory = new ThreadFactoryBuilder().setDaemon(true)
        .setNameFormat("FeedStatisticsManager-GatherStats-%d").build();

    private ThreadFactory sendJmsThreadFactory = new ThreadFactoryBuilder().setDaemon(true)
        .setNameFormat("FeedStatisticsManager-SendStats-%d").build();

    /**
     * Scheduled task to gather stats and send to JMS
     */
    private ScheduledFuture gatherStatsScheduledFuture;

    /**
     * Service to schedule the sending of events to activemq
     */
    private ExecutorService jmsService = Executors.newFixedThreadPool(3, sendJmsThreadFactory);

    private ScheduledExecutorService jmsGatherEventsToSendService = Executors.newSingleThreadScheduledExecutor(gatherStatsThreadFactory);



    public void addEvent(ProvenanceEventRecord event, Long eventId) {
        lock.lock();
        try {
            //build up feed flow file map relationships
            boolean isStartingFeedFlow = ProvenanceEventUtil.isStartingFeedFlow(event);
            if (isStartingFeedFlow) {
                FeedEventStatistics.getInstance().checkAndAssignStartingFlowFile(event);
            }
            FeedEventStatistics.getInstance().assignParentsAndChildren(event);

            //generate statistics and process the event
            String feedProcessorId = FeedEventStatistics.getInstance().getFeedProcessorId(event);
            if (feedProcessorId != null) {
                String key = feedProcessorId + event.getComponentId();
                feedStatisticsMap.computeIfAbsent(key, feedStatisticsKey -> new FeedStatistics(feedProcessorId, event.getComponentId())).addEvent(event, eventId);
            } else {
                //UNABLE TO FIND data in maps
            }
        } finally {
            lock.unlock();
        }
    }

    public void gatherStatistics() {
        lock.lock();
        List<ProvenanceEventRecordDTO> eventsToSend = null;
        Map<String, AggregatedFeedProcessorStatistics> statsToSend = null;
        try {
            //Gather Events and Stats to send Ops Manager
            eventsToSend = feedStatisticsMap.values().stream().flatMap(stats -> stats.getEventsToSend().stream()).collect(Collectors.toList());

            final String collectionId = UUID.randomUUID().toString();
            Map<String,Long> runningFlowsCount = new HashMap<>();

            for (FeedStatistics feedStatistics : feedStatisticsMap.values()) {
                if (feedStatistics.hasStats()) {
                    if (statsToSend == null) {
                        statsToSend = new ConcurrentHashMap<>();
                    }
                    AggregatedFeedProcessorStatistics
                        feedProcessorStatistics =
                        statsToSend.computeIfAbsent(feedStatistics.getFeedProcessorId(),
                                                    feedProcessorId -> new AggregatedFeedProcessorStatistics(feedStatistics.getFeedProcessorId(), collectionId, sendJmsTimeMillis));

                    AggregatedProcessorStatistics
                        processorStatistics =
                        feedProcessorStatistics.getProcessorStats()
                            .computeIfAbsent(feedStatistics.getProcessorId(), processorId -> new AggregatedProcessorStatisticsV2(feedStatistics.getProcessorId(), null, collectionId));

                    //accumulate the stats together into the processorStatistics object grouped by source connection id
                    feedStatistics.getStats().stream().forEach(stats -> {
                        FeedProcessorStatisticsAggregator.getInstance().addStats1(processorStatistics.getStats(stats.getSourceConnectionIdentifier()), stats);
                    });
                }
            }

            if ((eventsToSend != null && !eventsToSend.isEmpty()) || (statsToSend != null && !statsToSend.isEmpty())) {
                //send it off to jms on a different thread
                JmsSender jmsSender = new JmsSender(eventsToSend, statsToSend.values(),FeedEventStatistics.getInstance().getRunningFeedFlowsChanged());
                this.jmsService.submit(new JmsSenderConsumer(jmsSender));
            }
            else {
                //if we are empty but the runningFlows have changed, then send off as well
                if(FeedEventStatistics.getInstance().isFeedProcessorRunningFeedFlowsChanged()){
                    JmsSender jmsSender = new JmsSender(null, null,FeedEventStatistics.getInstance().getRunningFeedFlowsChanged());
                    this.jmsService.submit(new JmsSenderConsumer(jmsSender));
                }

            }


        } finally {
            FeedEventStatistics.getInstance().markFeedProcessorRunningFeedFlowsUnchanged();

            feedStatisticsMap.values().stream().forEach(stats -> stats.clear());
            lock.unlock();
        }


    }

    private Runnable gatherStatisticsTask = new Runnable() {
        @Override
        public void run() {
            gatherStatistics();
        }
    };

    public void resetStatisticsInterval(Long interval) {
        lock.lock();
        sendJmsTimeMillis = interval;
        try {
            if(gatherStatsScheduledFuture != null){
                gatherStatsScheduledFuture.cancel(true);
            }
            initGatherStatisticsTimerThread(interval);

        } finally {
            lock.unlock();
        }
    }

    public void resetMaxEvents(Integer limit) {
        lock.lock();
        try {
            feedStatisticsMap.values().forEach(stats -> stats.setLimit(limit));
        } finally {
            lock.unlock();
        }
    }


    private void initTimerThread() {
        Long runInterval = ConfigurationProperties.getInstance().getFeedProcessingRunInterval();
        this.sendJmsTimeMillis = runInterval;
        initGatherStatisticsTimerThread(runInterval);
        log.info("Initialized Timer Thread to gather statistics and send events to JMS running every {} ms ",sendJmsTimeMillis);
    }

    //jms thread

    /**
     * Start the timer thread
     */
    private ScheduledFuture initGatherStatisticsTimerThread(Long time) {
        gatherStatsScheduledFuture = jmsGatherEventsToSendService.scheduleAtFixedRate(gatherStatisticsTask, time, time, TimeUnit.MILLISECONDS);
        return gatherStatsScheduledFuture;

    }

}
