package com.thinkbiganalytics.scheduler.taskscheduler;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.JobInfo;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.support.CronTrigger;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by sr186054 on 9/23/15.
 */
public class SpringScheduler  implements JobScheduler{

    private TaskScheduler taskScheduler;

    public SpringScheduler(){

    }

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Runnable runnable, String cronExpression, TimeZone timeZone) throws JobSchedulerException {
        CronTrigger cronTrigger = new CronTrigger(cronExpression,timeZone);
        taskScheduler.schedule(runnable,cronTrigger);
    }

    @Override
    public void scheduleWithCronExpression(JobIdentifier jobIdentifier, Runnable task, String cronExpression) throws JobSchedulerException {
        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        taskScheduler.schedule(task, cronTrigger);
    }

    @Override
    public void schedule(JobIdentifier jobIdentifier, Runnable task, Date startTime) throws JobSchedulerException {
        taskScheduler.schedule(task, startTime);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long startDelay) throws JobSchedulerException {
        taskScheduler.scheduleWithFixedDelay(runnable, startTime, startDelay);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, long startDelay) throws JobSchedulerException {
        taskScheduler.scheduleWithFixedDelay(runnable, startDelay);
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, long period) throws JobSchedulerException {
        taskScheduler.scheduleAtFixedRate(runnable, period);
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period) throws JobSchedulerException {
        taskScheduler.scheduleAtFixedRate(runnable, startTime, period);
    }


    @Override
    public void startScheduler() throws JobSchedulerException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void pauseScheduler() throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pauseAll() throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeAll() throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pauseTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextFireTime(String cronExpression) throws ParseException {
        return CronExpressionUtil.getNextFireTime(cronExpression);
    }

    @Override
    public Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException {
        return CronExpressionUtil.getNextFireTime(lastFireTime,cronExpression);
    }

    @Override
    public List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException {
        return CronExpressionUtil.getNextFireTimes(cronExpression,count);
    }

    @Override
    public Date getPreviousFireTime(String cronExpression) throws ParseException {
        return CronExpressionUtil.getPreviousFireTime(cronExpression);
    }

    @Override
    public Date getPreviousFireTime(Date lastFireTime, String cronExpression) throws ParseException {
        return CronExpressionUtil.getPreviousFireTime(lastFireTime, cronExpression);
    }

    @Override
    public List<Date> getPreviousFireTimes(String cronExpression, Integer count) throws ParseException {
        return CronExpressionUtil.getPreviousFireTimes(cronExpression, count);
    }


    @Override
    public List<JobInfo> getJobs() throws JobSchedulerException {
        return null;
    }

    @Override
    public void updateTrigger(TriggerIdentifier triggerIdentifier, String cronExpression) throws JobSchedulerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getMetaData() throws JobSchedulerException {
       return null;
    }
}
