/**
 *
 */
package com.thinkbiganalytics.metadata.event.reactor;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.event.MetadataEventService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.Environment;
import reactor.bus.EventBus;

/**
 *
 */
@Configuration
public class ReactorConfiguration {

    @Bean(name = "reactorEnvironment")
    public Environment reactorEnvironment() {
        //force a new environment each time the bean creates it
        //this is needed now since the Upgrade Application creates this bean initially.  The time the final Kylo-Services app runs it has a stale environment
       if(Environment.alive()) {
           Environment.terminate();
       }
        return Environment.initializeIfEmpty();
    }

    @Bean(name = "alertsEventBus")
    public EventBus alertsEventBus() {
        Environment env = reactorEnvironment();
        Logger log = LoggerFactory.getLogger(EventBus.class.getName() + "- Alerts event bus");

        return EventBus.config()
            .env(env)
            .dispatcher(env.getDefaultDispatcher())
            .dispatchErrorHandler((t) -> {
                log.error("Alert event bus dispatch error", t);
            })
            .get();
    }

    @Bean(name = "respondableAlertsEventBus")
    public EventBus respondibleAlertsEventBus() {
        Environment env = reactorEnvironment();
        Logger log = LoggerFactory.getLogger(EventBus.class.getName() + "- Respondavle alerts event bus");

        return EventBus.config()
            .env(env)
            .dispatcher(env.getDefaultDispatcher())
            .dispatchErrorHandler((t) -> {
                log.error("Alert event bus dispatch error", t);
            })
            .get();
    }

    @Bean(name = "metadataEventBus")
    public EventBus metadataEventBus() {
        Environment env = reactorEnvironment();
        Logger log = LoggerFactory.getLogger(EventBus.class.getName() + "- Metadata event bus");

        return EventBus.config()
            .env(env)
            .dispatcher(env.getDefaultDispatcher())
            .dispatchErrorHandler((t) -> {
                log.error("Metadata event bus dispatch error", t);
            })
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
