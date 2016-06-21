/**
 * 
 */
package com.thinkbiganalytics.metadata.event.reactor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.event.MetadataEventService;

import reactor.Environment;
import reactor.bus.EventBus;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class ReactorContiguration {
    
    @Bean(name="reactorEnvironment")
    public Environment reactorEnvironment() {
        return Environment.initializeIfEmpty();
    }

    @Bean(name="metadataEventBus")
    public EventBus metadataEventBus() {
        return EventBus.create(reactorEnvironment());
    }
    
    @Bean
    public MetadataEventService eventService() {
        return new ReactorMetadataEventService();
    }
//
//    @Bean(name="metadataEventServer")
//    public TcpServer<Buffer, Buffer> eventServer() {
//        TcpServer<Buffer, Buffer> svr = NetStreams.tcpServer();
//        
//        return svr;
//    }
    
}
