package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-api
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

/**
 * Metadata about events in the {@link JobScheduler}
 */
public class JobSchedulerEvent {

    private EVENT event;
    private JobIdentifier jobIdentifier;
    private TriggerIdentifier triggerIdentifier;
    private String message;

    public JobSchedulerEvent(EVENT event) {
        this.event = event;
    }


    public JobSchedulerEvent(EVENT event, JobIdentifier jobIdentifier, TriggerIdentifier triggerIdentifier, String message) {
        this.event = event;
        this.jobIdentifier = jobIdentifier;
        this.triggerIdentifier = triggerIdentifier;
        this.message = message;
    }

    public JobSchedulerEvent(EVENT event, JobIdentifier jobIdentifier) {
        this.event = event;
        this.jobIdentifier = jobIdentifier;
    }

    public JobSchedulerEvent(EVENT event, TriggerIdentifier triggerIdentifier) {
        this.event = event;
        this.triggerIdentifier = triggerIdentifier;
    }

    public static JobSchedulerEvent pauseAllJobsEvent() {
        return new JobSchedulerEvent(EVENT.PAUSE_ALL_JOBS);
    }

    public static JobSchedulerEvent resumeAllJobsEvent() {
        return new JobSchedulerEvent(EVENT.RESUME_ALL_JOBS);
    }

    public static JobSchedulerEvent scheduledJobEvent(JobIdentifier jobIdentifier) {
        return new JobSchedulerEvent(EVENT.SCHEDULED_JOB, jobIdentifier);
    }

    public static JobSchedulerEvent pauseJobEvent(JobIdentifier jobIdentifier) {
        return new JobSchedulerEvent(EVENT.PAUSE_JOB, jobIdentifier);
    }

    public static JobSchedulerEvent resumeJobEvent(JobIdentifier jobIdentifier) {
        return new JobSchedulerEvent(EVENT.RESUME_JOB, jobIdentifier);
    }

    public static JobSchedulerEvent deleteJobEvent(JobIdentifier jobIdentifier) {
        return new JobSchedulerEvent(EVENT.DELETE_JOB, jobIdentifier);
    }

    public static JobSchedulerEvent triggerJobEvent(JobIdentifier jobIdentifier) {
        return new JobSchedulerEvent(EVENT.TRIGGER_JOB, jobIdentifier);
    }

    public static JobSchedulerEvent schedulerStartedEvent() {
        return new JobSchedulerEvent(EVENT.SCHEDULER_STARTED);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    public TriggerIdentifier getTriggerIdentifier() {
        return triggerIdentifier;
    }

    public void setTriggerIdentifier(TriggerIdentifier triggerIdentifier) {
        this.triggerIdentifier = triggerIdentifier;
    }

    public boolean isEvent(EVENT event) {
        return this.event.equals(event);
    }

    public EVENT getEvent() {
        return event;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JobSchedulerEvent{");
        sb.append("event=").append(event);
        sb.append(", jobIdentifier=").append(jobIdentifier);
        sb.append(", triggerIdentifier=").append(triggerIdentifier);
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public enum EVENT {
        SCHEDULER_STARTED, SCHEDULED_JOB, PAUSE_JOB, RESUME_JOB, PAUSE_ALL_JOBS, RESUME_ALL_JOBS, TRIGGER_JOB, DELETE_JOB;
    }
}
