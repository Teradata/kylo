package com.thinkbiganalytics.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {


  public JerseyConfig() {

    //Register Swagger
    Set<Class<?>> resources = new HashSet();
    resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
    resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
    registerClasses(resources);

    packages("com.thinkbiganalytics");
    register(JacksonFeature.class);
    register(MultiPartFeature.class);
    register(WadlResource.class);

    ObjectMapper om = new ObjectMapper();
    om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    provider.setMapper(om);
    register(provider);


  }

}
