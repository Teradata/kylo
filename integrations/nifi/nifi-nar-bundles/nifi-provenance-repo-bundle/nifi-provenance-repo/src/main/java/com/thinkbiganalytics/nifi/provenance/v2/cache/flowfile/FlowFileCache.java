package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

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
        cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build(new CacheLoader<String, ActiveFlowFile>() {
                                                                          @Override
                                                                          public ActiveFlowFile load(String id) throws Exception {
                                                                              return new ActiveFlowFile(id);
                                                                          }
                                                                      }
        );
    }

    public ActiveFlowFile getEntry(String id) {
        return cache.getUnchecked(id);
    }

}
