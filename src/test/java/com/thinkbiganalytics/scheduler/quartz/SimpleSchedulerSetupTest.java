package com.thinkbiganalytics.scheduler.quartz;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.*;

/**
 * Created by matthutton on 3/12/16.
 */
public class SimpleSchedulerSetupTest {

    @Mock
    private QuartzScheduler scheduler;

    @Mock
    private ApplicationContext context;

    private SimpleSchedulerSetup simpleSchedulerSetup;

    @Before
    public void setUp() throws Exception {
        context = Mockito.mock(ApplicationContext.class);
        scheduler = Mockito.mock(QuartzScheduler.class);

        Mockito.validateMockitoUsage();
        AutowireCapableBeanFactory factory = Mockito.mock(AutowireCapableBeanFactory.class);
        Mockito.when(context.getAutowireCapableBeanFactory()).thenReturn(factory);
        simpleSchedulerSetup = new SimpleSchedulerSetup(new TestJob(), "run", "cron");
        simpleSchedulerSetup.setApplicationContext(context);
        simpleSchedulerSetup.setQuartzScheduler(scheduler);

    }

    @Test
    public void test() throws Exception {
        simpleSchedulerSetup.afterPropertiesSet();
    }

    static class TestJob {
        public void run() {
            //
        }
    }
}