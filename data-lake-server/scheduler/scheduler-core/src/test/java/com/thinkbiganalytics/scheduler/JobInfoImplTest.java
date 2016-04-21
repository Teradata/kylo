package com.thinkbiganalytics.scheduler;

import com.thinkbiganalytics.scheduler.model.DefaultJobInfo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
