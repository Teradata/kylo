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

import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to lookup Kylo Feed information as it relates to a given Provenance Event.
 * This uses the {@link NiFiFlowCacheSync} which holds data from Kylo about the Flows in NiFi.
 */
@Deprecated
public class ProvenanceFeedLookup {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceFeedLookup.class);

    /**
     * The cache of Flow data
     */
    NiFiFlowCacheSync flowCache;

    /**
     * Time when the cache was last updated
     */
    private DateTime lastUpdated;

    /**
     * update this cache with the Kylo server
     *
     * @param updates the returned object from the KyloFlowProvider that has only those items that should be updated
     */
    public void updateFlowCache(NiFiFlowCacheSync updates) {
        if (updates.needsUpdate()) {
            if (flowCache == null) {
                flowCache = updates;
            } else {
                flowCache.getSnapshot().update(updates.getSnapshot());
            }
            lastUpdated = updates.getLastSync();
        }
    }

    /**
     * Returns the size of the Processors Found in Nifi that are cached
     *
     * @return the processors size mapped in the kylo cache
     */
    public Integer getProcessorIdMapSize() {
        return getFlowCache().getProcessorIdToFeedNameMap().size();
    }


    /**
     * Returns the Cache object.  if the cache is null it will return an Empty object NifiFlowCacheSnapshot.EMPTY
     *
     * @return the Cache in use or an NifiFlowCacheSnapshot.EMPTY cache object
     */
    private NifiFlowCacheSnapshot getFlowCache() {
        if (flowCache == null || flowCache.getSnapshot() == null) {
            return NifiFlowCacheSnapshot.EMPTY;
        } else {
            return flowCache.getSnapshot();
        }
    }

    /**
     * Get the FeedName for a given processorId
     *
     * @param processorId the processorId to check
     * @return the name associated with the processor
     */
    private String getFeedName(String processorId) {
        return getFlowCache().getProcessorIdToFeedNameMap().get(processorId);
    }

    /**
     * Get the Feed ProcessorGroup for a given processorId
     *
     * @param processorId the processorId to check
     * @return the feed process group id
     */
    private String getFeedProcessGroupId(String processorId) {
        return getFlowCache().getProcessorIdToFeedProcessGroupId().get(processorId);
    }

    /**
     * Get the Processor Display name for a given processorId
     *
     * @param processorId the processorId to check
     * @return the display name for the processor
     */
    public String getProcessorName(String processorId) {
        return getFlowCache().getProcessorIdToProcessorName().get(processorId);
    }

    public boolean isKyloManagedConnection(String connectionId) {
        return getFlowCache().getConnectionIdToConnectionName().containsKey(connectionId);
    }

    /**
     * Check to make sure the processorId is managed by Kylo and in the cache.
     *
     * @param processorId the processorId to check
     * @return true if the processor is mapped to a Kylo managed feed flow,  false if not
     */
    public boolean isKyloManaged(String processorId) {
        return StringUtils.isNotBlank(getProcessorName(processorId)) || isKyloManagedConnection(processorId);
    }


    /**
     * For the given event, look at the connectionName to determine if the event should be treated as a Failure in Kylo.
     * If the connection name has the word "failure" in it, this event will be marked as a Failure.
     * If the connection name has the word "warn" in it, this event will be marked as a Warning.
     * If the event is "Auto Terminated by Failure" it will be marked as a Failure.
     *
     * Connection data is maintained in the NifiFlowCache
     *
     * @param event the event to set/check
     * @return the event processorType
     */
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


    /**
     * Check to see if this event is a Failure event.
     *
     * @param eventRecordDTO the event to check
     * @return true if it is a failed event, false if not
     * @see this#setProcessorFlowType(ProvenanceEventRecordDTO)
     */
    public boolean isFailureEvent(ProvenanceEventRecordDTO eventRecordDTO) {
        KyloProcessorFlowType processorFlowType = eventRecordDTO.getProcessorType();
        if (processorFlowType == null) {
            processorFlowType = setProcessorFlowType(eventRecordDTO);
        }
        if (processorFlowType != null) {
            return KyloProcessorFlowType.FAILURE.equals(processorFlowType);
        } else {
            return false;
        }
    }

    /**
     * Check to see if this event is registered to a Template that is marked as being a Stream
     *
     * @param eventRecordDTO the event to check
     * @return true if the event is part of a feed/template from Kylo indicated as a stream, false if not
     */
    public boolean isStream(ProvenanceEventRecordDTO eventRecordDTO) {
        return getFlowCache().getAllStreamingFeeds().contains(eventRecordDTO.getFeedName());
    }


}
