package com.thinkbiganalytics.scheduler.quartz;

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

import com.thinkbiganalytics.scheduler.CustomQuartzSchedulerBean;
import com.thinkbiganalytics.scheduler.QuartzScheduler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import static org.mockito.Mockito.verify;

public class CustomQuartzSchedulerBeanTest {

    CustomQuartzSchedulerBean toTest;

    @Mock
    CronTriggerFactoryBean ctfb;
    @Mock
    JobDetail jobDetail;
    @Mock
    MethodInvokingJobDetailFactoryBean mijdfb;
    @Mock
    QuartzScheduler scheduler;
    @Mock
    Trigger trigger;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        toTest = new CustomQuartzSchedulerBean();
    }

    @Test
    public void afterPropertiesSetJobTestJobDetailAndTriggerNotNull() throws Exception {
        toTest.setCronTriggerFactoryBean(ctfb);
        toTest.setJobDetail(jobDetail);
        toTest.setMethodInvokingJobDetailFactoryBean(mijdfb);
        toTest.setQuartzScheduler(scheduler);
        toTest.setTrigger(trigger);

        toTest.afterPropertiesSet();

        verify(scheduler).scheduleJob(jobDetail, trigger);
    }

    @Test
    public void afterPropertiesSetJobTestJobDetailAndTriggerAreNullAndMethodInvokingJobDetailFactoryBeanNotNull()
        throws Exception {
        toTest.setCronTriggerFactoryBean(ctfb);
        toTest.setJobDetail(null);
        toTest.setMethodInvokingJobDetailFactoryBean(mijdfb);
        toTest.setQuartzScheduler(scheduler);
        toTest.setTrigger(null);

        toTest.afterPropertiesSet();

        verify(scheduler).scheduleJob(mijdfb, ctfb);
    }

    @Test
    public void afterPropertiesSetJobTestJobDetailNotNullTriggerNull() throws Exception {
        toTest.setCronTriggerFactoryBean(ctfb);
        toTest.setJobDetail(jobDetail);
        toTest.setMethodInvokingJobDetailFactoryBean(null);
        toTest.setQuartzScheduler(scheduler);
        toTest.setTrigger(null);

        toTest.afterPropertiesSet();

        verify(scheduler).scheduleJob(jobDetail, ctfb);
    }
}
