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
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionsGroupBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrSecurityRoleProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;

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
    @Order(PostMetadataConfigAction.LATE_ORDER - 10)
    public PostMetadataConfigAction servicesAllowedActionsSetup() {
        // This action copies the prototype services actions to the single instance set of actions for all services access control. 
        return () -> metadata.commit(() -> {
            Node securityNode = JcrUtil.getNode(JcrMetadataAccess.getActiveSession(), SecurityPaths.SECURITY.toString());
            Node svcAllowedNode = JcrUtil.getOrCreateNode(securityNode, AllowedActions.SERVICES, JcrAllowedActions.NODE_TYPE);

            allowedEntityActionsProvider().createEntityAllowedActions(AllowedActions.SERVICES, svcAllowedNode);
        }, MetadataAccess.SERVICE);
    }
    
    @Bean
    @Order(PostMetadataConfigAction.EARLY_ORDER)
    public PostMetadataConfigAction checkEntityAccessControl() {
        AccessController accessController = accessController();
        
        return () -> metadata.commit(() -> {
            Node securityNode = JcrUtil.getNode(JcrMetadataAccess.getActiveSession(), SecurityPaths.SECURITY.toString());
            boolean propertyEnabled = accessController.isEntityAccessControlled();
            boolean metadataEnabled = wasAccessControllEnabled(securityNode);
            
            if (metadataEnabled == true && propertyEnabled == false) {
                log.error(  "\n*************************************************************************************************************************************\n"
                            + "Kylo has previously been started with entity access control enabled and the current configuration is attempting to set it as disabled\n"
                            + "*************************************************************************************************************************************");
                throw new IllegalStateException("Entity access control is configured as disabled when it was previously enabled");
            } else {
                JcrPropertyUtil.setProperty(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED, propertyEnabled);
            }
        }, MetadataAccess.SERVICE);
    }

    private boolean wasAccessControllEnabled(Node securityNode) {
        if (JcrPropertyUtil.hasProperty(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED)) {
            return JcrPropertyUtil.getBoolean(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED);
        } else {
            return roleProvider().getRoles().values().stream().anyMatch(roles -> roles.size() > 0);
        }
    }
}
