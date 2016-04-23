package com.thinkbiganalytics.swagger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by sr186054 on 4/19/16.
 */
@Configuration
public class SwaggerResourceConfiguration extends WebMvcConfigurerAdapter {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/api-docs/**").addResourceLocations("classpath:/swagger-ui/");
  }

}
