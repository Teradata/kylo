package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-quartz
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultJobInfo;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerInfo;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

import org.apache.commons.lang3.BooleanUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Quartz implementation of the JobScheduler
 */
@Service
public class QuartzScheduler implements JobScheduler {

    @Autowired
    @Qualifier("schedulerFactoryBean")
    SchedulerFactoryBean schedulerFactoryBean;

    @Inject
    private  QuartzClusterMessageSender clusterMessageSender;


    private Set<JobSchedulerListener> listeners = new HashSet<>();

    public static JobIdentifier jobIdentifierForJobKey(JobKey jobKey) {
        return new DefaultJobIdentifier(jobKey.getName(), jobKey.getGroup());
    }

    public static JobKey jobKeyForJobIdentifier(JobIdentifier jobIdentifier) {
        return new JobKey(jobIdentifier.getName(), jobIdentifier.getGroup());
    }

    public static TriggerKey triggerKeyForTriggerIdentifier(TriggerIdentifier triggerIdentifier) {
        return new TriggerKey(triggerIdentifier.getName(), triggerIdentifier.getGroup());
    }

    public static TriggerIdentifier triggerIdentifierForTriggerKey(TriggerKey triggerKey) {
        return new DefaultTriggerIdentifier(triggerKey.getName(), triggerKey.getGroup());
    }

    public Scheduler getScheduler() {
        return schedulerFactoryBean.getScheduler();
    }

    private JobDetail getJobDetail(JobIdentifier jobIdentifier, Object task, String runMethod)
        throws NoSuchMethodException, ClassNotFoundException {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(task);
        bean.setTargetMethod(runMethod);
        bean.setName(jobIdentifier.getName());
        bean.setGroup(jobIdentifier.getGroup());
        bean.afterPropertiesSet();
        return bean.getObject();
    }




