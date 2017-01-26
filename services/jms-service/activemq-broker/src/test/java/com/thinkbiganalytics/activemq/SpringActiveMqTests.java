package com.thinkbiganalytics.activemq;

/**
 * Created by sr186054 on 3/3/16.
 */

import com.thinkbiganalytics.activemq.config.ActiveMqConfig;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.OutputCapture;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;


/**
 * Integration tests for demo application.
 *
 * @author Eddú Meléndez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {com.thinkbiganalytics.activemq.SpringActiveMQApplication.class, ActiveMqConfig.class, ActiveMqBrokerConfig.class})
@ConfigurationProperties(value = "classpath")
public class SpringActiveMqTests {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    private Producer producer;



    @Test
    public void sendSimpleMessage() throws InterruptedException, JMSException {

    //    Thread runner = new Thread(new TestProducer());
     //   runner.start();
        while(true) {
            //block
        }

    }

    private class TestProducer implements Runnable {

        int counter = 0;

        @Override
        public void run() {
            while(true){
                counter++;
                producer.send("A Person " + counter);
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
