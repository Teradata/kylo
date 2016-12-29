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
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 12/20/16.
 */
public class NifiFlowCacheSnapshot {

    private DateTime snapshotDate;

    //items to update
    private Set<String> addFailureProcessorIds = new HashSet<>();


    /**
     * Map of the Processor Status type to processor ids  (i.e. CRITICAL_FAILURE, Set, NON_CRITICAL_FAILURE > Set, NORMAL > Set)
     */
    private Map<KyloProcessorFlowType, Set<String>> processorFlowTypeToProcessorIdMap = new ConcurrentHashMap<>();

    private Map<String, KyloProcessorFlowType> processorIdToProcessorFlowTypeMap = new ConcurrentHashMap<>();

    //items to add
    private Map<String, String> addProcessorIdToFeedNameMap = new ConcurrentHashMap<>();

    private Map<String, String> addProcessorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> addProcessorIdToProcessorName = new ConcurrentHashMap<>();

    private Set<String> addStreamingFeeds = new HashSet<>();

    private Set<String> removeProcessorIds = new HashSet<>();

    public static NifiFlowCacheSnapshot EMPTY = new Builder()
        .withFailureProcessorId(ImmutableSet.<String>of())
        .withProcessorIdToFeedNameMap(ImmutableMap.of())
        .withProcessorIdToFeedProcessGroupId(ImmutableMap.of())
        .withProcessorIdToProcessorName(ImmutableMap.of())
        .withStreamingFeeds(ImmutableSet.<String>of())
        .build();

    public NifiFlowCacheSnapshot() {

    }

    public NifiFlowCacheSnapshot(Map<KyloProcessorFlowType, Set<String>> processorFlowTypeToProcessorIdMap, Map<String, String> processorIdToFeedNameMap,
                                 Map<String, String> addProcessorIdToFeedProcessGroupId, Map<String, String> addProcessorIdToProcessorName, Set<String> addStreamingFeeds) {
        this.processorFlowTypeToProcessorIdMap = processorFlowTypeToProcessorIdMap;
        this.addProcessorIdToFeedNameMap = processorIdToFeedNameMap;
        this.addProcessorIdToFeedProcessGroupId = addProcessorIdToFeedProcessGroupId;
        this.addProcessorIdToProcessorName = addProcessorIdToProcessorName;
        this.addStreamingFeeds = addStreamingFeeds;
    }

    public static class Builder {

        private Map<KyloProcessorFlowType, Set<String>> processorFlowTypeToProcessorIdMap;

        private Map<String, KyloProcessorFlowType> processorIdToProcessorFlowTypeMap;

        //items to add
        private Map<String, String> addProcessorIdToFeedNameMap;

        private Map<String, String> addProcessorIdToFeedProcessGroupId;

        private Map<String, String> addProcessorIdToProcessorName;

        private Set<String> addStreamingFeeds;

        private DateTime snapshotDate;

        private Set<String> removeProcessorIds;


        private Map<KyloProcessorFlowType, Set<String>> getProcessorFlowTypeToProcessorIdMap() {
            if (processorFlowTypeToProcessorIdMap == null) {
                processorFlowTypeToProcessorIdMap = new HashMap<>();
            }
            return processorFlowTypeToProcessorIdMap;
        }

        private Map<String, KyloProcessorFlowType> getProcessorIdToProcessorFlowTypeMap() {
            if (processorIdToProcessorFlowTypeMap == null) {
                processorIdToProcessorFlowTypeMap = new HashMap<>();
            }
            return processorIdToProcessorFlowTypeMap;
        }


        public Builder withFailureProcessorId(Set<String> addFailureProcessorIds) {
            getProcessorFlowTypeToProcessorIdMap().computeIfAbsent(KyloProcessorFlowType.CRITICAL_FAILURE, key -> new HashSet<>()).addAll(addFailureProcessorIds);
            addFailureProcessorIds.stream().forEach(processorId -> getProcessorIdToProcessorFlowTypeMap().put(processorId, KyloProcessorFlowType.CRITICAL_FAILURE));
            return this;
        }

        public Builder withNonCriticalFailureProcessorIds(Set<String> processorIds) {
            getProcessorFlowTypeToProcessorIdMap().computeIfAbsent(KyloProcessorFlowType.NON_CRITICAL_FAILURE, key -> new HashSet<>()).addAll(processorIds);
            processorIds.stream().forEach(processorId -> getProcessorIdToProcessorFlowTypeMap().put(processorId, KyloProcessorFlowType.NON_CRITICAL_FAILURE));
            return this;
        }

        public Builder withWarningProcessorIds(Set<String> processorIds) {
            getProcessorFlowTypeToProcessorIdMap().computeIfAbsent(KyloProcessorFlowType.WARNING, key -> new HashSet<>()).addAll(processorIds);
            processorIds.stream().forEach(processorId -> getProcessorIdToProcessorFlowTypeMap().put(processorId, KyloProcessorFlowType.WARNING));
            return this;
        }

