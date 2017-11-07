package com.thinkbiganalytics.metadata.rest.model.nifi;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 */
public class NifiFlowCacheSnapshot {

    public static final NifiFlowCacheSnapshot EMPTY = new Builder()
        .withProcessorIdToFeedNameMap(ImmutableMap.of())
        .withProcessorIdToFeedProcessGroupId(ImmutableMap.of())
        .withProcessorIdToProcessorName(ImmutableMap.of())
        .withStreamingFeeds(ImmutableSet.<String>of())
        .withFeeds(ImmutableSet.<String>of())
        .build();
    private DateTime snapshotDate;
    //items to add
    private Map<String, String> processorIdToFeedNameMap = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToFeedProcessGroupId = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();
    private Map<String, String> connectionIdToConnectionName = new ConcurrentHashMap<>();
    private Map<String, NiFiFlowCacheConnectionData> connectionIdToConnection = new ConcurrentHashMap<>();
    private Set<String> allStreamingFeeds = new HashSet<>();
    private Set<String> reusableTemplateProcessorIds = new HashSet<>();
    /**
     * Set of the category.feed names
     */
    private Set<String> allFeeds = new HashSet<>();

    public NifiFlowCacheSnapshot() {

    }

    public NifiFlowCacheSnapshot(Map<String, String> processorIdToFeedNameMap,
                                 Map<String, String> processorIdToFeedProcessGroupId, Map<String, String> processorIdToProcessorName, Set<String> allStreamingFeeds, Set<String> allFeeds) {
        this.processorIdToFeedNameMap = processorIdToFeedNameMap;
        this.processorIdToFeedProcessGroupId = processorIdToFeedProcessGroupId;
        this.processorIdToProcessorName = processorIdToProcessorName;
        this.allStreamingFeeds = allStreamingFeeds;
        this.allFeeds = allFeeds;
    }

    public Map<String, String> getProcessorIdToFeedNameMap() {
        if (processorIdToFeedNameMap == null) {
            this.processorIdToFeedNameMap = new HashMap<>();
        }
        return processorIdToFeedNameMap;
    }

    public void setProcessorIdToFeedNameMap(Map<String, String> processorIdToFeedNameMap) {
        this.processorIdToFeedNameMap = processorIdToFeedNameMap;
    }

    public Map<String, String> getProcessorIdToFeedProcessGroupId() {
        if (processorIdToFeedProcessGroupId == null) {
            this.processorIdToFeedProcessGroupId = new HashMap<>();
        }
        return processorIdToFeedProcessGroupId;
    }

    public void setProcessorIdToFeedProcessGroupId(Map<String, String> processorIdToFeedProcessGroupId) {
        this.processorIdToFeedProcessGroupId = processorIdToFeedProcessGroupId;
    }

    public Map<String, String> getProcessorIdToProcessorName() {
        if (processorIdToProcessorName == null) {
            this.processorIdToProcessorName = new HashMap<>();
        }
        return processorIdToProcessorName;
    }

    public void setProcessorIdToProcessorName(Map<String, String> processorIdToProcessorName) {
        this.processorIdToProcessorName = processorIdToProcessorName;
    }

    public Set<String> getAllStreamingFeeds() {
        if (allStreamingFeeds == null) {
            return new HashSet<>();
        }
        return allStreamingFeeds;
    }

    public void setAllStreamingFeeds(Set<String> allStreamingFeeds) {
        this.allStreamingFeeds = allStreamingFeeds;
    }

