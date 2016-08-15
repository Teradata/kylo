package com.thinkbiganalytics.nifi.provenance.collector;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

/**
 * Created by sr186054 on 8/14/16.
 * Group Events by ProcessorId
 */
public class ProvenanceEventProcessorCollector extends AbstractProvenanceEventCollector implements ProvenanceEventCollector {


    @Override
    public String getMapKey(ProvenanceEventRecordDTO event) {
        return event.getComponentId();
    }

}
