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


import java.util.Date;
import java.util.List;

/**
 * Hold schedule information about a job
 */
public class ScheduledJob {

    private ScheduleIdentifier jobIdentifier;
    private String jobName;
    private String jobStatus;
    private String jobGroup;
    private String nextFireTimeString;
    private String cronExpression;
    private Date nextFireTime;
    private List<TriggerInfo> triggers;
    private String state;
    private String cronExpressionSummary;
    private boolean isRunning;
    private boolean isPaused;
    private boolean isScheduled;

    public ScheduledJob() {
    }


    public String getJobStatus() {
        return jobStatus;
    }


    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }


    public String getJobGroup() {
        return jobGroup;
    }


    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }


    public String getNextFireTimeString() {
        return nextFireTimeString;
    }


    public void setNextFireTimeString(String nextFireTimeString) {
        this.nextFireTimeString = nextFireTimeString;
    }


    public Date getNextFireTime() {
        return nextFireTime;
    }


    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getJobName() {
        return jobName;
    }


    public void setJobName(String jobName) {
        this.jobName = jobName;
    }


    public List<TriggerInfo> getTriggers() {
        return triggers;
    }


    public void setTriggers(List<TriggerInfo> triggers) {
        this.triggers = triggers;
    }


    public ScheduleIdentifier getJobIdentifier() {
        return jobIdentifier;
    }


    public void setJobIdentifier(ScheduleIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {

        this.state = state;
    }

    public String getCronExpressionSummary() {
        return cronExpressionSummary;
    }


    public boolean isRunning() {

        return isRunning;
    }


    public boolean isPaused() {

        return isPaused;
    }


    public boolean isScheduled() {

        return isScheduled;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void setIsScheduled(boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    public void setState() {
        String state = "";
        if (isRunning()) {
            state = "RUNNING";
        } else if (isPaused()) {
            state = "PAUSED";
        } else if (isScheduled()) {
            state = "SCHEDULED";
        } else {
            state = "UNKNOWN";
        }
        this.state = state;
    }

    public void setCronExpressionData() {
        if (getTriggers() != null) {
            for (TriggerInfo triggerInfo : getTriggers()) {
                if (triggerInfo.getCronExpression() != null && !triggerInfo.getCronExpression().equalsIgnoreCase("")) {
                    this.setCronExpression(triggerInfo.getCronExpression());
                    this.nextFireTime = triggerInfo.getNextFireTime();
                    this.cronExpressionSummary = triggerInfo.getCronExpressionSummary();
                    break;
                }
            }
        }
    }

}
