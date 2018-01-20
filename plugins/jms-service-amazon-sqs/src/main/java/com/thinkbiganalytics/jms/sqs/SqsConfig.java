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

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.thinkbiganalytics.jms.JmsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

@Profile("jms-amazon-sqs")
@Configuration
@PropertySource(value = "file:${kylo.nifi.configPath}/config.properties", ignoreResourceNotFound = true)
public class SqsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SqsConfig.class);

    @Inject
    private Environment env;

    @Bean
    public JmsService sqsService() {
        return new SqsService();
    }

    @Bean
    public SQSConnectionFactory connectionFactory() {
        String regionName = env.getProperty("sqs.region.name");
        SQSConnectionFactory factory = SQSConnectionFactory.builder()
            .withRegionName(regionName)
            .withEndpoint("sqs." + regionName + ".amazonaws.com")
            .withAWSCredentialsProvider(new DefaultAWSCredentialsProviderChain())
            .build();

        LOG.info("Setup Amazon SQS ConnectionFactory for " + regionName);

        return factory;
    }

    @Bean
    public SqsDestinationResolver destinationResolver() {
        return new SqsDestinationResolver();
    }
}
