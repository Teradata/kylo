/**
 * 
 */
package com.thinkbiganalytics.metadata.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.thinkbiganalytics.metadata.rest.RestConfiguration;

import reactor.Environment;
import reactor.bus.EventBus;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableAutoConfiguration
@Import({ RestConfiguration.class })
public class ServerConfiguration {
    
    @Bean(name="reactorEnvironment")
    public Environment reactorEnvironment() {
        return Environment.initializeIfEmpty();
    }

    @Bean(name="metadataEventBus")
    public EventBus metadataEventBus() {
        return EventBus.create(reactorEnvironment());
    }
}
