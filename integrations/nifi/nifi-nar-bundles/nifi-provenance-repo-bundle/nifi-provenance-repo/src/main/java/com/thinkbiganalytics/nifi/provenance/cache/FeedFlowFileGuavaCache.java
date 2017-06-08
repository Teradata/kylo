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

import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemCacheBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.nifi.provenance.AggregationEventProcessingStats;
import com.thinkbiganalytics.nifi.provenance.BatchProvenanceEvents;;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;
import com.thinkbiganalytics.nifi.provenance.repo.KyloPersistentProvenanceEventRepository;

import org.apache.nifi.controller.ConfigurationContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * As a feed runs through NiFi the root {@link FeedFlowFile} keeps track of its progress and the status of its child flow files {@link FeedFlowFile#activeChildFlowFiles} and last processed
 * ProvenanceEvent {@link FeedFlowFile#flowFileLastEventTime} When a {@link FeedFlowFile} is marked as the complete {@link FeedFlowFile#isFeedComplete()} it will be removed from this cache via the
 * {@link this#expire()} thread When NiFi shuts down the cache is persisted to disk via the {@link FeedFlowFileMapDbCache#persistFlowFiles()} called by the {@link KyloPersistentProvenanceEventRepository#close()}
 *  This is to ensure that on startup of NiFi the tracking of the running flow files is kept in tact When NiFi starts the persisted disk cache is checked and loaded back into this cache via the {@link KyloPersistentProvenanceEventRepository#initializeFlowFilesFromMapDbCache()}
 */
public class FeedFlowFileGuavaCache {


    @Autowired
    BatchProvenanceEvents batchProvenanceEvents;

    private static final Logger log = LoggerFactory.getLogger(FeedFlowFileGuavaCache.class);
    /**
     * The cache of FeedFlowFiles
     */
    private final Cache<String, FeedFlowFile> cache;


    /**
     * When the ops manager feeds complete their stats tracking listener will be called to send any items to ops manager that didnt get sent during the standard processing.
     * This is because for rapid fire events we sample data and dont send the final processor event data to ops manager.
     * This flag will enable the listener to update ops manager step information when the entire feed is completed
     */
    private boolean primaryFlowFileCompleteListenerEnabled = false;


    /**
     * Keep a mapping between flow files that start rapidly for a given feed to those that are being tracked in Kylo Ops Manager
     */
    private final Cache<String, String> flowFileToPrimaryFlow;

    /**
     * Keep the reverse mapping to the flowFileToPrimaryFlow.
     * This will be decremented every time a flow file is processed
     */
    private final Map<String, AtomicInteger> primaryFlowFilesActive = new ConcurrentHashMap<>();

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
     * Every 30sec
     */
    private Long PRINT_LOG_MILLIS = 20 * 1000L;

    public FeedFlowFileGuavaCache() {

      cache =  FileSystemCacheBuilder.newBuilder().persistenceDirectory(new File("/var/kylo/cache"))
            .maximumSize(100L)
            .build();
       // cache = CacheBuilder.newBuilder().build();
        flowFileToPrimaryFlow = CacheBuilder.newBuilder().build();
      //  log.info("Created new FlowFileGuavaCache running timer every {} seconds to check and expire finished flow files", expireTimerCheckSeconds);
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


    private boolean isFeedAndRelatedFeedComplete(FeedFlowFile flowFile){
        boolean allComplete = true;
        if(flowFileToPrimaryFlow.getIfPresent(flowFile.getId()) != null) {
            AtomicInteger active =  primaryFlowFilesActive.get(flowFile.getPrimaryRelatedBatchFeedFlow());
            int remainingActive = 0;
            if(active != null) {
                remainingActive = active.decrementAndGet();
            }
            flowFileToPrimaryFlow.invalidate(flowFile.getId());
            allComplete = (remainingActive == 0);
            if(allComplete){
                primaryFlowFilesActive.remove(flowFile.getPrimaryRelatedBatchFeedFlow());
            }
        }
        return allComplete;
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
            //remove the relationship

            listeners.stream().forEach(flowFileCacheListener -> {
                flowFileCacheListener.onInvalidate(flowFile);
            });
        }
    }

    /**
     * Relate the flows together
     * @param flowFileId a flow file
     * @param primaryFlowFileId the flow marked as being primary
     */
    public void addRelatedFlowFile(String flowFileId,String primaryFlowFileId) {
        if(isPrimaryFlowFileCompleteListenerEnabled() && flowFileToPrimaryFlow.getIfPresent(flowFileId) == null) {
            flowFileToPrimaryFlow.put(flowFileId, primaryFlowFileId);
            primaryFlowFilesActive.computeIfAbsent(primaryFlowFileId,key -> new AtomicInteger(0)).incrementAndGet();
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
                Set<String> allCompleteFlowFileIds = new HashSet<>();
                for (FeedFlowFile root : rootFiles) {
                    invalidate(root);
                    if(primaryFlowFileCompleteListenerEnabled) {
                        if (isFeedAndRelatedFeedComplete(root)) {
                            allCompleteFlowFileIds.add(root.getPrimaryRelatedBatchFeedFlow() != null ? root.getPrimaryRelatedBatchFeedFlow() : root.getId());
                        }
                    }
                }
                if(!allCompleteFlowFileIds.isEmpty()){
                    listeners.stream().forEach(flowFileCacheListener -> {
                       flowFileCacheListener.onPrimaryFeedFlowsComplete(allCompleteFlowFileIds);
                    });
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

    public boolean isPrimaryFlowFileCompleteListenerEnabled() {
        return primaryFlowFileCompleteListenerEnabled;
    }

    /**
     * Log some summary data about the cache and JMS activity
     */
    public void printSummary() {
        Map<String, FeedFlowFile> map = cache.asMap();
        log.info("FeedFlowFile Cache Size: {}.  RelatedFlowsSize: {}  ", map.size(), flowFileToPrimaryFlow.size());
        batchProvenanceEvents.logStats();
        log.info("ProvenanceEvent JMS Stats:  Sent {} statistics events to JMS.  Sent {} batch events to JMS ", AggregationEventProcessingStats.getStreamingEventsSent(),
                 AggregationEventProcessingStats.getBatchEventsSent());


    }

    /**
     * Start the timer thread using the {@link this#expireTimerCheckSeconds} as the schedule interval in SECONDS
     */
    private void initTimerThread() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            printSummary();
        }, 30, 30, TimeUnit.SECONDS);


    }




}
