package com.thinkbiganalytics.nifi.provenance.collector;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collect Events and group based upon a strategy putting data into the map based upon the supplied getMapKey method
 *
 * Created by sr186054 on 8/13/16.
 */
public abstract class AbstractProvenanceEventCollector {

    //Group the Events up
    private Map<String, List<ProvenanceEventRecordDTO>> events = new HashMap<>();


    public AbstractProvenanceEventCollector() {
    }

    /**
     * Return a KEY based upon the ProcessorId and Root flowfile event to determine if this event partakes in a STREAM or a BATCH
     */
    public abstract String getMapKey(ProvenanceEventRecordDTO event);

    public void collect(ProvenanceEventRecordDTO event) {
        addToCollection(events, event);
    }


    public void collect(List<ProvenanceEventRecordDTO> events) {
        if (events != null) {
            events.forEach(event -> addToCollection(this.events, event));
        }

    }


    private void addToCollection(Map<String, List<ProvenanceEventRecordDTO>> map, ProvenanceEventRecordDTO event) {
        String key = getMapKey(event);
        if (map.get(key) == null) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(event);
    }

    public Map<String, List<ProvenanceEventRecordDTO>> getEvents() {
        return events;
    }
}
