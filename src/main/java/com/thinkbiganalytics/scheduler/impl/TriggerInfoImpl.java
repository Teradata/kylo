package com.thinkbiganalytics.scheduler.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.TriggerInfo;

import java.util.Date;

/**
 * Created by sr186054 on 9/24/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TriggerInfoImpl implements TriggerInfo {


    private TriggerIdentifier triggerIdentifier;
    private JobIdentifier jobIdentifier;
    private Date nextFireTime;
    private Date previousFireTime;
    private Date startTime;
    private Date endTime;
    private String cronExpression;
    private String description;
    public TriggerState state;
    private Class triggerClass;
    private String cronExpressionSummary;

    public TriggerInfoImpl() {

    }
    public TriggerInfoImpl(@JsonProperty("jobIdentifier")JobIdentifier jobIdentifier,@JsonProperty("triggerIdentifier") TriggerIdentifier triggerIdentifier){
        this.jobIdentifier = jobIdentifier;
        this.triggerIdentifier = triggerIdentifier;
    }

    @Override
    public TriggerIdentifier getTriggerIdentifier() {
        return triggerIdentifier;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    @Override
    public Date getNextFireTime() {
        return nextFireTime;
    }

    @Override
    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public TriggerState getState() {
        return state;
    }

    @Override
    public void setTriggerIdentifier(TriggerIdentifier triggerIdentifier) {
        this.triggerIdentifier = triggerIdentifier;
    }

    @Override
    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    @Override
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    @Override
    public void setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setState(TriggerState state) {
        this.state = state;
    }

    @Override
    public Class getTriggerClass() {
        return triggerClass;
    }

    @Override
    public void setTriggerClass(Class triggerClass) {
        this.triggerClass = triggerClass;
    }

    @Override
    public String getCronExpressionSummary() {
        return cronExpressionSummary;
    }

    @Override
    public void setCronExpressionSummary(String cronExpressionSummary) {
        this.cronExpressionSummary = cronExpressionSummary;
    }
}
