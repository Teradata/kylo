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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Retry processing events if not registered in Kylo
 */
public class RetryProvenanceEventWithDelay {

    private static final Logger log = LoggerFactory.getLogger(RetryProvenanceEventWithDelay.class);


    @Value("${kylo.provenance.retry.unregistered.events.enabled:true}")
    private boolean enabled = true;

    @Value("${kylo.provenance.retry.unregistered.events.waitTimeSec:5}")
    private Integer waitTimeSec = 5;

    @Value("${kylo.provenance.retry.unregistered.events.maxRetries:3}")
    private Integer maxRetries = 3;

    private ScheduledExecutorService executorService;


    BlockingQueue<RetryProvenanceEventRecordHolder> queue = new LinkedBlockingQueue<>();
    BlockingQueue<RetryAggregatedFeedProcessorStatisticsHolder> statsQueue = new LinkedBlockingQueue<>();

    private ProvenanceEventReceiver receiver;

    private NifiStatsJmsReceiver statsJmsReceiver;


    private DateTime startTime;


    public RetryProvenanceEventWithDelay(Integer waitTimeSec, Integer maxRetries, boolean enabled) {
        this.enabled = enabled;
        this.maxRetries = maxRetries;
        this.waitTimeSec = waitTimeSec;
    }

    public RetryProvenanceEventWithDelay() {
        init();
    }


    public void setStatsJmsReceiver(NifiStatsJmsReceiver statsJmsReceiver) {
        this.statsJmsReceiver = statsJmsReceiver;
    }

    private void init() {
        if (enabled) {
            start();
        }
    }

    public void setReceiver(ProvenanceEventReceiver receiver) {
        this.receiver = receiver;
    }

    public void delay(ProvenanceEventRecordDTOHolder eventHolder, List<ProvenanceEventRecordDTO> unregisteredEvents) {
        if (enabled) {
            RetryProvenanceEventRecordHolder holder = null;
            if (eventHolder instanceof RetryProvenanceEventRecordHolder) {
                holder = (RetryProvenanceEventRecordHolder) eventHolder;
                holder.setLastRetryTime(DateTime.now());
                holder.incrementRetryAttempt();
            } else {
                holder = new RetryProvenanceEventRecordHolder(maxRetries);
            }
            holder.setEvents(unregisteredEvents);
            if (receiver != null && !queue.contains(holder)) {
                if(!queue.offer(holder)) {
                    throw new RuntimeException("Error adding item to the queue");
                }
            }
        }
    }

    public void delay(AggregatedFeedProcessorStatisticsHolder statsHolder, List<AggregatedFeedProcessorStatistics> unregisteredEvents) {
        if (enabled) {
            RetryAggregatedFeedProcessorStatisticsHolder holder = null;
            if (statsHolder instanceof RetryAggregatedFeedProcessorStatisticsHolder) {
                holder = (RetryAggregatedFeedProcessorStatisticsHolder) statsHolder;
                holder.setLastRetryTime(DateTime.now());
                holder.incrementRetryAttempt();
            } else {
                holder = new RetryAggregatedFeedProcessorStatisticsHolder(maxRetries);
            }
            holder.setFeedStatistics(unregisteredEvents);
            if (statsJmsReceiver != null && !statsQueue.contains(holder)) {
                if(!statsQueue.offer(holder)) {
                    throw new RuntimeException("Error adding item to the queue");
                }
            }
        }
    }

    private void start() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true)
            .setNameFormat("RetryProvenanceEvent-%d").build();
        this.startTime = DateTime.now();
        executorService = Executors.newSingleThreadScheduledExecutor(threadFactory);
        executorService.scheduleAtFixedRate(() -> processQueues(), waitTimeSec, waitTimeSec, TimeUnit.SECONDS);

    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void processQueues() {
        processEventQueue();
        processStatsQueue();
    }

    private void processEventQueue() {
        try {
            List<ProvenanceEventRecordDTOHolder> events = new ArrayList<>();
            int numberOfEvents = queue.drainTo(events);
            if(numberOfEvents > 0) {
                events.stream().forEach(e -> receiver.receiveEvents(e));
            }
        } catch (Exception e) {
            log.error("Error Processing Retry Provenance Events", e);
        }
    }

    private void processStatsQueue() {
        try {
            List<AggregatedFeedProcessorStatisticsHolder> stats = new ArrayList<>();
            int numberOfEvents = statsQueue.drainTo(stats);
            if(numberOfEvents > 0) {
                stats.stream().forEach(e -> statsJmsReceiver.receiveTopic(e));
            }
        } catch (Exception e) {
            log.error("Error Processing Retry Provenance Stats", e);
        }
    }

}
