package com.thinkbiganalytics.scheduler.rest.model;

/*-
 * #%L
 * thinkbig-scheduler-rest-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Trigger Info user interface object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TriggerInfo {

    private TriggerState state;
    private ScheduleIdentifier triggerIdentifier;
    private ScheduleIdentifier jobIdentifier;
    private Date nextFireTime;
    private Date previousFireTime;
    private Date startTime;
    private Date endTime;
    private String cronExpression;
    private String description;
    private Class triggerClass;
    private String cronExpressionSummary;

    public TriggerInfo() {

    }

    public TriggerInfo(@JsonProperty("jobIdentifier") ScheduleIdentifier jobIdentifier,
                       @JsonProperty("triggerIdentifier") ScheduleIdentifier triggerIdentifier) {
        this.jobIdentifier = jobIdentifier;
        this.triggerIdentifier = triggerIdentifier;
    }

    public ScheduleIdentifier getTriggerIdentifier() {
        return triggerIdentifier;
    }

    public void setTriggerIdentifier(ScheduleIdentifier triggerIdentifier) {
        this.triggerIdentifier = triggerIdentifier;
    }

    public ScheduleIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public void setJobIdentifier(ScheduleIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    public void setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TriggerState getState() {
        return state;
    }

    public void setState(TriggerState state) {
        this.state = state;
    }

    public Class getTriggerClass() {
        return triggerClass;
    }

    public void setTriggerClass(Class triggerClass) {
        this.triggerClass = triggerClass;
    }

    public String getCronExpressionSummary() {
        return cronExpressionSummary;
    }

    public void setCronExpressionSummary(String cronExpressionSummary) {
        this.cronExpressionSummary = cronExpressionSummary;
    }


    public static enum TriggerState {
        NONE,
        NORMAL,
        PAUSED,
        COMPLETE,
        ERROR,
        BLOCKED;
    }
}
