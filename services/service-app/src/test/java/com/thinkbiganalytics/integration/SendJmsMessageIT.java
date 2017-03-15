package com.thinkbiganalytics.integration;

import com.thinkbiganalytics.activemq.SendJmsMessage;
import com.thinkbiganalytics.activemq.config.ActiveMqConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ActiveMqConfig.class})
public class SendJmsMessageIT {

    @Inject
    private SendJmsMessage sender;

    @Test
    public void testJmsIsRunning() throws Exception {
        boolean running = sender.testJmsIsRunning();
        Assert.assertTrue("Jms broker should be running, but its not", running);
    }

}