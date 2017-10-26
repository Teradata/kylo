package com.thinkbiganalytics.policy.standardization;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link StripNonNumeric}
 */
public class StripNonNumericTest {

    @Test
    public void testConvertValue() throws Exception {

        assertEquals("24.32", StripNonNumeric.instance().convertValue("$abc24.e32"));
        assertEquals("190000232.1234", StripNonNumeric.instance().convertValue("190000232.1234"));
        assertEquals("999100000", StripNonNumeric.instance().convertValue("999,100,000"));
    }

    @Test
    public void testAcceptValidType() {
        StripNonNumeric stripNonNumeric = StripNonNumeric.instance();
        assertTrue(stripNonNumeric.accepts("$abc24.e32"));
    }

    @Test
    public void testAcceptInvalidType() {
        StripNonNumeric stripNonNumeric = StripNonNumeric.instance();
        Double doubleValue = 1000.05d;
        assertFalse(stripNonNumeric.accepts(doubleValue));
    }

    @Test
    public void testConvertRawValueValidType() {
        Object expectedValue = "24.32";
        Object rawValue = "$abc24.e32";
        StripNonNumeric stripNonNumeric = StripNonNumeric.instance();
        assertEquals(expectedValue, stripNonNumeric.convertRawValue(rawValue));
    }

    @Test
    public void testConvertRawValueInvalidType() {
        Object expectedValue = Double.valueOf("1000e05");
        Object rawValue = Double.valueOf("1000e05");
        StripNonNumeric stripNonNumeric = StripNonNumeric.instance();
        assertEquals(expectedValue, stripNonNumeric.convertRawValue(rawValue));
    }
    
    @Test
    public void testIdenticalResults() {
        StripNonNumeric stripNonNumeric = StripNonNumeric.instance();
        Object rawValueObj = "$abc24.e32";
        Object expectedValueObj = "24.32";
        String rawValueStr = "$abc24.e32";
        String expectedValueStr = "24.32";
        assertEquals(stripNonNumeric.convertValue(rawValueStr), stripNonNumeric.convertRawValue(rawValueObj).toString());
        assertEquals(stripNonNumeric.convertValue(rawValueStr), expectedValueStr);
        assertEquals(stripNonNumeric.convertRawValue(rawValueObj), expectedValueObj);
    }

    @Test
    public void testConvertValuesWithSigns() throws Exception {
        assertEquals("-24.32", StripNonNumeric.instance().convertValue("-$abc24.e32"));
        assertEquals("+232.1234", StripNonNumeric.instance().convertValue("+232.ab1234"));
    }
}
