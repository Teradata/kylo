package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.DelayedProvenanceEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.EventMapDbCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.stats.ProvenanceStatsCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Created by sr186054 on 8/14/16.
 */
public class DelayedProvenanceEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DelayedProvenanceEventProducer.class);

    private StreamConfiguration configuration;

    // Creates an instance of blocking queue using the DelayQueue.
    private BlockingQueue<DelayedProvenanceEvent> queue;


    public DelayedProvenanceEventProducer(StreamConfiguration configuration, BlockingQueue queue) {
        super();
        this.configuration = configuration;
        this.queue = queue;
    }

    ///   FF1 ->     FF2


    public void add(ProvenanceEventRecordDTO event) {
        try {
            CacheUtil.instance().cache(event);
            try {
                ProvenanceStatsCalculator.instance().calculateStats(event);
            } catch (Exception e) {
                log.error("ERROR CALCULATING STATISTICS for event:  {}. EXCEPTION: {}  ", event, e.getMessage());
            }
            EventMapDbCache.instance().save(event);
        //    log.info("Process Event! {} ",event);
            DelayedProvenanceEvent delayedProvenanceEvent = new DelayedProvenanceEvent(event, configuration.getProcessDelay());
            queue.offer(delayedProvenanceEvent);
        } catch (Exception e) {
            log.error("ERROR PROCESSING EVENT! {}.  ERROR: {} ", event, e.getMessage(), e);
        }

    }


}
