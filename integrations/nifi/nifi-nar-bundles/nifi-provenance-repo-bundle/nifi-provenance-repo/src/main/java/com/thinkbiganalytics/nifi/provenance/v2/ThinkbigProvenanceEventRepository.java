package com.thinkbiganalytics.nifi.provenance.v2;

import org.apache.nifi.provenance.PersistentProvenanceRepository;
import org.apache.nifi.provenance.ProvenanceEventRepository;

import java.io.IOException;

/**
 * This is used just as a pass through to the NiFi PersistenceProvenanceRepository.
 * This is only needed on previous Kylo versions that were pointing to this store prior to the upgrade.
 * NiFi would fail to start because it couldnt find this class assocated with the provenacne events.
 *
 * Created by sr186054 on 12/30/16.
 */
@Deprecated
public class ThinkbigProvenanceEventRepository extends PersistentProvenanceRepository implements ProvenanceEventRepository {


    public ThinkbigProvenanceEventRepository() throws IOException {
        super();

    }
}

