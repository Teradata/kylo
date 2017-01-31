package com.thinkbiganalytics.policy.validation;

/*-
 * #%L
 * thinkbig-field-policy-default
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * test the {@link EmailValidator}
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

    }

}
