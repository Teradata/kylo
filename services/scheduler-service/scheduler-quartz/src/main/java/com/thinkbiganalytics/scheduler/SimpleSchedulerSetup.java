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


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import java.util.UUID;

/**
 * Configures Quartz to run a job with one step
 */
public class SimpleSchedulerSetup implements InitializingBean {

    @Autowired
    private QuartzScheduler quartzScheduler;

    @Autowired
    private ApplicationContext applicationContext;

    private Object jobRunner;

    private String cronExpression;

    private String targetMethod;

    private String jobName;
    private String groupName;
    private Boolean concurrent = true;

    /**
     * Configure the quartz scheduler
     *
     * @param jobRunner      the target job runner (implementing a run() method)
     * @param cronExpression a cron expression for the schedule
     */
    public SimpleSchedulerSetup(Object jobRunner, String cronExpression) {
        this(jobRunner, "run", cronExpression);
    }

    /**
     * Configure the quartz scheduler
     *
     * @param jobRunner      the target job runner
     * @param targetMethod   the method to invoke on the job runner
     * @param cronExpression a cron expression for the schedule
     */
    public SimpleSchedulerSetup(Object jobRunner, String targetMethod, String cronExpression) {
        this.jobRunner = jobRunner;
        this.cronExpression = cronExpression;
        this.targetMethod = targetMethod;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactory.setTargetObject(this.jobRunner);
        jobDetailFactory.setTargetMethod(this.targetMethod);
        jobDetailFactory.setName(this.jobName);
        jobDetailFactory.setGroup(this.groupName);
        jobDetailFactory.setConcurrent(concurrent);
        applicationContext.getAutowireCapableBeanFactory().initializeBean(jobDetailFactory, UUID.randomUUID().toString());

        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setCronExpression(cronExpression);
        triggerFactoryBean.setJobDetail(jobDetailFactory.getObject());
        triggerFactoryBean.setGroup(this.groupName);
        if (this.jobName != null) {
            triggerFactoryBean.setName("trigger_" + this.jobName);
        }
        applicationContext.getAutowireCapableBeanFactory().initializeBean(triggerFactoryBean, UUID.randomUUID().toString());

        quartzScheduler.scheduleJob(jobDetailFactory, triggerFactoryBean);
    }

    public void setQuartzScheduler(QuartzScheduler quartzScheduler) {
        this.quartzScheduler = quartzScheduler;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setConcurrent(Boolean concurrent) {
        this.concurrent = concurrent;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }
}
