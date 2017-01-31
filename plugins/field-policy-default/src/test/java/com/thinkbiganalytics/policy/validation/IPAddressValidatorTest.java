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
 * Test the {@link IPAddressValidator}
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
