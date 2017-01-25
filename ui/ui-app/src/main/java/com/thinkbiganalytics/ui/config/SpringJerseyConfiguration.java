package com.thinkbiganalytics.ui.config;

/*-
 * #%L
 * thinkbig-ui-app
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
