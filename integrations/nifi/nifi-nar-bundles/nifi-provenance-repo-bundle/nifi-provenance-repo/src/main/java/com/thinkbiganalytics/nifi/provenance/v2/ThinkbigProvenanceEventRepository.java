package com.thinkbiganalytics.nifi.provenance.v2;

import com.thinkbiganalytics.util.SpringApplicationContext;

import org.apache.nifi.provenance.PersistentProvenanceRepository;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventRepository;
import org.apache.nifi.provenance.RepositoryConfiguration;
import org.apache.nifi.provenance.serialization.RecordWriter;
import org.apache.nifi.util.NiFiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Custom Event Repository that will take the {@link ProvenanceEventRecord} objects and write them to some output.
 */
public class ThinkbigProvenanceEventRepository extends PersistentProvenanceRepository implements ProvenanceEventRepository {

    private static final Logger log = LoggerFactory.getLogger(ThinkbigProvenanceEventRepository.class);

    /**
     * Default no arg constructor for service loading only.
     */
    public ThinkbigProvenanceEventRepository() throws IOException {
        super();
        log.info("New ThinkbigProvenanceEventRepository");
        SpringApplicationContext.getInstance().initializeSpring();
    }

    public ThinkbigProvenanceEventRepository(final NiFiProperties niFiProperties) throws IOException {
        super(niFiProperties);
        log.info("New ThinkbigProvenanceEventRepository with NiFiProperties");
        SpringApplicationContext.getInstance().initializeSpring();
    }

    public ThinkbigProvenanceEventRepository(final RepositoryConfiguration configuration, final int rolloverCheckMillis) throws IOException {
        super(configuration, rolloverCheckMillis);
        log.info("New ThinkbigProvenanceEventRepository with RepositoryConfiguration");
        SpringApplicationContext.getInstance().initializeSpring();
    }

    @Override
    protected RecordWriter[] createWriters(final RepositoryConfiguration config, final long initialRecordId) throws IOException {
        log.info("Creating new ThinkbigRecordWriterDelegates");
        final RecordWriter[] writers = super.createWriters(config, initialRecordId);
        final RecordWriter[] interceptEventIdWriters = new RecordWriter[writers.length];
        for (int i = 0; i < writers.length; i++) {
            interceptEventIdWriters[i] = new ThinkbigRecordWriterDelegate(writers[i]);
        }
        return interceptEventIdWriters;
    }
}

