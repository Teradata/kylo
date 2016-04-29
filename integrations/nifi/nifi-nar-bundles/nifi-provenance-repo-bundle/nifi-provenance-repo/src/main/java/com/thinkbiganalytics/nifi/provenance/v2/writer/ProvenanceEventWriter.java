package com.thinkbiganalytics.nifi.provenance.v2.writer;

import org.apache.nifi.provenance.ProvenanceEventRecord;

/**
 * Created by sr186054 on 3/4/16.
 */
public interface ProvenanceEventWriter {
    void setMaxEventId(Long l);

    Long getMaxEventId();

    Long writeEvent(ProvenanceEventRecord event);

    void checkAndSetMaxEventId(Long l);
}
