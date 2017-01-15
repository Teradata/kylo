package com.thinkbiganalytics.feedmgr.nifi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowTypeRelationship;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailability;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailabilityListener;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 12/20/16. Cache processor definitions in a flow for use by the KyloProvenanceReportingTask
 *
 * Each Processor has an internal {@code flowId} generated why Kylo walks the flow This internal id is used to associate the Feed flow as a template with the Feed flow created when the feed is
 * saved/updated
 *
 * @see com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor
 */
public class NifiFlowCache implements NifiConnectionListener, ModeShapeAvailabilityListener {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowCache.class);

    @Inject
    ModeShapeAvailability modeShapeAvailability;

    @Inject
    LegacyNifiRestClient nifiRestClient;

    @Inject
    private NifiConnectionService nifiConnectionService;

    @Inject
    MetadataService metadataService;

    @Inject
    FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    MetadataAccess metadataAccess;

    private Map<String, String> feedNameToTemplateNameMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> templateNameToFlowIdMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, List<NifiFlowProcessor>>> feedFlowIdProcessorMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, List<NifiFlowProcessor>>> feedProcessorIdProcessorMap = new ConcurrentHashMap<>();

    private Map<String, NifiFlowProcessor> processorIdMap = new ConcurrentHashMap<>();

    //feed , map<
    private Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> feedToProcessorIdToFlowTypeMap = new ConcurrentHashMap<>();

    /**
     * Flag to mark if the cache is loaded or not This is used to determine if the cache is ready to be used
     */
    private boolean loaded = false;

    private boolean nifiConnected = false;

    private boolean modeShapeAvailable = false;

    private Map<String, String> processorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> processorIdToFeedNameMap = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();

    /**
     * Set of the category.feed names for those that are just streaming feeds
     */
    private Set<String> streamingFeeds = new HashSet();

    /**
     * Set of the category.feed names
     */
    private Set<String> allFeeds = new HashSet<>();

    private Map<String, Long> feedLastUpated = new ConcurrentHashMap<>();

    /**
     * Map of the sync id to cache
     */
    private Map<String, NiFiFlowCacheSync> syncMap = new ConcurrentHashMap<>();


    /**
     * Map with the sync Id and the last time that item was sync'd with the system
     * This is used to expire the stale non used caches
     */
    private Map<String, DateTime> lastSyncTimeMap = new ConcurrentHashMap<>();

    private DateTime lastUpdated = null;

    private boolean useTemplateFlowTypeProcessorMap = true;


    public boolean isUseTemplateFlowTypeProcessorMap() {
        return useTemplateFlowTypeProcessorMap;
    }

    public void setUseTemplateFlowTypeProcessorMap(boolean useTemplateFlowTypeProcessorMap) {
        this.useTemplateFlowTypeProcessorMap = useTemplateFlowTypeProcessorMap;
    }

    @Override
    public void onConnected() {
        this.nifiConnected = true;
        checkAndInitializeCache();
    }

    @Override
    public void onDisconnected() {
        this.nifiConnected = false;
    }


    @Override
    public void modeShapeAvailable() {
        this.modeShapeAvailable = true;
        checkAndInitializeCache();
    }

    @PostConstruct
    private void init() {
        nifiConnectionService.subscribeConnectionListener(this);
        modeShapeAvailability.subscribe(this);
        initExpireTimerThread();
    }

    private void checkAndInitializeCache() {
        if (modeShapeAvailable && nifiConnected && !loaded) {
            rebuildAll();
        }
    }

    public NiFiFlowCacheSync refreshAll(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);
        if (!sync.isUnavailable()) {
            sync.reset();
            return syncAndReturnUpdates(sync, false);
        } else {
            return NiFiFlowCacheSync.UNAVAILABLE;
        }
    }

    public boolean isAvailable() {
        return loaded;
    }

    /**
     * Return only the records that were updated since the last sync
     */
    public NiFiFlowCacheSync syncAndReturnUpdates(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);
        if (!sync.isUnavailable()) {
            return syncAndReturnUpdates(sync);
        }
        return sync;
    }

    public NiFiFlowCacheSync getCache(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);

        return sync;
    }

    public NiFiFlowCacheSync previewUpdates(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId, true);
        if (!sync.isUnavailable()) {
            return previewUpdates(sync);
        }
        return sync;
    }


    private NiFiFlowCacheSync previewUpdates(NiFiFlowCacheSync sync) {
        return syncAndReturnUpdates(sync, true);
    }

    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync) {
        return syncAndReturnUpdates(sync, false);
    }

    private NiFiFlowCacheSync getSync(String syncId) {
        return getSync(syncId, false);
    }

    private NiFiFlowCacheSync getSync(String syncId, boolean forPreview) {
        if (isAvailable()) {
            NiFiFlowCacheSync sync = null;
            if (syncId == null || !syncMap.containsKey(syncId)) {
                sync = new NiFiFlowCacheSync();
                if (StringUtils.isNotBlank(syncId)) {
                    sync.setSyncId(syncId);
                }
                if (!forPreview) {
                    syncMap.put(sync.getSyncId(), sync);
                }
            } else {
                sync = syncMap.get(syncId);
            }
            return sync;
        } else {
            return NiFiFlowCacheSync.UNAVAILABLE;
        }
    }


    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync, boolean preview) {
        if (!preview) {
            lastSyncTimeMap.put(sync.getSyncId(), DateTime.now());
        }
        if (sync.needsUpdate(lastUpdated)) {
            Map<String, String> processorIdToFeedNameMapCopy = ImmutableMap.copyOf(processorIdToFeedNameMap);
            Map<String, String> processorIdToFeedProcessGroupIdCopy = ImmutableMap.copyOf(processorIdToFeedProcessGroupId);
            Map<String, String> processorIdToProcessorNameCopy = ImmutableMap.copyOf(processorIdToProcessorName);
            Set<String> streamingFeedsCopy = ImmutableSet.copyOf(streamingFeeds);
            Set<String> allFeedsCopy = ImmutableSet.copyOf(allFeeds);
            Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> feedToProcessorIdToFlowTypeMapCopy = ImmutableMap.copyOf(feedToProcessorIdToFlowTypeMap);


            //get feeds updated since last sync
            NifiFlowCacheSnapshot latest = new NifiFlowCacheSnapshot.Builder()
                .withProcessorIdToFeedNameMap(processorIdToFeedNameMapCopy)
                .withProcessorIdToFeedProcessGroupId(processorIdToFeedProcessGroupIdCopy)
                .withProcessorIdToProcessorName(processorIdToProcessorNameCopy)
                .withStreamingFeeds(streamingFeedsCopy)
                .withFeeds(allFeedsCopy)
                .withFeedToProcessorIdToFlowTypeMap(feedToProcessorIdToFlowTypeMapCopy)
                .withSnapshotDate(lastUpdated).build();
            return syncAndReturnUpdates(sync, latest, preview);
        } else {
            return NiFiFlowCacheSync.EMPTY(sync.getSyncId());
        }
    }


    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync, NifiFlowCacheSnapshot latest, boolean preview) {
        if (latest != null && sync.needsUpdate(latest.getSnapshotDate())) {

            NifiFlowCacheSnapshot updated = new NifiFlowCacheSnapshot.Builder()
                .withProcessorIdToFeedNameMap(sync.getProcessorIdToFeedNameMapUpdatedSinceLastSync(latest.getAddProcessorIdToFeedNameMap()))
                .withProcessorIdToFeedProcessGroupId(sync.getProcessorIdToProcessGroupIdUpdatedSinceLastSync(latest.getAddProcessorIdToFeedProcessGroupId()))
                .withProcessorIdToProcessorName(sync.getProcessorIdToProcessorNameUpdatedSinceLastSync(latest.getAddProcessorIdToProcessorName()))
                .withStreamingFeeds(sync.getStreamingFeedsUpdatedSinceLastSync(latest.getAddStreamingFeeds()))
                .withFeeds(sync.getFeedsUpdatedSinceLastSync(latest.getAllFeeds()))
                .withFeedToProcessorIdToFlowTypeMap(latest.getFeedToProcessorIdToFlowTypeMap())
                .build();
            //reset the pointers on this sync to be the latest
            if (!preview) {
                sync.setSnapshot(latest);
                sync.setLastSync(latest.getSnapshotDate());
            }
            return new NiFiFlowCacheSync(sync.getSyncId(), updated);
        }

        return NiFiFlowCacheSync.EMPTY(sync.getSyncId());
    }


    private void clearAll() {
        processorIdToFeedProcessGroupId.clear();
        processorIdToFeedProcessGroupId.clear();
        processorIdToProcessorName.clear();
        feedToProcessorIdToFlowTypeMap.clear();
        streamingFeeds.clear();
        allFeeds.clear();
        templateNameToFlowIdMap.clear();
        feedNameToTemplateNameMap.clear();
    }

    //@TODO deal with feed versions

    private void populateTemplateMappingCache(RegisteredTemplate template, Map<String, RegisteredTemplate> feedTemplatesMap) {

        template.getFeedNames().stream().forEach(feedName -> {
            if (feedTemplatesMap != null) {
                feedTemplatesMap.put(feedName, template);
            }
            feedNameToTemplateNameMap.put(feedName, template.getTemplateName());
            templateNameToFlowIdMap.computeIfAbsent(template.getTemplateName(), templateName -> new HashMap<>()).putAll(new HashMap<>(template.getProcessorFlowTypesMap()));
            if (template.isStream()) {
                streamingFeeds.add(feedName);
            } else {
                streamingFeeds.remove(feedName);
            }
        });
    }

    public synchronized void rebuildAll() {
        loaded = false;

        List<NifiFlowProcessGroup> allFlows = nifiRestClient.getFeedFlows();

        List<RegisteredTemplate> templates = null;
        clearAll();

        templates = metadataAccess.read(() -> metadataService.getRegisteredTemplates(), MetadataAccess.SERVICE);
        Map<String, RegisteredTemplate> feedTemplatesMap = new HashMap<>();

        Map<String, Map<String, Set<KyloProcessorFlowTypeRelationship>>> feedToProcessorFlowTypMap = new HashMap<>();

        metadataAccess.read(() -> {
            List<FeedManagerFeed> feeds = feedManagerFeedProvider.findAll();
            feeds.stream().forEach(feedManagerFeed -> {
                String json = feedManagerFeed.getFlowProcessorTypes();
                if (StringUtils.isNotBlank(json)) {
                    try {
                        Map<String, List<Map<String, String>>> jsonMap = ObjectMapperSerializer.deserialize(json, Map.class);
                        if (jsonMap != null) {
                            Map<String, Set<KyloProcessorFlowTypeRelationship>>
                                processorFlowTypeMap =
                                jsonMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().stream().map(
                                    m -> new KyloProcessorFlowTypeRelationship(m.getOrDefault("relationship", "all"),
                                                                               KyloProcessorFlowType.valueOf(m.getOrDefault("flowType", KyloProcessorFlowType.NORMAL_FLOW.name()))))
                                    .collect(Collectors.toSet())));
                            feedToProcessorFlowTypMap.put(feedManagerFeed.getQualifiedName(), processorFlowTypeMap);
                        }
                    } catch (Exception e) {
                        log.error("Error attempting to deserialize the stored flowProcessorTypes  on feed {} - {} ", feedManagerFeed.getQualifiedName(), e.getMessage());
                    }
                }
            });

        }, MetadataAccess.SERVICE);




        //populate the template mappings
        templates.stream().forEach(template -> populateTemplateMappingCache(template, feedTemplatesMap));


        //get template associated with flow to determine failure process flow ids
        allFlows.stream().forEach(nifiFlowProcessGroup -> {
            RegisteredTemplate template = feedTemplatesMap.get(nifiFlowProcessGroup.getFeedName());
            if (template != null) {
                Map<String, Set<KyloProcessorFlowTypeRelationship>> flowTypeMap = feedToProcessorFlowTypMap.get(nifiFlowProcessGroup.getFeedName());
                if (flowTypeMap != null) {
                    nifiFlowProcessGroup.resetProcessorsFlowType(flowTypeMap);
                } else if (isUseTemplateFlowTypeProcessorMap()) {
                    nifiFlowProcessGroup.resetProcessorsFlowType(template.getProcessorFlowTypesMap());
                }
                updateFlow(nifiFlowProcessGroup.getFeedName(), template.isStream(), nifiFlowProcessGroup);
            }
        });
        loaded = true;

    }

