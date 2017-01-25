package com.thinkbiganalytics.activemq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    @JmsListener(destination = "sample.topic", containerFactory = "jmsContainerFactory")
    public void receiveTopic(Object text) {
       log.info((String)text);
    }



}
