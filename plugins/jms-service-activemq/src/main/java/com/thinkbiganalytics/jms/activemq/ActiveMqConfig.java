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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.ConnectionFactory;

/**
 * ActiveMQ configuration
 */
@Profile("jms-activemq")
@Configuration
@PropertySource(value = "file:${kylo.nifi.configPath}/config.properties", ignoreResourceNotFound = true)
public class ActiveMqConfig {

    private static final Logger log = LoggerFactory.getLogger(ActiveMqConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public JmsService activeMqJmsService() {
        return new ActiveMqJmsService();

    }

    @Bean(name = "activemqConnectionPool")
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(env.getProperty("jms.activemq.broker.url"));
        factory.setTrustAllPackages(true);
        factory.setRedeliveryPolicy(new RedeliveryPolicy());
        factory.getRedeliveryPolicy().setMaximumRedeliveries(env.getProperty("jms.maximumRedeliveries", Integer.class, 100));
        factory.getRedeliveryPolicy().setRedeliveryDelay(env.getProperty("jms.redeliveryDelay", Long.class, 1000L));
        factory.getRedeliveryPolicy().setInitialRedeliveryDelay(env.getProperty("jms.initialRedeliveryDelay", Long.class, 1000L));
        factory.getRedeliveryPolicy().setMaximumRedeliveryDelay(env.getProperty("jms.maximumRedeliveryDelay", Long.class, 600000L));  // try for 10 min
        factory.getRedeliveryPolicy().setBackOffMultiplier(env.getProperty("jms.backOffMultiplier", Double.class, 5d));
        factory.getRedeliveryPolicy().setUseExponentialBackOff(env.getProperty("jms.useExponentialBackOff", Boolean.class, false));
        PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setIdleTimeout(0);
        pool.setConnectionFactory(getCredentialsAdapter(factory));

        log.info("Setup ActiveMQ ConnectionFactory for " + env.getProperty("jms.activemq.broker.url"));
        return pool;
    }

    private UserCredentialsConnectionFactoryAdapter getCredentialsAdapter(ConnectionFactory connectionFactory) {
        UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
        adapter.setTargetConnectionFactory(connectionFactory);
        String username = env.getProperty("jms.activemq.broker.username");
        String password = env.getProperty("jms.activemq.broker.password");
        adapter.setUsername(username);
        adapter.setPassword(password);

        log.info("Connecting to ActiveMQ {} ", username != null ? "as " + username : "anonymously");

        return adapter;
    }
}
