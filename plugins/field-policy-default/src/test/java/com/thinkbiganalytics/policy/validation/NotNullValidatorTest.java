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
public class NotNullValidatorTest {

    @Test
    public void testValidate() throws Exception {
        NotNullValidator validator = new NotNullValidator(false, true);
        assertTrue(validator.validate("foo"));
        assertFalse(validator.validate(""));
        assertFalse(validator.validate("   "));
        assertFalse(validator.validate(null));

        validator = new NotNullValidator(true, true);
        assertTrue(validator.validate("foo"));
        assertTrue(validator.validate(""));
        assertTrue(validator.validate("   "));
        assertFalse(validator.validate(null));

        validator = new NotNullValidator(true, false);
        assertTrue(validator.validate("foo"));
        assertTrue(validator.validate(""));
        assertTrue(validator.validate("   "));
        assertFalse(validator.validate(null));

    }
}