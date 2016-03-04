/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq.config;

import com.thinkbiganalytics.activemq.ObjectMapperSerializer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;


/**
 * Created by sr186054 on 3/2/16.
 */
@Configuration
@EnableJms
public class ActiveMqConfig {

    @Value("${spring.activemq.broker-url}")
    private String activeMqBrokerUrl;





    @Bean
    public ConnectionFactory connectionFactory() {
        PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setIdleTimeout(0);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(activeMqBrokerUrl);
        factory.setTrustAllPackages(true);
        pool.setConnectionFactory(factory);
        return pool;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setPubSubDomain(true);
        factory.setConnectionFactory(connectionFactory);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("jms_javatype");
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public ObjectMapperSerializer objectMapperSerializer(){
        return new ObjectMapperSerializer();
    }


    @Bean
    @Qualifier("jmsTemplate")
    public  JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
        JmsMessagingTemplate template = new JmsMessagingTemplate(connectionFactory);
    return template;
    }


}
