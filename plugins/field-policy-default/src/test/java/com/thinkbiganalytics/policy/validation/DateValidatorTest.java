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
public class DateValidatorTest {

    @Test
    public void testInstance() throws Exception {
        DateValidator ts = DateValidator.instance();
        assertTrue(ts.validate("2015-01-15"));
        assertFalse(ts.validate("2015-01-15 11:00:15"));
        assertFalse(ts.validate("2015/01/15 11:10:20"));
        assertFalse(ts.validate("12/01/2015"));
        assertFalse(ts.validate("13/01/2015"));
    }

    @Test
    public void testParse() throws Exception {
        DateValidator ts = DateValidator.instance();
        assertNotNull(ts.parseDate("2015-01-15"));
        try {
            ts.parseDate("1/1/2015");
            fail();
        } catch (IllegalArgumentException e) {
            // good
        }

    }
}