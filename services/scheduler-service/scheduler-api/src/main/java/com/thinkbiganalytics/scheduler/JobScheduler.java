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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Scheduler interface
 */
public interface JobScheduler {

    void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Runnable runnable, String cronExpression,
                                              TimeZone timeZone) throws JobSchedulerException;

    void scheduleWithCronExpression(JobIdentifier jobIdentifier, Runnable runnable, String cronExpression)
        throws JobSchedulerException;

    /**
     * Perform a single Job Execution at a given Start Time
     */
    void schedule(JobIdentifier jobIdentifier, Runnable task, Date startTime) throws JobSchedulerException;

    /**
     * Schedule a single Job Execution at a given Start Time and Delay (i.e. wait 5 min and then run)
     *
     * @param jobIdentifier unique identifier for this job
     * @param runnable      the Job Task
     * @param startTime     Time to start the run without the delay
     * @param startDelay    delay in milliseconds
     */
    void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long startDelay)
        throws JobSchedulerException;

    /**
     * Schedule a Job to run with a given delay.
     *
     * @param startDelay delay in milliseconds
     */
    void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, long startDelay) throws JobSchedulerException;

    /**
     * Schedule a Job to run with a specific interval starting at a specific time
     *
     * @param runnable  the Job Task
     * @param startTime time to start the Job
     * @param period    interval in milliseconds
     */
    void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period)
        throws JobSchedulerException;

    /**
     * Schedule a Job to run with a specific interval starting now
     *
     * @param jobIdentifier a job identifier
     * @param runnable      a runnable task
     * @param period        interval in millis
     */
    void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, long period) throws JobSchedulerException;

    /**
     * Return the next fire time for a cron expression
     *
     * @param cronExpression a cron expression
     * @return the next fire time
     */
    Date getNextFireTime(String cronExpression) throws ParseException;

    /**
     * Return the next fire time for a cron expression from a previous fire time
     *
     * @param lastFireTime   a previous fire time
     * @param cronExpression a cron expression
     * @return the next fire time, relative to the lastFireTime
     */
    Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException;

    /**
     * Return a list of the next fire times for a cron expression
     *
     * @param cronExpression a cron expression
     * @param count          the number of dates to return
     * @return a list of dates
     */
    List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException;

    /**
     * Return the previous fire time for a cron expression
     *
     * @param cronExpression a cron expression
     * @return the previous fire time
     */
    Date getPreviousFireTime(String cronExpression) throws ParseException;

    /**
     * Return the previous time from lastFireTime for a cron expression
     *
     * @param lastFireTime   the previous fire time
     * @param cronExpression a cron expression
     * @return the previous time from lastFireTime for a cron expression
     */
    Date getPreviousFireTime(Date lastFireTime, String cronExpression) throws ParseException;

    /**
     * Return a list of previous fire times for a cron expression
     *
     * @param cronExpression a cron expression
     * @param count          the number of dates to return
     * @return a list of previous fire times for a cron expression
     */
    List<Date> getPreviousFireTimes(String cronExpression, Integer count) throws ParseException;

    /**
     * Start the Scheduler after it has been Paused
     */
    void startScheduler() throws JobSchedulerException;

    /**
     * Pause the Scheduler and Halts the firing of all Triggers.
     */
    void pauseScheduler() throws JobSchedulerException;

    /**
     * Pause all
     */
    void pauseAll() throws JobSchedulerException;

    /**
     * Resume all jobs and triggers
     */
    void resumeAll() throws JobSchedulerException;

    /**
     * Manually Trigger a specific Job
     *
     * @param jobIdentifier a job identifier
     */
    void triggerJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

    /**
     * Pause a given Trigger
     *
     * @param triggerIdentifier a trigger identifier
     */
    void pauseTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException;

    /**
     * Resume a give Trigger
     *
     * @param triggerIdentifier a trigger identifier
     */
    void resumeTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException;


    /**
     * pause a given job
     *
     * @param jobIdentifier a job identifier
     */
    void pauseTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

    /**
     * resume a given job
     *
     * @param jobIdentifier a job identifier
     */
    void resumeTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

    /**
     * Update a Triggers Cron Expression
     *
     * @param triggerIdentifier a trigger identifier
     * @param cronExpression    an updated cron expression
     */
    void updateTrigger(TriggerIdentifier triggerIdentifier, String cronExpression) throws JobSchedulerException;

    /**
     * Delete a Job
     *
     * @param jobIdentifier a job identifier
     */
    void deleteJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

    /**
     * Return a list of jobs scheduled
     *
     * @return a list of jobs scheduled
     */
    List<JobInfo> getJobs() throws JobSchedulerException;

    /**
     * Return Metadata and properties about the schedule
     *
     * @return Metadata and properties about the schedule
     */
    Map<String, Object> getMetaData() throws JobSchedulerException;

    /**
     * Listen when schedule events are fired.
     *
     * @param listener a scheduler listener
     */
    void subscribeToJobSchedulerEvents(JobSchedulerListener listener);


}