    @Override
    public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Runnable task, String cronExpression,
                                                     TimeZone timeZone) throws JobSchedulerException {
        scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression, null);
    }

    @Override
    public void scheduleWithCronExpression(JobIdentifier jobIdentifier, Runnable task, String cronExpression)
        throws JobSchedulerException {
        scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression, null);
    }

    public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Object task, String runMethod,
                                                     String cronExpression, TimeZone timeZone) throws JobSchedulerException {
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
            scheduleJob(jobDetail, trigger);
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
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, long startDelay)
        throws JobSchedulerException {
        scheduleWithFixedDelay(jobIdentifier, runnable, "run", new Date(), 0L);
    }

    @Override
    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long startDelay)
        throws JobSchedulerException {
        scheduleWithFixedDelay(jobIdentifier, runnable, "run", startTime, startDelay);
    }

    public void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Object task, String runMethod, Date startTime, long startDelay)
        throws JobSchedulerException {
        try {
            JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
            Date triggerStartTime = startTime;
            if (startDelay > 0L || startTime == null) {
                triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
            }
            Trigger
                trigger =
                newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroup())).forJob(jobDetail)
                    .startAt(triggerStartTime).build();
            getScheduler().scheduleJob(jobDetail, trigger);
            triggerListeners(JobSchedulerEvent.scheduledJobEvent(jobIdentifier));
        } catch (Exception e) {
            throw new JobSchedulerException();
        }
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period)
        throws JobSchedulerException {
        scheduleAtFixedRate(jobIdentifier, runnable, "run", startTime, period);
    }

    @Override
    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, long period) throws JobSchedulerException {
        scheduleAtFixedRate(jobIdentifier, runnable, "run", new Date(), period);
    }

    public void scheduleAtFixedRate(JobIdentifier jobIdentifier, Object task, String runMethod, Date startTime, long period)
        throws JobSchedulerException {
        scheduleAtFixedRateWithDelay(jobIdentifier, task, runMethod, startTime, period, 0L);
    }

    public void scheduleAtFixedRateWithDelay(JobIdentifier jobIdentifier, Object task, String runMethod, Date startTime,
                                             long period, long startDelay) throws JobSchedulerException {

        JobDetail jobDetail = null;
        try {
            jobDetail = getJobDetail(jobIdentifier, task, runMethod);

            Date triggerStartTime = startTime;
            if (startDelay > 0L || startTime == null) {
                triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
            }
            Trigger
                trigger =
                TriggerBuilder.newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroup()))
                    .forJob(jobDetail).startAt(triggerStartTime)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(period).repeatForever()).build();
            getScheduler().scheduleJob(jobDetail, trigger);
            triggerListeners(JobSchedulerEvent.scheduledJobEvent(jobIdentifier));
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

    /**
     * Start the Scheduler after it has been Paused Misfires on Triggers during the pause time will be ignored
     */
    @Override
    public void startScheduler() throws JobSchedulerException {
        try {
            getScheduler().start();
            clusterMessageSender.notifySchedulerResumed();
            triggerListeners(JobSchedulerEvent.schedulerStartedEvent());
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Start the Scheduler", e);
        }
    }

    /**
     * Pause the Scheduler and Halts the firing of all Triggers Misfires on Triggers during the pause time will be ignored
     */
    @Override
    public void pauseScheduler() throws JobSchedulerException {
        try {
            getScheduler().standby();
            clusterMessageSender.notifySchedulerPaused();
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Pause the Scheduler", e);
        }
    }

    @Override
    public void triggerJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            getScheduler().triggerJob(jobKeyForJobIdentifier(jobIdentifier));
            triggerListeners(JobSchedulerEvent.triggerJobEvent(jobIdentifier));
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Trigger the Job " + jobIdentifier, e);
        }
    }

    public void pauseTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            JobKey jobKey = jobKeyForJobIdentifier(jobIdentifier);
            List<? extends Trigger> jobTriggers = getScheduler().getTriggersOfJob(jobKey);
            if (jobTriggers != null) {
                for (Trigger trigger : jobTriggers) {
                    TriggerIdentifier triggerIdentifier = triggerIdentifierForTriggerKey(trigger.getKey());
                    try {
                        pauseTrigger(triggerIdentifier);
                    } catch (JobSchedulerException e) {

                    }
                }
                triggerListeners(JobSchedulerEvent.pauseJobEvent(jobIdentifier));
                clusterMessageSender.notifyJobPaused(jobIdentifier);
            }
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to pause Active Triggers the Job " + jobIdentifier, e);
        }
    }

    public void resumeTriggersOnJob(JobIdentifier jobIdentifier) throws JobSchedulerException {
        try {
            JobKey jobKey = jobKeyForJobIdentifier(jobIdentifier);
            List<? extends Trigger> jobTriggers = getScheduler().getTriggersOfJob(jobKey);
            if (jobTriggers != null) {
                for (Trigger trigger : jobTriggers) {
                    TriggerIdentifier triggerIdentifier = triggerIdentifierForTriggerKey(trigger.getKey());
                    try {
                        resumeTrigger(triggerIdentifier);
                    } catch (JobSchedulerException e) {

                    }
                }
                triggerListeners(JobSchedulerEvent.resumeJobEvent(jobIdentifier));
                clusterMessageSender.notifyJobResumed(jobIdentifier);
            }
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to resume paused Triggers the Job " + jobIdentifier, e);
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

    public void updateTrigger(TriggerIdentifier triggerIdentifier, String cronExpression) throws JobSchedulerException {

        CronTrigger
            trigger =
            newTrigger().withIdentity(triggerIdentifier.getName(), triggerIdentifier.getGroup())
                .withSchedule(cronSchedule(cronExpression)).build();
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
            triggerListeners(JobSchedulerEvent.deleteJobEvent(jobIdentifier));
        } catch (SchedulerException e) {
            throw new JobSchedulerException("Unable to Delete the Job " + jobIdentifier, e);
        }
    }

    public JobInfo buildJobInfo(JobDetail jobDetail) {
        JobInfo detail = new DefaultJobInfo(jobIdentifierForJobKey(jobDetail.getKey()));

        detail.setDescription(jobDetail.getDescription());
        detail.setJobClass(jobDetail.getJobClass());
        detail.setJobData(jobDetail.getJobDataMap().getWrappedMap());
        return detail;
    }

    private TriggerInfo buildTriggerInfo(JobIdentifier jobIdentifier, Trigger trigger) {
        TriggerInfo triggerInfo = new DefaultTriggerInfo(jobIdentifier, triggerIdentifierForTriggerKey(trigger.getKey()));
        triggerInfo.setDescription(trigger.getDescription());
        triggerInfo.setTriggerClass(trigger.getClass());
        String cronExpression = null;
        triggerInfo.setCronExpressionSummary("");
        if (trigger instanceof CronTrigger) {
            CronTrigger ct = (CronTrigger) trigger;
            cronExpression = ct.getCronExpression();
            triggerInfo.setCronExpressionSummary(ct.getExpressionSummary());
        }

        boolean
            isSimpleTrigger =
            (!CronTrigger.class.isAssignableFrom(trigger.getClass()) && SimpleTrigger.class.isAssignableFrom(trigger.getClass()));
        triggerInfo.setSimpleTrigger(isSimpleTrigger);

        boolean isScheduled = CronTrigger.class.isAssignableFrom(triggerInfo.getTriggerClass());
        triggerInfo.setScheduled(isScheduled);
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
                    JobInfo detail = buildJobInfo(jobDetail);
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

    /**
     * Pause all jobs and triggers.
     * {@link #resumeAll()} will be needed to resume. Misfires on Triggers will be applied depending upon the Trigger misfire instructions
     */
    @Override
    public void pauseAll() throws JobSchedulerException {
        try {
            getScheduler().pauseAll();
            triggerListeners(JobSchedulerEvent.pauseAllJobsEvent());
        } catch (SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    /**
     * Resume All Triggers that have been paused.  Misfires on Triggers will be applied depending upon the Trigger misfire instructions
     */
    @Override
    public void resumeAll() throws JobSchedulerException {
        try {
            getScheduler().resumeAll();
            triggerListeners(JobSchedulerEvent.resumeAllJobsEvent());
        } catch (SchedulerException e) {
            throw new JobSchedulerException(e);
        }
    }

    public Map<String, Object> getMetaData() throws JobSchedulerException {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SchedulerMetaData metaData = getScheduler().getMetaData();
            if (metaData != null) {

                ObjectMapper objectMapper = new ObjectMapper();
                map = objectMapper.convertValue(metaData, Map.class);

            }
        } catch (IllegalArgumentException | SchedulerException e) {
            throw new JobSchedulerException(e);
        }

        return map;
    }

    public boolean jobExists(JobIdentifier jobIdentifier) {
        Set<JobKey> jobKeys = null;
        try {
            jobKeys = getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(jobIdentifier.getGroup()));
            if (jobKeys != null && !jobKeys.isEmpty()) {
                for (JobKey key : jobKeys) {
                    if (jobIdentifierForJobKey(key).equals(jobIdentifier)) {
                        return true;
                    }
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean triggerExists(TriggerIdentifier triggerIdentifier) {
        Trigger trigger = null;
        try {
            trigger = getScheduler().getTrigger(triggerKeyForTriggerIdentifier(triggerIdentifier));
            if (trigger != null) {
                return true;
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public SchedulerMetaData getSchedulerMetaData() throws SchedulerException {
        return getScheduler().getMetaData();
    }


    public void scheduleJob(JobDetail jobDetail, Trigger cronTrigger) throws SchedulerException {
        getScheduler().scheduleJob(jobDetail, cronTrigger);
    }

    public void scheduleJob(MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean,
                            CronTriggerFactoryBean cronTriggerFactoryBean) throws SchedulerException {
        JobDetail job = methodInvokingJobDetailFactoryBean.getObject();
        CronTrigger trigger = cronTriggerFactoryBean.getObject();
        scheduleJob(job, trigger);
    }

    public void scheduleJob(JobDetail job, CronTriggerFactoryBean cronTriggerFactoryBean) throws SchedulerException {
        CronTrigger trigger = cronTriggerFactoryBean.getObject();
        scheduleJob(job, trigger);
    }

    public void scheduleJob(JobIdentifier jobIdentifier, TriggerIdentifier triggerIdentifier, Object obj, String targetMethod,
                            String cronExpression, Map<String, Object> jobData) throws SchedulerException {
        MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactory.setTargetObject(obj);
        jobDetailFactory.setTargetMethod(targetMethod);
        jobDetailFactory.setName(jobIdentifier.getName());
        jobDetailFactory.setGroup(jobIdentifier.getGroup());

        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setCronExpression(cronExpression);
        triggerFactoryBean.setJobDetail(jobDetailFactory.getObject());
        triggerFactoryBean.setGroup(triggerIdentifier.getGroup());
        triggerFactoryBean.setName(triggerIdentifier.getName());
        scheduleJob(jobDetailFactory, triggerFactoryBean);

    }

    public void scheduleJob(JobIdentifier jobIdentifier, TriggerIdentifier triggerIdentifier, Class<? extends QuartzJobBean> clazz,
                            String cronExpression, Map<String, Object> jobData) throws SchedulerException {
        scheduleJob(jobIdentifier, triggerIdentifier, clazz, cronExpression, jobData, false);
    }

    public void scheduleJob(JobIdentifier jobIdentifier, TriggerIdentifier triggerIdentifier, Class<? extends QuartzJobBean> clazz,
                            String cronExpression, Map<String, Object> jobData, boolean fireImmediately) throws SchedulerException {
        if (jobData == null) {
            jobData = new HashMap<>();
        }

        JobDataMap jobDataMap = new JobDataMap(jobData);
        JobDetail job = newJob(clazz)
            .withIdentity(jobIdentifier.getName(), jobIdentifier.getGroup())
            .requestRecovery(false)
            .setJobData(jobDataMap)
            .build();
        TriggerBuilder triggerBuilder = newTrigger()
            .withIdentity(triggerIdentifier.getName(), triggerIdentifier.getGroup())
            .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("UTC"))
                              .withMisfireHandlingInstructionFireAndProceed())
            .forJob(job.getKey());

        if (fireImmediately) {
            Date previousTriggerTime = null;
            try {
                previousTriggerTime = CronExpressionUtil.getPreviousFireTime(cronExpression);
                if (previousTriggerTime != null) {
                    triggerBuilder.startAt(previousTriggerTime);
                }
            } catch (ParseException e) {

            }
        }
        Trigger trigger = triggerBuilder.build();
        scheduleJob(job, trigger);

    }


    public void scheduleJob(String groupName, String jobName, Class<? extends QuartzJobBean> clazz, String cronExpression,
                            Map<String, Object> jobData) throws SchedulerException {
        scheduleJob(groupName, jobName, clazz, cronExpression, jobData, false);
    }

    public void scheduleJob(String groupName, String jobName, Class<? extends QuartzJobBean> clazz, String cronExpression,
                            Map<String, Object> jobData, boolean fireImmediately) throws SchedulerException {
        scheduleJob(new DefaultJobIdentifier(jobName, groupName), new DefaultTriggerIdentifier(jobName, groupName), clazz,
                    cronExpression, jobData, fireImmediately);
    }

    public void updateTrigger(TriggerIdentifier triggerIdentifier, Trigger trigger) throws SchedulerException {
        getScheduler().rescheduleJob(triggerKeyForTriggerIdentifier(triggerIdentifier), trigger);
    }

    @Override
    public void subscribeToJobSchedulerEvents(JobSchedulerListener listener) {
        listeners.add(listener);
    }

    private void triggerListeners(JobSchedulerEvent event) {
        listeners.stream().forEach(listener -> listener.onJobSchedulerEvent(event));
    }
}
