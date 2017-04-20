/**
 * 
 */
package com.thinkbiganalytics.security.rest.model;

/*-
 * #%L
 * kylo-security-rest-model
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

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleMembership {

    private Role role;
    private Set<String> users = new HashSet<>();
    private Set<String> groups = new HashSet<>();

    public RoleMembership() {
    }

    public RoleMembership(String systemName, String name, String description){
        this.role = new Role();
        this.role.setSystemName(systemName);
        this.role.setTitle(name);
        this.role.setDescription(description);
    }
    
    public RoleMembership(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<String> getUsers() {
        return users;
    }

    public Set<String> getGroups() {
        return groups;
    }


    public void addUser(String user){
        getUsers().add(user);
    }

    public void addGroup(String group){
        getGroups().add(group);
    }


    public boolean hasAction(String action){
        return role.getAllowedActions() != null ? role.getAllowedActions().hasAction(action) : false;

    }

}
