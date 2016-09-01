/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.jms.Topic;

@Component
public class Producer {


    @Autowired
    @Qualifier("sampleTopic")
    private Topic topic;

    @Autowired
    private SendJmsMessage sendJmsMessage;


    public void send(String msg) {
        //    this.sendJmsMessage.sendMessage(this.topic, msg);
    }

    public void sendObject(String msg){
        //      final com.thinkbiganalytics.activemq.TestObject testObject = new com.thinkbiganalytics.activemq.TestObject(msg,1);
        //      this.sendJmsMessage.sendObject(this.topic,testObject);
    }

}