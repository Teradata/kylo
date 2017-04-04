/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

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

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 *
 */
public class SecurityRoleAlreadyExistsException extends MetadataException {

    private static final long serialVersionUID = 1L;
    
    private final String roleName;
    private final String entityName;
    
    public SecurityRoleAlreadyExistsException(String entityName, String roleName) {
        super("A role for the entity \"" + entityName + "\" already exists with the name: \"" + roleName + "\"");
        this.entityName = entityName;
        this.roleName = roleName;
    }
    
    /**
     * @return the entityName
     */
    public String getEntityName() {
        return entityName;
    }
    
    /**
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }
}
