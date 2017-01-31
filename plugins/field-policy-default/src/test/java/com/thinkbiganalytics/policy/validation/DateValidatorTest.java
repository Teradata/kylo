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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * test the {@link DateValidator}
 */
public class DateValidatorTest {

    @Test
    public void testInstance() throws Exception {
        DateValidator ts = DateValidator.instance();
        assertTrue(ts.validate("2015-01-15"));
        assertFalse(ts.validate("2015-01-15 11:00:15"));
        assertFalse(ts.validate("2015/01/15 11:10:20"));
        assertFalse(ts.validate("12/01/2015"));
        assertFalse(ts.validate("13/01/2015"));
    }

    @Test
    public void testParse() throws Exception {
        DateValidator ts = DateValidator.instance();
        assertNotNull(ts.parseDate("2015-01-15"));
        try {
            ts.parseDate("1/1/2015");
            fail();
        } catch (IllegalArgumentException e) {
            // good
        }

    }
}
