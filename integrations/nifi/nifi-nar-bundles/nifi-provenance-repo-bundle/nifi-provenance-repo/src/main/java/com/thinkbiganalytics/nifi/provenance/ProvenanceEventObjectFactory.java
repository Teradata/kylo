package com.thinkbiganalytics.nifi.provenance;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
