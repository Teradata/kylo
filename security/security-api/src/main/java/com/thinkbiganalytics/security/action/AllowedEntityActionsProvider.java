/**
 *
 */
package com.thinkbiganalytics.security.action;

/*-
 * #%L
 * thinkbig-security-api
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

import java.util.Optional;

/**
 * Provides a means to lookup the allowed actions of a particular entity.  Lookup can be for all
 * existing actions or for only those that have been granted to the current user; i.e. the
 * principals of the current security context.
 * <p> 
 * Note getAvailableActions(entityName) usually returns the default action permission for the
 * specified type of entity.  In cases of a singleton entity, such as "services", this returns
 * the actual permissions used for access control check of those actions.  
 */
public interface AllowedEntityActionsProvider {

    /**
     * Retrieves all of the available actions organized under the given entity type name.
     *
     * @param entityName the name of the entity type
     * @return an optional of allowed actions if they exist for the given group name
     */
    Optional<AllowedActions> getAvailableActions(String entityName);

    /**
     * Retrieves all of the actions allowed by the current user organized under the given entity type name.
     *
     * @param entityName the name of the entity type
     * @return an optional of allowed actions if they exist for the given group name
     */
    Optional<AllowedActions> getAllowedActions(String entityName);

    /**
     * This is a convenience method to check whether the current user has permission to perform
     * the specified action for the entity type name.  It is equivalent to retrieving the allowed
     * actions for the entity type and then performing a permission check on the given action.
     *
     * @param entityName the name of the entity type
     */
    void checkPermission(String entityName, Action action);
}
