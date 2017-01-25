package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.nifi.provenance.ProvenanceEventRepository;

/**
 * An Object Pool to store the Objects while processing the range of records  in the {@link com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask}
 *
 * @see com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask#processEventsInRange(ProvenanceEventRepository, Long, Long)
 */
public class ProvenanceEventObjectFactory extends BasePooledObjectFactory<ProvenanceEventRecordDTO> {

    @Override
    public ProvenanceEventRecordDTO create() throws Exception {
        return new ProvenanceEventRecordDTO();
    }

    @Override
    public PooledObject<ProvenanceEventRecordDTO> wrap(ProvenanceEventRecordDTO eventRecordDTO) {
        return new DefaultPooledObject<>(eventRecordDTO);
    }

    @Override
    public boolean validateObject(PooledObject<ProvenanceEventRecordDTO> p) {
        return super.validateObject(p);
    }

    @Override
    public void activateObject(PooledObject<ProvenanceEventRecordDTO> p) throws Exception {
        super.activateObject(p);
    }

    @Override
    public void passivateObject(PooledObject<ProvenanceEventRecordDTO> p) throws Exception {
        p.getObject().reset();
    }

    @Override
    public void destroyObject(PooledObject<ProvenanceEventRecordDTO> p) throws Exception {
        super.destroyObject(p);
    }
}
