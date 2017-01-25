package com.thinkbiganalytics.metadata.rest.model.nifi;

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
 * Created by sr186054 on 12/20/16.
 */
public class NifiFlowCacheSnapshot {

    private DateTime snapshotDate;

    //items to add
    private Map<String, String> processorIdToFeedNameMap = new ConcurrentHashMap<>();

    private Map<String, String> processorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();

    private Map<String, String> connectionIdToConnectionName = new ConcurrentHashMap<>();

    private Map<String, NiFiFlowCacheConnectionData> connectionIdToConnection = new ConcurrentHashMap<>();


    private Set<String> allStreamingFeeds = new HashSet<>();

    /**
     * Set of the category.feed names
     */
    private Set<String> allFeeds = new HashSet<>();


    public static NifiFlowCacheSnapshot EMPTY = new Builder()
        .withProcessorIdToFeedNameMap(ImmutableMap.of())
        .withProcessorIdToFeedProcessGroupId(ImmutableMap.of())
        .withProcessorIdToProcessorName(ImmutableMap.of())
        .withStreamingFeeds(ImmutableSet.<String>of())
        .withFeeds(ImmutableSet.<String>of())
        .build();

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




    //DEAL WITH REMOVAL of items... removal /change of streaming feeds!
    public void update(NifiFlowCacheSnapshot syncSnapshot) {
        processorIdToFeedNameMap.putAll(syncSnapshot.getProcessorIdToFeedNameMap());
        processorIdToFeedProcessGroupId.putAll(syncSnapshot.getProcessorIdToFeedProcessGroupId());
        processorIdToProcessorName.putAll(syncSnapshot.getProcessorIdToProcessorName());
        allStreamingFeeds = new HashSet<>(syncSnapshot.getAllStreamingFeeds());
        allFeeds.addAll(syncSnapshot.getAllFeeds());
        snapshotDate = syncSnapshot.getSnapshotDate();
        connectionIdToConnection = syncSnapshot.getConnectionIdToConnection();
        connectionIdToConnectionName = syncSnapshot.getConnectionIdToConnectionName();
    }


}
