package com.thinkbiganalytics.metadata.event.jms;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * Initialize JMS objects.
 */
@Configuration
@ComponentScan({"com.thinkbiganalytics.activemq"})
public class MetadataJmsConfig {

    /** JMS connection factory */
    @Inject
    private ConnectionFactory connectionFactory;

    /**
     * Gets the queue for triggering feeds for cleanup.
     *
     * @return the cleanup trigger queue
     */
    @Bean(name = "cleanupTriggerQueue")
    @Nonnull
    public Queue cleanupTriggerQueue() {
        return new ActiveMQQueue(MetadataQueues.CLEANUP_TRIGGER);
    }

    /**
     * Gets the topic for data source changes.
     *
     * @return the data source change topic
     */
    @Bean(name = "datasourceChangeTopic")
    @Nonnull
    public Topic datasourceChangeTopic() {
        return new ActiveMQTopic(MetadataTopics.DATASOURCE_CHANGE);
    }

    /**
     * Creates a Spring JMS Messaging Template.
     *
     * @return the JMS messaging template
     */
    @Bean(name = "metadataMessagingTemplate")
    @Nonnull
    public JmsMessagingTemplate jmsMessagingTemplate() {
        return new JmsMessagingTemplate(connectionFactory);
    }

    /**
     * Creates a Spring JMS Listener Container Factory.
     *
     * @return the JMS listener container factory
     */
    @Bean(name = "metadataListenerContainerFactory")
    @Nonnull
    public DefaultJmsListenerContainerFactory listenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    /**
     * Gets the queue for triggering feeds based on preconditions.
     *
     * @return the precondition trigger queue
     */
    @Bean(name = "preconditionTriggerQueue")
    @Nonnull
    public Queue preconditionTriggerQueue() {
        return new ActiveMQQueue(MetadataQueues.PRECONDITION_TRIGGER);
    }
}
