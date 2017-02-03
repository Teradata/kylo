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

import com.thinkbiganalytics.scheduler.QuartzScheduler;
import com.thinkbiganalytics.scheduler.SimpleSchedulerSetup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 *
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
