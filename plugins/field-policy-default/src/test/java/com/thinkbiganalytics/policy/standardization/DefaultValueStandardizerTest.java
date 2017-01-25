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
public class DefaultValueStandardizerTest {

    @Test
    public void testConvertValue() throws Exception {

        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        assertEquals("default", standardizer.convertValue(""));
        assertEquals("default", standardizer.convertValue(null));
        assertEquals("foo", standardizer.convertValue("foo"));
    }
}
