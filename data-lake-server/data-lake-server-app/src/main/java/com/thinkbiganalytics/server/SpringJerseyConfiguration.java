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


}
