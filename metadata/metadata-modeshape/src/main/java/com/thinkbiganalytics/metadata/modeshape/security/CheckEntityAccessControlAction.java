/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/*-
 * #%L
 * kylo-metadata-modeshape
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
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * An action invoked early post-metadata configuration that checks if access control has been enabled or disabled. If it was enabled when previously disabled then roles are setup.  If it was disabled
 * when previously enabled then an exception is thrown to fail startup.
 */
@Order(PostMetadataConfigAction.EARLY_ORDER)
public class CheckEntityAccessControlAction implements PostMetadataConfigAction {

    private static final Logger log = LoggerFactory.getLogger(CheckEntityAccessControlAction.class);

    @Inject
    private AccessControlConfigurator configurator;
    
    @Inject
    private MetadataAccess metadata;

    @Inject
    private AccessController accessController;

    @Inject
    private SecurityRoleProvider roleProvider;


    @Override
    public void run() {
        metadata.commit(() -> {
            Node securityNode = JcrUtil.getNode(JcrMetadataAccess.getActiveSession(), SecurityPaths.SECURITY.toString());
            boolean propertyEnabled = accessController.isEntityAccessControlled();
            boolean metadataEnabled = wasAccessControllEnabled(securityNode);

            if (metadataEnabled == true && propertyEnabled == false) {
                log.error("\n*************************************************************************************************************************************\n"
                          + "Kylo has previously been started with entity access control enabled and the current configuration is attempting to set it as disabled\n"
                          + "*************************************************************************************************************************************");
                throw new IllegalStateException("Entity access control is configured as disabled when it was previously enabled");
            } else if (metadataEnabled == false && propertyEnabled == true) {
                configurator.configureDefaultEntityRoles();
            }

            JcrPropertyUtil.setProperty(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED, propertyEnabled);
        }, MetadataAccess.SERVICE);
    }

    private boolean wasAccessControllEnabled(Node securityNode) {
        if (JcrPropertyUtil.hasProperty(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED)) {
            return JcrPropertyUtil.getBoolean(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED);
        } else {
            return false;
        }
    }

}
