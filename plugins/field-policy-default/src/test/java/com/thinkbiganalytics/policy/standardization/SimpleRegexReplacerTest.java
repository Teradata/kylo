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

/**
 * Created by matthutton on 5/7/16.
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

}
