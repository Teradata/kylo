package com.thinkbiganalytics.nifi.v2.core.cleanup;

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

import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventConsumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

/**
 * Bean configuration for the {@link JmsCleanupEventService}.
 */
@Configuration
public class CleanupJmsConfiguration {

    /**
     * Gets the cleanup event consumer.
     *
     * @return the cleanup event consumer
     */
    @Bean
    @Nonnull
    public CleanupEventConsumer cleanupEventConsumer() {
        return new JmsCleanupEventConsumer();
    }
}
