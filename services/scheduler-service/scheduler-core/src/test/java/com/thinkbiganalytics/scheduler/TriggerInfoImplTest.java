package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-core
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

import com.thinkbiganalytics.scheduler.model.DefaultTriggerInfo;
import com.thinkbiganalytics.scheduler.support.JavaBeanTester;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 */
public class TriggerInfoImplTest {

    private TriggerInfo trigger;
    private Date today;

    @Before
    public void setUp() throws Exception {

        today = new Date();

        trigger = new DefaultTriggerInfo(Mockito.mock(JobIdentifier.class), Mockito.mock(TriggerIdentifier.class));
        trigger.setJobIdentifier(Mockito.mock(JobIdentifier.class));
        trigger.setState(TriggerInfo.TriggerState.BLOCKED);
        trigger.setTriggerClass(Object.class);
        trigger.setTriggerIdentifier(Mockito.mock(TriggerIdentifier.class));
    }

    @Test
    public void test() throws Exception {
        JavaBeanTester.test(DefaultTriggerInfo.class, "jobIdentifier", "state", "triggerClass", "triggerIdentifier");
        assertNotNull(trigger.getState());
        assertNotNull(trigger.getJobIdentifier());
        assertNotNull(trigger.getState());
        assertNotNull(trigger.getTriggerClass());
        assertNotNull(trigger.getTriggerIdentifier());
    }


}
