/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by matthutton on 5/7/16.
 */
public class TimestampValidatorTest {

    @Test
    public void testInstance() throws Exception {
        TimestampValidator ts = new TimestampValidator(false);
        assertTrue(ts.validate("2015-01-15 11:10:20.333"));
        assertTrue(ts.validate("2015-01-15 11:10:20"));
        assertTrue(ts.validate("2015-01-15 11:10:20.333444555"));
        assertTrue(ts.validate("2016-02-03T07:55:29Z"));
        assertTrue(ts.validate("1994-11-05T08:15:30-05:00"));

        assertFalse(ts.validate("2015/01/15 11:10:20"));
        assertFalse(ts.validate("2015-01-15"));
    }

    @Test
    public void testParse() throws Exception {
        TimestampValidator ts = new TimestampValidator(false);
        assertNotNull(ts.parseTimestamp("2015-01-15 11:10:20.333"));
        assertNotNull(ts.parseTimestamp("2015-01-15 11:10:20"));
        try {
            ts.parseTimestamp("2015-01-15");
            fail();
        } catch (IllegalArgumentException e) {
            // good
        }

    }

    @Test
    public void testNull() throws Exception {
        TimestampValidator ts = new TimestampValidator(true);

        assertTrue(ts.validate(null));
        assertTrue(ts.validate("NULL"));
        assertTrue(ts.validate("null"));
    }
}