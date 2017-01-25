package com.thinkbiganalytics.policy.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by matthutton on 5/7/16.
 */
public class LengthValidatorTest {

    @Test
    public void validatorTests() throws Exception {
        LengthValidator validator = new LengthValidator(0, 10);
        assertTrue(validator.validate("0123459789"));
        assertTrue(validator.validate(""));
        assertTrue(validator.validate("123"));
        assertTrue(validator.validate("255.0.0.0"));
        assertFalse(validator.validate("01234597899"));

        validator = new LengthValidator(3, 3);
        assertTrue(validator.validate("255"));
        assertTrue(validator.validate("abc"));
        assertFalse(validator.validate("abcd"));
        assertFalse(validator.validate("12"));

        try {
            validator = new LengthValidator(10, 0);
            fail();
        } catch (AssertionError e) {
            // ok
        }
    }


}
