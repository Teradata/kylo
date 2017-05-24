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
 * test the {@link UppercaseStandardizer}
 */
public class UppercaseStandardizerTest {

    @Test
    public void testConvertValue() throws Exception {
        UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
        assertEquals("ABC", standardizer.convertValue("abc"));
        assertEquals("HOW ARE YOU?", standardizer.convertValue("how are you?"));
        assertEquals("12 PYRAMID ST.", standardizer.convertValue("12 pyramid st."));
        assertEquals("12 PYRAMID ST.", standardizer.convertValue("12 PYRAMID ST."));
        assertEquals("", standardizer.convertValue(""));
    }

    @Test
    public void testAcceptValidType() {
        UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
        assertTrue(standardizer.accepts("abc"));
    }

    @Test
    public void testAcceptInvalidType() {
        UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
        Double doubleValue = 1000.05d;
        assertFalse(standardizer.accepts(doubleValue));
    }

    @Test
    public void testConvertRawValueValidType() {
        Object expectedValue = "ABC";
        Object rawValue = "abc";
        UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
        assertEquals(expectedValue, standardizer.convertRawValue(rawValue));
    }

    @Test
    public void testConvertRawValueInvalidType() {
        Object expectedValue = Double.valueOf("1000e05");
        Object rawValue = Double.valueOf("1000e05");
        UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
        assertEquals(expectedValue, standardizer.convertRawValue(rawValue));
    }

    @Test
    public void testIdenticalResults() {
        UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
        Object rawValueObj = "abc";
        Object expectedValueObj = "ABC";
        String rawValueStr = "abc";
        String expectedValueStr = "ABC";
        assertEquals(standardizer.convertValue(rawValueStr), standardizer.convertRawValue(rawValueObj).toString());
        assertEquals(standardizer.convertValue(rawValueStr), expectedValueStr);
        assertEquals(standardizer.convertRawValue(rawValueObj), expectedValueObj);
    }
}
