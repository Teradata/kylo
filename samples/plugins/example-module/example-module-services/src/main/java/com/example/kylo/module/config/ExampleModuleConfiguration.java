package com.example.kylo.module.config;

/*-
 * #%L
 * example-module-services
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

import com.example.kylo.module.ExampleAccessControl;
import com.example.kylo.module.rest.ExampleModuleController;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;

import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

/**
 * Configuration for accessing example module
 */
public class ExampleModuleConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsModuleBuilder builder;

    @Bean
    public PostMetadataConfigAction exampleAccessConfigAction() {
        return () -> metadata.commit(() -> builder
            .module(AllowedActions.SERVICES)
            .action(ExampleAccessControl.ACCESS_EXAMPLE)
            .add()
            .build(), MetadataAccess.SERVICE);
    }
    @Bean
    public ExampleModuleController exampleModuleController(){
        return new ExampleModuleController();
    }

}
