package com.thinkbiganalytics.nifi.provenance.model;

import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/15/16.
 */
public class ProcessorStats {

    private String processorId;

    public ProcessorStats(String processorId) {
        this.processorId = processorId;
    }

    private AtomicLong eventsCount = new AtomicLong(0L);

    private Double avgProcessTime;

    private Long totalDurationTime = 0L;

    private Long lastEventId;

    private DateTime lastProcessDate;

    public String getProcessorId() {
        return processorId;
    }


    public void addEvent(ProvenanceEventRecordDTO event) {
        Long totalEvents = eventsCount.incrementAndGet();
        lastEventId = event.getEventId();
        //?? store the last n events?
        lastProcessDate = new DateTime(event.getEventTime());
        totalDurationTime += event.getEventDuration();
        avgProcessTime = totalDurationTime.doubleValue() / totalEvents.doubleValue();


    }

    public AtomicLong getEventsCount() {
        return eventsCount;
    }

    public Double getAvgProcessTime() {
        return avgProcessTime;
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
}
