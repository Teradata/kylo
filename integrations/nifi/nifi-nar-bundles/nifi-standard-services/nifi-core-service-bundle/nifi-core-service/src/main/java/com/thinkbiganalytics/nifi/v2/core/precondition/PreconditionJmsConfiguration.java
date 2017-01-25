package com.thinkbiganalytics.nifi.v2.core.precondition;

import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionEventConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

/**
 * @author Sean Felten
 */
@Configuration
public class PreconditionJmsConfiguration {

    @Bean
    public ConfigurationClassPostProcessor configurationClassPostProcessor() {
        return new ConfigurationClassPostProcessor();
    }

    @Bean
    public PreconditionEventConsumer preconditionEventJmsConsumer() {
        return new JmsPreconditionEventConsumer();
    }

}
