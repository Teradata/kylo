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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 6/5/17.
 */
public class BatchEventsBySampling   implements BatchProvenanceEvents {

    private static final Logger log = LoggerFactory.getLogger(BatchEventsBySampling.class);

    /**
     * Event Cache has a key of the processor Id + the feed flowfile id.
     * This should expire after access > than the sampleTimeInMills
     */
    Cache<String, BatchFeedProcessorEventCacheEntry> feedProcessorEventCache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(3, TimeUnit.SECONDS).build();

    /**
     * Starting flow cache has a key of the processorId for the starting processor.
     * This is cleared internally and has a longer expire time
     */
    Cache<String, BatchFeedStartingJobEventCacheEntry> startingFlowsCache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(15, TimeUnit.SECONDS).build();


    /**
     * unique mapKey along with the set of batch keys sent to jms
     * Used to ensure we dont send duplicate events to jms
     */
    Cache<String, String> batchedEvents = CacheBuilder.newBuilder().recordStats().expireAfterWrite(10, TimeUnit.SECONDS).build();

    @Autowired
    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    @Autowired
    private FeedFlowFileGuavaCache cache;


    /**
     * Track additional attributes pertaining to the lifecycle of the feed flow
     */
    Map<String, FeedFlowFileJobTrackingStats> flowFileJobTrackingStatsMap = new ConcurrentHashMap<>();


    /**
     * Events to be sent to ops manager jms queue
     */
    private List<ProvenanceEventRecordDTO> jmsEvents = new ArrayList<>();

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
     * The number of events to group together
     */
    private Integer jmsGroupSize = 30;


    /**
     * When a FeedFlowFile is completed this listener will be invoked
     */
    private FeedFlowFileExpireListener feedFlowFileExpireListener = new FeedFlowFileExpireListener();



    @PostConstruct
    private void init(){
        cache.subscribe(feedFlowFileExpireListener);
    }

    /**
     * Unique key describing the event for ops manager
     *
     * @param event the provenance event
     * @return the unique key
     */
    private String batchEventKey(ProvenanceEventRecordDTO event) {
        return event.getComponentId() + "-" + (event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow() == null ? event.getJobFlowFileId() : event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow() )+ event.isStartOfJob() + event.isEndOfJob();
    }

    /**
     * Return the unique key identifying the batch
     * if its a starting job event (first event for a feed) then us the ProcessorId of the event.
     * otherwise use the processId + the feed flowfile id
     *
     * @param event the event to process
     * @return the unique batch key
     */
    protected String mapKey(ProvenanceEventRecordDTO event) {
        String key = event.getFeedFlowFile().getFirstEventProcessorId() + ":" + event.getComponentId();

        //events that create a new feed flow file do not include the flow file as the tracking key
        if (!event.isStartOfJob() && !event.getFeedFlowFile().isStream()) {
            key += ":" + event.getJobFlowFileId();
        }
        return key;
    }



    private BatchFeedProcessorEventCacheEntry getBatchFeedProcessorEventCacheEntry(ProvenanceEventRecordDTO event){
        final String key = mapKey(event);
        if(event.isStartOfJob()) {
            if(startingFlowsCache.getIfPresent(key) == null){
                startingFlowsCache.put(key,new BatchFeedStartingJobEventCacheEntry(key, event.getEventTime(), sampleTimeInMillis, sampleEventRate));
            }
            return startingFlowsCache.getIfPresent(key);
        }
        else {

        if(feedProcessorEventCache.getIfPresent(key) == null){
            feedProcessorEventCache.put(key,new BatchFeedProcessorEventCacheEntry(key, event.getEventTime(), sampleTimeInMillis, sampleEventRate));
        }
        return feedProcessorEventCache.getIfPresent(key);

        }
    }


