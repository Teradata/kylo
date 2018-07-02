package com.thinkbiganalytics.nifi.v2.core.precondition;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionEventConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

/**
 * The spring configuration class for the precondition beans
 */
@Configuration
public class PreconditionJmsConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(PreconditionJmsConfiguration.class);

    @Bean
    public ConfigurationClassPostProcessor configurationClassPostProcessor() {
        return new ConfigurationClassPostProcessor();
    }

    @Bean
    public PreconditionEventConsumer preconditionEventJmsConsumer() {
        JmsPreconditionEventConsumer consumer = new JmsPreconditionEventConsumer();
        LOG.debug("Created new JmsPreconditionEventConsumer bean {}", consumer);
        return consumer;
    }

}
