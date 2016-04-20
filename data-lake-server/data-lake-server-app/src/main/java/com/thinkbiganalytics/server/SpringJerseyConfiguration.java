package com.thinkbiganalytics.server;

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.jaxrs.config.BeanConfig;

@Configuration
public class SpringJerseyConfiguration {


  @Bean(name = "mainJerseyServlet")
  public ServletRegistrationBean jerseyServlet() {
    ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(new JerseyConfig()));
    registration.addUrlMappings("/api/*");
    // our rest resources will be available in the path /api/*
    registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyConfig.class.getName());
    return registration;
  }


  @Bean(name = "mainSwaggerBeanConfig")
  public BeanConfig jobRepoBeanConfig() {
    //swagger init
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1.0");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setHost("localhost:8284");
    beanConfig.setBasePath("/api");
    beanConfig.setConfigId("core");
    beanConfig.setPrettyPrint(true);
    beanConfig.setResourcePackage(
        "com.thinkbiganalytics.server.DataLakeServerApiDefinition,com.thinkbiganalytics.servicemonitor.rest.controller,com.thinkbiganalytics.jobrepo.rest.controller,com.thinkbiganalytics.scheduler.rest.controller,com.thinkbiganalytics.hive.rest.controller,com.thinkbiganalytics.feedmgr.rest.controller");
    beanConfig.setScan(true);
    return beanConfig;
  }

}
