/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * Created by sr186054 on 3/3/16.
 */
@Component
public class SendJmsMessage {

    @Autowired
    ObjectMapperSerializer objectMapperSerializer;

    @Autowired
    @Qualifier("jmsTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;



    public void sendMessage(Topic topic, String msg) {
        this.jmsMessagingTemplate.convertAndSend(topic, msg);
    }



    public void sendObject(Topic topic, final Object obj){
        MessageCreator creator = new MessageCreator() {
            TextMessage message = null;
            @Override
            public javax.jms.Message createMessage(Session session) throws JMSException {
                message = session.createTextMessage();
                message.setStringProperty("jms_javatype", obj.getClass().getName());
                message.setText(objectMapperSerializer.serialize(obj));
                return message;
            }
        };
        this.jmsMessagingTemplate.getJmsTemplate().send(topic,creator);
    }
}
