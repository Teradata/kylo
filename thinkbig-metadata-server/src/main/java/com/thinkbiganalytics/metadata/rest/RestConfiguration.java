/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Sean Felten
 */
@Configuration
@ComponentScan(basePackages={"com.thinkbiganalytics.metadata.rest.api"})
public class RestConfiguration {
    
    @Bean
    public ResourceConfig jerseyConfig() {
        JerseyConfig conf = new JerseyConfig();
        conf.packages(true, "com.thinkbiganalytics.metadata.rest.api");
        conf.setApplicationName("ThinkBig Metadata Server");
        return conf;
    }

    
    @ApplicationPath("/api") // TODO Must be a better way
    private static class JerseyConfig extends ResourceConfig { }
}
