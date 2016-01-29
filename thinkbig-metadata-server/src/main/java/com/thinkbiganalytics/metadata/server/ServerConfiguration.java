/**
 * 
 */
package com.thinkbiganalytics.metadata.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.Environment;
import reactor.bus.EventBus;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class ServerConfiguration {
    
    @Bean
    public Environment reactorEnvironment() {
        return Environment.initializeIfEmpty();
    }

    @Bean(name="metadataEventBus")
    public EventBus metadataEventBus() {
        return EventBus.create(reactorEnvironment());
    }
}
