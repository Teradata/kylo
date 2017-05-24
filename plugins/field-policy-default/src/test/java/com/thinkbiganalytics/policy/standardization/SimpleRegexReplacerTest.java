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
 * Test the {@link SimpleRegexReplacer}
 */
public class SimpleRegexReplacerTest {

    @Test
    public void testConversions() throws Exception {
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("(?i)foo", "bar");
        assertEquals("barfeebarfie", regexReplacer.convertValue("foofeefoofie"));
        assertEquals("", regexReplacer.convertValue(""));
        assertEquals("barfeebarfie", regexReplacer.convertValue("barfeebarfie"));

        SimpleRegexReplacer regexReplacer2 = new SimpleRegexReplacer("N/A", null);
        assertEquals("", regexReplacer2.convertValue("N/A"));

        SimpleRegexReplacer regexReplacer3 = new SimpleRegexReplacer("N/A", "");
        assertEquals("", regexReplacer3.convertValue("N/A"));


    }


    @Test
    public void testSpaces() throws Exception {
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("\\s", "");
        assertEquals("", regexReplacer.convertValue("        "));
    }

    @Test
    public void testAcceptValidType() {
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("(?i)foo", "bar");
        assertTrue(regexReplacer.accepts("foofeefoofie"));
    }

    @Test
    public void testAcceptInvalidType() {
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("(?i)foo", "bar");
        Double doubleValue = 1000.05d;
        assertFalse(regexReplacer.accepts(doubleValue));
    }

    @Test
    public void testConvertRawValueValidType() {
        Object expectedValue = "barfeebarfie";
        Object rawValue = "foofeefoofie";
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("(?i)foo", "bar");
        assertEquals(expectedValue, regexReplacer.convertRawValue(rawValue));
    }

    @Test
    public void testConvertRawValueInvalidType() {
        Object expectedValue = Double.valueOf("1000.05");
        Object rawValue = Double.valueOf("1000.05");
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("[0-8]", "9");
        assertEquals(expectedValue, regexReplacer.convertRawValue(rawValue));
    }

    @Test
    public void testIdenticalResults() {
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("(?i)foo", "bar");
        Object rawValueObj = "foofeefoofie";
        Object expectedValueObj = "barfeebarfie";
        String rawValueStr = "foofeefoofie";
        String expectedValueStr = "barfeebarfie";
        assertEquals(regexReplacer.convertValue(rawValueStr), regexReplacer.convertRawValue(rawValueObj).toString());
        assertEquals(regexReplacer.convertValue(rawValueStr), expectedValueStr);
        assertEquals(regexReplacer.convertRawValue(rawValueObj), expectedValueObj);
    }

}
