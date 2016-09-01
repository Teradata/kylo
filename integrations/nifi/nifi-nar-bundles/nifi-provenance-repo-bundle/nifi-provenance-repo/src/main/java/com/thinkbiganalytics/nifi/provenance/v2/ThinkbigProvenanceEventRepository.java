package com.thinkbiganalytics.nifi.provenance.v2;

import com.thinkbiganalytics.util.SpringApplicationContext;

import org.apache.nifi.provenance.PersistentProvenanceRepository;
import org.apache.nifi.provenance.ProvenanceEventRepository;
import org.apache.nifi.provenance.RepositoryConfiguration;
import org.apache.nifi.provenance.serialization.RecordWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Custom Event Repository that will take the PovenanceEvents and write them to some output.
 */
public class ThinkbigProvenanceEventRepository extends PersistentProvenanceRepository implements ProvenanceEventRepository {
    private static final Logger log = LoggerFactory.getLogger(ThinkbigProvenanceEventRepository.class);


    public ThinkbigProvenanceEventRepository() throws IOException {
        super();

        log.info("New ThinkbigProvenanceEventRepository");
        SpringApplicationContext.getInstance().initializeSpring();
    }
    @Override
    protected RecordWriter[] createWriters(RepositoryConfiguration config, long initialRecordId) throws IOException {
        log.info("Creating new ThinkbigRecordWriterDelegates ");
        RecordWriter[] writers = super.createWriters(config, initialRecordId);
        RecordWriter[] interceptEventIdWriters = new RecordWriter[writers.length];
        for (int i = 0; i < writers.length; i++) {
            interceptEventIdWriters[i] = new ThinkbigRecordWriterDelegate(writers[i]);
        }
        return interceptEventIdWriters;
    }

    public ThinkbigProvenanceEventRepository(RepositoryConfiguration configuration, int rolloverCheckMillis) throws IOException {
        super(configuration, rolloverCheckMillis);
        log.info("New ThinkbigProvenanceEventRepository with configuration");
        SpringApplicationContext.getInstance().initializeSpring();
    }
}

