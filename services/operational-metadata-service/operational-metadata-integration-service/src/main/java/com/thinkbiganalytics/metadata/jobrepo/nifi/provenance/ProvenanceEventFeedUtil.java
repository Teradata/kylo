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

import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventProvider;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.provenance.KyloProcessorFlowType;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    @PostConstruct
    private void init() {

    }


    public ProvenanceEventFeedUtil() {

    }

    /**
     * Ensure the event has all the necessary information needed to be processed from the NiFi Flow Cache
     *
     * @param event the provenance event
     * @return true if the data exists in the cache, false if not
     */
    public boolean validateNiFiFeedInformation(ProvenanceEventRecordDTO event) {
        String feedName = getFeedName(event.getFirstEventProcessorId());
        if (StringUtils.isBlank(feedName)) {
            feedName = event.getFeedName();
        }
        String processGroupId = getFeedProcessGroupId(event.getFirstEventProcessorId());
        if (StringUtils.isBlank(processGroupId)) {
            processGroupId = event.getFeedProcessGroupId();
        }
        String processorName = getProcessorName(event.getComponentId());
        if (StringUtils.isBlank(processorName)) {
            processorName = event.getComponentName();
        }
        return StringUtils.isNotBlank(feedName) && StringUtils.isNotBlank(processGroupId) && StringUtils.isNotBlank(processorName);
    }


    public ProvenanceEventRecordDTO enrichEventWithFeedInformation(ProvenanceEventRecordDTO event) {
        String feedName = getFeedName(event.getFirstEventProcessorId());
        if (StringUtils.isBlank(feedName) && StringUtils.isNotBlank(event.getFeedName())) {
            feedName = event.getFeedName();
        }
        //if we cant get the feed name check to see if the NiFi flow cache is updated... and wait for it to be updated before processing
        if(StringUtils.isBlank(feedName) && needsUpdateFromCluster()){
            log.info("Unable to find the feed for processorId: {}.  Changes were detected from the cluster.  Refreshing the cache ...",event.getFirstEventProcessorId());
            nifiFlowCache.applyClusterUpdates();
            feedName = getFeedName(event.getFirstEventProcessorId());
            if (StringUtils.isNotBlank(feedName)) {
                log.info("Cache Refreshed.  Found the feed: {} ",feedName);
            }
            else {
                log.info("Cache Refreshed, but still unable to find the feed.  This event {} will not be processed ",event);
            }
        }


        String processGroupId = getFeedProcessGroupId(event.getFirstEventProcessorId());
        if (StringUtils.isBlank(processGroupId)) {
            processGroupId = event.getFeedProcessGroupId();
        }
        String processorName = getProcessorName(event.getComponentId());
        if (StringUtils.isBlank(processorName)) {
            processorName = event.getComponentName();
        }
        event.setFeedName(feedName);
        event.setFeedProcessGroupId(processGroupId);
        event.setComponentName(processorName);
        setProcessorFlowType(event);

        if (StringUtils.isNotBlank(feedName)) {
            OpsManagerFeed feed = opsManagerFeedProvider.findByNameWithoutAcl(feedName);
            if (feed != null && !OpsManagerFeed.NULL_FEED.equals(feed)) {
                event.setStream(feed.isStream());
            }
        }

        return event;
    }

    public OpsManagerFeed getFeed(String feedName) {
        if (StringUtils.isNotBlank(feedName)) {
            OpsManagerFeed feed = opsManagerFeedProvider.findByNameWithoutAcl(feedName);
            if (feed != null && !OpsManagerFeed.NULL_FEED.equals(feed)) {
                return feed;
            }
        }
        return null;
    }

    public OpsManagerFeed getFeed(ProvenanceEventRecordDTO event) {
        String feedName = event.getFeedName();
        if (StringUtils.isBlank(feedName)) {
            feedName = getFeedName(event.getFirstEventProcessorId());
        }
        return getFeed(feedName);
    }


    public KyloProcessorFlowType setProcessorFlowType(ProvenanceEventRecordDTO event) {
        if (event.getProcessorType() == null) {

            if (event.isTerminatedByFailureRelationship()) {
                event.setProcessorType(KyloProcessorFlowType.FAILURE);
                event.setIsFailure(true);
            }
            KyloProcessorFlowType flowType = getProcessorFlowType(event.getSourceConnectionIdentifier());
            event.setProcessorType(flowType);

            if (flowType.equals(KyloProcessorFlowType.FAILURE)) {
                event.setIsFailure(true);
            }
        }
        return event.getProcessorType();
    }


    public boolean isFailure(String sourceConnectionIdentifer) {
        return KyloProcessorFlowType.FAILURE.equals(getProcessorFlowType(sourceConnectionIdentifer));
    }

    private KyloProcessorFlowType getProcessorFlowType(String sourceConnectionIdentifer) {

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

    public boolean isReusableFlowProcessor(String processorId) {
        return getFlowCache().getReusableTemplateProcessorIds().contains(processorId);
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
            OpsManagerFeed feed = opsManagerFeedProvider.findByNameWithoutAcl(feedName);
            if (feed == null || OpsManagerFeed.NULL_FEED.equals(feed)) {
                log.debug("Not processing operational metadata for feed {} , event {} because it is not registered in feed manager ", feedName, event);
                // opsManagerFeedCache.invalidateFeed(feedName);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }


    public String getFeedName(ProvenanceEventRecordDTO event) {
        return getFeedName(event.getFirstEventProcessorId());
    }

    public String getFeedName(String feedProcessorId) {
        return getFlowCache().getProcessorIdToFeedNameMap().get(feedProcessorId);
    }

    public String getFeedProcessGroupId(String feedProcessorId) {
        return feedProcessorId != null ? getFlowCache().getProcessorIdToFeedProcessGroupId().get(feedProcessorId) : null;
    }

    public String getProcessorName(String processorId) {
        return processorId != null ? getFlowCache().getProcessorIdToProcessorName().get(processorId) : null;
    }

    public boolean needsUpdateFromCluster(){
        return nifiFlowCache.needsUpdateFromCluster();
    }

    private NifiFlowCacheSnapshot getFlowCache() {
        return nifiFlowCache.getLatest();
    }


    public boolean isNifiFlowCacheAvailable() {
        return nifiFlowCache.isAvailable();
    }

    public boolean isConnectedToNifi() {
        return nifiFlowCache.isConnectedToNiFi();
    }
}
