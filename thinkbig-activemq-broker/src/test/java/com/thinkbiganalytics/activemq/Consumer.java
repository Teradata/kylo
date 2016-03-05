/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq;


import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @JmsListener(destination = "sample.topic", containerFactory = "jmsContainerFactory")
    public void receiveTopic(Object text) {
        System.out.println(text);
    }



}