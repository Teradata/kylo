package com.thinkbiganalytics.nifi.provenance.model;

import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/15/16.
 */
public class ProcessorStats {

    private String processorId;


    private AtomicLong eventsCount = new AtomicLong(0L);

    private Long totalDurationTime = 0L;

    private Long lastEventId;

    private DateTime lastProcessDate;


    public ProcessorStats(String processorId) {
        this.processorId = processorId;
    }


    public ProcessorStats(ProcessorStats p1, ProcessorStats p2) {
        Long total = p1.getEventsCount().get() + p2.getEventsCount().get();
        DateTime lastProcessDate = p1.getLastProcessDate().isAfter(p2.getLastProcessDate()) ? p1.getLastProcessDate() : p2.getLastProcessDate();
        Long lastEventId = p1.getLastEventId() > p2.getLastEventId() ? p1.getLastEventId() : p2.getLastEventId();
        this.eventsCount = new AtomicLong(total);
        this.lastEventId = lastEventId;
        this.lastProcessDate = lastProcessDate;
        this.totalDurationTime = p1.getTotalDurationTime() + p2.getTotalDurationTime();
    }

    public String getProcessorId() {
        return processorId;
    }


    public void addEvent(ProvenanceEventRecordDTO event) {
        eventsCount.incrementAndGet();
        lastEventId = event.getEventId();
        lastProcessDate = new DateTime(event.getEventTime());
        totalDurationTime += event.getEventDuration();
    }

    public AtomicLong getEventsCount() {
        return eventsCount;
    }

    public Double getAvgProcessTime() {
        return totalDurationTime.doubleValue() / eventsCount.get();
    }

    public Long getTotalDurationTime() {
        return totalDurationTime;
    }

    public Long getLastEventId() {
        return lastEventId;
    }

    public DateTime getLastProcessDate() {
        return lastProcessDate;
    }


    public ProcessorStats join(ProcessorStats that) {
        return new ProcessorStats(this, that);

    }
}
