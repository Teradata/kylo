package com.thinkbiganalytics.scheduler.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobInfo;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.TriggerInfo;
import com.thinkbiganalytics.scheduler.impl.JobInfoImpl;
import com.thinkbiganalytics.scheduler.impl.TriggerInfoImpl;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

/**
 * Created by sr186054 on 9/23/15.
 */
@Service
public class QuartzScheduler implements JobScheduler {

    @Autowired
    @Qualifier("schedulerFactoryBean")
    SchedulerFactoryBean schedulerFactoryBean;

    public Scheduler getScheduler() {
        return schedulerFactoryBean.getScheduler();
    }

    private JobDetail getJobDetail(JobIdentifier jobIdentifier, Object task, String runMethod) throws NoSuchMethodException, ClassNotFoundException {
        final MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
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
            final JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
            if (timeZone == null) {
                timeZone = TimeZone.getDefault();
            }

            final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(new TriggerKey("trigger_" + jobIdentifier.getUniqueName(), jobIdentifier.getGroup()))
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .inTimeZone(timeZone)
                            .withMisfireHandlingInstructionFireAndProceed()).build();
            // scheduleJob(jobDetail,trigger);
        } catch (final Exception e) {
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
            final JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
            Date triggerStartTime = startTime;
            if (startDelay > 0L || startTime == null) {
                triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
            }
            final Trigger trigger = newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroup())).forJob(jobDetail).startAt(triggerStartTime).build();
            getScheduler().scheduleJob(jobDetail, trigger);
        } catch (final Exception e) {
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
            final Trigger trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroup())).forJob(jobDetail).startAt(triggerStartTime).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(period).repeatForever()).build();
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
        } catch (final SchedulerException e) {
            throw new JobSchedulerException("Unable to Start the Scheduler", e);
        }
    }

    @Override
    public void pauseScheduler() throws JobSchedulerException {
        try {
            getScheduler().standby();
        } catch (final SchedulerException e) {
            throw new JobSchedulerException("Unable to Pause the Scheduler", e);
        }
    }

    @Override
    public void triggerJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            getScheduler().triggerJob(jobKeyForJobIdentifier(jobIdentifier));
        } catch (final SchedulerException e) {
            throw new JobSchedulerException("Unable to Trigger the Job " + jobIdentifier, e);
        }
    }

    @Override
    public void pauseTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            final JobKey jobKey = jobKeyForJobIdentifier(jobIdentifier);
            final List<? extends Trigger> jobTriggers = getScheduler().getTriggersOfJob(jobKey);
            final List<TriggerInfo> triggerInfoList = new ArrayList<TriggerInfo>();
            if (jobTriggers != null) {
                for (final Trigger trigger : jobTriggers) {
                   final TriggerIdentifier triggerIdentifier = triggerIdentifierForTriggerKey(trigger.getKey());
                    try {
                        pauseTrigger(triggerIdentifier);
                    }catch (final JobSchedulerException e){

                    }
                }
            }
        } catch (final SchedulerException e) {
            throw new JobSchedulerException("Unable to pause Active Triggers the Job " + jobIdentifier, e);
        }
    }

    @Override
    public void resumeTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            final JobKey jobKey = jobKeyForJobIdentifier(jobIdentifier);
            final List<? extends Trigger> jobTriggers = getScheduler().getTriggersOfJob(jobKey);
            final List<TriggerInfo> triggerInfoList = new ArrayList<TriggerInfo>();
            if (jobTriggers != null) {
                for (final Trigger trigger : jobTriggers) {
                    final TriggerIdentifier triggerIdentifier = triggerIdentifierForTriggerKey(trigger.getKey());
                    try {
                        resumeTrigger(triggerIdentifier);
                    }catch (final JobSchedulerException e){

                    }
                }
            }
        } catch (final SchedulerException e) {
            throw new JobSchedulerException("Unable to resume paused Triggers the Job " + jobIdentifier, e);
        }
    }

    @Override
    public void resumeTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException {
        try {
            getScheduler().resumeTrigger(triggerKeyForTriggerIdentifier(triggerIdentifier));
        } catch (final SchedulerException e) {
            throw new JobSchedulerException("Unable to Resume the Trigger " + triggerIdentifier, e);
        }
    }

    @Override
    public void pauseTrigger(TriggerIdentifier triggerIdentifier) throws JobSchedulerException {
        try {
            getScheduler().pauseTrigger(triggerKeyForTriggerIdentifier(triggerIdentifier));
        } catch (final SchedulerException e) {
            throw new JobSchedulerException("Unable to Pause the Trigger " + triggerIdentifier, e);
        }
    }

    @Override
    public void updateTrigger(TriggerIdentifier triggerIdentifier, String cronExpression) throws  JobSchedulerException {

        final CronTrigger trigger = newTrigger().withIdentity(triggerIdentifier.getName(), triggerIdentifier.getGroup()).withSchedule(cronSchedule(cronExpression)).build();
        try {
            updateTrigger(triggerIdentifier, trigger);
        } catch (final SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void deleteJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            getScheduler().deleteJob(jobKeyForJobIdentifier(jobIdentifier));
        } catch (final SchedulerException e) {
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
        final JobInfo detail = new JobInfoImpl(jobIdentifierForJobKey(jobDetail.getKey()));

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
        final TriggerInfo triggerInfo = new TriggerInfoImpl(jobIdentifier,triggerIdentifierForTriggerKey(trigger.getKey()));
        triggerInfo.setDescription(trigger.getDescription());

        String cronExpression = null;
        triggerInfo.setCronExpressionSummary("");
        if (trigger instanceof CronTrigger) {
            final CronTrigger ct = (CronTrigger) trigger;
            cronExpression = ct.getCronExpression();
            triggerInfo.setCronExpressionSummary(ct.getExpressionSummary());
        }

        triggerInfo.setTriggerClass(trigger.getClass());
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
        final List<JobInfo> list = new ArrayList<JobInfo>();
        final Scheduler sched = getScheduler();
        try {
            // enumerate each job group
            for (final String group : sched.getJobGroupNames()) {
                // enumerate each job in group
                for (final JobKey jobKey : sched.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                    final JobDetail jobDetail = sched.getJobDetail(jobKey);
                    final JobInfo detail =buildJobInfo(jobDetail);
                    list.add(detail);
                    final List<? extends Trigger> jobTriggers = sched.getTriggersOfJob(jobKey);

                    final List<TriggerInfo> triggerInfoList = new ArrayList<TriggerInfo>();
                    if (jobTriggers != null) {
                        for (final Trigger trigger : jobTriggers) {
                            final TriggerInfo triggerInfo = buildTriggerInfo(detail.getJobIdentifier(), trigger);
                            final Trigger.TriggerState state = sched.getTriggerState(trigger.getKey());
                            triggerInfo.setState(TriggerInfo.TriggerState.valueOf(state.name()));
                            triggerInfoList.add(triggerInfo);
                        }
                    }
                    detail.setTriggers(triggerInfoList);
                }

            }
        } catch (final SchedulerException e) {

        }
        return list;
    }

    @Override
    public void pauseAll() throws JobSchedulerException {
        try {
            getScheduler().pauseAll();
        } catch (final SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void resumeAll() throws JobSchedulerException {
        try {
            getScheduler().resumeAll();
        } catch (final SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public Map<String,Object> getMetaData()  throws JobSchedulerException {
        Map<String,Object> map = new HashMap<String,Object>();
       try {
        final SchedulerMetaData metaData =  getScheduler().getMetaData();
        if(metaData != null) {

            final ObjectMapper objectMapper = new ObjectMapper();
            map = objectMapper.convertValue(metaData,Map.class);

        }
        } catch (IllegalArgumentException | SchedulerException e) {
               throw new JobSchedulerException(e);
            }

    return map;
    }

    public boolean jobExists(JobIdentifier jobIdentifier){
        Set<JobKey> jobKeys = null;
        try {
            jobKeys = getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(jobIdentifier.getGroup()));
         if(jobKeys != null && !jobKeys.isEmpty()){
            for(final JobKey key : jobKeys){
                if(jobIdentifierForJobKey(key).equals(jobIdentifier)){
                    return true;
                }
            }
        }
        } catch (final SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean triggerExists(TriggerIdentifier triggerIdentifier){
        Trigger trigger = null;
        try {
          trigger = getScheduler().getTrigger(triggerKeyForTriggerIdentifier(triggerIdentifier));
            if(trigger != null){
                return true;
            }
        } catch (final SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }



    //Quartz Specific methods

    public SchedulerMetaData getSchedulerMetaData()  throws SchedulerException {
           return getScheduler().getMetaData();
    }


    public void scheduleJob(JobDetail jobDetail, Trigger cronTrigger) throws SchedulerException{
        getScheduler().scheduleJob(jobDetail, cronTrigger);
    }

    public void scheduleJob(MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean, CronTriggerFactoryBean cronTriggerFactoryBean)  throws SchedulerException{
        final JobDetail job = methodInvokingJobDetailFactoryBean.getObject();
        final CronTrigger trigger = cronTriggerFactoryBean.getObject();
        scheduleJob(job, trigger);
    }

    public void scheduleJob(JobDetail job, CronTriggerFactoryBean cronTriggerFactoryBean)  throws SchedulerException{
        final CronTrigger trigger = cronTriggerFactoryBean.getObject();
        scheduleJob(job, trigger);
    }

    public void scheduleJob(JobIdentifier jobIdentifier, TriggerIdentifier triggerIdentifier,Object obj, String targetMethod, String cronExpression,Map<String,Object> jobData)  throws SchedulerException{
        final MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactory.setTargetObject(obj);
        jobDetailFactory.setTargetMethod(targetMethod);
        jobDetailFactory.setName(jobIdentifier.getName());
        jobDetailFactory.setGroup(jobIdentifier.getGroup());

        final JobDetailFactoryBean jobDetailFactoryBean2 = new JobDetailFactoryBean();

       // applicationContext.getAutowireCapableBeanFactory().initializeBean(jobDetailFactory, UUID.randomUUID().toString());

        final CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setCronExpression(cronExpression);
        triggerFactoryBean.setJobDetail(jobDetailFactory.getObject());
        triggerFactoryBean.setGroup(triggerIdentifier.getGroup());
         triggerFactoryBean.setName(triggerIdentifier.getName());
        scheduleJob(jobDetailFactory,triggerFactoryBean);

    }

    public void scheduleJob(JobIdentifier jobIdentifier, TriggerIdentifier triggerIdentifier,Class<? extends QuartzJobBean> clazz, String cronExpression,Map<String,Object> jobData)  throws SchedulerException{
        scheduleJob(jobIdentifier,triggerIdentifier,clazz,cronExpression,jobData,false);
    }
    public void scheduleJob(JobIdentifier jobIdentifier, TriggerIdentifier triggerIdentifier,Class<? extends QuartzJobBean> clazz, String cronExpression,Map<String,Object> jobData, boolean fireImmediately)  throws SchedulerException{
        if(jobData == null){
            jobData = new HashMap<>();
        }

        final JobDataMap jobDataMap = new JobDataMap(jobData);
        final JobDetail job = newJob(clazz)
                .withIdentity(jobIdentifier.getName(), jobIdentifier.getGroup())
                .requestRecovery(false)
                .setJobData(jobDataMap)
                .build();
       final TriggerBuilder triggerBuilder = newTrigger()
                .withIdentity(triggerIdentifier.getName(),triggerIdentifier.getGroup())
                .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("UTC"))
                        .withMisfireHandlingInstructionFireAndProceed())
                .forJob(job.getKey());

        if(fireImmediately) {
            Date previousTriggerTime = null;
            try {
                previousTriggerTime =   CronExpressionUtil.getPreviousFireTime(cronExpression);
                if(previousTriggerTime != null) {
                    triggerBuilder.startAt(previousTriggerTime);
                }
            }
            catch (final ParseException e) {

            }
        }
        final Trigger trigger = triggerBuilder.build();
        scheduleJob(job, trigger);

    }



    public void scheduleJob(String groupName, String jobName,Class<? extends QuartzJobBean> clazz, String cronExpression,Map<String,Object> jobData)  throws SchedulerException{
        scheduleJob(groupName,jobName, clazz,cronExpression,jobData,false);
    }
    public void scheduleJob(String groupName, String jobName,Class<? extends QuartzJobBean> clazz, String cronExpression,Map<String,Object> jobData,boolean fireImmediately)  throws SchedulerException{
        scheduleJob(new JobIdentifier(jobName,groupName), new TriggerIdentifier(jobName,groupName), clazz,cronExpression,jobData,fireImmediately);
    }

    public void updateTrigger(TriggerIdentifier triggerIdentifier, Trigger trigger) throws SchedulerException {
        getScheduler().rescheduleJob(triggerKeyForTriggerIdentifier(triggerIdentifier), trigger);
    }
}
