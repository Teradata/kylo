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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;

import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

/**
 *
 */
public class DefaultAccessController implements AccessController {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @org.springframework.beans.factory.annotation.Value("${security.entity.access.controlled:false}")
    private boolean entityAccessControlled;

    public DefaultAccessController() {

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessController#checkPermission(java.lang.String, com.thinkbiganalytics.security.action.Action, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public void checkPermission(String moduleName, Action action, Action... others) {
        checkPermission(moduleName, Stream.concat(Stream.of(action),
                                                  Arrays.stream(others)).collect(Collectors.toSet()));
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessController#checkPermission(java.lang.String, java.util.Set)
     */
    @Override
    public void checkPermission(String moduleName, Set<Action> actions) {
        this.metadata.read(() -> {
            return this.actionsProvider.getAllowedActions(moduleName)
                .map((allowed) -> {
                    allowed.checkPermission(actions);
                    return moduleName;
                })
                .<AccessControlException>orElseThrow(() -> new AccessControlException("No actions are defined for the module named: " + moduleName));
        });
    }

    /**
     * Check to see if the user has an service permission for a given module
     *
     * @param moduleName the service module to check
     * @param action     the permission to check
     * @param others     additional permissions
     * @return true if valid, false if not
     */
    public boolean hasPermission(String moduleName, Action action, Action... others) {
        try {
            checkPermission(moduleName, action, others);
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }

    public boolean isEntityAccessControlled() {
        return entityAccessControlled;
    }

    public void setEntityAccessControlled(boolean entityAccessControlled) {
        this.entityAccessControlled = entityAccessControlled;
    }
}
