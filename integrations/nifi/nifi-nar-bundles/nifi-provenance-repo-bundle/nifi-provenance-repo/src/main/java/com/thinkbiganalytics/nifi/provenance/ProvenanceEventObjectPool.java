package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * An Object Pool to store the Objects while processing the range of records  in the {@link com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask}
 *
 * @see com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask#processEventsInRange(ProvenanceEventRepository, Long, Long)
 */
public class ProvenanceEventObjectPool extends GenericObjectPool<ProvenanceEventRecordDTO> {


    public ProvenanceEventObjectPool(PooledObjectFactory<ProvenanceEventRecordDTO> factory) {
        super(factory);
    }


    public ProvenanceEventObjectPool(PooledObjectFactory<ProvenanceEventRecordDTO> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }
}
