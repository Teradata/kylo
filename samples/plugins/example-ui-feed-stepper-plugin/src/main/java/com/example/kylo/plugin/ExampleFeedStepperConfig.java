package com.example.kylo.plugin;

/*-
 * #%L
 * example-ui-feed-stepper-plugin
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
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class ExampleFeedStepperConfig {

    // Create the plugin definition
    @Bean
    public ExampleFeedStepper exampleFeedStepper() {
        return new ExampleFeedStepper();
    }

    // Map web resources to classpath
    @Bean
    public WebMvcConfigurerAdapter myPluginWebMvcConfigurerAdapter() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/example-ui-feed-stepper-plugin-1.0/**").addResourceLocations("classpath:/example-ui-feed-stepper-plugin-1.0/");
            }
        };
    }
}
