package com.thinkbiganalytics.nifi.rest.model.flow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/12/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiProcessingStatistics {

    private Long runningComponents;
    private Long finishedComponents;
    private Long avgDuration;
    private Long minDuration;
    private Long maxDuration;

    private AtomicLong failedComponents = new AtomicLong(0L);
    private AtomicLong eventsProcessed = new AtomicLong(0L);

    public NifiProcessingStatistics() {

    }

    public void incrementEventsProcessed() {
        eventsProcessed.incrementAndGet();
    }

    public Long getRunningComponents() {
        return runningComponents;
    }

    public void setRunningComponents(Long runningComponents) {
        this.runningComponents = runningComponents;
    }

    public Long getFinishedComponents() {
        return finishedComponents;
    }

    public void setFinishedComponents(Long finishedComponents) {
        this.finishedComponents = finishedComponents;
    }

    public Long getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(Long avgDuration) {
        this.avgDuration = avgDuration;
    }

    public Long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(Long minDuration) {
        this.minDuration = minDuration;
    }

    public Long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Long maxDuration) {
        this.maxDuration = maxDuration;
    }


    public Long getEventsProcessed() {
        return eventsProcessed.get();
    }

    public void setEventsProcessed(Long eventsProcessed) {
        this.eventsProcessed = new AtomicLong(eventsProcessed);
    }

    public Long incrementFailedComponents() {
        return failedComponents.incrementAndGet();
    }

    public Long getFailedComponents() {
        return failedComponents.get();
    }

    public void setFailedComponents(Long failedComponents) {
        this.failedComponents = new AtomicLong(failedComponents);
    }
}
