/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

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
        
        // This approach does not appear to work
//        conf.register(JodaModule.class);
        
        ObjectMapper om = new ObjectMapper();
        JodaModule m = new JodaModule();
//        m.
        om.registerModule(m);
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(om);
        
        conf.register(provider);
        
        return conf;
    }

    
    @ApplicationPath("/api/metadata") // TODO Must be a better way
    private static class JerseyConfig extends ResourceConfig { }
}
