package com.thinkbiganalytics.nifi.provenance.model;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by sr186054 on 8/14/16.
 */
public class DelayedProvenanceEvent implements Delayed {

    private ProvenanceEventRecordDTO event;
    private long startTime;

    public DelayedProvenanceEvent(ProvenanceEventRecordDTO event, long delay) {
        this.event = event;
        this.startTime = System.currentTimeMillis() + delay;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((DelayedProvenanceEvent) o).startTime) {
            return -1;
        }
        if (this.startTime > ((DelayedProvenanceEvent) o).startTime) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DelayedProvenanceEvent{");
        sb.append("event=").append(event);
        sb.append(", startTime=").append(startTime);
        sb.append('}');
        return sb.toString();
    }

    public ProvenanceEventRecordDTO getEvent() {
        return event;
    }
}
