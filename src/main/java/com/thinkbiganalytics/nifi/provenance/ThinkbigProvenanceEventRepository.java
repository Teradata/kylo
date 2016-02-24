package com.thinkbiganalytics.nifi.provenance;

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

/**
 * Created by sr186054 on 2/24/16.
 */
public class ThinkbigProvenanceEventRepository implements ProvenanceEventRepository {

    private PersistentProvenanceRepository repository;

    public ThinkbigProvenanceEventRepository(){
        try {
            repository = new PersistentProvenanceRepository();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(EventReporter eventReporter) throws IOException {
        logEvent("initialize");
        repository.initialize(eventReporter);
    }

    @Override
    public ProvenanceEventBuilder eventBuilder() {
        logEvent("eventBuilder");
       return repository.eventBuilder();
    }

    @Override
    public void registerEvent(ProvenanceEventRecord provenanceEventRecord) {
        logEvent("registerEvent "+provenanceEventRecord.getEventId()+", "+provenanceEventRecord.getFlowFileUuid());
repository.registerEvent(provenanceEventRecord);
    }

    @Override
    public void registerEvents(Iterable<ProvenanceEventRecord> iterable) {
        logEvent("registerEvent ");
repository.registerEvents(iterable);
    }

    @Override
    public List<ProvenanceEventRecord> getEvents(long firstRecordId, int maxRecords) throws IOException {
        logEvent("registerEvent: first Record Id: "+firstRecordId+", max records: "+maxRecords);
      return repository.getEvents(firstRecordId,maxRecords);
    }

    @Override
    public Long getMaxEventId() {
        logEvent("getMaxEventId ");
       return  repository.getMaxEventId();
    }

    @Override
    public QuerySubmission submitQuery(Query query) {
        logEvent("submitQuery "+query.getIdentifier());
        return repository.submitQuery(query);
    }

    @Override
    public QuerySubmission retrieveQuerySubmission(String s) {
        logEvent("retrieveQuerySubmission "+s);
        return repository.retrieveQuerySubmission(s);
    }

    @Override
    public ComputeLineageSubmission submitLineageComputation(String s) {
        logEvent("submitLineageComputation "+s);
       return repository.submitLineageComputation(s);
    }

    @Override
    public ComputeLineageSubmission retrieveLineageSubmission(String s) {
        logEvent("retrieveLineageSubmission "+s);
        return repository.retrieveLineageSubmission(s);
    }

    @Override
    public ProvenanceEventRecord getEvent(long l) throws IOException {
       return repository.getEvent(l);
    }

    @Override
    public ComputeLineageSubmission submitExpandParents(long eventId) {
        logEvent("submitExpandParents "+eventId);
        return repository.submitExpandParents(eventId);
    }

    @Override
    public ComputeLineageSubmission submitExpandChildren(long eventId) {
        logEvent("submitExpandChildren "+eventId);
        return repository.submitExpandChildren(eventId);
    }

    @Override
    public void close() throws IOException {
        logEvent("close");
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

    private void logEvent(String msg){
        System.out.println("Message is "+msg);
    }
}
