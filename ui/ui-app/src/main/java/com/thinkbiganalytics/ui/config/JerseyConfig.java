package com.thinkbiganalytics.ui.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import io.swagger.jaxrs.config.BeanConfig;

/**
 * Created by sr186054 on 4/13/16.
 */
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {


    public JerseyConfig() {

        //Register Swagger
        Set<Class<?>> resources = new HashSet();
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        registerClasses(resources);

    /*    packages("com.thinkbiganalytics.ui.rest.controller","com.thinkbiganalytics.servicemonitor.rest.controller", "com.thinkbiganalytics.scheduler.rest.controller",
                 "com.thinkbiganalytics.jobrepo.rest.controller", "com.thinkbiganalytics.hive.rest.controller",
                 "com.thinkbiganalytics.feedmgr.rest.controller");
                 */
        packages("com.thinkbiganalytics.ui.rest.controller");
        register(JacksonFeature.class);
        register(MultiPartFeature.class);
        register(WadlResource.class);

        ObjectMapper om = new ObjectMapper();

        om.registerModule(new JodaModule());
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(om);

        register(provider);

    }

}
