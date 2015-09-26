package com.thinkbiganalytics.scheduler.quartz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.scheduler.*;
import com.thinkbiganalytics.scheduler.impl.JobInfoImpl;
import com.thinkbiganalytics.scheduler.impl.TriggerInfoImpl;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by sr186054 on 9/23/15.
 */
@Service
public class QuartzScheduler implements JobScheduler {

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
        bean.setGroup(jobIdentifier.getGroup());
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Override
    public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Runnable task, String cronExpression, TimeZone timeZone) throws JobSchedulerException {
        scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression, null);
    }

    @Override
    public void scheduleWithCronExpression(JobIdentifier jobIdentifier, Runnable task, String cronExpression) throws JobSchedulerException {
        scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression, null);
    }

    public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Object task, String runMethod, String cronExpression, TimeZone timeZone) throws JobSchedulerException {
        try {
            JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
            if (timeZone == null) {
                timeZone = TimeZone.getDefault();
            }

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(new TriggerKey("trigger_" + jobIdentifier.getUniqueName(), jobIdentifier.getGroup()))
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .inTimeZone(timeZone)
                            .withMisfireHandlingInstructionFireAndProceed()).build();
            // scheduleJob(jobDetail,trigger);
        } catch (Exception e) {
            throw new JobSchedulerException();
        }
    }


    @Override
    public void schedule(JobIdentifier jobIdentifier, Runnable task, Date startTime) throws JobSchedulerException {
        schedule(jobIdentifier, task, "run", startTime);
    }

    public void schedule(JobIdentifier jobIdentifier, Object task, String runMethod, Date startTime) throws JobSchedulerException {

        scheduleWithFixedDelay(jobIdentifier, task, runMethod, startTime, 0L);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, long startDelay) throws JobSchedulerException {
        scheduleWithFixedDelay(jobIdentifier, runnable, "run", new Date(), 0L);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long startDelay) throws JobSchedulerException {
        scheduleWithFixedDelay(jobIdentifier, runnable, "run", startTime, startDelay);
    }

    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Object task, String runMethod, Date startTime, long startDelay) throws JobSchedulerException {
        try {
            JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
            Date triggerStartTime = startTime;
            if (startDelay > 0L || startTime == null) {
                triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
            }
            Trigger trigger = newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroup())).forJob(jobDetail).startAt(triggerStartTime).build();
            getScheduler().scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw new JobSchedulerException();
        }
    }


    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period) throws JobSchedulerException {
        scheduleAtFixedRate(jobIdentifier, runnable, "run", startTime, period);
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, long period) throws JobSchedulerException {
        scheduleAtFixedRate(jobIdentifier, runnable, "run", new Date(), period);
    }

    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Object task, String runMethod, Date startTime, long period) throws JobSchedulerException {
        scheduleAtFixedRateWithDelay(jobIdentifier, task, runMethod, startTime, period, 0L);
    }

    public void scheduleAtFixedRateWithDelay(JobIdentifier jobIdentifier, Object task, String runMethod, Date startTime, long period, long startDelay) throws JobSchedulerException {

        JobDetail jobDetail = null;
        try {
            jobDetail = getJobDetail(jobIdentifier, task, runMethod);

            Date triggerStartTime = startTime;
            if (startDelay > 0L || startTime == null) {
                triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
            }
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroup())).forJob(jobDetail).startAt(triggerStartTime).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(period).repeatForever()).build();
            getScheduler().scheduleJob(jobDetail, trigger);
        } catch (NoSuchMethodException | ClassNotFoundException | SchedulerException e) {
            throw new JobSchedulerException("Error calling scheduleAtFixedRateWithDelay", e);
        }


    }


    @Override
    public Date getNextFireTime(String cronExpression) throws ParseException {
        return CronExpressionUtil.getNextFireTime(cronExpression);
    }

    @Override
    public Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException {
        return CronExpressionUtil.getNextFireTime(lastFireTime, cronExpression);
    }

    @Override
    public List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException {
        return CronExpressionUtil.getNextFireTimes(cronExpression, count);
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
    public void startScheduler() throws JobSchedulerException {
        try {
            getScheduler().start();
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Start the Scheduler", e);
        }
    }

    @Override
    public void pauseScheduler() throws JobSchedulerException {
        try {
            getScheduler().standby();
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Pause the Scheduler", e);
        }
    }

    @Override
    public void triggerJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            getScheduler().triggerJob(jobKeyForJobIdentifier(jobIdentifier));
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Trigger the Job " + jobIdentifier, e);
        }
    }

    @Override
    public void resumeTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException {
        try {
            getScheduler().resumeTrigger(triggerKeyForTriggerIdentifier(triggerIdentifier));
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Resume the Trigger " + triggerIdentifier, e);
        }
    }

    @Override
    public void pauseTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException {
        try {
            getScheduler().pauseTrigger(triggerKeyForTriggerIdentifier(triggerIdentifier));
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Pause the Trigger " + triggerIdentifier, e);
        }
    }

    public void updateTrigger(TriggerIdentifier triggerIdentifier, String cronExpression) throws  JobSchedulerException {

        CronTrigger trigger = newTrigger().withIdentity(triggerIdentifier.getName(), triggerIdentifier.getGroup()).withSchedule(cronSchedule(cronExpression)).build();
        try {
            updateTrigger(triggerIdentifier, trigger);
        } catch (SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void deleteJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            getScheduler().deleteJob(jobKeyForJobIdentifier(jobIdentifier));
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Delete the Job " + jobIdentifier, e);
        }
    }



    public static JobIdentifier jobIdentifierForJobKey(JobKey jobKey) {
        return new JobIdentifier(jobKey.getName(), jobKey.getGroup());
    }

    public static JobKey jobKeyForJobIdentifier(JobIdentifier jobIdentifier) {
        return new JobKey(jobIdentifier.getName(), jobIdentifier.getGroup());
    }

    public static TriggerKey triggerKeyForTriggerIdentifier(TriggerIdentifier triggerIdentifier) {
        return new TriggerKey(triggerIdentifier.getName(), triggerIdentifier.getGroup());
    }

    public static TriggerIdentifier triggerIdentifierForTriggerKey(TriggerKey triggerKey){
        return new TriggerIdentifier(triggerKey.getName(),triggerKey.getGroup());
    }

    public JobInfo buildJobInfo(JobDetail jobDetail){
        JobInfo detail = new JobInfoImpl(jobIdentifierForJobKey(jobDetail.getKey()));

        detail.setDescription(jobDetail.getDescription());
        detail.setJobClass(jobDetail.getJobClass());
        detail.setJobData(jobDetail.getJobDataMap().getWrappedMap());
        /*
        this.durability = jobDetail.isDurable();
        this.shouldRecover = jobDetail.requestsRecovery();
        this.isPersistJobDataAfterExecution = jobDetail.isPersistJobDataAfterExecution();
        this.isConcurrentExectionDisallowed = jobDetail.isConcurrentExectionDisallowed();
        this.requestsRecovery = jobDetail.requestsRecovery();
        */
        return detail;
    }

    private TriggerInfo buildTriggerInfo(JobIdentifier jobIdentifier,Trigger trigger){
        TriggerInfo triggerInfo = new TriggerInfoImpl(jobIdentifier,triggerIdentifierForTriggerKey(trigger.getKey()));
        triggerInfo.setDescription(trigger.getDescription());
        String cronExpression = null;
        if (trigger instanceof CronTrigger) {
            CronTrigger ct = (CronTrigger) trigger;
            cronExpression = ct.getCronExpression();
        }
        triggerInfo.setCronExpression(cronExpression);
        triggerInfo.setNextFireTime(trigger.getNextFireTime());
        triggerInfo.setStartTime(trigger.getStartTime());
        triggerInfo.setEndTime(trigger.getEndTime());
        //triggerInfo.setFinalFireTime(trigger.getFinalFireTime());
        triggerInfo.setPreviousFireTime(trigger.getPreviousFireTime());
        return triggerInfo;
    }

    @Override
    public List<JobInfo> getJobs() throws JobSchedulerException {
        List<JobInfo> list = new ArrayList<JobInfo>();
        Scheduler sched = getScheduler();
        try {
            // enumerate each job group
            for (String group : sched.getJobGroupNames()) {
                // enumerate each job in group
                for (JobKey jobKey : sched.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                    JobDetail jobDetail = sched.getJobDetail(jobKey);
                    JobInfo detail =buildJobInfo(jobDetail);
                    list.add(detail);
                    List<? extends Trigger> jobTriggers = sched.getTriggersOfJob(jobKey);

                    List<TriggerInfo> triggerInfoList = new ArrayList<TriggerInfo>();
                    if (jobTriggers != null) {
                        for (Trigger trigger : jobTriggers) {
                            TriggerInfo triggerInfo = buildTriggerInfo(detail.getJobIdentifier(), trigger);
                            Trigger.TriggerState state = sched.getTriggerState(trigger.getKey());
                            triggerInfo.setState(TriggerInfo.TriggerState.valueOf(state.name()));
                            triggerInfoList.add(triggerInfo);
                        }
                    }
                    detail.setTriggers(triggerInfoList);
                }

            }
        } catch (SchedulerException e) {

        }
        return list;
    }

    @Override
    public void pauseAll() throws JobSchedulerException {
        try {
            getScheduler().pauseAll();
        } catch (SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void resumeAll() throws JobSchedulerException {
        try {
            getScheduler().resumeAll();
        } catch (SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    public Map<String,Object> getMetaData()  throws JobSchedulerException {
        Map<String,Object> map = new HashMap<String,Object>();
       try {
        SchedulerMetaData metaData =  getScheduler().getMetaData();
        if(metaData != null) {

            ObjectMapper objectMapper = new ObjectMapper();
            map = objectMapper.convertValue(metaData,Map.class);

        }
        } catch (IllegalArgumentException | SchedulerException e) {
               throw new JobSchedulerException(e);
            }

    return map;
    }



    //Quartz Specific methods

    public SchedulerMetaData getSchedulerMetaData()  throws SchedulerException {
           return getScheduler().getMetaData();
    }


    public void scheduleJob(JobDetail jobDetail, Trigger cronTrigger) throws SchedulerException{
        getScheduler().scheduleJob(jobDetail, cronTrigger);
    }

    public void scheduleJob(MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean, CronTriggerFactoryBean cronTriggerFactoryBean)  throws SchedulerException{
        JobDetail job = methodInvokingJobDetailFactoryBean.getObject();
        CronTrigger trigger = cronTriggerFactoryBean.getObject();
        scheduleJob(job, trigger);
    }

    public void scheduleJob(JobDetail job, CronTriggerFactoryBean cronTriggerFactoryBean)  throws SchedulerException{
        CronTrigger trigger = cronTriggerFactoryBean.getObject();
        scheduleJob(job, trigger);
    }


    public void scheduleJob(String groupName, String jobName,Class<? extends QuartzJobBean> clazz, String cronExpression,Map<String,Object> jobData)  throws SchedulerException{
        JobDataMap jobDataMap = new JobDataMap(jobData);
        JobDetail job = newJob(clazz)
                .withIdentity(jobName, groupName)
                .requestRecovery(false)
                .setJobData(jobDataMap)
                .build();
        CronTrigger trigger = newTrigger()
                .withIdentity("triggerFor_"+jobName, groupName)
                .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("UTC"))
                        .withMisfireHandlingInstructionFireAndProceed())
                .forJob(job.getKey())
                .build();
        scheduleJob(job, trigger);
    }

    public void updateTrigger(TriggerIdentifier triggerIdentifier, Trigger trigger) throws SchedulerException {
        getScheduler().rescheduleJob(triggerKeyForTriggerIdentifier(triggerIdentifier), trigger);
    }
}
