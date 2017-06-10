package com.thinkbiganalytics.nifi.provenance;

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
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileGuavaCache;
import com.thinkbiganalytics.nifi.provenance.cache.NoopFeedFlowFileCacheListener;
import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFileJobTrackingStats;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 6/5/17.
 */
public class StartingFeedFlowFileUtil {

    private static final Logger log = LoggerFactory.getLogger(StartingFeedFlowFileUtil.class);

    /**
     * Starting flow cache has a key of the processorId for the starting processor.
     * This is cleared internally and has a longer expire time
     */
    Cache<String, BatchFeedStartingJobEventCacheEntry> startingFlowsCache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(15, TimeUnit.SECONDS).build();

    LoadingCache<String, AtomicLong> startingFlowsCountCache = CacheBuilder.newBuilder().build(
           new CacheLoader<String, AtomicLong>() {
        @Override
        public AtomicLong load(String id) throws Exception {
            return new AtomicLong(0);
        }
    }
       );

    @Autowired
    private FeedFlowFileGuavaCache cache;


    /**
     * Track additional attributes pertaining to the lifecycle of the feed flow
     */
    Map<String, FeedFlowFileJobTrackingStats> flowFileJobTrackingStatsMap = new ConcurrentHashMap<>();

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


    /**
     * Return the unique key identifying the batch
     * if its a starting job event (first event for a feed) then us the ProcessorId of the event.
     * otherwise use the processId + the feed flowfile id
     *
     * @param event the event to process
     * @return the unique batch key
     */
    protected String mapKey(ProvenanceEventRecordDTO event) {
        String key = event.getComponentId();
        return key;
    }



    private BatchFeedProcessorEventCacheEntry getBatchFeedProcessorEventCacheEntry(ProvenanceEventRecordDTO event){
        final String key = mapKey(event);
            if(startingFlowsCache.getIfPresent(key) == null){
                startingFlowsCache.put(key,new BatchFeedStartingJobEventCacheEntry(key, event.getEventTime(), sampleTimeInMillis, sampleEventRate));
            }
            return startingFlowsCache.getIfPresent(key);
    }


    public  boolean process(ProvenanceEventRecordDTO event){
        boolean added = false;
        BatchFeedProcessorEventCacheEntry feedProcessorEventCacheEntry = getBatchFeedProcessorEventCacheEntry(event);

            boolean processed = feedProcessorEventCacheEntry.process(event);
            if(processed){
                //assign and add the flow files
            }
            if(!processed ){
            //    cache.addRelatedFlowFile(event.getFlowFileUuid(),event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow());
                try {
                    startingFlowsCountCache.get(mapKey(event)).incrementAndGet();
                }catch (ExecutionException e){

                }
            }
            return processed;
    }


}
