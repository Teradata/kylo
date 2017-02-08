package com.thinkbiganalytics.scheduler.support;

/*-
 * #%L
 * thinkbig-scheduler-core
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Ensure the unique identifier is working
 */
public class IdentifierUtilTest {

    @Test
    public void testId() {
        String name1 = IdentifierUtil.createUniqueName("test", 10);
        String name2 = IdentifierUtil.createUniqueName("test", 10);

        Assert.assertNotEquals(name1, name2);
    }
}
