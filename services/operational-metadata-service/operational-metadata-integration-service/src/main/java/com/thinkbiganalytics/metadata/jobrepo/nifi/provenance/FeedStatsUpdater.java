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

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStatisticsProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedStats;

import org.jgroups.util.ConcurrentLinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component
public class FeedStatsUpdater {

    private static final Logger log = LoggerFactory.getLogger(FeedStatsUpdater.class);

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private NifiFeedStatisticsProvider nifiFeedStatisticsProvider;

    @Value("${kylo.ops.mgr.feed-stats.update-changed-only:true}")
    private boolean updateChangedOnly = true;

    @Value("${kylo.ops.mgr.feed-stats.defer-update:true}")
    private boolean deferFeedUpdate = true;

    @Value("${kylo.ops.mgr.feed-stats.defer-update-interval:5000}")
    private Long deferFeedUpdateInterval = 5000L;


    private ThreadFactory saveFeedStatsThreadFactory = new ThreadFactoryBuilder().setDaemon(true)
        .setNameFormat("FeedStatsUpdater-%d").build();


    /**
     * Scheduled task to gather stats and send to JMS
     */
    private ScheduledFuture saveFeedStatsFuture;


    private ScheduledExecutorService saveFeedStatsExecutorService = Executors.newSingleThreadScheduledExecutor(saveFeedStatsThreadFactory);

    public FeedStatsUpdater(){

    }
    @PostConstruct
    private void init() {
        if(deferFeedUpdate) {
            initTimerThread();
        }
    }


    private Lock lock = new ReentrantLock();

    private Map<String,JpaNifiFeedStats> lastSavedValues = new ConcurrentHashMap<>();

    private Map<String,JpaNifiFeedStats> feedStatsMap = new ConcurrentHashMap<>();

    public void  updateStats(Map<String,JpaNifiFeedStats>  stats) {
        lock.lock();
        try {
            feedStatsMap.putAll(stats);
        }
        finally {
            lock.unlock();
        }
    }
    public boolean statsChanged(JpaNifiFeedStats oldStats, JpaNifiFeedStats newStats){
        return (oldStats == null && newStats != null ||
                oldStats != null && newStats != null &&
                                (oldStats.getRunningFeedFlows() != newStats.getRunningFeedFlows() || oldStats.getLastActivityTimestamp() != newStats.getLastActivityTimestamp()));
    }

    public List<JpaNifiFeedStats> getChangedStats(){
        return feedStatsMap.entrySet().stream().filter(e -> !lastSavedValues.containsKey(e.getKey()) || statsChanged(lastSavedValues.get(e.getKey()), e.getValue())).map(e-> e.getValue()).collect(Collectors.toList());
    }

    public void saveStats(){
    lock.lock();
    try {
        if (!feedStatsMap.isEmpty()) {
            Collection<JpaNifiFeedStats> updatedStats = updateChangedOnly ? getChangedStats() : feedStatsMap.values();
            int size = updatedStats.size();
            if (log.isDebugEnabled()) {
                String names = updatedStats.stream().map(s -> s.getFeedName()).collect(Collectors.joining(","));
                log.debug("Saving Feed Stats ({}) feeds {}.   ({} total)", size, names, feedStatsMap.size());
            }
            Stopwatch saveFeedStatsStopwatch = Stopwatch.createStarted();
            metadataAccess.commit(() -> {
                nifiFeedStatisticsProvider.saveLatestFeedStats(new ArrayList<>(updatedStats));
                lastSavedValues = new ConcurrentHashMap<>(feedStatsMap);
                feedStatsMap.clear();
            },MetadataAccess.SERVICE);
            saveFeedStatsStopwatch.stop();
            log.debug("Time to save {} Feed Stats: {} ms",size,saveFeedStatsStopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }
    finally {
    lock.unlock();
    }
    }

    private Runnable saveStatsTAsk = new Runnable() {
        @Override
        public void run() {
            saveStats();
        }
    };


    private void initTimerThread() {
        Long runInterval = deferFeedUpdateInterval;
        initSaveFeedStatsTimerThread(runInterval);
        log.info("Initialized Timer Thread to check and save feed stats every {} ms ",runInterval);
    }

    //jms thread

    /**
     * Start the timer thread
     */
    private ScheduledFuture initSaveFeedStatsTimerThread(Long time) {
        saveFeedStatsFuture = saveFeedStatsExecutorService.scheduleAtFixedRate(saveStatsTAsk, time, time, TimeUnit.MILLISECONDS);
        return saveFeedStatsFuture;

    }

}
