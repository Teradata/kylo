package com.thinkbiganalytics.policy.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthutton on 5/7/16.
 */
public class IPAddressValidatorTest {

    @Test
    public void validTests() throws Exception {
        IPAddressValidator validator = IPAddressValidator.instance();
        assertTrue(validator.validate("10.0.0.0"));
        assertTrue(validator.validate("10.0.0.255"));
        assertTrue(validator.validate("10.0.255.0"));
        assertTrue(validator.validate("10.255.0.0"));
        assertTrue(validator.validate("255.0.0.0"));

    }

    @Test
    public void invalidTests() throws Exception {
        IPAddressValidator validator = IPAddressValidator.instance();
        assertFalse(validator.validate("10.0.0.x"));
        assertFalse(validator.validate("10.0.0.255.0"));
        assertFalse(validator.validate("10.0.256.0"));
        assertFalse(validator.validate("255.0.0"));
        assertFalse(validator.validate("0"));

    }
}