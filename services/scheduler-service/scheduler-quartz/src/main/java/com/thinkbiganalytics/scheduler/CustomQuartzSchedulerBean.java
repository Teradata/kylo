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

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

/**
 * Quartz scheduler bean
 */
public class CustomQuartzSchedulerBean implements InitializingBean {

    private QuartzScheduler quartzScheduler;

    private CronTriggerFactoryBean cronTriggerFactoryBean;

    private MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean;

    private JobDetail jobDetail;

    private Trigger trigger;

    public void setQuartzScheduler(QuartzScheduler quartzScheduler) {
        this.quartzScheduler = quartzScheduler;
    }

    public void setCronTriggerFactoryBean(CronTriggerFactoryBean cronTriggerFactoryBean) {
        this.cronTriggerFactoryBean = cronTriggerFactoryBean;
    }

    public void setMethodInvokingJobDetailFactoryBean(MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean) {
        this.methodInvokingJobDetailFactoryBean = methodInvokingJobDetailFactoryBean;
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (jobDetail != null && trigger != null) {
            quartzScheduler.scheduleJob(jobDetail, trigger);
        } else {

            if (methodInvokingJobDetailFactoryBean != null) {
                quartzScheduler.scheduleJob(methodInvokingJobDetailFactoryBean, cronTriggerFactoryBean);
            } else {
                quartzScheduler.scheduleJob(jobDetail, cronTriggerFactoryBean);
            }
        }

    }
}
