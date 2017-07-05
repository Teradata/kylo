/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.common;

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

import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.nio.file.Path;

/**
 * Defines and resolves security-related paths within the JCR repository.
 */
public interface SecurityPaths {
    
    String ENTITY_ACCESS_CONTROL_ENABLED = "tba:entityAccessControlled";

    Path METADATA = JcrUtil.path("metadata");
    Path SECURITY = METADATA.resolve("security");
    Path PROTOTYPES = SECURITY.resolve("prototypes");
    Path ROLES = SECURITY.resolve("roles");
    
    static Path roleEntityPath(String entityName) {
        return ROLES.resolve(entityName);
    }
    
    static Path rolePath(String entityName, String roleName) {
        return roleEntityPath(entityName).resolve(roleName);
    }

    static Path prototypeActionsPath(String name) {
        return PROTOTYPES.resolve(name);
    }

    static Path moduleActionPath(String name) {
        return SECURITY.resolve(name);
    }
}
