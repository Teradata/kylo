package com.thinkbiganalytics.jms.sqs;

/*-
 * #%L
 * kylo-jms-service-amazon-sqs
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

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.thinkbiganalytics.jms.JmsService;
import com.thinkbiganalytics.jms.Queues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Amazon SQS Service
 */
public class SqsService implements JmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SqsService.class);

    @Inject
    private SQSConnectionFactory connectionFactory;

    @Inject
    private SqsDestinationResolver destinationResolver;

    @Override
    public Queue getQueue(String queueName) {
        String name = destinationResolver.resolveName(queueName);

        try {
            //Create a new Queue if required
            SQSConnection connection = connectionFactory.createConnection();
            AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
            if (!client.queueExists(name)) {
                client.createQueue(name);
            }

            //Queue must already exist
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            return session.createQueue(name);
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void configureContainerFactory(DefaultJmsListenerContainerFactory factory) {
        factory.setSessionTransacted(false);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setDestinationResolver(destinationResolver);

    }

    @Override
    public void configureJmsMessagingTemplate(JmsMessagingTemplate template) {
        template.getJmsTemplate().setDestinationResolver(destinationResolver);
    }

    /**
     * Amazon SQS expects Queues to be created before they can be accessed.
     */
    @PostConstruct
    void preCreateQueues() {
        LOG.info("Pre-creating Queues");
        getQueue(Queues.FEED_MANAGER_QUEUE);
        getQueue(Queues.PROVENANCE_EVENT_STATS_QUEUE);
    }

    @Override
    public Topic getTopic(String topicName) {
        throw new UnsupportedOperationException("Topics not supported on SQS");
    }
}
