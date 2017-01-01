package com.thinkbiganalytics.metadata.rest.model.nifi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sr186054 on 12/20/16.
 */
public class NifiFlowCacheSnapshot {

    private DateTime snapshotDate;

    //items to add
    private Map<String, String> addProcessorIdToFeedNameMap = new ConcurrentHashMap<>();

    private Map<String, String> addProcessorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> addProcessorIdToProcessorName = new ConcurrentHashMap<>();


    private Map<String, Map<String, KyloProcessorFlowType>> feedToProcessorIdToFlowTypeMap = new ConcurrentHashMap<>();


    private Set<String> addStreamingFeeds = new HashSet<>();

    private Set<String> removeProcessorIds = new HashSet<>();

    public static NifiFlowCacheSnapshot EMPTY = new Builder()
        .withProcessorIdToFeedNameMap(ImmutableMap.of())
        .withProcessorIdToFeedProcessGroupId(ImmutableMap.of())
        .withProcessorIdToProcessorName(ImmutableMap.of())
        .withStreamingFeeds(ImmutableSet.<String>of())
        .build();

    public NifiFlowCacheSnapshot() {

    }

    public NifiFlowCacheSnapshot(Map<String, String> processorIdToFeedNameMap,
                                 Map<String, String> addProcessorIdToFeedProcessGroupId, Map<String, String> addProcessorIdToProcessorName, Set<String> addStreamingFeeds) {
        this.addProcessorIdToFeedNameMap = processorIdToFeedNameMap;
        this.addProcessorIdToFeedProcessGroupId = addProcessorIdToFeedProcessGroupId;
        this.addProcessorIdToProcessorName = addProcessorIdToProcessorName;
        this.addStreamingFeeds = addStreamingFeeds;
    }

    public static class Builder {


        private Map<String, Map<String, KyloProcessorFlowType>> feedToProcessorIdToFlowTypeMap;

        //items to add
        private Map<String, String> addProcessorIdToFeedNameMap;

        private Map<String, String> addProcessorIdToFeedProcessGroupId;

        private Map<String, String> addProcessorIdToProcessorName;

        private Set<String> addStreamingFeeds;

        private DateTime snapshotDate;

        private Set<String> removeProcessorIds;


        private Map<String, Map<String, KyloProcessorFlowType>> getFeedToProcessorIdToFlowTypeMap() {
            if (feedToProcessorIdToFlowTypeMap == null) {
                feedToProcessorIdToFlowTypeMap = new HashMap<>();
            }
            return feedToProcessorIdToFlowTypeMap;
        }


        public Builder withFeedToProcessorIdToFlowTypeMap(Map<String, Map<String, KyloProcessorFlowType>> feedToProcessorIdToFlowTypeMap) {
            if (feedToProcessorIdToFlowTypeMap != null) {
                getFeedToProcessorIdToFlowTypeMap().putAll(feedToProcessorIdToFlowTypeMap);
            }

            return this;
        }


        public Builder withProcessorIdToFeedNameMap(Map<String, String> addProcessorIdToFeedNameMap) {
            this.addProcessorIdToFeedNameMap = addProcessorIdToFeedNameMap;
            return this;
        }


        public Builder withProcessorIdToFeedProcessGroupId(Map<String, String> addProcessorIdToFeedProcessGroupId) {
            this.addProcessorIdToFeedProcessGroupId = addProcessorIdToFeedProcessGroupId;
            return this;
        }

        public Builder withProcessorIdToProcessorName(Map<String, String> addProcessorIdToProcessorName) {
            this.addProcessorIdToProcessorName = addProcessorIdToProcessorName;
            return this;
        }


        public Builder withStreamingFeeds(Set<String> addStreamingFeeds) {
            this.addStreamingFeeds = addStreamingFeeds;
            return this;
        }

        public Builder withSnapshotDate(DateTime snapshotDate) {
            this.snapshotDate = snapshotDate;
            return this;
        }


