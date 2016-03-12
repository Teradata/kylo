package com.thinkbiganalytics.scheduler.quartz;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;



/**
 * Created by matthutton on 3/11/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = QuartzSpringConfiguration.class)
public class QuartzSpringConfigurationTest {

    @Autowired
    private QuartzSpringConfiguration configuration;

    @Test
    public void testSchedulerFactoryBean() throws Exception {
        assertNotNull(configuration);
        assertNotNull(configuration.schedulerFactoryBean());
    }
}

