/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.Environment;
import reactor.bus.EventBus;
import reactor.io.buffer.Buffer;
import reactor.io.net.NetStreams;
import reactor.io.net.tcp.TcpServer;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class EventsContiguration {
    
    @Bean(name="reactorEnvironment")
    public Environment reactorEnvironment() {
        return Environment.initializeIfEmpty();
    }

    @Bean(name="metadataEventBus")
    public EventBus metadataEventBus() {
        return EventBus.create(reactorEnvironment());
    }

    @Bean(name="metadataEventBus")
    public TcpServer<Buffer, Buffer> eventServer() {
        TcpServer<Buffer, Buffer> svr = NetStreams.tcpServer();
        
        return svr;
    }
    
}
