package com.thinkbiganalytics.nifi.provenance.processor;

import com.thinkbiganalytics.nifi.provenance.StreamConfiguration;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

/**
 * Created by sr186054 on 8/14/16.
 */
public class ProvenanceEventProcessor extends AbstractProvenanceEventProcessor {


    public ProvenanceEventProcessor(StreamConfiguration streamConfiguration) {
        super(streamConfiguration);
    }

    /**
     * Return the key based upon this processor id
     */
    public String streamingMapKey(ProvenanceEventRecordDTO event) {
        return event.getComponentId();
    }


}
