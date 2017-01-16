package com.thinkbiganalytics.metadata.rest.model.nifi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowTypeRelationship;

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
    private Map<String, String> addProcessorIdToFeedNameMap = new ConcurrentHashMap<>();

    private Map<String, String> addProcessorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> addProcessorIdToProcessorName = new ConcurrentHashMap<>();


    private Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> feedToProcessorIdToFlowTypeMap = new ConcurrentHashMap<>();

    private Set<String> allStreamingFeeds = new HashSet<>();

    /**
     * Set of the category.feed names
     */
    private Set<String> allFeeds = new HashSet<>();

    private Set<String> removeProcessorIds = new HashSet<>();

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
                                 Map<String, String> addProcessorIdToFeedProcessGroupId, Map<String, String> addProcessorIdToProcessorName, Set<String> allStreamingFeeds, Set<String> allFeeds) {
        this.addProcessorIdToFeedNameMap = processorIdToFeedNameMap;
        this.addProcessorIdToFeedProcessGroupId = addProcessorIdToFeedProcessGroupId;
        this.addProcessorIdToProcessorName = addProcessorIdToProcessorName;
        this.allStreamingFeeds = allStreamingFeeds;
        this.allFeeds = allFeeds;
    }

    public static class Builder {


        private Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> feedToProcessorIdToFlowTypeMap;

        //items to add
        private Map<String, String> addProcessorIdToFeedNameMap;

        private Map<String, String> addProcessorIdToFeedProcessGroupId;

        private Map<String, String> addProcessorIdToProcessorName;

        private Set<String> addStreamingFeeds;


        /**
         * Set of the category.feed names
         */
        private Set<String> allFeeds = new HashSet<>();

        private DateTime snapshotDate;

        private Set<String> removeProcessorIds;


        private Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> getFeedToProcessorIdToFlowTypeMap() {
            if (feedToProcessorIdToFlowTypeMap == null) {
                feedToProcessorIdToFlowTypeMap = new HashMap<>();
            }
            return feedToProcessorIdToFlowTypeMap;
        }


        public Builder withFeedToProcessorIdToFlowTypeMap(Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> feedToProcessorIdToFlowTypeMap) {
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
                new NifiFlowCacheSnapshot(addProcessorIdToFeedNameMap, addProcessorIdToFeedProcessGroupId, addProcessorIdToProcessorName, addStreamingFeeds, allFeeds);
            snapshot.setSnapshotDate(this.snapshotDate);
            snapshot.setFeedToProcessorIdToFlowTypeMap(getFeedToProcessorIdToFlowTypeMap());
            return snapshot;
        }


    }

    public Set<KyloProcessorFlowTypeRelationship> getProcessorFlowTypes(String feedName, String processorId) {
        Map<String, Set<KyloProcessorFlowTypeRelationship>> map = getFeedToProcessorIdToFlowTypeMap().get(feedName);
        if (map != null) {
            return map.getOrDefault(processorId, KyloProcessorFlowTypeRelationship.DEFAULT_SET);
        } else {
            return KyloProcessorFlowTypeRelationship.DEFAULT_SET;
        }
    }

    public Map<String, KyloProcessorFlowType> getProcessorFlowTypesAsMap(String feedName, String processorId) {
        return getProcessorFlowTypes(feedName, processorId).stream().collect(Collectors.toMap(kyloProcessorFlowTypeRelationship -> kyloProcessorFlowTypeRelationship.getRelationship(),
                                                                                              kyloProcessorFlowTypeRelationship -> kyloProcessorFlowTypeRelationship.getFlowType()));
    }

    public boolean hasProcessorFlowTypesMapped(String feedName) {
        Map<String, Set<KyloProcessorFlowTypeRelationship>> map = getFeedToProcessorIdToFlowTypeMap().get(feedName);
        return map != null & !map.isEmpty();
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


    public Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> getFeedToProcessorIdToFlowTypeMap() {
        return feedToProcessorIdToFlowTypeMap;
    }

    public void setFeedToProcessorIdToFlowTypeMap(Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> feedToProcessorIdToFlowTypeMap) {
        this.feedToProcessorIdToFlowTypeMap = feedToProcessorIdToFlowTypeMap;
    }

    public Set<String> getAllFeeds() {
        return allFeeds;
    }

    public void setAllFeeds(Set<String> allFeeds) {
        this.allFeeds = allFeeds;
    }


    //DEAL WITH REMOVAL of items... removal /change of streaming feeds!
    public void update(NifiFlowCacheSnapshot syncSnapshot) {
        addProcessorIdToFeedNameMap.putAll(syncSnapshot.getAddProcessorIdToFeedNameMap());
        addProcessorIdToFeedProcessGroupId.putAll(syncSnapshot.getAddProcessorIdToFeedProcessGroupId());
        addProcessorIdToProcessorName.putAll(syncSnapshot.getAddProcessorIdToProcessorName());
        allStreamingFeeds.addAll(syncSnapshot.getAllStreamingFeeds());
        allFeeds.addAll(syncSnapshot.getAllFeeds());
        snapshotDate = syncSnapshot.getSnapshotDate();
        feedToProcessorIdToFlowTypeMap.putAll(syncSnapshot.getFeedToProcessorIdToFlowTypeMap());
    }

}
