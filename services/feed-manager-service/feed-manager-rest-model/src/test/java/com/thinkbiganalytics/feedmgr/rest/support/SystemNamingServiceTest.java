package com.thinkbiganalytics.feedmgr.rest.support;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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
 */
public class SystemNamingServiceTest {


    @Test
    public void testSystemName() {
        String name = "MYFeedName";

        String systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("my_feed_name", systemName);
        name = "myFeedNameTEST";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("my_feed_name_test", systemName);
        name = "My-TestFeedName";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("my_test_feed_name", systemName);
        name = "MY_TESTFeedName";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("my_test_feed_name", systemName);

        name = "ALPHA1";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("alpha1", systemName);

        name = "myALPHA13242";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("my_alpha13242", systemName);

        name = "ALPHA13242";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("alpha13242", systemName);

        name = "ALPHA13242TEST";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("alpha13242_test", systemName);

        name = "m;,y_feed_name";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("my_feed_name", systemName);

        name = ";,my feed name";
        systemName = SystemNamingService.generateSystemName(name);
        Assert.assertEquals("my_feed_name", systemName);

    }


}
