package com.thinkbiganalytics.nifi.provenance.v2.cache.feed;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.model.ProcessorStats;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventFeedProcessorStats;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 8/15/16.
 */
public class ProvenanceFeedStatsCalculator {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceFeedStatsCalculator.class);

    Integer MAX_SIZE = 1000;

    private static ProvenanceFeedStatsCalculator instance = new ProvenanceFeedStatsCalculator();

    public static ProvenanceFeedStatsCalculator instance() {
        return instance;
    }


    private final LoadingCache<String, ProcessorStats> processorStatsCache;

    private final LoadingCache<String, ProvenanceEventFeedProcessorStats> feedStatsCache;

    private ProvenanceFeedStatsCalculator() {
        feedStatsCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, ProvenanceEventFeedProcessorStats>() {
                                                                                                 @Override
                                                                                                 public ProvenanceEventFeedProcessorStats load(String feedName) throws Exception {
                                                                                                     ProvenanceEventFeedProcessorStats stats = new ProvenanceEventFeedProcessorStats(feedName);
                                                                                                     return stats;
                                                                                                 }
                                                                                             }
        );

        processorStatsCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, ProcessorStats>() {
                                                                                @Override
                                                                                public ProcessorStats load(String processorId) throws Exception {
                                                                                    ProcessorStats stats = new ProcessorStats(processorId);
                                                                                    return stats;
                                                                                }
                                                                            }
        );
    }


    public void calculateStats(ProvenanceEventRecordDTO event) {

        //1 get Feed Name for event
        String feedName = NifiFlowCache.instance().getFlow(event.getFlowFile()).getFeedName();
        ProvenanceEventFeedProcessorStats stats = feedStatsCache.getUnchecked(feedName);
        stats.add(event.getFlowFile().getRootFlowFile().getFirstEvent().getGroupId(), event);
        ProcessorStats processorStats = stats.getProcessorStats().get(event.getComponentId());
        //get and update processor stats
        ProcessorStats cachedStats = processorStatsCache.getUnchecked(event.getComponentId());
        cachedStats.add(processorStats);
    }

    public ProvenanceEventFeedProcessorStats getFeedStats(String feedName) {
        return feedStatsCache.getIfPresent(feedName);
    }


    public List<ProcessorStats> getProcessorStats() {
        return Lists.newArrayList(processorStatsCache.asMap().values());
    }

    public ProcessorStats getProcessorStats(String processorId) {
        return processorStatsCache.getUnchecked(processorId);
    }

    public ProcessorStats aggregrateAllStats() {
        ProcessorStats allStats = new ProcessorStats("allStats");
        processorStatsCache.asMap().values().stream().forEach(stats -> allStats.add(stats));
        return allStats;
    }

    public Collection<ProvenanceEventFeedProcessorStats> getStats() {
        return feedStatsCache.asMap().values();
    }

    public String printStats() {
        StringBuffer sb = new StringBuffer();
        for (ProvenanceEventFeedProcessorStats feedStats : getStats()) {
            sb.append(feedStats).append("\n");
        }
        log.info(sb.toString());
        return sb.toString();
    }
}
