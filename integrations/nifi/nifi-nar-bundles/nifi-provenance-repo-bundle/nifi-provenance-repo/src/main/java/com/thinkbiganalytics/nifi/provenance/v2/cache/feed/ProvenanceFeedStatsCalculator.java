package com.thinkbiganalytics.nifi.provenance.v2.cache.feed;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventFeedProcessorStats;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

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

    private final LoadingCache<String, ProvenanceEventFeedProcessorStats> feedStatsCache;

    private ProvenanceFeedStatsCalculator() {
        feedStatsCache = CacheBuilder.newBuilder().recordStats().maximumSize(MAX_SIZE).build(new CacheLoader<String, ProvenanceEventFeedProcessorStats>() {
                                                                                                 @Override
                                                                                                 public ProvenanceEventFeedProcessorStats load(String feedName) throws Exception {
                                                                                                     ProvenanceEventFeedProcessorStats stats = new ProvenanceEventFeedProcessorStats(feedName);
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


    }

    public ProvenanceEventFeedProcessorStats getFeedStats(String feedName) {
        return feedStatsCache.getIfPresent(feedName);
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
