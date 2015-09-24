package com.thinkbiganalytics.scheduler.taskscheduler;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerExecption;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    public void scheduleWithCronExpressionInTimeZone(JobIdentifier scheduleIdentifier, Runnable task, String cronExpression, TimeZone timeZone) throws JobSchedulerExecption {
        CronTrigger cronTrigger = new CronTrigger(cronExpression,timeZone);
        taskScheduler.schedule(task,cronTrigger);
    }

    @Override
    public void scheduleWithCronExpression(JobIdentifier scheduleIdentifier, Runnable task, String cronExpression) throws JobSchedulerExecption {
        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        taskScheduler.schedule(task, cronTrigger);
    }

    @Override
    public void schedule(JobIdentifier scheduleIdentifier, Runnable task, Date startTime) throws JobSchedulerExecption {
        taskScheduler.schedule(task, startTime);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier scheduleIdentifier, Runnable runnable, Date startTime, long startDelay) throws JobSchedulerExecption {
        taskScheduler.scheduleWithFixedDelay(runnable, startTime, startDelay);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier scheduleIdentifier, Runnable runnable, long startDelay) throws JobSchedulerExecption {
        taskScheduler.scheduleWithFixedDelay(runnable, startDelay);
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier scheduleIdentifier, Runnable runnable, long period) throws JobSchedulerExecption {
        taskScheduler.scheduleAtFixedRate(runnable,period);
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier scheduleIdentifier, Runnable runnable, Date startTime, long period) throws JobSchedulerExecption {
        taskScheduler.scheduleAtFixedRate(runnable,startTime,period);
    }


    @Override
    public void startScheduler() throws JobSchedulerExecption {
      throw new UnsupportedOperationException();
    }

    @Override
    public void pauseScheduler() throws JobSchedulerExecption {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdownScheduler() throws JobSchedulerExecption {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerJob(JobIdentifier jobKey) throws JobSchedulerExecption {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pauseTrigger(TriggerIdentifier triggerKey) throws JobSchedulerExecption {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteJob(JobIdentifier jobKey) throws JobSchedulerExecption {
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
    public void scheduleQuartzJobBean(JobIdentifier jobKey, Class<? extends QuartzJobBean> clazz, String cronExpression, Map<String, Object> jobData) {
        throw new UnsupportedOperationException();
    }
}
