package com.thinkbiganalytics.scheduler.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;

/**
 * Created by sr186054 on 9/25/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SimpleTriggerInfo {
    private TriggerIdentifier triggerIdentifier;
    private JobIdentifier jobIdentifier;
    private String cronExpression;

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    public SimpleTriggerInfo() {

    }

    public SimpleTriggerInfo(@JsonProperty("jobIdentifier")JobIdentifier jobIdentifier,@JsonProperty("triggerIdentifier") TriggerIdentifier triggerIdentifier){
        this.jobIdentifier = jobIdentifier;
        this.triggerIdentifier = triggerIdentifier;
    }

    public TriggerIdentifier getTriggerIdentifier() {
        return triggerIdentifier;
    }

    public void setTriggerIdentifier(TriggerIdentifier triggerIdentifier) {
        this.triggerIdentifier = triggerIdentifier;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
