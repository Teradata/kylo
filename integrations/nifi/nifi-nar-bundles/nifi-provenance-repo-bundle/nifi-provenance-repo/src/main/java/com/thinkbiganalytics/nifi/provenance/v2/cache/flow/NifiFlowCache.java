package com.thinkbiganalytics.nifi.provenance.v2.cache.flow;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.rest.model.flow.SimpleNifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.SimpleNifiFlowProcessor;

/**
 * Created by sr186054 on 8/11/16. Cache of the Nifi Flow graph
 */
public class NifiFlowCache {

    private Integer MAX_SIZE = 100;

    public static NifiFlowCache instance() {
        return instance;
    }

    private static NifiFlowCache instance = new NifiFlowCache();

    private final LoadingCache<String, SimpleNifiFlowProcessGroup> feedFlowCache;

    private final LoadingCache<String, SimpleNifiFlowProcessor> processorCache;

    private NifiFlowCache() {

        feedFlowCache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build(new CacheLoader<String, SimpleNifiFlowProcessGroup>() {
                                                                                  @Override
                                                                                  public SimpleNifiFlowProcessGroup load(String processGroupId) throws Exception {
                                                                                      ///CALL OUT TO rest client to walk the flow and build the cache
                                                                                      SimpleNifiFlowProcessGroup group = new SimpleNifiFlowProcessGroup("1", "2");
                                                                                      //add the processors to the processor cache
                                                                                      processorCache.putAll(group.getProcessorMap());
                                                                                      return group;
                                                                                  }
                                                                              }
        );

        processorCache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build(new CacheLoader<String, SimpleNifiFlowProcessor>() {
                                                                                   @Override
                                                                                   public SimpleNifiFlowProcessor load(String processorId) throws Exception {
                                                                                       ///CALL OUT TO rest client to walk the flow and build the cache starting with any processor in the flow.
                                                                                       //1 find parent processGroup for processor
                                                                                       //2 feedFlowFileCache.get(parentProcessGroup)
                                                                                       //return feedFlowCache.getProcessor(processorId);
                                                                                       return new SimpleNifiFlowProcessor("1", "2");
                                                                                   }
                                                                               }
        );
    }

    /**
     * Gets a Processor by Id
     */
    public SimpleNifiFlowProcessor getProcessor(String processorId) {
        return processorCache.getUnchecked(processorId);
    }

    /**
     * Gets the Parent Feed Flow Holder describing the Entire flow for any given processorId
     */
    public SimpleNifiFlowProcessGroup getFeedFlowForProcessor(String processorId) {
        return getProcessor(processorId).getProcessGroup();
    }

    /**
     * Find The Feed Flow for a feed name This will look at the Process Group Name in Nifi and try to match it to the feed name NOTE: this assumes the ProcessGroup name == Feed System Name
     */
    public SimpleNifiFlowProcessGroup getFeedFlowForFeedName(String category, String feedName) {
        return feedFlowCache.asMap().values().stream().filter(flow -> (feedName.equalsIgnoreCase(flow.getName()) && category.equalsIgnoreCase(flow.getParentGroupName()))).findAny().orElse(null);
    }

}
