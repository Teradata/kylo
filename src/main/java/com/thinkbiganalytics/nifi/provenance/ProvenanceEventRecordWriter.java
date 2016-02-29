package com.thinkbiganalytics.nifi.provenance;

import org.apache.nifi.provenance.ProvenanceEventRecord;

/**
 * Created by sr186054 on 2/24/16.
 */
public interface ProvenanceEventRecordWriter {


    /**
     * persist Event
     * @param event
     */
    Long persistProvenanceEventRecord(ProvenanceEventRecord event);

     Long getLastRecordedEventId();
}
