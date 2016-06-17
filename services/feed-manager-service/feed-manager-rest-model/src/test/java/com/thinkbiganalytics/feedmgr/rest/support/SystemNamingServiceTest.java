package com.thinkbiganalytics.feedmgr.rest.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by sr186054 on 6/17/16.
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
    }


}
