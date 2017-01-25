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
public class StripNonNumericTest {

    @Test
    public void testConvertValue() throws Exception {

        assertEquals("24.32", StripNonNumeric.instance().convertValue("$abc24.e32"));
        assertEquals("190000232.1234", StripNonNumeric.instance().convertValue("190000232.1234"));
        assertEquals("999100000", StripNonNumeric.instance().convertValue("999,100,000"));
    }
}
