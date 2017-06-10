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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.BatchFeedStartingJobEventCacheEntry;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFileJobTrackingStats;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 6/5/17.
 */
public class ThrottleEvents {

    private static final Logger log = LoggerFactory.getLogger(ThrottleEvents.class);

    AtomicLong skippedEvents = new AtomicLong(0);
    /**
     * Sample a number of events every x millis
     * Every {sampleEventRate}  per {sampleTimeInMillis} process
     */
    private final Long sampleTimeInMillis = 1000L;

    /**
     * The number of events per the threshold to sample
     * Every {sampleEventRate}  per {sampleTimeInMillis} process
     * This is for each Feed and Processor in the feed
     */
    private final Integer sampleEventRate = 3;

    Cache<String, String> flowFileToStartingFlow = CacheBuilder.newBuilder().build();

    /**
     * Starting flow cache has a key of the processorId for the starting processor.
     * This is cleared internally and has a longer expire time
     */
    LoadingCache<String, ProvenanceEventRecordThrottle>
        cache =
        CacheBuilder.newBuilder().recordStats().expireAfterAccess(15, TimeUnit.SECONDS).build(new CacheLoader<String, ProvenanceEventRecordThrottle>() {
            @Override
            public ProvenanceEventRecordThrottle load(String key) throws Exception {
                return new ProvenanceEventRecordThrottle(key, null, sampleTimeInMillis, sampleEventRate);
            }
        });

    private static final ThrottleEvents instance = new ThrottleEvents();

    private ThrottleEvents() {

    }

    public static ThrottleEvents getInstance(){
        return instance;
    }

    /**
     * Return the unique key identifying the batch
     * if its a starting job event (first event for a feed) then us the ProcessorId of the event.
     * otherwise use the processId + the feed flowfile id
     *
     * @param event the event to process
     * @return the unique batch key
     */
    protected String mapKey(ProvenanceEventRecord event) {
        String key = event.getComponentId();

        if (!isStartingFeedFlow(event)) {
            key += ":" + event.getFlowFileUuid();
        }
        return key;
    }

    private boolean isStartingFeedFlow(ProvenanceEventRecord event){
        return (ProvenanceEventType.CREATE.equals(event.getEventType()) || ProvenanceEventType.RECEIVE.equals(event.getEventType()));
    }

    public boolean isProcessEvent(ProvenanceEventRecord event) {
        boolean isProcess = false;

        if(isStartingFeedFlow(event)){
            isProcess = cache.getUnchecked( mapKey(event)).isProcessEvent(event);
            if(isProcess){
                flowFileToStartingFlow.put(event.getFlowFileUuid(),event.getFlowFileUuid());
            }
        }
        else {
            isProcess = isProcessPreCheck(event) && cache.getUnchecked( mapKey(event)).isProcessEvent(event);
        }
         String start = flowFileToStartingFlow.getIfPresent(event.getFlowFileUuid());
        if(ProvenanceEventType.DROP.equals(event.getEventType()) && start != null && start.equalsIgnoreCase(event.getFlowFileUuid())) {
            flowFileToStartingFlow.invalidate(start);
        }
    if(!isProcess){
        skippedEvents.incrementAndGet();
    }
    return isProcess;

    }


    private boolean isProcessPreCheck(ProvenanceEventRecord event){
        String startingFlowFile = flowFileToStartingFlow.getIfPresent(event.getFlowFileUuid());
        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {
            for (String parent : event.getParentUuids()) {
                startingFlowFile = flowFileToStartingFlow.getIfPresent(parent);
                if(startingFlowFile != null) {
                   flowFileToStartingFlow.put(event.getFlowFileUuid(), startingFlowFile);
                  break;
                }
            }
        }
        if (startingFlowFile != null && event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                flowFileToStartingFlow.put(child, startingFlowFile);
            }
        }
        return startingFlowFile != null;

    }

    public Long getSkippedEvents(){
       return skippedEvents.get();
    }

    public long getFlowFileToStartingFlowCacheSize(){
        return flowFileToStartingFlow.size();
    }


}
