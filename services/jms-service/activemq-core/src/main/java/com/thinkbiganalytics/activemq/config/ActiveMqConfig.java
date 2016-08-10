/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq.config;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.thinkbiganalytics.activemq.ObjectMapperSerializer;


/**
 * Created by sr186054 on 3/2/16.
 */
@Configuration
@EnableJms
public class ActiveMqConfig {

    private static final Logger log = LoggerFactory.getLogger(ActiveMqConfig.class);

    @Value("${jms.activemq.broker.url:tcp://localhost:61616}")
    private String activeMqBrokerUrl;
    @Value("${jms.client.id:thinkbig.feedmgr}")
    private String jmsClientId;

    @Bean
    public ConnectionFactory connectionFactory() {
        PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setIdleTimeout(0);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(activeMqBrokerUrl);
        factory.setTrustAllPackages(true);
        pool.setConnectionFactory(factory);
        log.info("Setup ActiveMQ ConnectionFactory for "+activeMqBrokerUrl);
        return pool;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setPubSubDomain(false);
        factory.setConnectionFactory(connectionFactory);
        //factory.setSubscriptionDurable(true);
        factory.setClientId(jmsClientId);
        factory.setConcurrency("1-1");
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
