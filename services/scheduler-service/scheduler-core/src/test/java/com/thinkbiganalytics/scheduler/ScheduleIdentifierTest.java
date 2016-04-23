package com.thinkbiganalytics.scheduler;

import com.thinkbiganalytics.scheduler.model.DefaultScheduleIdentifier;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by matthutton on 3/12/16.
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

        assertTrue(identifier.compareTo(identifier)==0);
        assertTrue(identifier.compareTo(identifier2)==0);
        assertTrue(identifier.compareTo(identifier3)==-2);
        assertTrue(identifier3.compareTo(identifier)==2);
        assertTrue(identifier.hashCode() == identifier2.hashCode());
        assertTrue(identifier.equals(identifier2));
        assertTrue(identifier.getGroup().equals("group"));
        assertTrue(identifier.getName().equals("name1"));
        assertTrue(identifier.getUniqueName() != null);

    }
}