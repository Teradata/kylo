package com.thinkbiganalytics.scheduler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by matthutton on 3/14/16.
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