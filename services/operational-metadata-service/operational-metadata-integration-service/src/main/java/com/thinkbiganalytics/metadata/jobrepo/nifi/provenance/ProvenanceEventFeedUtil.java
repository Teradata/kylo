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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventProvider;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.provenance.KyloProcessorFlowType;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class ProvenanceEventFeedUtil {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventFeedUtil.class);

    @Inject
    private NifiFlowCache nifiFlowCache;

    @Inject
    NifiEventProvider nifiEventProvider;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    private MetadataEventService eventService;

    private FeedOperationListener listener = new FeedCompletedListener();


    Cache<String, ProvenanceEventRecordDTO> runningJobs = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

    BiMap<String,String> relatedFlowFiles = HashBiMap.create();

    private String nifiFlowCacheSyncId = null;

    @PostConstruct
    public void addEventListener() {
        this.eventService.addListener(this.listener);
    }


    private Long timeBetweenStartingJobs(String feedName) {
        return 2000L;
    }

    public ProvenanceEventRecordDTO enrichEventWithFeedInformation(ProvenanceEventRecordDTO event) {
        String feedName = getFeedName(event.getFeedFlowFile().getFirstEventProcessorId());
        String processGroupId = getFeedProcessGroupId(event.getFeedFlowFile().getFirstEventProcessorId());
        String processorName = getProcessorName(event.getComponentId());

        if (event.getFeedFlowFile().isStream() && StringUtils.isNotBlank(event.getStreamingBatchFeedFlowFileId())) {
            //reassign the feedFlowFile to the batch
            log.info("Reassigned FlowFile from {} to {} ", event.getJobFlowFileId(), event.getStreamingBatchFeedFlowFileId());
            event.setJobFlowFileId(event.getStreamingBatchFeedFlowFileId());
        }
        if (event.isStartOfJob()) {
            ProvenanceEventRecordDTO lastEvent = runningJobs.getIfPresent(feedName);
            Long timeBetweenJobs = timeBetweenStartingJobs(feedName);
            if (timeBetweenJobs == -1L || lastEvent == null || (lastEvent != null && (event.getEventTime().getMillis() - lastEvent.getEventTime().getMillis()) > timeBetweenJobs)) {
                //create it
                runningJobs.put(feedName,event);
                //notify clusters its running
            }
            else {
                //relate it
                relatedFlowFiles.put(event.getJobFlowFileId(),lastEvent.getJobFlowFileId());
                //notify clusters its related
            }
        }
        //if the job flow file is part of the related ones reassign it
        if(relatedFlowFiles.containsKey(event.getJobFlowFileId())) {
            event.setJobFlowFileId(relatedFlowFiles.get(event.getJobFlowFileId()));
        }

        event.setIsBatchJob(true);
        event.getFeedFlowFile().setFeedName(feedName);
        event.setFeedName(feedName);
        event.setFeedProcessGroupId(processGroupId);
        event.setComponentName(processorName);
        setProcessorFlowType(event);

        return event;
    }



    public KyloProcessorFlowType setProcessorFlowType(ProvenanceEventRecordDTO event) {
        if (event.getProcessorType() == null) {

            if (event.isTerminatedByFailureRelationship()) {
                event.setProcessorType(KyloProcessorFlowType.FAILURE);
                event.setIsFailure(true);
            }
            if (event.getSourceConnectionIdentifier() != null) {
                NiFiFlowCacheConnectionData connectionData = getFlowCache().getConnectionIdToConnection().get(event.getSourceConnectionIdentifier());
                if (connectionData != null && connectionData.getName() != null) {
                    if (connectionData.getName().toLowerCase().contains("failure")) {
                        event.setProcessorType(KyloProcessorFlowType.FAILURE);
                        event.setIsFailure(true);
                        //if this is a failure because of the connection name it means the previous event failed.
                        //todo is there a way to efficiently set the previous event as being failed
                    } else if (connectionData.getName().toLowerCase().contains("warn")) {
                        event.setProcessorType(KyloProcessorFlowType.WARNING);
                    }

                }
                if (event.getProcessorType() == null) {
                    event.setProcessorType(KyloProcessorFlowType.NORMAL_FLOW);
                }
            }
        }
        return event.getProcessorType();
    }


    public String getFeedName(String feedProcessorId){
        return getFlowCache().getProcessorIdToFeedNameMap().get(feedProcessorId);
    }

    public String getFeedProcessGroupId(String feedProcessorId){
        return getFlowCache().getProcessorIdToFeedProcessGroupId().get(feedProcessorId);
    }

    public String getProcessorName(String processorId){
        return getFlowCache().getProcessorIdToProcessorName().get(processorId);
    }


    private Integer batchJobsPerSecond(String feedName) {
        return -1;
    }

    private NiFiFlowCacheSync getNiFiFlowCacheData() {
        NiFiFlowCacheSync sync =  nifiFlowCache.syncAndReturnUpdates(nifiFlowCacheSyncId);
        nifiFlowCacheSyncId = sync.getSyncId();
        return sync;
    }

    private NifiFlowCacheSnapshot getFlowCache() {
        NiFiFlowCacheSync flowCache = getNiFiFlowCacheData();
        if (flowCache == null || flowCache.getSnapshot() == null) {
            return NifiFlowCacheSnapshot.EMPTY;
        } else {
            return flowCache.getSnapshot();
        }
    }



    private class FeedCompletedListener implements MetadataEventListener<FeedOperationStatusEvent> {

        @Override
        public void notify(FeedOperationStatusEvent event) {
           ProvenanceEventRecordDTO provenanceEventRecordDTO = runningJobs.getIfPresent(event.getData().getFeedFlowFileId());
           if(provenanceEventRecordDTO != null){
               runningJobs.invalidate(provenanceEventRecordDTO);
               relatedFlowFiles.remove(event.getData().getFeedFlowFileId());
               relatedFlowFiles.inverse().remove(event.getData().getFeedFlowFileId());
           }
        }
    }

}
