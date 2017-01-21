package com.thinkbiganalytics.metadata.rest.model.nifi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cache used to help Kylo and NiFi keep the Flows and processors in Sync. This is used by Kylo when creating feeds and registering templates. It is also used by the KyloProvenanceEventReportingTask
 * in NiFi to determine ProvenanceEvents and the processsor/feed information. Created by sr186054 on 12/21/16.
 */
public class NiFiFlowCacheSync {

    private String syncId;
    private NifiFlowCacheSnapshot snapshot;
    private DateTime lastSync;
    private String message;
    private boolean updated = false;


    public static NiFiFlowCacheSync UNAVAILABLE = new NiFiFlowCacheSync("NiFi Flow Cache is unavailable. Try again in a few seconds");

    public static NiFiFlowCacheSync EMPTY(String syncId) {
        NiFiFlowCacheSync empty = new NiFiFlowCacheSync();
        empty.setSyncId(syncId);
        return empty;
    }

    public NiFiFlowCacheSync() {
        this((NifiFlowCacheSnapshot) null);
    }

    public NiFiFlowCacheSync(String message) {
        this((NifiFlowCacheSnapshot) null);
        this.message = message;
    }


    public NiFiFlowCacheSync(NifiFlowCacheSnapshot snapshot) {
        this.snapshot = snapshot;
        if (this.snapshot == null) {
            this.snapshot = new NifiFlowCacheSnapshot();
        }
        this.syncId = UUID.randomUUID().toString();
    }

    public NiFiFlowCacheSync(String syncId, NifiFlowCacheSnapshot snapshot) {
        this.snapshot = snapshot;
        if (this.snapshot == null) {
            this.snapshot = new NifiFlowCacheSnapshot();
        }
        this.syncId = syncId != null ? syncId : UUID.randomUUID().toString();
    }

    @JsonIgnore
    public NifiFlowCacheSnapshot getCache() {
        return snapshot;
    }


    public boolean needsUpdate(DateTime lastUpdated) {

        return (lastUpdated == null || snapshot == null || (snapshot.getSnapshotDate() == null) || (snapshot.getSnapshotDate() != null && lastUpdated.getMillis() != snapshot.getSnapshotDate()
            .getMillis()));
    }

    public Map<String, NiFiFlowCacheConnectionData> getConnectionIdToConnectionUpdatedSinceLastSync(Map<String, NiFiFlowCacheConnectionData> connectionDataMap) {
        MapDifference<String, NiFiFlowCacheConnectionData> diff = Maps.difference(snapshot.getConnectionIdToConnection(), connectionDataMap);
        return diff.entriesOnlyOnRight();
    }


    public Map<String, NiFiFlowCacheConnectionData> getConnectionIdToConnectionUpdatedSinceLastSync(Map<String, String> latestConnectionIdToNameMap,
                                                                                                    Map<String, NiFiFlowCacheConnectionData> latestConnectionDataMap) {
        MapDifference<String, String> diff = Maps.difference(snapshot.getConnectionIdToConnectionName(), latestConnectionIdToNameMap);
        Map<String, NiFiFlowCacheConnectionData> differences = new HashMap<>();
        Map<String, String> diffs = diff.entriesOnlyOnRight();
        if (diffs != null && !diffs.isEmpty()) {
            for (String connId : diffs.keySet()) {
                differences.put(connId, latestConnectionDataMap.get(connId));
            }
        }

        Set<String> updates = diff.entriesDiffering().keySet();
        if (updates != null) {
            for (String key : updates) {
                differences.put(key, latestConnectionDataMap.get(key));
            }
        }

        return differences;
    }


    public Map<String, String> getProcessorIdToProcessorNameUpdatedSinceLastSync(Map<String, String> processorIdToProcessorName) {
        MapDifference<String, String> diff = Maps.difference(snapshot.getAddProcessorIdToProcessorName(), processorIdToProcessorName);
        return diff.entriesOnlyOnRight();
    }

    public Map<String, String> getProcessorIdToProcessGroupIdUpdatedSinceLastSync(Map<String, String> processorIdToFeedProcessGroupId) {
        MapDifference<String, String> diff = Maps.difference(snapshot.getAddProcessorIdToFeedProcessGroupId(), processorIdToFeedProcessGroupId);
        return diff.entriesOnlyOnRight();
    }

    public Map<String, String> getProcessorIdToFeedNameMapUpdatedSinceLastSync(Map<String, String> processorIdToFeedNameMap) {
        MapDifference<String, String> diff = Maps.difference(snapshot.getAddProcessorIdToFeedNameMap(), processorIdToFeedNameMap);
        return diff.entriesOnlyOnRight();
    }


    public Set<String> getStreamingFeedsUpdatedSinceLastSync(Set<String> streamingFeeds) {
        com.google.common.collect.Sets.SetView<String> diff = Sets.difference(streamingFeeds, snapshot.getAllStreamingFeeds());
        return diff.copyInto(new HashSet<>());
    }

    public Set<String> getFeedsUpdatedSinceLastSync(Set<String> feeds) {
        com.google.common.collect.Sets.SetView<String> diff = Sets.difference(feeds, snapshot.getAllFeeds());
        return diff.copyInto(new HashSet<>());
    }

    public NifiFlowCacheSnapshot getSnapshot() {
        return snapshot;
    }

    public DateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(DateTime lastSync) {
        this.lastSync = lastSync;
    }

    public void setSnapshot(NifiFlowCacheSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void reset() {
        this.snapshot = null;
        this.lastSync = null;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public String getSyncId() {
        return syncId;
    }

    public boolean needsUpdate() {
        return this.getSnapshot() == null || this.updated;
    }


    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isUnavailable() {
        return this.equals(UNAVAILABLE);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
