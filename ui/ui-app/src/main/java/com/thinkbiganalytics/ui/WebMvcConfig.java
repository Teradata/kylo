package com.thinkbiganalytics.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by sr186054 on 4/19/16.
 */
@Configuration
public class WebMvcConfig {

  @Bean
  public WebMvcConfigurerAdapter forwardToIndex() {
    return new WebMvcConfigurerAdapter() {
      @Override
      public void addViewControllers(ViewControllerRegistry registry) {
        // forward requests to /admin and /user to their index.html
        registry.addRedirectViewController("/","/ops-mgr/index.html");
        registry.addViewController("/ops-mgr").setViewName(
            "forward:/ops-mgr/index.html");
        registry.addViewController("/feed-mgr").setViewName(
            "forward:/feed-mgr/index.html");
      }
    };
  }

}