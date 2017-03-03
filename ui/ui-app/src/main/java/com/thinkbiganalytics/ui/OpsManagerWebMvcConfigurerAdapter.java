package com.thinkbiganalytics.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by sr186054 on 2/24/17.
 */
@Configuration
public class OpsManagerWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/ops-mgr/**").addResourceLocations("classpath:/static/");
        }

}
