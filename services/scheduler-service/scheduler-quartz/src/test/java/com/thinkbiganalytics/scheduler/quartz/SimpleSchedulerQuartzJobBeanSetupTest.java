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