    public  boolean process(ProvenanceEventRecordDTO event){
        boolean added = false;
        BatchFeedProcessorEventCacheEntry feedProcessorEventCacheEntry = getBatchFeedProcessorEventCacheEntry(event);

            boolean processed = feedProcessorEventCacheEntry.process(event);
            if(!processed && event.isStartOfJob()){
                cache.addRelatedFlowFile(event.getFlowFileUuid(),event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow());
            }

            if(cache.isPrimaryFlowFileCompleteListenerEnabled()) {
                //track the additional attrs
                String key = event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow();
                if (key != null) {
                    flowFileJobTrackingStatsMap
                        .computeIfAbsent(key, k -> new FeedFlowFileJobTrackingStats(event.getFeedFlowFile().getFirstEventProcessorId(), event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow()))
                        .trackExtendedAttributes(event);
                }
            }

            if(processed) {
                //ensure we only send 1 unique event result
                String batchKey = batchEventKey(event);
                if(batchedEvents.getIfPresent(batchKey) == null){
                    event.setIsBatchJob(true);
                    //reassign the flowfile to a batch one
                    if(event.getFeedFlowFile().hasRelatedBatchFlows()) {
                        String ffId = event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow();
                        if(ffId != null) {
                            event.setStreamingBatchFeedFlowFileId(ffId);
                        }
                    }
                    batchedEvents.put(batchKey,batchKey);
                    jmsEvents.add(event);
                    added = true;
                }
            }
            return added;
    }


    /**
     * Send the events off to jms
     */
    public void sendToJms() {

        if (!jmsEvents.isEmpty()) {
            Lists.partition(jmsEvents, jmsGroupSize).forEach(eventsSubList -> {
                ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
                eventRecordDTOHolder.setEvents(Lists.newArrayList(eventsSubList));
                provenanceEventActiveMqWriter.writeBatchEvents(eventRecordDTOHolder);
            });
        }

        //clear it
        jmsEvents.clear();

    }


    /**
     * Used to send additional tracking stats when the feed completes.
     * This is only enabled if the cache.isPrimaryFlowFileCompleteListenerEnabled()
     */
    private class FeedFlowFileExpireListener extends NoopFeedFlowFileCacheListener {


        public FeedFlowFileExpireListener() {

        }

        private List<ProvenanceEventRecordDTO> getUpdatedEvents(String primaryFeedFlowId){

            List<ProvenanceEventRecordDTO> updatedEvents = null;
            FeedFlowFileJobTrackingStats stats = flowFileJobTrackingStatsMap.get(primaryFeedFlowId);
            if(stats != null){
                List<ProvenanceEventRecordDTO> dirtyEvents =  stats.getUpdatedProvenanceEvents();
                if(dirtyEvents != null && !dirtyEvents.isEmpty()) {
                    if(updatedEvents == null){
                        updatedEvents = new ArrayList<>();
                    }
                    updatedEvents.addAll(dirtyEvents);
                }

                flowFileJobTrackingStatsMap.remove(primaryFeedFlowId);
            }
            return updatedEvents;
        }

        @Override
        public void onPrimaryFeedFlowsComplete(Set<String> primaryFeedFlowIds) {
            if(primaryFeedFlowIds != null){
                List<ProvenanceEventRecordDTO> allUpdatedEvents = new ArrayList<>();
                long start = System.currentTimeMillis();
               for(String ffId : primaryFeedFlowIds){
                   List<ProvenanceEventRecordDTO> updatedEvents = getUpdatedEvents(ffId);
                   if(updatedEvents != null){
                       allUpdatedEvents.addAll(updatedEvents);
                   }
               }
                long end = System.currentTimeMillis();
                log.info("time to collect {} completion events {} ms ",allUpdatedEvents.size(),(end-start));

                if(!allUpdatedEvents.isEmpty()) {
                    long start2 = System.currentTimeMillis();
                    ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
                    eventRecordDTOHolder.setEvents(allUpdatedEvents);
                    provenanceEventActiveMqWriter.writeBatchEvents(eventRecordDTOHolder);
                    end = System.currentTimeMillis();
                    log.info("time to send {} completion events {} ms ",allUpdatedEvents.size(),(end-start2));
                    log.info("Total Time for events {} is {} ms ",allUpdatedEvents.size(),(end-start2));
                }


            }






        }


    }

    public void logStats(){
        log.info("Batched Events Stats: {} ",batchedEvents.stats());
        log.info("Feed Processor Events Stats: {} ",feedProcessorEventCache.stats());
        log.info("Starting Feed Flows Stats: {} ",startingFlowsCache.stats());
    }

}
