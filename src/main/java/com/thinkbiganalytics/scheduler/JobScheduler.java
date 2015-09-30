
package com.thinkbiganalytics.scheduler;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by sr186054 on 9/23/15.
 */
public interface JobScheduler {

    void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Runnable runnable, String cronExpression, TimeZone timeZone) throws JobSchedulerException;

    void scheduleWithCronExpression(JobIdentifier jobIdentifier, Runnable runnable, String cronExpression) throws JobSchedulerException;

    /**
     * Perform a single Job Execution at a given Start Time
     * @param jobIdentifier
     * @param task
     * @param startTime
     * @throws JobSchedulerException
     */
    void schedule(JobIdentifier jobIdentifier, Runnable task, Date startTime) throws JobSchedulerException;

    /**
     * Schedule a single Job Execution at a given Start Time and Delay (i.e. wait 5 min and then run)
     * @param jobIdentifier  unique identifier for this job
     * @param runnable the Job Task
     * @param startTime Time to start the run without the delay
     * @param startDelay delay in milliseconds
     * @throws JobSchedulerException
     */
    void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long startDelay) throws JobSchedulerException;

    /**
     *  Schedule a Job to run with a given delay.
     *
     * @param jobIdentifier
     * @param runnable
     * @param startDelay  delay in milliseconds
     * @throws JobSchedulerException
     */
    void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, long startDelay) throws JobSchedulerException;

    /**
     * Schedule a Job to run with a specific interval starting at a specific time
     * @param jobIdentifier
     * @param runnable  the Job Task
     * @param startTime  time to start the Job
     * @param period interval in milliseconds
     * @throws JobSchedulerException
     */
    void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period) throws JobSchedulerException;

    /**
     * Schedule a Job to run with a specific interval starting now
     * @param jobIdentifier
     * @param runnable
     * @param period interval in milliseconds
     * @throws JobSchedulerException
     */
    void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, long period) throws JobSchedulerException;

    //Cron Utils
    Date getNextFireTime(String cronExpression) throws ParseException;

    Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException;

    List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException;

    Date getPreviousFireTime(String cronExpression) throws ParseException;

    Date getPreviousFireTime(Date lastFireTime, String cronExpression) throws ParseException;

    List<Date> getPreviousFireTimes(String cronExpression, Integer count) throws ParseException;


    //Manage Scheduler and Jobs

    /**
     * Start the Scheduler after it has been Paused
     * Misfires on Triggers during the pause time will be ignored
     * @throws JobSchedulerException
     */
    void startScheduler() throws JobSchedulerException;

    /**
     * Pause the Scheduler and Halts the firing of all Triggers
     * Misfires on Triggers during the pause time will be ignored
     * @throws JobSchedulerException
     */
    void pauseScheduler()  throws JobSchedulerException;

    /**
     * Pause all Triggers
     * Resume All is needed to be called
     * Misfires on Triggers will be applied depending upon the Trigger misfire Instructions
     * @throws JobSchedulerException
     */
    void pauseAll() throws JobSchedulerException;

    /**
     * Resume All Triggers that have been Paused
     * Misfires on Triggers will be applied depending upon the Trigger misfire Instructions
     * @throws JobSchedulerException
     */
    void resumeAll() throws JobSchedulerException;

    /**
     * Check to see if a Trigger currently exists in the Scheduler
     * @param triggerIdentifier
     * @return
     */
    boolean triggerExists(TriggerIdentifier triggerIdentifier);

    /**
     * Check to see if a Job currently exists in the Scheduler
     * @param jobIdentifier
     * @return
     */
    boolean jobExists(JobIdentifier jobIdentifier);

    /**
     * Manually Trigger a specific Job
     * @param jobIdentifier
     * @throws JobSchedulerException
     */
    void triggerJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

    /**
     * Pause a given Trigger
     *
     * @param triggerIdentifier
     * @throws JobSchedulerException
     */
    void pauseTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException;

    /**
     * Resume a give Trigger
     *
     * @param triggerIdentifier
     * @throws JobSchedulerException
     */
    void resumeTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException;


    /**
     * Update a Triggers Cron Expression
     * @param triggerIdentifier
     * @param cronExpression
     * @throws JobSchedulerException
     */
    void updateTrigger(TriggerIdentifier triggerIdentifier, String cronExpression) throws  JobSchedulerException;

    /**
     * Delete a Job
     *
     * @param jobIdentifier
     * @throws JobSchedulerException
     */
    void deleteJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

    /**
     * get a List of all the Jobs scheduled along with their current Triggers
     * @return
     * @throws JobSchedulerException
     */
    List<JobInfo> getJobs() throws JobSchedulerException;

    Map<String,Object> getMetaData()  throws JobSchedulerException ;




}
