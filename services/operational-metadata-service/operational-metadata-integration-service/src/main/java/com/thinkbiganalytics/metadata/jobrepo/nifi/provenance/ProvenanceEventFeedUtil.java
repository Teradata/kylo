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
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventProvider;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.provenance.KyloProcessorFlowType;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    OpsManagerFeedProvider opsManagerFeedProvider;

    /**
     * Empty feed object for Loading Cache
     */
   public static OpsManagerFeed NULL_FEED = new OpsManagerFeed() {
        @Override
        public ID getId() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public FeedType getFeedType() {
            return null;
        }

        @Override
        public boolean isStream() {
            return false;
        }
    };

    /**
     * Cache of the Ops Manager Feed Object to ensure that we only process and create Job Executions for feeds that have been registered in Feed Manager
     */
    LoadingCache<String, OpsManagerFeed> opsManagerFeedCache = null;


    Cache<String, ProvenanceEventRecordDTO> runningJobs = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

    Map<String,String> relatedFlowFiles = new ConcurrentHashMap<>();

    ListMultimap<String, String> inverseRelatedFlowFiles = ArrayListMultimap.create();

    public ProvenanceEventFeedUtil() {


        // create the loading Cache to get the Feed Manager Feeds.  If its not in the cache, query the JCR store for the Feed object otherwise return the NULL_FEED object
        opsManagerFeedCache = CacheBuilder.newBuilder().build(new CacheLoader<String, OpsManagerFeed>() {
                                                                  @Override
                                                                  public OpsManagerFeed load(String feedName) throws Exception {
                                                                      OpsManagerFeed feed = null;
                                                                      try {
                                                                          feed = metadataAccess.commit(() -> opsManagerFeedProvider.findByName(feedName),
                                                                                                       MetadataAccess.SERVICE);
                                                                      } catch (Exception e) {

                                                                      }
                                                                      return feed == null ? NULL_FEED : feed;
                                                                  }

                                                              }
        );
    }

    public ProvenanceEventRecordDTO enrichEventWithFeedInformation(ProvenanceEventRecordDTO event) {
        String feedName = getFeedName(event.getFirstEventProcessorId());
        String processGroupId = getFeedProcessGroupId(event.getFirstEventProcessorId());
        String processorName = getProcessorName(event.getComponentId());
        event.setFeedName(feedName);
        event.setFeedProcessGroupId(processGroupId);
        event.setComponentName(processorName);
        setProcessorFlowType(event);

        if (StringUtils.isNotBlank(feedName)) {
            OpsManagerFeed feed = opsManagerFeedCache.getUnchecked(feedName);
            if (feed != null && !ProvenanceEventFeedUtil.NULL_FEED.equals(feed)) {
                event.setStream(feed.isStream());
            }
        }
        return event;
    }



    public KyloProcessorFlowType setProcessorFlowType(ProvenanceEventRecordDTO event) {
        if (event.getProcessorType() == null) {

            if (event.isTerminatedByFailureRelationship()) {
                event.setProcessorType(KyloProcessorFlowType.FAILURE);
                event.setIsFailure(true);
            }
            KyloProcessorFlowType flowType = getProcessorFlowType(event.getSourceConnectionIdentifier());
            event.setProcessorType(flowType);

            if(flowType.equals(KyloProcessorFlowType.FAILURE)){
                event.setIsFailure(true);
            }
        }
        return event.getProcessorType();
    }


    public boolean isFailure(String sourceConnectionIdentifer){
        return KyloProcessorFlowType.FAILURE.equals(getProcessorFlowType(sourceConnectionIdentifer));
    }

    private KyloProcessorFlowType getProcessorFlowType(String sourceConnectionIdentifer){

        if (sourceConnectionIdentifer != null) {
            NiFiFlowCacheConnectionData connectionData = getFlowCache().getConnectionIdToConnection().get(sourceConnectionIdentifer);
            if (connectionData != null && connectionData.getName() != null) {
                if (connectionData.getName().toLowerCase().contains("failure")) {
                   return KyloProcessorFlowType.FAILURE;
                } else if (connectionData.getName().toLowerCase().contains("warn")) {
                  return KyloProcessorFlowType.WARNING;
                }
            }
        }
        return KyloProcessorFlowType.NORMAL_FLOW;


    }

    /**
     * Check to see if the event has a relationship to Feed Manager
     * In cases where a user is experimenting in NiFi and not using Feed Manager the event would not be registered
     *
     * @param event a provenance event
     * @return {@code true} if the event has a feed associaetd with it {@code false} if there is no feed associated with it
     */
    public boolean isRegisteredWithFeedManager(ProvenanceEventRecordDTO event) {

        String feedName = event.getFeedName();
        if (StringUtils.isNotBlank(feedName)) {
            OpsManagerFeed feed = opsManagerFeedCache.getUnchecked(feedName);
            if (feed == null || ProvenanceEventFeedUtil.NULL_FEED.equals(feed)) {
                log.debug("Not processing operational metadata for feed {} , event {} because it is not registered in feed manager ", feedName, event);
                opsManagerFeedCache.invalidate(feedName);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

public void deletedFeed(String feedName){
        opsManagerFeedCache.invalidate(feedName);
}


public String getFeedName(ProvenanceEventRecordDTO event){
        return getFeedName(event.getFirstEventProcessorId());
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


     private NifiFlowCacheSnapshot getFlowCache() {
        return nifiFlowCache.getLatest();
    }






}
