package com.thinkbiganalytics.scheduler.quartz;

import com.thinkbiganalytics.scheduler.CustomQuartzSchedulerBean;
import com.thinkbiganalytics.scheduler.QuartzScheduler;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

public class CustomQuartzSchedulerBeanTest {

    CustomQuartzSchedulerBean toTest;

    @Mock
    CronTriggerFactoryBean             ctfb;
    @Mock
    JobDetail                          jobDetail;
    @Mock
    MethodInvokingJobDetailFactoryBean mijdfb;
    @Mock
    QuartzScheduler scheduler;
    @Mock
    Trigger                            trigger;

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
