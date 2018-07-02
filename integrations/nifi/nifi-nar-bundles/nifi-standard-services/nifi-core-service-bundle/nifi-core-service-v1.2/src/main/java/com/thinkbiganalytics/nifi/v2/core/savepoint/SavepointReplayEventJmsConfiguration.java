package com.thinkbiganalytics.nifi.v2.core.savepoint;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The spring configuration class for the precondition beans
 */
@Configuration
public class SavepointReplayEventJmsConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SavepointReplayEventJmsConfiguration.class);

    public SavepointReplayEventJmsConfiguration() {
        LOG.info("CREATING NEW SavepointReplayEventJmsConfiguration ");
    }

    @Bean
    public com.thinkbiganalytics.nifi.v2.core.savepoint.JmsSavepointReplayEventConsumer savepointReplayEventConsumer() {
        LOG.info("Creating new Spring Bean for JmsSavepointReplayEventConsumer");
        return new com.thinkbiganalytics.nifi.v2.core.savepoint.JmsSavepointReplayEventConsumer();
    }

    @Bean
    public com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointReplayResponseJmsProducer savepointReplayResponseJmsProducer() {
        LOG.info("Creating new Spring Bean for SavepointReplayResponseJmsProducer");
        return new com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointReplayResponseJmsProducer();
    }

}
