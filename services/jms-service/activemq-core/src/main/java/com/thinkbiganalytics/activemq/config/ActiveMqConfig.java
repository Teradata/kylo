/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq.config;

import com.thinkbiganalytics.activemq.ObjectMapperSerializer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.jms.ConnectionFactory;


/**
 * Created by sr186054 on 3/2/16.
 */
@Configuration
@EnableJms
@ComponentScan(basePackages = {"com.thinkbiganalytics.activemq"})
@PropertySources({
                     @PropertySource(value = "file:${thinkbig.nifi.configPath}/config.properties", ignoreResourceNotFound = true),
                     @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
                 })
public class ActiveMqConfig {

    private static final Logger log = LoggerFactory.getLogger(ActiveMqConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public ConnectionFactory connectionFactory() {
        PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setIdleTimeout(0);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(env.getProperty("jms.activemq.broker.url"));
        factory.setTrustAllPackages(true);
        pool.setConnectionFactory(factory);
        log.info("Setup ActiveMQ ConnectionFactory for " + env.getProperty("jms.activemq.broker.url"));
        return pool;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setPubSubDomain(false);
        factory.setConnectionFactory(connectionFactory);
        //factory.setSubscriptionDurable(true);
        factory.setClientId(env.getProperty("jms.client.id:thinkbig.feedmgr"));
        factory.setConcurrency("1-1");
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    @Bean
    public ObjectMapperSerializer objectMapperSerializer() {
        return new ObjectMapperSerializer();
    }


    @Bean
    @Qualifier("jmsTemplate")
    public JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
        JmsMessagingTemplate template = new JmsMessagingTemplate(connectionFactory);
        return template;
    }


}
