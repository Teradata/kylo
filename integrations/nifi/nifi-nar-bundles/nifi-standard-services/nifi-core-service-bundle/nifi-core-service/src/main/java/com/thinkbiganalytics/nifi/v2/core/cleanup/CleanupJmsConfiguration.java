package com.thinkbiganalytics.nifi.v2.core.cleanup;

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
