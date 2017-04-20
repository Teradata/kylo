package com.thinkbiganalytics.activemq.config;

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
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.jms.ConnectionFactory;


/**
 */
@Configuration
@EnableJms
@ComponentScan(basePackages = {"com.thinkbiganalytics.activemq"})
@PropertySources({
                     @PropertySource(value = "file:${kylo.nifi.configPath}/config.properties", ignoreResourceNotFound = true),
                     @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
                 })
public class ActiveMqConfig {

    private static final Logger log = LoggerFactory.getLogger(ActiveMqConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(env.getProperty("jms.activemq.broker.url"));
        factory.setTrustAllPackages(true);

        PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setIdleTimeout(0);
        pool.setConnectionFactory(getCredentialsAdapter(factory));

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

    private UserCredentialsConnectionFactoryAdapter getCredentialsAdapter(ConnectionFactory connectionFactory){
        UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
        adapter.setTargetConnectionFactory(connectionFactory);
        String username = env.getProperty("jms.activemq.broker.username");
        String password = env.getProperty("jms.activemq.broker.password");
        adapter.setUsername(username);
        adapter.setPassword(password);

        log.info("Connecting to ActiveMQ {} ", username != null ? "as " + username : "anonymously");

        return adapter;
    }


    @Bean
    public ObjectMapperSerializer objectMapperSerializer() {
        return new ObjectMapperSerializer();
    }


    @Bean
    @Qualifier("jmsTemplate")
    public JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
        return new JmsMessagingTemplate(connectionFactory);
    }


}
