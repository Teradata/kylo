package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-quartz
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

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;

import java.util.Date;
import java.util.List;

/**
 * Java class representing a scheduled job.
 */
public class QuartzScheduledJob implements ScheduledJob {

    private JobIdentifier jobIdentifier;
    private String jobName;
    private String jobStatus;
    private String jobGroup;
    private String nextFireTimeString;
    private String cronExpression;
    private Date nextFireTime;
    private List<TriggerInfo> triggers;
    private String state;
    private String cronExpressionSummary;

    public QuartzScheduledJob() {
    }

    @Override
    public String getJobStatus() {
        return jobStatus;
    }

    @Override
    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public String getJobGroup() {
        return jobGroup;
    }

    @Override
    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    @Override
    public String getNextFireTimeString() {
        return nextFireTimeString;
    }

    @Override
    public void setNextFireTimeString(String nextFireTimeString) {
        this.nextFireTimeString = nextFireTimeString;
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
    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public List<TriggerInfo> getTriggers() {
        return triggers;
    }

    @Override
    public void setTriggers(List<TriggerInfo> triggers) {
        this.triggers = triggers;
    }


    @Override
    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    @Override
    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    @Override
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

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getCronExpressionSummary() {
        return cronExpressionSummary;
    }

    @Override
    public void setCronExpressionData() {
        if (getTriggers() != null) {
            for (TriggerInfo triggerInfo : getTriggers()) {
                if (CronTrigger.class.isAssignableFrom(triggerInfo.getTriggerClass())) {
                    this.setCronExpression(triggerInfo.getCronExpression());
                    this.nextFireTime = triggerInfo.getNextFireTime();
                    this.cronExpressionSummary = triggerInfo.getCronExpressionSummary();
                    break;
                }
            }
        }
    }

    @Override
    public boolean isRunning() {
        boolean running = false;
        if (getTriggers() != null) {
            for (TriggerInfo triggerInfo : getTriggers()) {
                running =
                    triggerInfo.getState().equals(TriggerInfo.TriggerState.BLOCKED) || (
                        !CronTrigger.class.isAssignableFrom(triggerInfo.getTriggerClass()) && SimpleTrigger.class
                            .isAssignableFrom(triggerInfo.getTriggerClass()));
                if (running) {
                    break;
                }
            }
        }
        return running;
    }

    @Override
    public boolean isPaused() {
        boolean paused = false;
        if (getTriggers() != null) {
            for (TriggerInfo triggerInfo : getTriggers()) {
                paused = triggerInfo.getState().equals(TriggerInfo.TriggerState.PAUSED);
                if (paused) {
                    break;
                }
            }
        }
        return paused;
    }

    @Override
    public boolean isScheduled() {
        boolean scheduled = false;
        if (getTriggers() != null) {
            for (TriggerInfo triggerInfo : getTriggers()) {
                if (CronTrigger.class.isAssignableFrom(triggerInfo.getTriggerClass())) {
                    scheduled = true;
                    break;
                }
            }
        }
        return scheduled;
    }


}
