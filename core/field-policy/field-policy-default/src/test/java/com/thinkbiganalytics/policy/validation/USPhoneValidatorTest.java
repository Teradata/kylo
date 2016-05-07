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
public class USPhoneValidatorTest {

    @Test
    public void testPhoneValidator() throws Exception {
        USPhoneValidator validator = USPhoneValidator.instance();
        assertTrue(validator.validate("800-555-1212"));
        assertTrue(validator.validate("1-800-555-1212"));
        assertTrue(validator.validate("8005551212"));
        assertTrue(validator.validate("+1800-555-1212"));
        assertTrue(validator.validate("(800)-555-1212"));
        assertTrue(validator.validate("(800)5551212"));
        assertTrue(validator.validate("+43 720 0101010"));
        assertFalse(validator.validate("555-1212"));
        assertFalse(validator.validate("925413201"));
        assertFalse(validator.validate(""));


    }
}