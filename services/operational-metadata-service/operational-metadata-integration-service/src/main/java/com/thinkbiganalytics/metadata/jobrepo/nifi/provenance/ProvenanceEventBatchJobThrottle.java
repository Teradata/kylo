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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 6/3/17.
 */
public class ProvenanceEventBatchJobThrottle {


    @Inject
    private ProvenanceEventFeedUtil provenanceEventFeedUtil;

    @Inject
    private MetadataEventService eventService;

    private FeedCompletedListener listener = new FeedCompletedListener();


    Cache<String, ProvenanceEventRecordDTO> runningJobs = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

    Map<String,String> relatedFlowFiles = new ConcurrentHashMap<>();

    ListMultimap<String, String> inverseRelatedFlowFiles = ArrayListMultimap.create();


    @PostConstruct
    public void addEventListener() {
        this.eventService.addListener(this.listener);
    }


    private Long timeBetweenStartingJobs(String feedName) {
        return 2000L;
    }



    public boolean isProcessEvent(ProvenanceEventRecordDTO event){
        boolean process = true;
        if (event.isStartOfJob()) {
            String feedName = provenanceEventFeedUtil.getFeedName(event);
            ProvenanceEventRecordDTO lastEvent = runningJobs.getIfPresent(feedName);
            Long timeBetweenJobs = timeBetweenStartingJobs(feedName);
            if (timeBetweenJobs == -1L || lastEvent == null || (lastEvent != null && (event.getEventTime() - lastEvent.getEventTime()) > timeBetweenJobs)) {
                //create it
                runningJobs.put(feedName,event);
                //notify clusters its running
                process = true;
            }
            else {
                //relate it
                relatedFlowFiles.put(event.getJobFlowFileId(),lastEvent.getJobFlowFileId());
                inverseRelatedFlowFiles.put(lastEvent.getJobFlowFileId(),event.getJobFlowFileId());
                //notify clusters its related
                process = false;
            }
        }
        //if the job flow file is part of the related ones reassign it
        if(relatedFlowFiles.containsKey(event.getJobFlowFileId())) {
            event.setJobFlowFileId(relatedFlowFiles.get(event.getJobFlowFileId()));
        }

        return process;
    }

    private class FeedCompletedListener implements MetadataEventListener<FeedOperationStatusEvent> {

        @Override
        public void notify(FeedOperationStatusEvent event) {
            ProvenanceEventRecordDTO provenanceEventRecordDTO = runningJobs.getIfPresent(event.getData().getFeedFlowFileId());
            if(provenanceEventRecordDTO != null){
                //String feedName = provenanceEventFeedUtil.getFeedName(provenanceEventRecordDTO);
                //runningJobs.invalidate(feedName);
                relatedFlowFiles.remove(event.getData().getFeedFlowFileId());
                inverseRelatedFlowFiles.removeAll(event.getData().getFeedFlowFileId());
            }
        }
    }

}
