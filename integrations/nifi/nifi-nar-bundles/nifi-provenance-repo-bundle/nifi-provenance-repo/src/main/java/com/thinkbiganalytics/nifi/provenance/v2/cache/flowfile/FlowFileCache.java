package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.FlowFileStatus;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.feed.ProvenanceFeedStatsCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/11/16. A cache of Flowfiles active in the system
 */
public class FlowFileCache {

    private static final Logger log = LoggerFactory.getLogger(FlowFileCache.class);

    private static final long MAX_SIZE = 10000;

    private static FlowFileCache instance = new FlowFileCache();

    public static FlowFileCache instance() {
        return instance;
    }

    private final LoadingCache<String, ActiveFlowFile> cache;

    private FlowFileCache() {
        cache = CacheBuilder.newBuilder().recordStats().maximumSize(MAX_SIZE).build(new CacheLoader<String, ActiveFlowFile>() {
                                                                          @Override
                                                                          public ActiveFlowFile load(String id) throws Exception {
                                                                              return new ActiveFlowFile(id);
                                                                          }

                                                                        }
        );
        init();
    }

    public ActiveFlowFile getEntry(String id) {
        return cache.getUnchecked(id);
    }

    public List<ActiveFlowFile> getRootFlowFiles() {
        return cache.asMap().values().stream().filter(flowFile -> flowFile.isRootFlowFile()).collect(Collectors.toList());
    }


    public CacheStats stats(){
        return cache.stats();
    }

    public void printSummary(){
        System.out.println("ActiveFlowFile CACHE summary");
        Map<String,ActiveFlowFile> map = cache.asMap();
        map.values().stream().filter(flowFile -> flowFile.isRootFlowFile()).forEach(flowFile -> {
            ProvenanceEventRecordDTO firstEvent = flowFile.getFirstEvent();
            if(firstEvent != null){
                String firstProcessorId = firstEvent != null ? firstEvent.getComponentId() : "";
                //lookup the processor?
                String summary = flowFile.summary();
                summary =firstProcessorId+"  "+summary;
                System.out.println(summary);
            }

        });

    }

    public void invalidate(ActiveFlowFile flowFile) {
        log.info("Invalidate Flow File {} ", flowFile.getId());
        cache.invalidate(flowFile.getId());
        //also invalidate all children
        flowFile.getChildren().forEach(child -> invalidate(child));
    }

    private void init() {
        /**
         * TIMER to print summary of what is in the cache
         * CURRENTLY DISABLED
         */
        Timer summaryTimer = new Timer();
        TimerTask task =  new TimerTask(){
            @Override
            public void run() {

                //   FlowFileCache.instance().printSummary();
                //feed stats
                ProvenanceFeedStatsCalculator.instance().printStats();
            }
        };
        summaryTimer.schedule(task, 10 * 1000, 10 * 1000);

        /**
         * Timer to evict all completed FlowFiles from the Cache.
         * CURRENTLY DISABLED
         */
        Timer evictCompletedFlowFilesTimer = new Timer();

        TimerTask evictCompletedFlowFilesTimerTask = new TimerTask() {
            @Override
            public void run() {
                FlowFileCache.instance().getRootFlowFiles().stream().filter(flowFile -> FlowFileStatus.isComplete(flowFile)).forEach(completedFile -> {
                    log.info("FLOW FILE {} IS COMPLETE.  REMOVE FROM CACHE!!", completedFile);
                    invalidate(completedFile);
                });

            }
        };
        //  evictCompletedFlowFilesTimer.schedule(evictCompletedFlowFilesTimerTask,0,20);  //run every 30 seconds
    }


}
