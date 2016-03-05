package com.thinkbiganalytics.nifi.provenance;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.writer.ProvenanceEventActiveMqWriter;
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

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom Event Repository that will take the PovenanceEvents and write them to some output.
 */
public class ThinkbigProvenanceEventRepository implements ProvenanceEventRepository {

    private PersistentProvenanceRepository repository;


    private ProvenanceEventActiveMqWriter provenanceEventRecordWriter;

    private Timer timer;
    private AtomicLong eventCounter = new AtomicLong(0);

    public ThinkbigProvenanceEventRepository(){
        try {

            repository = new PersistentProvenanceRepository();
            provenanceEventRecordWriter = new ProvenanceEventActiveMqWriter();
            System.out.println(" new event recorder "+provenanceEventRecordWriter);
          //  provenanceEventRecordWriter.setEventId(repository.getMaxEventId());
            SpringInitializer.getInstance().initializeSpring();
            SpringApplicationListener listener = new SpringApplicationListener();
            listener.addObjectToAutowire("provenanceEventRecordWriter",provenanceEventRecordWriter);
            listener.autowire("provenanceEventRecordWriter",provenanceEventRecordWriter);

        } catch (IOException e) {
            e.printStackTrace();
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
        System.out.println("REGISTER SINGLE EVENT : "+ provenanceEventRecord.getComponentId()+", "+ provenanceEventRecord.getComponentType());
        repository.registerEvent(provenanceEventRecord);
        provenanceEventRecordWriter.checkAndSetMaxEventId(getMaxEventId());
        provenanceEventRecordWriter.writeEvent(provenanceEventRecord);
    }

    @Override
    public void registerEvents(Iterable<ProvenanceEventRecord> iterable) {
        List<ProvenanceEventRecord> events = Lists.newArrayList(iterable);
        provenanceEventRecordWriter.checkAndSetMaxEventId(getMaxEventId());
        repository.registerEvents(events);
        if(events != null) {
            System.out.println("REGISTER Multiple EVENTs : " + events.size());
            for (ProvenanceEventRecord event : events) {
                provenanceEventRecordWriter.writeEvent(event);
            }
        }
    }


    @Override
    public List<ProvenanceEventRecord> getEvents(long firstRecordId, int maxRecords) throws IOException {
        List<ProvenanceEventRecord> events = repository.getEvents(firstRecordId, maxRecords);
        return events;
    }

    @Override
    public Long getMaxEventId() {
        Long maxEventId=  repository.getMaxEventId();
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
        timer.cancel();
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

