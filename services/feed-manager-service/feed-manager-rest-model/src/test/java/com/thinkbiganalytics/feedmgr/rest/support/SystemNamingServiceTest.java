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