        public NifiFlowCacheSnapshot build() {
            NifiFlowCacheSnapshot
                snapshot =
                new NifiFlowCacheSnapshot(addProcessorIdToFeedNameMap, addProcessorIdToFeedProcessGroupId, addProcessorIdToProcessorName, addStreamingFeeds);
            snapshot.setSnapshotDate(this.snapshotDate);
            snapshot.setFeedToProcessorIdToFlowTypeMap(getFeedToProcessorIdToFlowTypeMap());
            return snapshot;
        }


    }

    public KyloProcessorFlowType getProcessorFlowType(String feedName, String processorId) {
        Map<String, KyloProcessorFlowType> map = getFeedToProcessorIdToFlowTypeMap().get(feedName);
        if (map != null) {
            return map.getOrDefault(processorId, KyloProcessorFlowType.NORMAL_FLOW);
        } else {
            return KyloProcessorFlowType.NORMAL_FLOW;
        }
    }

    public Map<String, String> getAddProcessorIdToFeedNameMap() {
        if (addProcessorIdToFeedNameMap == null) {
            this.addProcessorIdToFeedNameMap = new HashMap<>();
        }
        return addProcessorIdToFeedNameMap;
    }

    public void setAddProcessorIdToFeedNameMap(Map<String, String> addProcessorIdToFeedNameMap) {
        this.addProcessorIdToFeedNameMap = addProcessorIdToFeedNameMap;
    }

    public Set<String> getRemoveProcessorIds() {
        return removeProcessorIds;
    }

    public void setRemoveProcessorIds(Set<String> removeProcessorIds) {
        this.removeProcessorIds = removeProcessorIds;
    }


    public Map<String, String> getAddProcessorIdToFeedProcessGroupId() {
        if (addProcessorIdToFeedProcessGroupId == null) {
            this.addProcessorIdToFeedProcessGroupId = new HashMap<>();
        }
        return addProcessorIdToFeedProcessGroupId;
    }

    public void setAddProcessorIdToFeedProcessGroupId(Map<String, String> addProcessorIdToFeedProcessGroupId) {
        this.addProcessorIdToFeedProcessGroupId = addProcessorIdToFeedProcessGroupId;
    }

    public Map<String, String> getAddProcessorIdToProcessorName() {
        if (addProcessorIdToProcessorName == null) {
            this.addProcessorIdToProcessorName = new HashMap<>();
        }
        return addProcessorIdToProcessorName;
    }

    public void setAddProcessorIdToProcessorName(Map<String, String> addProcessorIdToProcessorName) {
        this.addProcessorIdToProcessorName = addProcessorIdToProcessorName;
    }

    public Set<String> getAddStreamingFeeds() {
        if (addStreamingFeeds == null) {
            return new HashSet<>();
        }
        return addStreamingFeeds;
    }

    public void setAddStreamingFeeds(Set<String> addStreamingFeeds) {
        this.addStreamingFeeds = addStreamingFeeds;
    }

    public DateTime getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(DateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
    }


    public Map<String, Map<String, KyloProcessorFlowType>> getFeedToProcessorIdToFlowTypeMap() {
        return feedToProcessorIdToFlowTypeMap;
    }

    public void setFeedToProcessorIdToFlowTypeMap(Map<String, Map<String, KyloProcessorFlowType>> feedToProcessorIdToFlowTypeMap) {
        this.feedToProcessorIdToFlowTypeMap = feedToProcessorIdToFlowTypeMap;
    }

    public void update(NifiFlowCacheSnapshot syncSnapshot) {
        addProcessorIdToFeedNameMap.putAll(syncSnapshot.getAddProcessorIdToFeedNameMap());
        addProcessorIdToFeedProcessGroupId.putAll(syncSnapshot.getAddProcessorIdToFeedProcessGroupId());
        addProcessorIdToProcessorName.putAll(syncSnapshot.getAddProcessorIdToProcessorName());
        addStreamingFeeds.addAll(syncSnapshot.getAddStreamingFeeds());
        snapshotDate = syncSnapshot.getSnapshotDate();
    }

}
