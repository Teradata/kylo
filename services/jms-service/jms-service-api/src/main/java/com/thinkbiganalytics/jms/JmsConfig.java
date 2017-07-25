package com.thinkbiganalytics.jms;

/*-
 * #%L
 * kylo-jms-service-api
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
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;


/**
 */
@Configuration
@EnableJms
@ComponentScan(basePackages = {"com.thinkbiganalytics.jms"})
@PropertySources({
                     @PropertySource(value = "file:${kylo.nifi.configPath}/config.properties", ignoreResourceNotFound = true),
                     @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
                 })
public class JmsConfig {

    @Inject
    private Environment env;

    @Inject
    private JmsService jmsService;


    @Bean
    public ObjectMapperSerializer objectMapperSerializer() {
        return new ObjectMapperSerializer();
    }

    @Bean
    @Qualifier("jmsTemplate")
    public JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
        JmsMessagingTemplate template = new JmsMessagingTemplate(connectionFactory);
        jmsService.configureJmsMessagingTemplate(template);
        return template;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setPubSubDomain(false);
        factory.setConnectionFactory(connectionFactory);
        //factory.setSubscriptionDurable(true);
        factory.setClientId(env.getProperty("jms.client.id:thinkbig.feedmgr"));
        String concurrency = env.getProperty("jms.connections.concurrent");
        if (StringUtils.isEmpty(concurrency)) {
            concurrency = "1-1";
        }
        factory.setConcurrency(concurrency);
        factory.setMessageConverter(new SimpleMessageConverter());

        jmsService.configureContainerFactory(factory);

        return factory;
    }
}
