package com.thinkbiganalytics.security.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleMembership {

    private Role role;
    private List<User> users;
    private List<UserGroup>groups;

    public RoleMembership(){

    }

    public RoleMembership(String systemName, String name, String description){
       this.role = new Role();
       this.role.setSystemName(systemName);
       this.role.setTitle(name);
       this.role.setDescription(description);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<User> getUsers() {
        if(users == null){
            users = new ArrayList<>();
        }
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<UserGroup> getGroups() {
        if(groups == null){
            groups = new ArrayList<>();
        }
        return groups;
    }

    public void setGroups(List<UserGroup> groups) {
        this.groups = groups;
    }

    public void addUser(User user){
        getUsers().add(user);
    }

    public void addUser(String systemName){
        User userPrincipal = new User();
        userPrincipal.setSystemName(systemName);
        addUser(userPrincipal);
    }



    public void addGroup(UserGroup group){
        getGroups().add(group);
    }

    public void addGroup(String systemName){
        UserGroup groupPrincipal = new UserGroup();
        groupPrincipal.setSystemName(systemName);
        addGroup(groupPrincipal);
    }
}
