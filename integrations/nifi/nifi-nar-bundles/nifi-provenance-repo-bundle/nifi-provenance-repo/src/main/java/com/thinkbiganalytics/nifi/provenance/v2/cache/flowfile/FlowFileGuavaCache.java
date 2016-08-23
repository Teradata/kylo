package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.FlowFile;
import com.thinkbiganalytics.nifi.provenance.model.IdReferenceFlowFile;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

;

/**
 * Created by sr186054 on 8/11/16. A cache of Flowfiles active in the system
 */
public class FlowFileGuavaCache implements FlowFileCache {

    private static final Logger log = LoggerFactory.getLogger(FlowFileGuavaCache.class);


    private static FlowFileGuavaCache instance = new FlowFileGuavaCache();

    public static FlowFileGuavaCache instance() {
        return instance;
    }

    private final LoadingCache<String, FlowFile> cache;

    private FlowFileGuavaCache() {
        cache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, FlowFile>() {
                                                                  @Override
                                                                  public FlowFile load(String id) throws Exception {
                                                                      return loadFromCache(id);
                                                                  }

                                                              }
        );
        init();
    }

    public ActiveFlowFile loadGraph(IdReferenceFlowFile parentFlowFile) {
        ActiveFlowFile parent = (ActiveFlowFile) cache.getIfPresent(parentFlowFile.getId());
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
                ActiveFlowFile child = (ActiveFlowFile) cache.getIfPresent(childId);
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

    public ActiveFlowFile loadFromCache(String flowFileId) {
        ActiveFlowFile ff = null;
        IdReferenceFlowFile idReferenceFlowFile = FlowFileMapDbCache.instance().getCachedFlowFile(flowFileId);
        if (idReferenceFlowFile != null) {
            //try to get the root file and build the graph and then return this ff
            if (StringUtils.isNotBlank(idReferenceFlowFile.getRootFlowFileId())) {
                ActiveFlowFile rootFile = (ActiveFlowFile) cache.getIfPresent(idReferenceFlowFile.getRootFlowFileId());
                if (rootFile == null) {
                    IdReferenceFlowFile root = FlowFileMapDbCache.instance().getCachedFlowFile(idReferenceFlowFile.getRootFlowFileId());
                    loadGraph(root);
                }
            }
            ff = (ActiveFlowFile) cache.getIfPresent(flowFileId);

        }
        if (ff == null) {
            ff = new ActiveFlowFile(flowFileId);
        } else {
            log.info("LOADED FF from cached id map {} ", ff);
        }
        return ff;
    }

    @Override
    public FlowFile getEntry(String id) {
        return cache.getUnchecked(id);
    }


    @Override
    public List<FlowFile> getRootFlowFiles() {
        return cache.asMap().values().stream().filter(flowFile -> flowFile.isRootFlowFile()).collect(Collectors.toList());
    }

    public List<FlowFile> getCompletedRootFlowFiles() {
        return cache.asMap().values().stream().filter(flowFile -> (flowFile.isRootFlowFile() && flowFile.isFlowComplete())).collect(Collectors.toList());
    }


    public CacheStats stats() {
        return cache.stats();
    }

    @Override
    public void printSummary() {

        Map<String, FlowFile> map = cache.asMap();
        List<FlowFile> rootFiles = getRootFlowFiles();
        CacheUtil.instance().logStats();
        log.info("FLOW FILE Cache Size: {} , root files {} ", map.size(), rootFiles.size());
        if (map.size() > 0 && rootFiles.size() == 0) {
            for (FlowFile ff : map.values()) {
                log.info("Flowfile is still in cache, but for some reason was not cleared {} ", ff);
            }
        }

        FlowFileMapDbCache.instance().summary();
    }

    @Override
    public void invalidate(FlowFile flowFile) {
        //   log.info("Invalidate Flow File {} ", flowFile.getId());
        EventMapDbCache.instance().expire(flowFile);
        FlowFileMapDbCache.instance().expire(flowFile);
        cache.invalidate(flowFile.getId());
        //also invalidate all children
        flowFile.getChildren().forEach(child -> invalidate((FlowFile) child));
    }


    public void expire() {
        long start = System.currentTimeMillis();
        List<FlowFile> rootFiles = getCompletedRootFlowFiles();
        if (!rootFiles.isEmpty()) {
            log.info("Attempt to expire {} root flow files", rootFiles.size());
            for (FlowFile root : rootFiles) {
                invalidate(root);
            }
            long stop = System.currentTimeMillis();
            if (rootFiles.size() > 0) {
                log.info("Time to expire {} flowfiles {} ms. Root Flow Files left in cache: {} ", rootFiles.size(), (stop - start), getRootFlowFiles().size());
            }
        }
    }

    private void init() {
        /**
         * TIMER to print summary of what is in the cache
         * CURRENTLY DISABLED
         */
        Timer summaryTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                expire();
                FlowFileGuavaCache.instance().printSummary();
                //feed stats
                //ProvenanceFeedStatsCalculator.instance().printStats();
            }
        };
        summaryTimer.schedule(task, 10 * 1000, 10 * 1000);

    }

    public void commit() {

    }


    @Override
    public FlowFile save(FlowFile flowFile) {
        cache.put(flowFile.getId(), flowFile);
        return flowFile;
    }
}
