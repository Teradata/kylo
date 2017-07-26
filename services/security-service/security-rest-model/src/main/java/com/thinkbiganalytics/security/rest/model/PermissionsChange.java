/**
 *
 */
package com.thinkbiganalytics.security.rest.model;

/*-
 * #%L
 * thinkbig-security-rest-model
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a request to change permissions for set of user/roles.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionsChange {

    public enum ChangeType {ADD, REMOVE, REPLACE}

    private ChangeType change;
    private ActionGroup actionSet;
    private Set<String> users = new HashSet<>();
    private Set<String> groups = new HashSet<>();

    public PermissionsChange() {
    }

    public PermissionsChange(ChangeType change, String name) {
        this(change, new ActionGroup(name));
    }

    public PermissionsChange(ChangeType change, ActionGroup actions) {
        super();
        this.change = change;
        this.actionSet = actions;
    }

    public ChangeType getChange() {
        return change;
    }

    public void setChange(ChangeType change) {
        this.change = change;
    }

    public ActionGroup getActionSet() {
        return actionSet;
    }

    public void setActions(ActionGroup actions) {
        this.actionSet = actions;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public boolean addUser(String name) {
        return this.users.add(name);
    }

    public boolean addGroup(String name) {
        return this.groups.add(name);
    }

    public boolean addAction(Action action) {
        return this.actionSet.addAction(action);
    }

    public void union(ActionGroup otherGroup) {
        List<Action> existingActions = actionSet.getActions();
        List<Action> otherActions = otherGroup.getActions();

        List<Action> newActions = new ArrayList<>();

        for (Action otherAction : otherActions) {
            Optional<Action> existingAction = actionSet.getAction(otherAction.getSystemName());
            if (existingAction.isPresent()) {
                existingAction.get().union(otherAction);
            } else {
                newActions.add(otherAction);
            }
        }

        existingActions.addAll(newActions);
    }

}
