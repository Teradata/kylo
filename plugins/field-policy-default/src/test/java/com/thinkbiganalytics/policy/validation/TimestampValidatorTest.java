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
 * test the {@link TimestampValidator}
 */
public class TimestampValidatorTest {

    @Test
    public void testInstance() throws Exception {
        TimestampValidator ts = new TimestampValidator(false);
        assertTrue(ts.validate("2015-01-15 11:10:20.333"));
        assertTrue(ts.validate("2015-01-15 11:10:20"));
        assertTrue(ts.validate("2015-01-15 11:10:20.333444555"));
        assertTrue(ts.validate("2016-02-03T07:55:29Z"));
        assertTrue(ts.validate("1994-11-05T08:15:30-05:00"));

        assertFalse(ts.validate("2015/01/15 11:10:20"));
        assertFalse(ts.validate("2015-01-15"));
    }

    @Test
    public void testParse() throws Exception {
        TimestampValidator ts = new TimestampValidator(false);
        assertNotNull(ts.parseTimestamp("2015-01-15 11:10:20.333"));
        assertNotNull(ts.parseTimestamp("2015-01-15 11:10:20"));
        try {
            ts.parseTimestamp("2015-01-15");
            fail();
        } catch (IllegalArgumentException e) {
            // good
        }

    }

    @Test
    public void testNull() throws Exception {
        TimestampValidator ts = new TimestampValidator(true);

        assertTrue(ts.validate(null));
        assertTrue(ts.validate("NULL"));
        assertTrue(ts.validate("null"));
    }
}
