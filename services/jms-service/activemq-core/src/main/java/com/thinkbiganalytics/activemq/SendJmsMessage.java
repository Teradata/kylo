package com.thinkbiganalytics.activemq;

/*-
 * #%L
 * thinkbig-activemq-core
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 */
@Component
public class SendJmsMessage {

    private static final Logger log = LoggerFactory.getLogger(SendJmsMessage.class);

    private static final String TEST_MESSAGE_QUEUE_NAME = "thinkbig.nifi.test-queue";
    private static final String TEST_MESSAGE = "testing123";

    @Autowired
    ObjectMapperSerializer objectMapperSerializer;

    @Autowired
    @Qualifier("jmsTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;

    private void sendObjectToQueue(String queueName, final Object obj) {
        sendObjectToQueue(queueName, obj, obj.getClass().getName());
    }

    public void sendSerializedObjectToQueue(String queueName, final Serializable obj) throws JmsException {
        log.info("Sending ActiveMQ message [" + obj + "] to queue [" + queueName + "]");

        jmsMessagingTemplate.convertAndSend(queueName, obj);

    }


    private void sendObjectToQueue(String queueName, final Object obj, final String objectClassType) throws JmsException {
        log.info("Sending ActiveMQ message [" + obj + "] to queue [" + queueName + "]");
        MessageCreator creator = new MessageCreator() {
            TextMessage message = null;

            @Override
            public javax.jms.Message createMessage(Session session) throws JMSException {
                message = session.createTextMessage();
                message.setStringProperty("jms_javatype", objectClassType);
                message.setText(objectMapperSerializer.serialize(obj));
                return message;
            }
        };
        this.jmsMessagingTemplate.getJmsTemplate().send(queueName, creator);
    }

    public boolean testJmsIsRunning() {
        log.info("Testing JMS connection");
        try {
            sendObjectToQueue(TEST_MESSAGE_QUEUE_NAME, TEST_MESSAGE);
            log.info("sending message to the topic");
            Message jmsMessage = jmsMessagingTemplate.receive(TEST_MESSAGE_QUEUE_NAME);
            log.info("received the JMS message back");
            String payload = (String) jmsMessage.getPayload();
            if (("\"" + TEST_MESSAGE + "\"").equals(payload)) {
                return true;
            }
        } catch (Throwable t) {
            log.info("Error testing JMS connection. This is most likely because it's down", t);
        }
        return false;
    }
}
