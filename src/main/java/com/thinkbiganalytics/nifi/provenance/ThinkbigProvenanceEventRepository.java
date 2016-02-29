package com.thinkbiganalytics.nifi.provenance;

import com.google.common.collect.Lists;
import org.apache.nifi.events.EventReporter;
import org.apache.nifi.provenance.*;
import org.apache.nifi.provenance.lineage.ComputeLineageSubmission;
import org.apache.nifi.provenance.search.Query;
import org.apache.nifi.provenance.search.QuerySubmission;
import org.apache.nifi.provenance.search.SearchableField;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom Event Repository that will take the PovenanceEvents and write them to some output.
 */
public class ThinkbigProvenanceEventRepository implements ProvenanceEventRepository {

    private PersistentProvenanceRepository repository;

    private ProvenanceEventRecordWriter provenanceEventRecordWriter;

    private Timer timer;
    private AtomicInteger eventCounter = new AtomicInteger(0);

    public ThinkbigProvenanceEventRepository(){
        try {
            provenanceEventRecordWriter = new ProvenanceEventRecordDatabaseWriter();
            repository = new PersistentProvenanceRepository();
            timer = new Timer();
            timer.schedule(new ProvenanceEventRecordTimer(),0, 1 * 1000);
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
        repository.registerEvent(provenanceEventRecord);
    }

    @Override
    public void registerEvents(Iterable<ProvenanceEventRecord> iterable) {
        List<ProvenanceEventRecord> events = Lists.newArrayList(iterable);
        //Event registration is captured in the Timer thread below.
        //a timer is needed because the EventId is not set on the incoming iterable.  it is set later in a different thread
       repository.registerEvents(events);

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

    private void logEvent(String msg){
        System.out.println("****-----**********-----******  " + msg);
    }


    /**
     * Timer to run every second to check and see if there are any new Events to store
     * This is needed because the eventId is assigned to the ProvenanceEventRecord in a different thread.
     *
     */
    private class ProvenanceEventRecordTimer extends TimerTask {
        private Long lastEventId = null;
        private UUID id;
        private boolean running;

        public ProvenanceEventRecordTimer() {
            init();
        }

        public ProvenanceEventRecordTimer(Long startingEventId) {
            lastEventId = startingEventId;
            init();
        }

        private void init(){
            this.id =UUID.randomUUID();
            running = false;
        }

        @Override
        public void run() {
            if(!running) {
                running = true;
                if(lastEventId == null){
                    lastEventId = provenanceEventRecordWriter.getLastRecordedEventId();
                }
                Long maxId = getMaxEventId();
                System.out.println("TIMER ID CHECK = last: "+lastEventId+" MAX: " + maxId);
                try {

                    if (lastEventId == null && maxId != null) {
                        lastEventId = maxId;
                        System.out.println("NEW TIMER WITH START ID OF " + lastEventId);
                    }
                    if (lastEventId != null && maxId != null && maxId > lastEventId) {
                        int records = new Long(maxId - lastEventId).intValue();
                        List<ProvenanceEventRecord> events = null;
                        try {
                            events = getEvents((lastEventId + 1), records);
                            if (events != null) {
                                for (ProvenanceEventRecord event : events) {
                                    logEvent("About to persist event "+event.getEventId());
                                    provenanceEventRecordWriter.persistProvenanceEventRecord(event);
                                    lastEventId = event.getEventId();
                                }
                            }
                            lastEventId = maxId;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                running = false;
            }


        }
    }







}

