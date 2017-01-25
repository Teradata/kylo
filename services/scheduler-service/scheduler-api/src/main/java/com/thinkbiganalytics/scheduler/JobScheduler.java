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
 * Created by sr186054 on 9/23/15.
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
   * @param period interval in milliseconds
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
   * Start the Scheduler after it has been Paused Misfires on Triggers during the pause time will be ignored
   */
  void startScheduler() throws JobSchedulerException;

  /**
   * Pause the Scheduler and Halts the firing of all Triggers Misfires on Triggers during the pause time will be ignored
   */
  void pauseScheduler() throws JobSchedulerException;

  /**
   * Pause all Triggers Resume All is needed to be called Misfires on Triggers will be applied depending upon the Trigger misfire
   * Instructions
   */
  void pauseAll() throws JobSchedulerException;

  /**
   * Resume All Triggers that have been Paused Misfires on Triggers will be applied depending upon the Trigger misfire
   * Instructions
   */
  void resumeAll() throws JobSchedulerException;

  /**
   * Manually Trigger a specific Job
   */
  void triggerJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

  /**
   * Pause a given Trigger
   */
  void pauseTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException;

  /**
   * Resume a give Trigger
   */
  void resumeTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException;


  /**
   *
   * @param jobIdentifier
   * @throws JobSchedulerException
   */
  void pauseTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

  /**
   *
   * @param jobIdentifier
   * @throws JobSchedulerException
   */
  void resumeTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

  /**
   * Update a Triggers Cron Expression
   */
  void updateTrigger(TriggerIdentifier triggerIdentifier, String cronExpression) throws JobSchedulerException;

  /**
   * Delete a Job
   */
  void deleteJob(JobIdentifier jobIdentifier) throws JobSchedulerException;

  /**
   * get a List of all the Jobs scheduled along with their current Triggers
   */
  List<JobInfo> getJobs() throws JobSchedulerException;

  Map<String, Object> getMetaData() throws JobSchedulerException;

  void subscribeToJobSchedulerEvents(JobSchedulerListener listener);


}
