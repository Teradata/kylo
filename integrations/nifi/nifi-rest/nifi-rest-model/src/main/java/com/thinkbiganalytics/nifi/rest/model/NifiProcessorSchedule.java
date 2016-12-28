package com.thinkbiganalytics.nifi.rest.model;


import com.thinkbiganalytics.metadata.MetadataField;

/**
 * Created by sr186054 on 1/13/16.
 */
public class NifiProcessorSchedule {

    @MetadataField
    private String schedulingPeriod;
    @MetadataField
    private String schedulingStrategy;
    @MetadataField
    private Integer concurrentTasks;

    public String getSchedulingPeriod() {
        return schedulingPeriod;
    }

    public void setSchedulingPeriod(String schedulingPeriod) {
        this.schedulingPeriod = schedulingPeriod;
    }

    public String getSchedulingStrategy() {
        return schedulingStrategy;
    }

    public void setSchedulingStrategy(String schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
    }

    public Integer getConcurrentTasks() {
        return concurrentTasks;
    }

    public void setConcurrentTasks(Integer concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
    }
}
