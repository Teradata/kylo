/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq;


import com.thinkbiganalytics.activemq.config.Topics;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @JmsListener(destination = Topics.THINKBIG_NIFI_EVENT_TOPIC, containerFactory = "jmsContainerFactory")
    public void receiveQueue(com.thinkbiganalytics.activemq.TestObject text) {
        System.out.println(text);
    }



}