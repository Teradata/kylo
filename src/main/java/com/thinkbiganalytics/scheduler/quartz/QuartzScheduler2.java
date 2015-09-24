package com.thinkbiganalytics.scheduler.quartz;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerExecption;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by sr186054 on 9/23/15.
 */
public class QuartzScheduler2 implements JobScheduler {

    @Autowired
    @Qualifier("schedulerFactoryBean")
    private SchedulerFactoryBean schedulerFactoryBean;

    public Scheduler getScheduler() {
        return schedulerFactoryBean.getScheduler();
    }

    private JobDetail getJobDetail(JobIdentifier jobIdentifier, Object task, String runMethod) throws NoSuchMethodException, ClassNotFoundException {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(task);
        bean.setTargetMethod(runMethod);
        bean.setName(jobIdentifier.getName());
        bean.setGroup(jobIdentifier.getGroupName());
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Override
    public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Runnable task, String cronExpression, TimeZone timeZone) throws JobSchedulerExecption {
        scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression, null);
    }

    @Override
    public void scheduleWithCronExpression(JobIdentifier jobIdentifier, Runnable task, String cronExpression) throws JobSchedulerExecption {
        scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression,null);
    }

    public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier,Object task, String runMethod, String cronExpression, TimeZone timeZone) throws JobSchedulerExecption {
        try {
            JobDetail jobDetail = getJobDetail(jobIdentifier,task,runMethod);
            if(timeZone == null){
                timeZone = TimeZone.getDefault();
            }

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(new TriggerKey("trigger_"+jobIdentifier.getUniqueName(), jobIdentifier.getGroupName()))
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .inTimeZone(timeZone)
                            .withMisfireHandlingInstructionFireAndProceed()).build();


           // scheduleJob(jobDetail,trigger);
        }
        catch(Exception e){
            throw new JobSchedulerExecption();
        }
    }



    @Override
    public void schedule(JobIdentifier jobIdentifier, Runnable task, Date startTime) throws JobSchedulerExecption {
        schedule(jobIdentifier, task, "run", startTime);
    }
    public  void schedule(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime) throws JobSchedulerExecption {

        scheduleWithFixedDelay(jobIdentifier, task, runMethod, startTime, 0L);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, long startDelay) throws JobSchedulerExecption {
        scheduleWithFixedDelay(jobIdentifier, runnable, "run", new Date(), 0L);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long startDelay) throws JobSchedulerExecption {
        scheduleWithFixedDelay(jobIdentifier, runnable, "run", startTime, startDelay);
    }

    public  void scheduleWithFixedDelay(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime, long startDelay) throws JobSchedulerExecption {
        try {
            JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
            Date triggerStartTime = startTime;
            if(startDelay > 0L || startTime == null) {
                triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
            }
            Trigger trigger = newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroupName())).forJob(jobDetail).startAt(triggerStartTime).build();
            getScheduler().scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw new JobSchedulerExecption();
        }
    }



    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period) throws JobSchedulerExecption {
        scheduleAtFixedRate(jobIdentifier, runnable, "run", startTime, period);
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, long period) throws JobSchedulerExecption {
        scheduleAtFixedRate(jobIdentifier, runnable, "run", new Date(), period);
    }

    public  void scheduleAtFixedRate(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime, long period) throws JobSchedulerExecption {
        scheduleAtFixedRateWithDelay(jobIdentifier, task, runMethod, startTime, period, 0L);
    }

    public  void scheduleAtFixedRateWithDelay(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime, long period, long startDelay) throws JobSchedulerExecption {
        try {
            JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
            Date triggerStartTime = startTime;
            if(startDelay > 0L || startTime == null) {
                triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
            }
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroupName())).forJob(jobDetail).startAt(triggerStartTime).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(period).repeatForever()).build();
           scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw new JobSchedulerExecption(e);
        }
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



    public Date scheduleJob(JobDetail job, Trigger trigger) throws SchedulerException {
        Scheduler sched = getScheduler();
        if (sched != null) {
            try {
                return sched.scheduleJob(job, trigger);
            }
            catch (ObjectAlreadyExistsException existsExc)
            {
                System.out.println("Someone has already scheduled such job/trigger. " + job.getKey() + " : " + trigger.getKey());
            }
        }
        return null;
    }


    @Override
    public void startScheduler() throws JobSchedulerExecption {

    }

    @Override
    public void pauseScheduler() throws JobSchedulerExecption {

    }

    @Override
    public void shutdownScheduler() throws JobSchedulerExecption {

    }

    @Override
    public void triggerJob(JobIdentifier jobKey) throws JobSchedulerExecption {

    }

    @Override
    public void pauseTrigger(TriggerIdentifier triggerKey) throws JobSchedulerExecption {

    }

    @Override
    public void deleteJob(JobIdentifier jobKey) throws JobSchedulerExecption {

    }

    @Override
    public void scheduleQuartzJobBean(JobIdentifier jobKey, Class<? extends QuartzJobBean> clazz, String cronExpression, Map<String, Object> jobData) {

    }
}
