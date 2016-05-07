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
public class EmailValidatorTest {

    @Test
    public void testValidEmail() throws Exception {
        EmailValidator validator = EmailValidator.instance();

        assertTrue(validator.validate("mail@domain3.com"));
        assertTrue(validator.validate("mail@domain.com"));
        assertTrue(validator.validate("firstname.lastname@domain.com"));
        assertTrue(validator.validate("email@subdomain.domain.com"));
        assertTrue(validator.validate("1234567890@domain.com"));
        assertTrue(validator.validate("email@domain-one.com"));
        assertTrue(validator.validate("_______@domain.com"));
        assertTrue(validator.validate("email@domain.name"));
        assertTrue(validator.validate("email@domain.co.jp"));
        assertTrue(validator.validate("firstname-lastname@domain.com"));

        // Valid obscure email addresses that aren't supported
        //assertTrue(validator.validate("firstname+lastname@domain.com"));
        //assertTrue(validator.validate("email@123.123.123.123"));
        //assertTrue(validator.validate("email@[123.123.123.123]"));
        //assertTrue(validator.validate("“email”@domain.com"));
    }

    @Test
    public void testInvalidEmail() throws Exception {
        EmailValidator validator = EmailValidator.instance();

        assertFalse(validator.validate("plainaddreess"));
        assertFalse(validator.validate(""));
        assertFalse(validator.validate("$#%"));
        assertFalse(validator.validate("Joe Smith <email@domain.com>"));
        assertFalse(validator.validate("domain.com"));
        assertFalse(validator.validate("email@domain@domain.com"));
        assertFalse(validator.validate("あいうえお@domain.com"));
        assertFalse(validator.validate("email@domain.com (Joe Smith)"));
        assertFalse(validator.validate("email@111.222.333.44444"));
        assertFalse(validator.validate("email@domain..com"));
        assertFalse(validator.validate("email@domain"));

        // Should be invalid
        // assertFalse(validator.validate(".email@domain.com"));
        // assertFalse(validator.validate("email@-domain.com"));
        //assertFalse(validator.validate("email@domain.web"));
    }

}