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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class JobSchedulerExceptionTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testExeption() {
        JobSchedulerException e = new JobSchedulerException();
        e = new JobSchedulerException("test");
        e = new JobSchedulerException("test", new RuntimeException());
        e = new JobSchedulerException(new RuntimeException());
        e = new JobSchedulerException("test", new RuntimeException(), false, false);
        assertTrue(e.getCause() instanceof RuntimeException);
        assertEquals(e.getMessage(), "test");
        assertEquals(e.getLocalizedMessage(), "test");
    }
}
