package com.thinkbiganalytics.scheduler.model;

/*-
 * #%L
 * thinkbig-scheduler-core
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
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.TriggerInfo;

import java.util.Date;

/**
 * Default trigger information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTriggerInfo implements TriggerInfo {


    private TriggerState state;
    boolean simpleTrigger;
    boolean scheduled;
    private TriggerIdentifier triggerIdentifier;
    private JobIdentifier jobIdentifier;
    private Date nextFireTime;
    private Date previousFireTime;
    private Date startTime;
    private Date endTime;
    private String cronExpression;
    private String description;
    private Class triggerClass;
    private String cronExpressionSummary;

    public DefaultTriggerInfo() {

    }

    public DefaultTriggerInfo(@JsonProperty("jobIdentifier") JobIdentifier jobIdentifier,
                              @JsonProperty("triggerIdentifier") TriggerIdentifier triggerIdentifier) {
        this.jobIdentifier = jobIdentifier;
        this.triggerIdentifier = triggerIdentifier;
    }

    @Override
    public TriggerIdentifier getTriggerIdentifier() {
        return triggerIdentifier;
    }

    @Override
    public void setTriggerIdentifier(TriggerIdentifier triggerIdentifier) {
        this.triggerIdentifier = triggerIdentifier;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    @Override
    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    @Override
    public Date getNextFireTime() {
        return nextFireTime;
    }

    @Override
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    @Override
    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    @Override
    public void setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public TriggerState getState() {
        return state;
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

    @Override
    public boolean isSimpleTrigger() {
        return simpleTrigger;
    }

    @Override
    public void setSimpleTrigger(boolean isSimpleTrigger) {
        this.simpleTrigger = isSimpleTrigger;
    }

    @Override
    public boolean isScheduled() {
        return scheduled;
    }

    @Override
    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
}
