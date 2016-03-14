/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

/**
 *
 * @author Sean Felten
 */
@Configuration
//@Import(MetadataJmsConfig.class)
//@ComponentScan({ "com.thinkbiganalytics.activemq", "com.thinkbiganalytics.metadata.event.jms" })
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
