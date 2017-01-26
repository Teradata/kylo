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

import com.thinkbiganalytics.scheduler.model.DefaultJobInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JobInfoImplTest {

    JobInfo toTest;

    @Before
    public void before() {
        toTest = new DefaultJobInfo();
    }

    @Test
    public void jobIdentifierTest() {
        final JobIdentifier identifier = Mockito.mock(JobIdentifier.class);
        toTest.setJobIdentifier(identifier);
        assertEquals(toTest.getJobIdentifier(), identifier);

        toTest = new DefaultJobInfo(identifier);
        assertEquals(toTest.getJobIdentifier(), identifier);
    }

    @Test
    public void triggersTest() {
        final List<TriggerInfo> triggers = new ArrayList<TriggerInfo>();
        toTest.setTriggers(triggers);
        assertEquals(toTest.getTriggers(), triggers);
    }

    @Test
    public void descriptionTest() {
        final String description = "description";
        toTest.setDescription(description);
        assertEquals(toTest.getDescription(), description);
    }

    @Test
    public void jobClassTest() {
        @SuppressWarnings("rawtypes")
        final Class jobClass = String.class;
        toTest.setJobClass(jobClass);
        assertEquals(toTest.getJobClass(), jobClass);
    }

    @Test
    public void jobDataTest() {
        final Map<String, Object> jobData = new HashMap<String, Object>();
        toTest.setJobData(jobData);
        assertEquals(toTest.getJobData(), jobData);
    }
}
