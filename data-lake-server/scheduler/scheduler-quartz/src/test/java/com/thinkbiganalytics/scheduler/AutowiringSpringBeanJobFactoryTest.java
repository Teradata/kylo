package com.thinkbiganalytics.scheduler;

import com.thinkbiganalytics.scheduler.quartz.MockJob;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Created by matthutton on 3/11/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/application-context.xml"})
public class AutowiringSpringBeanJobFactoryTest {

    private AutowiringSpringBeanJobFactory factory;

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        factory = new AutowiringSpringBeanJobFactory();
        factory.setApplicationContext(applicationContext);
    }

    @Test
    public void testSetApplicationContext() throws Exception {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setJobClass(MockJob.class);
        TriggerFiredBundle bundle = new TriggerFiredBundle(jobDetail, Mockito.mock(OperableTrigger.class), new BaseCalendar(), true, new Date(), new Date(), new Date(), new Date());
        assertNotNull(factory.createJobInstance(bundle));

    }

    @Test
    public void testCreateJobInstance() throws Exception {

    }

}