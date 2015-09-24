package com.thinkbiganalytics.scheduler;

import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by sr186054 on 9/23/15.
 */
public interface JobScheduler {


    //Schedule Jobs
    void scheduleWithCronExpressionInTimeZone(JobIdentifier scheduleIdentifier, Runnable task, String cronExpression, TimeZone timeZone) throws JobSchedulerExecption;

    void scheduleWithCronExpression(JobIdentifier scheduleIdentifier, Runnable task, String cronExpression) throws JobSchedulerExecption;

    void schedule(JobIdentifier scheduleIdentifier, Runnable task, Date startTime) throws JobSchedulerExecption;

    void scheduleWithFixedDelay(JobIdentifier scheduleIdentifier, Runnable runnable, Date startTime, long startDelay) throws JobSchedulerExecption;

    void scheduleWithFixedDelay(JobIdentifier scheduleIdentifier, Runnable runnable, long startDelay) throws JobSchedulerExecption;

    void scheduleAtFixedRate(JobIdentifier scheduleIdentifier, Runnable runnable, Date startTime, long period) throws JobSchedulerExecption;

    void scheduleAtFixedRate(JobIdentifier scheduleIdentifier, Runnable runnable, long period) throws JobSchedulerExecption;

    void scheduleQuartzJobBean(JobIdentifier jobKey,Class<? extends QuartzJobBean> clazz, String cronExpression,Map<String,Object> jobData);

    //Cron Utils
    Date getNextFireTime(String cronExpression) throws ParseException;

    Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException;

    List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException;


    //Manage Scheduler and Jobs
    void startScheduler() throws JobSchedulerExecption;

    void pauseScheduler()  throws JobSchedulerExecption;

    void shutdownScheduler() throws JobSchedulerExecption;

    void triggerJob(JobIdentifier jobKey) throws JobSchedulerExecption;

    void pauseTrigger(TriggerIdentifier triggerKey) throws JobSchedulerExecption;

    void deleteJob(JobIdentifier jobKey) throws JobSchedulerExecption;


}
