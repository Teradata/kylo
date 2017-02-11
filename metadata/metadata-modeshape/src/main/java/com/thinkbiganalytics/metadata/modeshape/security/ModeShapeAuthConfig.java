/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionsGroupBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActionsGroupProvider;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Defines ModeShape-managed implementations of security infrastructure components.
 */
@Configuration
public class ModeShapeAuthConfig {

    // TODO: Perhaps move this to somewhere else more appropriate?
    @Bean
    public AccessController accessController() {
        return new DefaultAccessController();
    }

    @Bean
    public JcrAllowedActionsGroupProvider allowedModuleActionsProvider() {
        return new JcrAllowedActionsGroupProvider();
    }

    @Bean(name = "prototypesActionGroupsBuilder")
    @Scope("prototype")
    public ActionsModuleBuilder prototypesActionGroupsBuilder() {
        return new JcrActionsGroupBuilder(SecurityPaths.PROTOTYPES.toString());
    }

}
