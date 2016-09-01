package com.thinkbiganalytics.nifi.provenance.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 8/30/16.
 */
public class ProvenanceEventRecordDTOHolder implements Serializable {

    private List<ProvenanceEventRecordDTO> events;

    public ProvenanceEventRecordDTOHolder() {

    }

    public List<ProvenanceEventRecordDTO> getEvents() {
        return events;
    }

    public void setEvents(List<ProvenanceEventRecordDTO> events) {
        this.events = events;
    }
}
