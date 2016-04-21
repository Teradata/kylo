package com.thinkbiganalytics.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.jaxrs.config.BeanConfig;

/**
 * Created by sr186054 on 4/13/16.
 */
@Configuration
public class SpringJerseyConfiguration {

    @Bean
    public JerseyConfig jerseyConfig(){
        return new JerseyConfig();
    }

    @Bean(name = "mainSwaggerBeanConfig")
    public BeanConfig swaggerConfig() {
        //swagger init
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8284");
        beanConfig.setBasePath("/api");
        beanConfig.setConfigId("core");
        beanConfig.setPrettyPrint(true);
        beanConfig.setResourcePackage(
            "com.thinkbiganalytics");
        beanConfig.setScan(true);
        return beanConfig;
    }
}
