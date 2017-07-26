/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import javax.inject.Inject;

/*-
 * #%L
 * kylo-metadata-rest-controller
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.security.MetadataAccessControl;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;

/**
 * Configuration related to translation between the domain and REST models.
 */
@Configuration
public class MetadataServiceConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsModuleBuilder builder;


    @Bean(name = "metadataModelTransform")
    public MetadataModelTransform metadataTransform() {
        return new MetadataModelTransform();
    }
    

    @Bean
    public PostMetadataConfigAction metadataSecurityConfigAction() {
        //@formatter:off

        return () -> metadata.commit(() -> {
            return builder
                            .module(AllowedActions.SERVICES)
                                .action(MetadataAccessControl.ACCESS_METADATA)
                                .action(MetadataAccessControl.ADMIN_METADATA)
                                .add()
                            .build();
            }, MetadataAccess.SERVICE);

        // @formatter:on
    }

}
