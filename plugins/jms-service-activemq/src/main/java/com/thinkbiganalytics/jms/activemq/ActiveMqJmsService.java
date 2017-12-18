package com.thinkbiganalytics.jms.activemq;

/*-
 * #%L
 * kylo-jms-service-activemq
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


import com.thinkbiganalytics.jms.JmsService;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;

import javax.jms.Queue;
import javax.jms.Topic;

/**
 */
public class ActiveMqJmsService implements JmsService {

    @Override
    public Queue getQueue(String queueName) {
        return new ActiveMQQueue(queueName);
    }

    @Override
    public void configureContainerFactory(DefaultJmsListenerContainerFactory factory) {
        factory.setSessionTransacted(true);
    }

    @Override
    public void configureJmsMessagingTemplate(JmsMessagingTemplate template) {
        //nothing to do here
    }

    @Override
    public Topic getTopic(String topicName) {
        return new ActiveMQTopic(topicName);
    }
}
