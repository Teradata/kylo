/**
 * 
 */
package com.thinkbiganalytics.metadata.event.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    @Bean(name="alertsEventBus")
    public EventBus alertsEventBus() {
        Environment env = reactorEnvironment();
        Logger log = LoggerFactory.getLogger(EventBus.class.getName() + "- Alerts event bus");
        
        return EventBus.config()
                        .env(env)
                        .dispatcher(env.getDefaultDispatcher())
                        .dispatchErrorHandler((t) -> { log.error("Alert event bus dispatch error", t); })
                        .get();
    }
    
    @Bean(name="respondableAlertsEventBus")
    public EventBus respondibleAlertsEventBus() {
        Environment env = reactorEnvironment();
        Logger log = LoggerFactory.getLogger(EventBus.class.getName() + "- Respondavle alerts event bus");
        
        return EventBus.config()
                        .env(env)
                        .dispatcher(env.getDefaultDispatcher())
                        .dispatchErrorHandler((t) -> { log.error("Alert event bus dispatch error", t); })
                        .get();
    }

    @Bean(name="metadataEventBus")
    public EventBus metadataEventBus() {
        Environment env = reactorEnvironment();
        Logger log = LoggerFactory.getLogger(EventBus.class.getName() + "- Metadata event bus");
        
        return EventBus.config()
                        .env(env)
                        .dispatcher(env.getDefaultDispatcher())
                        .dispatchErrorHandler((t) -> { log.error("Metadata event bus dispatch error", t); })
                        .get();
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
