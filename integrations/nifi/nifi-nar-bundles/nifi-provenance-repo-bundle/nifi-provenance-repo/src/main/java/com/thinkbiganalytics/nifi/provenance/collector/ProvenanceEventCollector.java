package com.thinkbiganalytics.nifi.provenance.collector;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/14/16.
 * Group Events before processing
 */
public interface ProvenanceEventCollector {

    String getMapKey(ProvenanceEventRecordDTO event);

    Map<String, List<ProvenanceEventRecordDTO>> collect(List<ProvenanceEventRecordDTO> events);
}
