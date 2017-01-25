package com.thinkbiganalytics.policy.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthutton on 5/7/16.
 */
public class RangeValidatorTest {

    @Test
    public void testValidate() throws Exception {
        RangeValidator validator = new RangeValidator(1, 5);
        assertTrue(validator.validate(4.5));
        assertTrue(validator.validate(1));
        assertTrue(validator.validate(5));
        assertFalse(validator.validate(0));
        assertFalse(validator.validate(6));
        assertTrue(validator.validate(null));

        validator = new RangeValidator(-100, null);
        assertTrue(validator.validate(-14.5));
        assertTrue(validator.validate(-100));
        assertFalse(validator.validate(-200));
        assertTrue(validator.validate(0));
        assertTrue(validator.validate(1000));
        assertTrue(validator.validate(null));

        validator = new RangeValidator(null, 2100);
        assertTrue(validator.validate(-14.5));
        assertTrue(validator.validate(-100));
        assertTrue(validator.validate(200));
        assertTrue(validator.validate(2100));
        assertFalse(validator.validate(2101));
        assertTrue(validator.validate(null));
    }
}