        public Builder withNormalFlowProcessorIds(Set<String> processorIds) {
            getProcessorFlowTypeToProcessorIdMap().computeIfAbsent(KyloProcessorFlowType.NORMAL_FLOW, key -> new HashSet<>()).addAll(processorIds);
            processorIds.stream().forEach(processorId -> getProcessorIdToProcessorFlowTypeMap().put(processorId, KyloProcessorFlowType.NORMAL_FLOW));
            return this;
        }


        public Builder withProcessorFlowTypeToProcessorIdMap(Map<KyloProcessorFlowType, Set<String>> processorFlowTypeToProcessorIdMap) {
            if (processorFlowTypeToProcessorIdMap != null) {
                getProcessorFlowTypeToProcessorIdMap().putAll(processorFlowTypeToProcessorIdMap);

                //fill the reverse lookup map
                processorFlowTypeToProcessorIdMap.entrySet().stream().forEach(entry -> {
                    Map<String, KyloProcessorFlowType>
                        processorStatusTypeMap1 =
                        entry.getValue().stream().collect(Collectors.toMap(processorId -> processorId, processorId -> entry.getKey()));
                    getProcessorIdToProcessorFlowTypeMap().putAll(processorStatusTypeMap1);
                });
            }

            return this;
        }

        //fill the reverse lookup map
        public Builder withProcessorIdToFlowTypeMap(Map<String, KyloProcessorFlowType> processorIdToFlowTypeMap) {
            if (processorIdToFlowTypeMap != null) {
                getProcessorIdToProcessorFlowTypeMap().putAll(processorIdToFlowTypeMap);

                processorIdToFlowTypeMap.entrySet().stream().forEach(entry -> {
                    getProcessorFlowTypeToProcessorIdMap().computeIfAbsent(entry.getValue(), key -> new HashSet<>()).add(entry.getKey());
                });

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
                new NifiFlowCacheSnapshot(getProcessorFlowTypeToProcessorIdMap(), addProcessorIdToFeedNameMap, addProcessorIdToFeedProcessGroupId, addProcessorIdToProcessorName, addStreamingFeeds);
            snapshot.setSnapshotDate(this.snapshotDate);
            snapshot.setProcessorIdToProcessorFlowTypeMap(getProcessorIdToProcessorFlowTypeMap());
            return snapshot;
        }


    }

    public KyloProcessorFlowType getProcessorFlowType(String processorId) {
        return processorIdToProcessorFlowTypeMap.getOrDefault(processorId, KyloProcessorFlowType.NORMAL_FLOW);
    }


    public Set<String> getAddFailureProcessorIds() {
        if (addFailureProcessorIds == null) {
            this.addFailureProcessorIds = new HashSet<>();
        }
        return addFailureProcessorIds;
    }

    public void setAddFailureProcessorIds(Set<String> addFailureProcessorIds) {
        this.addFailureProcessorIds = addFailureProcessorIds;
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


    public Map<KyloProcessorFlowType, Set<String>> getProcessorFlowTypeToProcessorIdMap() {
        return processorFlowTypeToProcessorIdMap;
    }

    public void setProcessorFlowTypeToProcessorIdMap(Map<KyloProcessorFlowType, Set<String>> processorFlowTypeToProcessorIdMap) {
        this.processorFlowTypeToProcessorIdMap = processorFlowTypeToProcessorIdMap;
    }

    public Map<String, KyloProcessorFlowType> getProcessorIdToProcessorFlowTypeMap() {
        return processorIdToProcessorFlowTypeMap;
    }

    public void setProcessorIdToProcessorFlowTypeMap(Map<String, KyloProcessorFlowType> processorIdToProcessorFlowTypeMap) {
        this.processorIdToProcessorFlowTypeMap = processorIdToProcessorFlowTypeMap;
    }

    public void update(NifiFlowCacheSnapshot syncSnapshot) {
        addProcessorIdToFeedNameMap.putAll(syncSnapshot.getAddProcessorIdToFeedNameMap());
        processorIdToProcessorFlowTypeMap.putAll(syncSnapshot.getProcessorIdToProcessorFlowTypeMap());
        processorFlowTypeToProcessorIdMap.putAll(syncSnapshot.getProcessorFlowTypeToProcessorIdMap());
        addFailureProcessorIds.addAll(syncSnapshot.getAddFailureProcessorIds());
        addProcessorIdToFeedProcessGroupId.putAll(syncSnapshot.getAddProcessorIdToFeedProcessGroupId());
        addProcessorIdToProcessorName.putAll(syncSnapshot.getAddProcessorIdToProcessorName());
        addStreamingFeeds.addAll(syncSnapshot.getAddStreamingFeeds());
        snapshotDate = syncSnapshot.getSnapshotDate();
    }

}
