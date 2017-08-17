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

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringJerseyConfiguration {

    @Bean
    public JerseyConfig jerseyConfig()
    {
        return new JerseyConfig();
    }

    @Bean(name = "mainJerseyServlet")
    public ServletRegistrationBean jerseyServlet(JerseyConfig jerseyConfig) {
        ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(jerseyConfig));
        registration.addUrlMappings("/api/*");
        // our rest resources will be available in the path /api/*
        registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyConfig.class.getName());
        return registration;
    }
}
