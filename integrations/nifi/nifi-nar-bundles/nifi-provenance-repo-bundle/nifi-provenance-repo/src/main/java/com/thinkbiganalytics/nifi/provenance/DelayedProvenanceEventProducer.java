package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.DelayedProvenanceEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.stats.ProvenanceStatsCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * This will take a ProvenanceEvent, prepare it by building the FlowFile graph of its relationship to the running flow, generate Statistics about the Event and then set it in the DelayQueue for the system to process as either a Batch or a Strem
 * Statistics are calculated by the (ProvenanceStatsCalculator) where the events are grouped by Feed and Processor and then aggregated during a given interval before being sent off to a JMS queue for processing.
 *
 *
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

    /**
     *  - Create/update the flowfile graph for this new event
     *  - Calculate statistics on the event and add them to the aggregated set to be processed
     *  - Add the event to the DelayQueue for further processing as a Batch or Stream
     *
     * @param event
     */
    public void prepareAndAdd(ProvenanceEventRecordDTO event) {
        try {
            CacheUtil.instance().cacheAndBuildFlowFileGraph(event);
            try {
                ProvenanceStatsCalculator.instance().calculateStats(event);
            } catch (Exception e) {
                log.error("ERROR CALCULATING STATISTICS for event:  {}. EXCEPTION: {}  ", event, e.getMessage());
            }

            DelayedProvenanceEvent delayedProvenanceEvent = new DelayedProvenanceEvent(event, configuration.getProcessDelay());
            queue.offer(delayedProvenanceEvent);
        } catch (Exception e) {
            log.error("ERROR PROCESSING EVENT! {}.  ERROR: {} ", event, e.getMessage(), e);
        }

    }


}
