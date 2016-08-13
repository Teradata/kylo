package com.thinkbiganalytics.nifi.provenance.v2;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventStreamWriter;
import com.thinkbiganalytics.util.SpringInitializer;

import org.apache.nifi.events.EventReporter;
import org.apache.nifi.provenance.PersistentProvenanceRepository;
import org.apache.nifi.provenance.ProvenanceEventBuilder;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventRepository;
import org.apache.nifi.provenance.lineage.ComputeLineageSubmission;
import org.apache.nifi.provenance.search.Query;
import org.apache.nifi.provenance.search.QuerySubmission;
import org.apache.nifi.provenance.search.SearchableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Custom Event Repository that will take the PovenanceEvents and write them to some output.
 */
public class ThinkbigProvenanceEventRepository implements ProvenanceEventRepository {
    private static final Logger log = LoggerFactory.getLogger(ThinkbigProvenanceEventRepository.class);

    private PersistentProvenanceRepository repository;

    private  Lock registerEventLock = null;


    private ProvenanceEventStreamWriter provenanceEventRecordWriter;



    public ThinkbigProvenanceEventRepository() {
        log.info(" ThinkbigProvenanceEventRepository START!!!");
        this.registerEventLock = new ReentrantReadWriteLock(true).readLock();
        try {


            repository = new PersistentProvenanceRepository();
            provenanceEventRecordWriter = new ProvenanceEventStreamWriter();
            log.info(" new event recorder " + provenanceEventRecordWriter);
            //  provenanceEventRecordWriter.setEventId(repository.getMaxEventId());
            SpringInitializer.getInstance().initializeSpring();
            SpringApplicationListener listener = new SpringApplicationListener();
           // listener.addObjectToAutowire("provenanceEventRecordWriter", provenanceEventRecordWriter);
           // listener.autowire("provenanceEventRecordWriter", provenanceEventRecordWriter);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void initialize(EventReporter eventReporter) throws IOException {
        repository.initialize(eventReporter);
    }

    @Override
    public ProvenanceEventBuilder eventBuilder() {
        return repository.eventBuilder();

    }

    @Override
    public void registerEvent(ProvenanceEventRecord provenanceEventRecord) {
        log.info("ThinkbigProvenanceEventRepository recording 1 single provenance event: " + provenanceEventRecord.getComponentId() + ", " + provenanceEventRecord.getComponentType());
        registerEvents(Collections.singleton(provenanceEventRecord));
    }

    /**
     * get the latest event id
     * if the getMaxEventId is null use -1 as a base id
     * the writer will do an incrementAndGet on it to start it off a 0 which is what Nifi starts with
     */
    private void checkAndSetEventId(){
        Long maxId = getMaxEventId();
        Long eventId = provenanceEventRecordWriter.checkAndSetMaxEventId(maxId == null ? -1L : maxId) ;
        if(maxId == null){
            log.info("Register Events ... getMaxEventId is null  using "+eventId+" from internal incrementer");
        }
    }

    @Override
    public void registerEvents(Iterable<ProvenanceEventRecord> iterable) {
        this.registerEventLock.lock();
        try {
        List<ProvenanceEventRecord> events = Lists.newArrayList(iterable);
        checkAndSetEventId();
        repository.registerEvents(events);
        if (events != null && events.size() > 0) {
            log.info("ThinkbigProvenanceEventRepository recording " + events.size() + " provenance events ");
            for (ProvenanceEventRecord event : events) {
                provenanceEventRecordWriter.writeEvent(event);
            }
        }
        } finally {
            this.registerEventLock.unlock();
        }
    }


    @Override
    public List<ProvenanceEventRecord> getEvents(long firstRecordId, int maxRecords) throws IOException {
        List<ProvenanceEventRecord> events = repository.getEvents(firstRecordId, maxRecords);
        return events;
    }

    @Override
    public Long getMaxEventId() {
        Long maxEventId = repository.getMaxEventId();
        return maxEventId;
    }

    @Override
    public QuerySubmission submitQuery(Query query) {
        return repository.submitQuery(query);
    }

    @Override
    public QuerySubmission retrieveQuerySubmission(String s) {
        return repository.retrieveQuerySubmission(s);
    }

    @Override
    public ComputeLineageSubmission submitLineageComputation(String s) {
        return repository.submitLineageComputation(s);
    }

    @Override
    public ComputeLineageSubmission retrieveLineageSubmission(String s) {
        return repository.retrieveLineageSubmission(s);
    }

    @Override
    public ProvenanceEventRecord getEvent(long l) throws IOException {
        return repository.getEvent(l);
    }

    @Override
    public ComputeLineageSubmission submitExpandParents(long eventId) {
        return repository.submitExpandParents(eventId);
    }

    @Override
    public ComputeLineageSubmission submitExpandChildren(long eventId) {
        return repository.submitExpandChildren(eventId);
    }

    @Override
    public void close() throws IOException {
        repository.close();
    }

    @Override
    public List<SearchableField> getSearchableFields() {
        return repository.getSearchableFields();
    }

    @Override
    public List<SearchableField> getSearchableAttributes() {
        return repository.getSearchableAttributes();
    }


}

