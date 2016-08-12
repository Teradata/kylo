package com.thinkbiganalytics.nifi.provenance.v2;

import com.thinkbiganalytics.nifi.provenance.v2.cache.event.ProvenanceEventCache;

import org.apache.nifi.provenance.ProvenanceEventRecord;

import java.util.List;

/**
 * Created by sr186054 on 8/11/16.
 */
public class ProcessorSummary {

    public List<ProvenanceEventRecord> eventsForProcessor(String processorId) {
        return ProvenanceEventCache.instance().getEventsForProcessor(processorId);
    }

}
