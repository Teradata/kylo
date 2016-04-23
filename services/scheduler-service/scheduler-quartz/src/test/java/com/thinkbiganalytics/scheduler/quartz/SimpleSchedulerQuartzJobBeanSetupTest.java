package com.thinkbiganalytics.scheduler.quartz;

import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.QuartzScheduler;
import com.thinkbiganalytics.scheduler.SimpleSchedulerQuartzJobBeanSetup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import java.util.HashMap;

public class SimpleSchedulerQuartzJobBeanSetupTest {

    QuartzScheduler scheduler;

    @Before
    public void setup() throws Exception {
        scheduler = Mockito.mock(QuartzScheduler.class);
        Mockito.validateMockitoUsage();
    }

    @Test
    public void test() throws Exception {
        SimpleSchedulerQuartzJobBeanSetup setup = new SimpleSchedulerQuartzJobBeanSetup();
        setup.setQuartzScheduler(scheduler);

        setup.setCronExpresson("cronExpression");
        setup.setDataMap(new HashMap<String, Object>());
        setup.setFireImmediately(false);
        setup.setGroupName("groupName");
        setup.setJobName("jobName");
        setup.setQuartzJobBean(MockJob.class.getCanonicalName());
        setup.scheduleMetadataJob();

        setup.setQuartzJobBean("ignore.error.TestMissingClass");
        setup.scheduleMetadataJob();
        Mockito.mock(QuartzScheduler.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                throw new JobSchedulerException();
            }
        });
    }

}