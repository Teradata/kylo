package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sr186054 on 8/11/16. A cache of Flowfiles active in the system
 */
public class FlowFileCache {

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

    private void init() {
        Timer summaryTimer = new Timer();
        TimerTask task =  new TimerTask(){
            @Override
            public void run() {
                FlowFileCache.instance().printSummary();
            }
        };
        summaryTimer.schedule(task,15*1000,15 * 1000);
    }




}
