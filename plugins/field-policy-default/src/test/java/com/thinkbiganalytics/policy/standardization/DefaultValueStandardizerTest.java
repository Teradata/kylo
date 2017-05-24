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
 * test the {@link DefaultValueStandardizer}
 */
public class DefaultValueStandardizerTest {

    @Test
    public void testConvertValue() throws Exception {

        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        assertEquals("default", standardizer.convertValue(""));
        assertEquals("default", standardizer.convertValue(null));
        assertEquals("foo", standardizer.convertValue("foo"));
    }

    @Test
    public void testAcceptValidType1() {
        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        assertTrue(standardizer.accepts("string-value"));
    }

    @Test
    public void testAcceptValidType2() {
        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        assertTrue(standardizer.accepts(null));
    }

    @Test
    public void testAcceptInvalidType() {
        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        Double doubleValue = 100.01d;
        assertFalse(standardizer.accepts(doubleValue));
    }

    @Test
    public void testConvertRawValueValidType() throws Exception {

        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        assertEquals("default", standardizer.convertRawValue(""));
        assertEquals("default", standardizer.convertRawValue(null));
        assertEquals("foo", standardizer.convertRawValue("foo"));
    }

    @Test
    public void testConvertRawValueInvalidType() {
        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        Object expectedValue = Double.valueOf("1000.05");
        Object rawValue = Double.valueOf("1000.05");
        assertEquals(expectedValue, standardizer.convertRawValue(rawValue));
    }

    @Test
    public void testIdenticalResults1() {
        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        Object rawValueObj = "hello";
        Object expectedValueObj = "hello";
        String rawValueStr = "hello";
        String expectedValueStr = "hello";
        assertEquals(standardizer.convertValue(rawValueStr), standardizer.convertRawValue(rawValueObj).toString());
        assertEquals(standardizer.convertValue(rawValueStr), expectedValueStr);
        assertEquals(standardizer.convertRawValue(rawValueObj), expectedValueObj);
    }

    @Test
    public void testIdenticalResults2() {
        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        Object rawValueObj = null;
        Object expectedValueObj = "default";
        String rawValueStr = null;
        String expectedValueStr = "default";

        assertEquals(standardizer.convertValue(rawValueStr), standardizer.convertRawValue(rawValueObj).toString());
        assertEquals(standardizer.convertValue(rawValueStr), expectedValueStr);
        assertEquals(standardizer.convertRawValue(rawValueObj), expectedValueObj);
    }
}
