package com.thinkbiganalytics.common.velocity.config;
/*-
 * #%L
 * kylo-commons-velocity-core
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

import com.thinkbiganalytics.common.velocity.DefaultVelocityService;
import com.thinkbiganalytics.common.velocity.service.VelocityService;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Created by sr186054 on 10/4/17.
 */
@Configuration
public class VelocitySpringConfiguration {


    @Bean(name = "kyloVelocityEngine")
    public VelocityEngine kyloVelocityEngine() throws VelocityException, IOException {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
        engine.setProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        engine.init();
        return engine;
    }

    @Bean
    public VelocityService velocityService() {
        return new DefaultVelocityService();
    }
}