/*
    public synchronized void updateRegisteredTemplate(RegisteredTemplate template) {

        populateTemplateMappingCache(template, null);

        //update the processortype cache
        List<String> feedNames = feedNameToTemplateNameMap.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(template.getTemplateName())).map(entry -> entry.getKey()).collect(Collectors.toList());
        feedNames.stream().forEach(feedName -> {
          Map<String,List<NifiFlowProcessor>>  flowIdProcessors = feedFlowIdProcessorMap.get(feedName);
            if(flowIdProcessors != null) {
                feedToProcessorIdToFlowTypeMap.get(feedName).clear();
                flowIdProcessors.entrySet().stream().forEach(entry -> {
                    String flowId = entry.getKey();
                    List<NifiFlowProcessor> processors = entry.getValue();
                    KyloProcessorFlowType kyloProcessorFlowType = template.getProcessorFlowTypeMap().get(flowId);
                    if (kyloProcessorFlowType != null) {
                        processors.stream().forEach(nifiFlowProcessor -> {

                            feedToProcessorIdToFlowTypeMap.computeIfAbsent(feedName ,name -> new HashMap<String, KyloProcessorFlowType>()).put(nifiFlowProcessor.getId(), kyloProcessorFlowType);


                        });
                    }

                });
            }

        });

    }
    */


    /**
     * Used by CreateFeed builder
     **/
    public void updateFlow(FeedMetadata feed, NifiFlowProcessGroup feedProcessGroup) {
        // feedProcessGroup.calculateCriticalPathProcessors();
        this.updateFlow(feed, feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values());
    }

    public void updateFlow(String feedName, boolean isStream, NifiFlowProcessGroup feedProcessGroup) {
        //  feedProcessGroup.calculateCriticalPathProcessors();
        this.updateFlow(feedName, isStream, feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values());
    }

    private void updateFlow(String feedName, boolean isStream, String feedProcessGroupId, Collection<NifiFlowProcessor> processors) {
        feedFlowIdProcessorMap.put(feedName, toFlowIdProcessorMap(processors));
        feedProcessorIdProcessorMap.put(feedName, toProcessorIdProcessorMap(processors));

        Map<String, Set<KyloProcessorFlowTypeRelationship>>
            processorFlowTypeMap =
            processors.stream().filter(processor -> !KyloProcessorFlowTypeRelationship.isNormalFlowSet(processor.getProcessorFlowTypes()))
                .collect(Collectors.toMap(flowProcessor1 -> flowProcessor1.getId(), flowProcessor1 -> flowProcessor1.getProcessorFlowTypes()));

      /*
        //Reset the critical path processors for job failure determination
        Set<String> criticalPathProcessorIds = processors.stream().filter(processor -> processor.isCriticalPath()).map(processor -> processor.getId()).collect(Collectors.toSet());
        feedToCriticalPathProcessors.put(feedName,criticalPathProcessorIds);
*/
        feedToProcessorIdToFlowTypeMap.put(feedName, processorFlowTypeMap);

        Map<String, String> processorIdToProcessGroupId = new HashMap<>();
        Map<String, String> processorIdToProcessorName = new HashMap<>();
        processors.stream().forEach(flowProcessor -> {
            processorIdToProcessGroupId.put(flowProcessor.getId(), feedProcessGroupId);
            processorIdToProcessorName.put(flowProcessor.getId(), flowProcessor.getName());
        });
        this.processorIdToFeedProcessGroupId.putAll(processorIdToProcessGroupId);
        this.processorIdToProcessorName.putAll(processorIdToProcessorName);

        processorIdMap.putAll(toProcessorIdMap(processors));
        processorIdToFeedNameMap.putAll(toProcessorIdFeedNameMap(processors, feedName));
        lastUpdated = DateTimeUtil.getNowUTCTime();

        if (isStream) {
            streamingFeeds.add(feedName);
        }
        allFeeds.add(feedName);
        feedLastUpated.put(feedName, lastUpdated.getMillis());

    }

    private void updateFlow(FeedMetadata feed, String feedProcessGroupId, Collection<NifiFlowProcessor> processors) {
        String feedName = feed.getCategoryAndFeedName();
        this.updateFlow(feedName, feed.getRegisteredTemplate().isStream(), feedProcessGroupId, processors);
    }


    private Map<String, NifiFlowProcessor> toProcessorIdMap(Collection<NifiFlowProcessor> processors) {
        return processors.stream().collect(Collectors.toMap(NifiFlowProcessor::getId, Function.identity()));
    }

    private Map<String, String> toProcessorIdFeedNameMap(Collection<NifiFlowProcessor> processors, String feedName) {
        return processors.stream().collect(Collectors.toMap(NifiFlowProcessor::getId, name -> feedName));
    }


    private Map<String, List<NifiFlowProcessor>> toFlowIdProcessorMap(Collection<NifiFlowProcessor> processors) {
        if (processors != null && !processors.isEmpty()) {
            return processors.stream().filter(nifiFlowProcessor -> nifiFlowProcessor.getFlowId() != null).collect(Collectors.groupingBy(NifiFlowProcessor::getFlowId));
        }
        return null;
    }


    private Map<String, List<NifiFlowProcessor>> toProcessorIdProcessorMap(Collection<NifiFlowProcessor> processors) {
        if (processors != null && !processors.isEmpty()) {
            return processors.stream().collect(Collectors.groupingBy(NifiFlowProcessor::getId));
        }
        return new HashMap<>();
    }

    public CacheSummary cacheSummary() {
        return CacheSummary.build(syncMap);
    }


    public static class CacheSummary {

        private Map<String, Integer> summary = new HashMap<>();
        private Integer cachedSyncIds;

        public static CacheSummary build(Map<String, NiFiFlowCacheSync> syncMap) {
            Map<String, Integer>
                cacheIds =
                syncMap.entrySet().stream().collect(Collectors.toMap(stringNiFiFlowCacheSyncEntry -> stringNiFiFlowCacheSyncEntry.getKey(),
                                                                     stringNiFiFlowCacheSyncEntry1 -> stringNiFiFlowCacheSyncEntry1.getValue().getSnapshot().getAddProcessorIdToFeedNameMap().size()));
            return new CacheSummary(cacheIds);
        }

        public CacheSummary() {

        }

        private CacheSummary(Map<String, Integer> cacheIds) {
            this.summary = cacheIds;
            this.cachedSyncIds = cacheIds.keySet().size();
        }

        public Map<String, Integer> getSummary() {
            return summary;
        }

        public void setSummary(Map<String, Integer> summary) {
            this.summary = summary;
        }

        public Integer getCachedSyncIds() {
            return cachedSyncIds;
        }

        public void setCachedSyncIds(Integer cachedSyncIds) {
            this.cachedSyncIds = cachedSyncIds;
        }
    }


    private void initExpireTimerThread() {
        long timer = 30; // run ever 30 sec to check and expire
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            checkAndExpireUnusedCache();
        }, timer, timer, TimeUnit.SECONDS);


    }

    public void checkAndExpireUnusedCache() {
        int minutes = 60;
        try {

            long expireAfter = minutes * 1000 * 60; //60 min
            Set<String> itemsRemoved = new HashSet<>();
            //find cache items that havent been synced in allotted time
            lastSyncTimeMap.entrySet().stream().filter(entry -> ((DateTime.now().getMillis() - entry.getValue().getMillis()) > expireAfter)).forEach(entry -> {
                syncMap.remove(entry.getKey());
                itemsRemoved.add(entry.getKey());
                log.info("Expiring Cache {}.  This cache has not been used in over {} minutes", minutes, entry.getKey());
            });
            itemsRemoved.stream().forEach(item -> lastSyncTimeMap.remove(item));

        } catch (Exception e) {
            log.error("Error attempting to invalidate flow cache for items not touched in {} or more minutes", minutes, e);
        }
    }


}
