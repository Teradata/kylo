package com.thinkbiganalytics.nifi.provenance.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by sr186054 on 8/30/16.
 */
public class ProvenanceEventRecordDTOHolder implements Serializable {

    private String batchId;

    private List<ProvenanceEventRecordDTO> events;

    public ProvenanceEventRecordDTOHolder() {
        this.batchId = UUID.randomUUID().toString();

    }

    public List<ProvenanceEventRecordDTO> getEvents() {
        return events;
    }

    public void setEvents(List<ProvenanceEventRecordDTO> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventRecordDTOHolder{");
        sb.append("events=").append(events != null ? events.size() : 0);
        sb.append('}');
        return sb.toString();
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