    public DateTime getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(DateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Set<String> getAllFeeds() {
        return allFeeds;
    }

    public void setAllFeeds(Set<String> allFeeds) {
        this.allFeeds = allFeeds;
    }

    public Map<String, String> getConnectionIdToConnectionName() {
        return connectionIdToConnectionName;
    }

    public void setConnectionIdToConnectionName(Map<String, String> connectionIdToConnectionName) {
        this.connectionIdToConnectionName = connectionIdToConnectionName;
    }

    public Map<String, NiFiFlowCacheConnectionData> getConnectionIdToConnection() {
        return connectionIdToConnection;
    }

    public void setConnectionIdToConnection(Map<String, NiFiFlowCacheConnectionData> connectionIdToConnection) {
        this.connectionIdToConnection = connectionIdToConnection;
    }

    public Set<String> getReusableTemplateProcessorIds() {
        return reusableTemplateProcessorIds;
    }

    public void setReusableTemplateProcessorIds(Set<String> reusableTemplateProcessorIds) {
        this.reusableTemplateProcessorIds = reusableTemplateProcessorIds;
    }

    public void update(NifiFlowCacheSnapshot syncSnapshot) {
        processorIdToFeedNameMap.putAll(syncSnapshot.getProcessorIdToFeedNameMap());
        processorIdToFeedProcessGroupId.putAll(syncSnapshot.getProcessorIdToFeedProcessGroupId());
        processorIdToProcessorName.putAll(syncSnapshot.getProcessorIdToProcessorName());
        allStreamingFeeds = new HashSet<>(syncSnapshot.getAllStreamingFeeds());
        allFeeds.addAll(syncSnapshot.getAllFeeds());
        snapshotDate = syncSnapshot.getSnapshotDate();
        connectionIdToConnection.putAll(syncSnapshot.getConnectionIdToConnection());
        connectionIdToConnectionName.putAll(syncSnapshot.getConnectionIdToConnectionName());
        reusableTemplateProcessorIds.addAll(syncSnapshot.getReusableTemplateProcessorIds());
    }

    public static class Builder {

        //items to add
        private Map<String, String> processorIdToFeedNameMap;

        private Map<String, String> processorIdToFeedProcessGroupId;

        private Map<String, String> processorIdToProcessorName;

        private Set<String> streamingFeeds;

        private Map<String, NiFiFlowCacheConnectionData> connectionIdToConnection;

        /**
         * Set of the category.feed names
         */
        private Set<String> allFeeds = new HashSet<>();

        private Set<String> reusableTemplateProcessorIds = new HashSet<>();
        private DateTime snapshotDate;


        public Builder withConnections(Map<String, NiFiFlowCacheConnectionData> connections) {
            this.connectionIdToConnection = connections;
            return this;
        }

        public Builder withProcessorIdToFeedNameMap(Map<String, String> addProcessorIdToFeedNameMap) {
            this.processorIdToFeedNameMap = addProcessorIdToFeedNameMap;
            return this;
        }


        public Builder withProcessorIdToFeedProcessGroupId(Map<String, String> addProcessorIdToFeedProcessGroupId) {
            this.processorIdToFeedProcessGroupId = addProcessorIdToFeedProcessGroupId;
            return this;
        }

        public Builder withProcessorIdToProcessorName(Map<String, String> addProcessorIdToProcessorName) {
            this.processorIdToProcessorName = addProcessorIdToProcessorName;
            return this;
        }


        public Builder withStreamingFeeds(Set<String> addStreamingFeeds) {
            this.streamingFeeds = addStreamingFeeds;
            return this;
        }

        public Builder withFeeds(Set<String> feeds) {
            this.allFeeds = feeds;
            return this;
        }

        public Builder withReusableTemplateProcessorIds(Set<String> processorIds) {
            this.reusableTemplateProcessorIds = processorIds;
            return this;
        }

        public Builder withSnapshotDate(DateTime snapshotDate) {
            this.snapshotDate = snapshotDate;
            return this;
        }


        public NifiFlowCacheSnapshot build() {
            NifiFlowCacheSnapshot
                snapshot =
                new NifiFlowCacheSnapshot(processorIdToFeedNameMap, processorIdToFeedProcessGroupId, processorIdToProcessorName, streamingFeeds, allFeeds);
            snapshot.setSnapshotDate(this.snapshotDate);
            snapshot.setConnectionIdToConnection(connectionIdToConnection);
            if (connectionIdToConnection != null) {
                Map<String, String> connectionIdNameMap = connectionIdToConnection.values().stream().collect(Collectors.toMap(conn -> conn.getConnectionIdentifier(), conn -> conn.getName()));
                snapshot.setConnectionIdToConnectionName(connectionIdNameMap);
            }
            return snapshot;
        }


    }


}
