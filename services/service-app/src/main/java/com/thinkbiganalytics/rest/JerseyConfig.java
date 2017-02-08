package com.thinkbiganalytics.rest;

/*-
 * #%L
 * thinkbig-service-app
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.config.BeanConfig;

@ApplicationPath("/api")
@SwaggerDefinition(
    info = @Info(
        title = "Kylo",
        description = "Kylo is a comprehensive Data Lake platform built-on Apache Hadoop, Spark, and NiFi.",
        version = "v1",
        contact = @Contact(name = "Think Big", url = "https://www.thinkbiganalytics.com/kylo/"),
        license = @License(name = "Apache License, Version 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
    ),
    consumes = MediaType.APPLICATION_JSON,
    produces = MediaType.APPLICATION_JSON,
    schemes = SwaggerDefinition.Scheme.HTTP
)
public class JerseyConfig extends ResourceConfig {


    public JerseyConfig() {

        //Register Swagger
        Set<Class<?>> resources = new HashSet();
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        registerClasses(resources);

        //TODO uncomment once we move away from Spring boot single app.
        //Spring Boot Jar plugin requires you to unpack all Jersey Controller jars
        //and also forces you to do package scanning at a more detailed level
        //TODO: either make the packages driven by a property, or change and uncomment the top level com.thinkbiganalytics package

        //packages("com.thinkbiganalytics");
        packages("com.thinkbiganalytics.ui.rest.controller",
                 "com.thinkbiganalytics.config.rest.controller",
                 "com.thinkbiganalytics.servicemonitor.rest.controller",
                 "com.thinkbiganalytics.scheduler.rest.controller",
                 "com.thinkbiganalytics.jobrepo.rest.controller",
                 "com.thinkbiganalytics.hive.rest.controller",
                 "com.thinkbiganalytics.feedmgr.rest.controller",
                 "com.thinkbiganalytics.policy.rest.controller",
                 "com.thinkbiganalytics.security.rest.controller",
                 "com.thinkbiganalytics.metadata.rest.api",
                 "com.thinkbiganalytics.metadata.migration.rest.controller",
                 "com.thinkbiganalytics.spark.rest.controller",
                 "com.thinkbiganalytics.rest.exception",
                 "com.thinkbiganalytics.discovery.rest.controller",
                 "com.thinkbiganalytics.audit.rest.controller",
                 "com.thinkbiganalytics.alerts.rest.controller",
                 "com.thinkbiganalytics.rest.controller"
        );

        register(JacksonFeature.class);
        register(MultiPartFeature.class);
        register(WadlResource.class);

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JodaModule());
//        om.registerModule(new JavaTimeModule());
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(om);
        register(provider);

        configureSwagger();
    }

    private void configureSwagger() {
        final BeanConfig config = new BeanConfig();
        config.setBasePath("/proxy");
        config.setResourcePackage("com.thinkbiganalytics");
        config.setPrettyPrint(true);
        config.setScan(true);
    }
}
