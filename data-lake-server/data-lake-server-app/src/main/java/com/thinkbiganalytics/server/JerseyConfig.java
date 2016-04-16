package com.thinkbiganalytics.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;


@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {


  public JerseyConfig() {

    packages("com.thinkbiganalytics.servicemonitor.rest.controller", "com.thinkbiganalytics.scheduler.rest.controller",
             "com.thinkbiganalytics.jobrepo.rest.controller");
    register(JacksonFeature.class);
    register(MultiPartFeature.class);

    ObjectMapper om = new ObjectMapper();
    om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    provider.setMapper(om);
    register(provider);
    // property(ServerProperties.TRACING, "ALL");
  }

}
