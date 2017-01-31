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
import static org.junit.Assert.fail;

/**
 * Test the {@link LengthValidator}
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
