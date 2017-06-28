/**
 * 
 */
package com.thinkbiganalytics.security.role;

/*-
 * #%L
 * kylo-security-api
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

import java.security.Principal;
import java.util.Collection;

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * A immutable implementation of SecurityRole.
 */
public class ImmutableSecurityRole implements SecurityRole {

    private final String systemName;
    private final String title;
    private final String description;
    private final AllowedActions allowedActions;
    
    public ImmutableSecurityRole(SecurityRole role) {
        this(role.getSystemName(), role.getTitle(), role.getDescription(), role.getAllowedActions());
    }
   
    public ImmutableSecurityRole(String systemName, String title, String description, AllowedActions allowedActions) {
        super();
        this.systemName = systemName;
        this.title = title;
        this.description = description;
        this.allowedActions = new ImmutableAllowedActions(allowedActions);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getPrincipal()
     */
    @Override
    public Principal getPrincipal() {
        return new GroupPrincipal(getSystemName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getSystemName()
     */
    @Override
    public String getSystemName() {
        return this.systemName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getTitle()
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Updating the description on this immutable role is not supported");
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.security.role.SecurityRole#getAllowedActions()
         */
    @Override
    public AllowedActions getAllowedActions() {
        return this.allowedActions;
    }

    @Override
    public void setPermissions(Action... actions) {
        throw new UnsupportedOperationException("Updating permissions on this immutable roles is not supported");
    }

    @Override
    public void setPermissions(Collection<Action> actions) {
        throw new UnsupportedOperationException("Updating permissions on this immutable roles is not supported");
    }

}
