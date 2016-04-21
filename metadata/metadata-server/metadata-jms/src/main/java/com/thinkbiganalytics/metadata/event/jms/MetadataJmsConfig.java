/**
 * 
 */
package com.thinkbiganalytics.metadata.event.jms;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;

/**
 *
 * @author Sean Felten
 */
@Configuration
@ComponentScan({ "com.thinkbiganalytics.activemq" })
public class MetadataJmsConfig {

    @Inject
    private ConnectionFactory connectionFactory;

    @Bean(name = "datasourceChangeTopic")
    public Topic datasourceChangeTopic() {
        ActiveMQTopic topic = new ActiveMQTopic(MetadataTopics.DATASOURCE_CHANGE);
        return topic;
    }

    @Bean(name = "preconditionTriggerTopic")
    public Topic preconditionTriggerTopic() {
        ActiveMQTopic topic = new ActiveMQTopic(MetadataTopics.PRECONDITION_TRIGGER);
        return topic;
    }

    @Bean(name = "metadataListenerContainerFactory")
    public DefaultJmsListenerContainerFactory listenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setPubSubDomain(true);
        factory.setConnectionFactory(this.connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    @Bean(name = "metadataMessagingTemplate")
    public JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
        JmsMessagingTemplate template = new JmsMessagingTemplate(connectionFactory);
        return template;
    }

}
