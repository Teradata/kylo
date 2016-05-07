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
public class USZipCodeValidatorTest {

    @Test
    public void testValidator() throws Exception {
        USZipCodeValidator validator = USZipCodeValidator.instance();
        assertTrue(validator.validate("95120"));
        assertTrue(validator.validate("95120-1234"));
        assertFalse(validator.validate("450224"));
        assertFalse(validator.validate(""));
    }

}