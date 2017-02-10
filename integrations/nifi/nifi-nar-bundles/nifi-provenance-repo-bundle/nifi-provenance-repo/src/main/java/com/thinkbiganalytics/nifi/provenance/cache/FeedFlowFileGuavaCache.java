package com.thinkbiganalytics.nifi.provenance.cache;

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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.nifi.provenance.AggregationEventProcessingStats;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;
import com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask;

import org.apache.nifi.controller.ConfigurationContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * As a feed runs through NiFi the root {@link FeedFlowFile} keeps track of its progress and the status of its child flow files {@link FeedFlowFile#activeChildFlowFiles} and last processed
 * ProvenanceEvent {@link FeedFlowFile#flowFileLastEventTime} When a {@link FeedFlowFile} is marked as the complete {@link FeedFlowFile#isFeedComplete()} it will be removed from this cache via the
 * {@link this#expire()} thread When NiFi shuts down the cache is persisted to disk via the {@link FeedFlowFileMapDbCache#persistFlowFiles()} called by the {@link
 * com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask#onShutdown(ConfigurationContext)} This is to ensure that on startup of NiFi the tracking of the running flow files
 * is kept in tact When NiFi starts the persisted disk cache is checked and loaded back into this cache via the {@link KyloProvenanceEventReportingTask#onConfigurationRestored()}
 */
public class FeedFlowFileGuavaCache {

    private static final Logger log = LoggerFactory.getLogger(FeedFlowFileGuavaCache.class);
    /**
     * The cache of FeedFlowFiles
     */
    private final Cache<String, FeedFlowFile> cache;
    /**
     * The amount of time the expire thread should run to check and expire the feed flow files
     */
    private Integer expireTimerCheckSeconds = 10;
    /**
     * Listeners that can get notified with a FeedFlowFile is invalidated and removed from the cache
     */
    private List<FeedFlowFileCacheListener> listeners = new ArrayList<>();
    /**
     * The last time the summary was printed
     */
    private DateTime lastPrintLogTime = null;
    /**
     * How often should the summary of whats in the cache be logged
     * Every 5 minutes
     */
    private Long PRINT_LOG_MILLIS = 60 * 5000L;

    public FeedFlowFileGuavaCache() {
        cache = CacheBuilder.newBuilder().build();
        log.info("Created new FlowFileGuavaCache running timer every {} seconds to check and expire finished flow files", expireTimerCheckSeconds);
        initTimerThread();
    }

    /**
     * A listener can subscribe to the invalidate calls on the cache.
     * the {@link FeedFlowFileMapDbCache} subscribes to this cache to get messages and invalidate the files persisted on disk when they are completed.
     */
    public void subscribe(FeedFlowFileCacheListener listener) {
        listeners.add(listener);
    }

    /**
     * Check to see if a given flowfile is in the cache
     *
     * @return true if in the cache, false if not
     */
    public boolean isCached(String flowFileId) {
        return cache.getIfPresent(flowFileId) != null;
    }


    /**
     * Get a FeedFlowFile from the cache.
     * If the FeedFlowFile is not there it will return  null
     *
     * @return the FeedFlowFile, or null if not present
     */
    public FeedFlowFile getEntry(String id) {
        return cache.getIfPresent(id);
    }


    /**
     * Return all the FeedFlowFiles in the cache
     */
    public Collection<FeedFlowFile> getFlowFiles() {
        return new HashSet<>(cache.asMap().values());
    }

    /**
     * Add a FeedFlowFile to the cache
     *
     * @param flowFileId   the id of the flowfile
     * @param feedFlowFile the FeedFlowFile to relate/add to the cache
     */
    public void add(String flowFileId, FeedFlowFile feedFlowFile) {
        cache.put(flowFileId, feedFlowFile);
    }


    /**
     * Return all the FeedFlowFiles in the cache that are complete and Done.
     *
     * @return the flow files that are completed
     */
    public List<FeedFlowFile> getCompletedFeedFlowFiles() {
        return getFlowFiles().stream().filter(flowFile -> (flowFile.isFeedComplete())).collect(Collectors.toList());
    }


    /**
     * Invalidate and remove the given FeedFlowFile from the cache
     *
     * @param flowFile the flow file to invalidate/remove
     */
    public void invalidate(FeedFlowFile flowFile) {
        if (flowFile != null && flowFile.isFeedComplete()) {
            invalidate(flowFile.getId());
            if (flowFile.getChildFlowFiles() != null) {
                flowFile.getChildFlowFiles().stream().forEach(flowFileId -> invalidate(flowFileId));
            }
            listeners.stream().forEach(flowFileCacheListener -> flowFileCacheListener.onInvalidate(flowFile));
        }
    }

    /**
     * Invalidate and remove the flowfile from the cache
     */
    public void invalidate(String flowFileId) {
        cache.invalidate(flowFileId);
    }


    /**
     * Expire any completed FeedFlowFiles checking the {@link FeedFlowFile#isFeedComplete()} to determine if the FeedFlowFile is complete
     */
    public void expire() {
        try {
            long start = System.currentTimeMillis();
            List<FeedFlowFile> rootFiles = getCompletedFeedFlowFiles();
            if (!rootFiles.isEmpty()) {
                for (FeedFlowFile root : rootFiles) {
                    invalidate(root);
                }
                long stop = System.currentTimeMillis();
                if (rootFiles.size() > 0) {
                    log.info("Time to expire {} flowfile and all references {} ms. FeedFlowFile and references left in cache: {} ", rootFiles.size(), (stop - start), getFlowFiles().size());
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

    /**
     * Log some summary data about the cache and JMS activity
     */
    public void printSummary() {
        Map<String, FeedFlowFile> map = cache.asMap();
        log.info("FeedFlowFile Cache Size: {}  ", map.size());
        log.info("ProvenanceEvent JMS Stats:  Sent {} statistics events to JMS.  Sent {} batch events to JMS ", AggregationEventProcessingStats.getStreamingEventsSent(),
                 AggregationEventProcessingStats.getBatchEventsSent());


    }

    /**
     * Start the timer thread using the {@link this#expireTimerCheckSeconds} as the schedule interval in SECONDS
     */
    private void initTimerThread() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            expire();
        }, expireTimerCheckSeconds, expireTimerCheckSeconds, TimeUnit.SECONDS);


    }


}
