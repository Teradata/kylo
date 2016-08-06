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
public class LookupValidatorTest {

    @Test
    public void testValidate() throws Exception {
        LookupValidator validator = new LookupValidator("true", "false");
        assertTrue(validator.validate("true"));
        assertTrue(validator.validate("false"));
        assertFalse(validator.validate("FALSE"));
        assertFalse(validator.validate("none"));
        assertFalse(validator.validate(""));
    }
}