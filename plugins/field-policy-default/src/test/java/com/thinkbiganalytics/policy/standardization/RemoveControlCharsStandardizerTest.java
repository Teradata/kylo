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
}
