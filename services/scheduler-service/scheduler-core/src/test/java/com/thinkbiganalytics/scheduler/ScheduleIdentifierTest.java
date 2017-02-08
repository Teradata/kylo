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

import com.thinkbiganalytics.scheduler.model.DefaultScheduleIdentifier;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 */
public class ScheduleIdentifierTest {

    ScheduleIdentifier identifier;
    ScheduleIdentifier identifier2;
    ScheduleIdentifier identifier3;

    @Before
    public void setUp() throws Exception {
        identifier = new DefaultScheduleIdentifier();
        identifier = new DefaultScheduleIdentifier("name1", "group");
        identifier2 = new DefaultScheduleIdentifier("name1", "group");
        identifier3 = new DefaultScheduleIdentifier("name3", "group");
    }

    @Test
    public void testIdentifier() throws Exception {

        assertTrue(identifier.compareTo(identifier) == 0);
        assertTrue(identifier.compareTo(identifier2) == 0);
        assertTrue(identifier.compareTo(identifier3) == -2);
        assertTrue(identifier3.compareTo(identifier) == 2);
        assertTrue(identifier.hashCode() == identifier2.hashCode());
        assertTrue(identifier.equals(identifier2));
        assertTrue(identifier.getGroup().equals("group"));
        assertTrue(identifier.getName().equals("name1"));
        assertTrue(identifier.getUniqueName() != null);

    }
}
