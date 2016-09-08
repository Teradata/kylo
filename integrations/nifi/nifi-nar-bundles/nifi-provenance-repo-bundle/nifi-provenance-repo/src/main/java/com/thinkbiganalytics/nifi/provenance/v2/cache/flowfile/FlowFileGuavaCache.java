package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.IdReferenceFlowFile;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.util.SpringApplicationContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * A cache of Flowfiles active in the system
 * The FlowFile Graph is build when a ProvenanceEvent comes into the system via the DelayedProvenanceEventProducer which delegates to the CacheUtil to get/build the graph
 * In addition to the in memory flowfile cache the system is also storing a lightweight IdReference Cache to disk.
 * If Nifi goes down during processing the Flowfiles will be restored from this disk cache and the graph will be rebuilt.
 * If the attempt to get the flowfile from this guava cache is not there the system will first attempt to find that item in the disk cache and build/return the object to the guava cache, otherwise it will create a new flowfile object
 *
 * A timer runs on a given interval to invalidate and remove flowfiles that are finished
 *
 * Created by sr186054 on 8/11/16.
 */
public class FlowFileGuavaCache {

    private static final Logger log = LoggerFactory.getLogger(FlowFileGuavaCache.class);


    private static FlowFileGuavaCache instance = new FlowFileGuavaCache();

    public static FlowFileGuavaCache instance() {
        return instance;
    }

    private final LoadingCache<String, ActiveFlowFile> cache;

    private FlowFileGuavaCache() {
        cache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, ActiveFlowFile>() {
                                                                  @Override
                                                                  public ActiveFlowFile load(String id) throws Exception {
                                                                      return loadFromCache(id);
                                                                  }

                                                              }
        );
        initTimerThread();
    }

    /**
     * Load the FlowFile from the saved "IdReference MapDB " or creates a new FlowFile object
     * @param parentFlowFile
     * @return
     */
    private ActiveFlowFile loadGraph(IdReferenceFlowFile parentFlowFile) {
        ActiveFlowFile parent = cache.getIfPresent(parentFlowFile.getId());
        if (parentFlowFile != null) {
            if (parent == null) {
                parent = new ActiveFlowFile(parentFlowFile.getId());
                parent.assignFeedInformation(parentFlowFile.getFeedName(), parentFlowFile.getFeedProcessGroupId());
                if (parentFlowFile.isRootFlowFile()) {
                    parent.markAsRootFlowFile();
                }
                parent.setCurrentFlowFileComplete(parentFlowFile.isComplete());
                cache.put(parent.getId(), parent);
            }

            for (String childId : parentFlowFile.getChildIds()) {
                ActiveFlowFile child = cache.getIfPresent(childId);
                if (child == null) {
                    IdReferenceFlowFile idReferenceFlowFile = FlowFileMapDbCache.instance().getCachedFlowFile(childId);
                    child = loadGraph(idReferenceFlowFile);
                    parent.addChild(child);
                    child.addParent(parent);
                    cache.put(childId, child);
                }
            }
        }
        return parent;
    }

    private ActiveFlowFile loadFromCache(String flowFileId) {
        ActiveFlowFile ff = null;
        IdReferenceFlowFile idReferenceFlowFile = FlowFileMapDbCache.instance().getCachedFlowFile(flowFileId);
        if (idReferenceFlowFile != null) {
            //try to get the root file and build the graph and then return this ff
            if (StringUtils.isNotBlank(idReferenceFlowFile.getRootFlowFileId())) {
                ActiveFlowFile rootFile = cache.getIfPresent(idReferenceFlowFile.getRootFlowFileId());
                if (rootFile == null) {
                    IdReferenceFlowFile root = FlowFileMapDbCache.instance().getCachedFlowFile(idReferenceFlowFile.getRootFlowFileId());
                    loadGraph(root);
                }
            }
            ff = (ActiveFlowFile) cache.getIfPresent(flowFileId);
        }
        if (ff == null) {
            log.info("Creating new flow file {} ", flowFileId);
            ff = new ActiveFlowFile(flowFileId);
        } else {
            log.info("LOADED FlowFile from cached id map {} ", ff);
        }
        return ff;
    }

    public ActiveFlowFile getEntry(String id) {
        return cache.getUnchecked(id);
    }


    public List<ActiveFlowFile> getRootFlowFiles() {
        return cache.asMap().values().stream().filter(flowFile -> flowFile.isRootFlowFile()).collect(Collectors.toList());
    }

    public List<ActiveFlowFile> getCompletedRootFlowFiles() {
        return cache.asMap().values().stream().filter(flowFile -> (flowFile.isRootFlowFile() && flowFile.isFlowComplete())).collect(Collectors.toList());
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public void printSummary() {
        long start = System.currentTimeMillis();
        Map<String, ActiveFlowFile> map = cache.asMap();
        List<ActiveFlowFile> rootFiles = getRootFlowFiles();
        CacheUtil cacheUtil = (CacheUtil) SpringApplicationContext.getInstance().getBean("cacheUtil");
        cacheUtil.logStats();

        NifiFlowCache nifiFlowCache = (NifiFlowCache) SpringApplicationContext.getInstance().getBean("nifiFlowCache");

        log.info("FLOW FILE Cache Size: {} , root files {}, processorNameMapSize: {} ", map.size(), rootFiles.size(), nifiFlowCache.processorNameMapSize());
        long stop = System.currentTimeMillis();
        log.info("Time to get cache summary {} ms ", (stop - start));
        FlowFileMapDbCache.instance().summary();


    }

    public void invalidate(ActiveFlowFile flowFile) {
        if (flowFile.getRootFlowFile().isFlowComplete()) {
            //EventMapDbCache.instance().expire(flowFile);
            FlowFileMapDbCache.instance().expire(flowFile);
            cache.invalidate(flowFile.getId());
            //also invalidate all children
            flowFile.getChildren().forEach(child -> invalidate(child));
        } else {
            //   log.info("skipping invlidation for {} ",flowFile.getId());
        }
    }


    public void expire() {
        try {
            long start = System.currentTimeMillis();
            log.info("Expiring root flow files");
            List<ActiveFlowFile> rootFiles = getCompletedRootFlowFiles();
            if (!rootFiles.isEmpty()) {
                for (ActiveFlowFile root : rootFiles) {
                    invalidate(root);
                }
                long stop = System.currentTimeMillis();
                if (rootFiles.size() > 0) {
                    log.info("Time to expire {} flowfiles {} ms. Root Flow Files left in cache: {} ", rootFiles.size(), (stop - start), getRootFlowFiles().size());
                }
            }
        } catch (Exception e) {
            log.error("Error attempting to invalidate FlowFileGuava cache {}, {}", e.getMessage(), e);
        }
    }

    private void initTimerThread() {
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            expire();
            FlowFileGuavaCache.instance().printSummary();
        }, 10, 10, TimeUnit.SECONDS);


    }


}
