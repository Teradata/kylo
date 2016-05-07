/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthutton on 5/7/16.
 */
public class TimestampValidatorTest {

    @Test
    public void testInstance() throws Exception {
        TimestampValidator ts = TimestampValidator.instance();
        assertTrue(ts.validate("2015-01-15 11:10:20.333"));
        assertFalse(ts.validate("2015/01/15 11:10:20"));
        assertFalse(ts.validate("2015-01-15"));

        assertTrue(ts.validate("2015-01-15 11:10:20"));
        assertTrue(ts.validate("2015-01-15 11:10:20.333444555"));

    }
}