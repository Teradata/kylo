package com.thinkbiganalytics.scheduler.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.TriggerInfo;

public class JobInfoImplTest {

    JobInfoImpl toTest;

    @Before
    public void before() {
        toTest = new JobInfoImpl();
    }

    @Test
    public void jobIdentifierTest() {
        final JobIdentifier identifier = mock(JobIdentifier.class);
        toTest.setJobIdentifier(identifier);
        assertEquals(toTest.getJobIdentifier(), identifier);

        toTest = new JobInfoImpl(identifier);
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
