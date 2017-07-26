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
 * test the {@link RemoveControlCharsStandardizer}
 */
public class RemoveControlCharsStandardizerTest {

    @Test
    public void testConversion() throws Exception {
        RemoveControlCharsStandardizer c = RemoveControlCharsStandardizer.instance();
        assertEquals("abcde", c.convertValue("a\u0000b\u0007c\u008fd\ne"));
        assertEquals("abc", c.convertValue("a\n\fbc"));
        assertEquals(" ", c.convertValue(" "));
        assertEquals("", c.convertValue(""));
    }

    @Test
    public void testAcceptValidType() {
        RemoveControlCharsStandardizer removeControlCharsStandardizer = RemoveControlCharsStandardizer.instance();
        assertTrue(removeControlCharsStandardizer.accepts("a\u0000b\u0007c\u008fd\ne"));
    }

    @Test
    public void testAcceptInvalidType() {
        RemoveControlCharsStandardizer removeControlCharsStandardizer = RemoveControlCharsStandardizer.instance();
        Double doubleValue = 1000.05d;
        assertFalse(removeControlCharsStandardizer.accepts(doubleValue));
    }

    @Test
    public void testConvertRawValueValidType() {
        Object expectedValue = "abcde";
        Object rawValue = "a\u0000b\u0007c\u008fd\ne";
        RemoveControlCharsStandardizer removeControlCharsStandardizer = RemoveControlCharsStandardizer.instance();
        assertEquals(expectedValue, removeControlCharsStandardizer.convertRawValue(rawValue));
    }

    @Test
    public void testConvertRawValueInvalidType() {
        Object expectedValue = Double.valueOf("1000.05\n");
        Object rawValue = Double.valueOf("1000.05\n");
        RemoveControlCharsStandardizer removeControlCharsStandardizer = RemoveControlCharsStandardizer.instance();
        assertEquals(expectedValue, removeControlCharsStandardizer.convertRawValue(rawValue));
    }

    @Test
    public void testIdenticalResults() {
        RemoveControlCharsStandardizer removeControlCharsStandardizer = RemoveControlCharsStandardizer.instance();
        Object rawValueObj = "a\u0000b\u0007c\u008fd\ne";
        Object expectedValueObj = "abcde";
        String rawValueStr = "a\u0000b\u0007c\u008fd\ne";
        String expectedValueStr = "abcde";
        assertEquals(removeControlCharsStandardizer.convertValue(rawValueStr), removeControlCharsStandardizer.convertRawValue(rawValueObj).toString());
        assertEquals(removeControlCharsStandardizer.convertValue(rawValueStr), expectedValueStr);
        assertEquals(removeControlCharsStandardizer.convertRawValue(rawValueObj), expectedValueObj);
    }
}
