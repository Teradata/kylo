package com.thinkbiganalytics.nifi.provenance.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.AggregationEventProcessingStats;
import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * A cache of Flowfiles active in the system Created by sr186054 on 8/11/16.
 */
@Component
public class FlowFileGuavaCache {

    private static final Logger log = LoggerFactory.getLogger(FlowFileGuavaCache.class);

    private Integer expireTimerCheckSeconds = 10;

    @Autowired
    CacheUtil cacheUtil;

    @Autowired
    ProvenanceFeedLookup feedLookup;

    private final LoadingCache<String, ActiveFlowFile> cache;

    private List<FlowFileCacheListener> listeners = new ArrayList<>();


    public void subscribe(FlowFileCacheListener listener) {
        listeners.add(listener);
    }

    private DateTime lastPrintLogTime = null;
    private Long PRINT_LOG_MILLIS = 60 * 5 * 1000L; //print the summary log every 5 minutes

    private FlowFileGuavaCache() {
        cache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, ActiveFlowFile>() {
                                                                  @Override
                                                                  public ActiveFlowFile load(String id) throws Exception {
                                                                      return loadFromCache(id);
                                                                  }

                                                              }
        );
        log.info("Created new FlowFileGuavaCache running timer every {} seconds to check and expire finished flow files", expireTimerCheckSeconds);
        initTimerThread();
    }

    public boolean isCached(String flowFileId) {
        return cache.getIfPresent(flowFileId) != null;
    }


    private ActiveFlowFile loadFromCache(String flowFileId) {
        log.info("Creating new FlowFile {} ", flowFileId);
        return new ActiveFlowFile(flowFileId);
    }

    public ActiveFlowFile getEntry(String id) {
        return cache.getUnchecked(id);
    }


    public List<ActiveFlowFile> getRootFlowFiles() {
        return cache.asMap().values().stream().filter(flowFile -> flowFile.isRootFlowFile()).collect(Collectors.toList());
    }

    public Collection<ActiveFlowFile> getAllFlowFiles() {
        return cache.asMap().values();
    }


    public List<ActiveFlowFile> getCompletedRootFlowFiles() {
        int backoffSeconds = 60;
        DateTime completeTime = DateTime.now().minusSeconds(backoffSeconds);
        //hold onto any completed flow files for xx seconds after they are complete before removing from cache
        return cache.asMap().values().stream().filter(
            flowFile -> (flowFile.isRootFlowFile() && flowFile.getRootFlowFile().isCanExpire() && completeTime.isAfter(flowFile.getRootFlowFile().getMinimiumExpireTime())))
            .collect(
                Collectors.toList());
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public void printSummary() {
        Map<String, ActiveFlowFile> map = cache.asMap();
        List<ActiveFlowFile> rootFiles = getRootFlowFiles();
        cacheUtil.logStats();
        log.info("FLOW FILE Cache Size: {} , root files {}, processorNameMapSize: {} ", map.size(), rootFiles.size(), feedLookup.getProcessorIdMapSize());
        log.info("JMS Stats:  Sent {} streaming events to JMS.  Sent {} batch events to JMS ", AggregationEventProcessingStats.getStreamingEventsSent(),
                 AggregationEventProcessingStats.getBatchEventsSent());


    }

    public void invalidate(ActiveFlowFile flowFile) {
        if (flowFile.getRootFlowFile().isFlowComplete()) {
            cache.invalidate(flowFile.getId());
            listeners.stream().forEach(flowFileCacheListener -> flowFileCacheListener.onInvalidate(flowFile));
            //also invalidate all children
            flowFile.getChildren().forEach(child -> invalidate(child));
        }
    }


    public void expire() {
        try {
            long start = System.currentTimeMillis();
            List<ActiveFlowFile> rootFiles = getCompletedRootFlowFiles();
            if (!rootFiles.isEmpty()) {
                for (ActiveFlowFile root : rootFiles) {
                    invalidate(root);
                }
                long stop = System.currentTimeMillis();
                if (rootFiles.size() > 0) {
                    log.info("Time to expire {} flowfiles {} ms. Root Flow Files left in cache: {} ", rootFiles.size(), (stop - start), getRootFlowFiles().size());
                }
            }
            if (lastPrintLogTime == null || (lastPrintLogTime != null && DateTime.now().getMillis() - lastPrintLogTime.getMillis() > (PRINT_LOG_MILLIS))) {
                printSummary();
                lastPrintLogTime = DateTime.now();
            }

        } catch (Exception e) {
            log.error("Error attempting to invalidate FlowFileGuava cache {}, {}", e.getMessage(), e);
        }
    }

    private void initTimerThread() {
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            expire();
        }, expireTimerCheckSeconds, expireTimerCheckSeconds, TimeUnit.SECONDS);


    }


}
