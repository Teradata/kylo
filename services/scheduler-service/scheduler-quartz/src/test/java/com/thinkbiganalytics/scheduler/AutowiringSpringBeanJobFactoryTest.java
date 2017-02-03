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

import com.thinkbiganalytics.scheduler.quartz.MockJob;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 *
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
