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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionsGroupBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrSecurityRoleProvider;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

import javax.inject.Inject;

/**
 * Defines ModeShape-managed implementations of security infrastructure components.
 */
@Configuration
public class ModeShapeAuthConfig {

    private static final Logger log = LoggerFactory.getLogger(ModeShapeAuthConfig.class);

    @Inject
    private MetadataAccess metadata;

    // TODO: Perhaps move this to somewhere else more appropriate?
    @Bean
    public AccessController accessController() {
        return new DefaultAccessController();
    }

    @Bean
    public JcrAllowedEntityActionsProvider allowedEntityActionsProvider() {
        return new JcrAllowedEntityActionsProvider();
    }

    @Bean
    public SecurityRoleProvider roleProvider() {
        return new JcrSecurityRoleProvider();
    }

    @Bean
    @Scope("prototype")
    public ActionsModuleBuilder prototypesActionGroupsBuilder() {
        return new JcrActionsGroupBuilder(SecurityPaths.PROTOTYPES.toString());
    }

    @Bean
    @Order(PostMetadataConfigAction.EARLY_ORDER)
    public PostMetadataConfigAction checkEntityAccessControl() {
        return new CheckEntityAccessControlAction();
    }

    @Bean
    @Order(PostMetadataConfigAction.LATE_ORDER)
    public PostMetadataConfigAction ensureServicesAccessControlAction() {
        return new EnsureServicesAccessControlAction();
    }
}
