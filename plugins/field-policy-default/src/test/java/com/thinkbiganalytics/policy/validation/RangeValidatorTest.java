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
 * test the {@link RangeValidator}
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
