/**
 *
 */
package com.thinkbiganalytics.security.rest.controller;

/*-
 * #%L
 * thinkbig-security-controller
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

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.rest.model.Action;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contains transformation functions and methods for converting objects between
 * the REST and domain access control models.
 */
public class ActionsModelTransform {

    public Function<AllowedActions, PermissionsChange> availableActionsToPermissionsChange(ChangeType changeType,
                                                                                           String name,
                                                                                           Set<String> users,
                                                                                           Set<String> groups) {
        return (allowed) -> {
            ActionGroup actionSet = allowedActionsToActionSet(name).apply(allowed);
            PermissionsChange change = new PermissionsChange(changeType, actionSet);
            change.setUsers(users);
            change.setGroups(groups);
            return change;
        };
    }

    public Function<AllowedActions, ActionGroup> allowedActionsToActionSet(String module) {
        return (allowed) -> {
            List<Action> list = allowed.getAvailableActions().stream()
                .map(allowableActionToActionSet())
                .collect(Collectors.toList());
            ActionGroup actions = new ActionGroup(module);
            actions.setActions(list);
            return actions;
        };
    }

    public Function<AllowableAction, Action> allowableActionToActionSet() {
        return (allowable) -> {
            Action action = new Action(allowable.getSystemName(), allowable.getTitle(), allowable.getDescription());
            action.setActions(allowable.getSubActions().stream()
                                  .map(allowableActionToActionSet())
                                  .collect(Collectors.toList()));
            return action;
        };
    }
//
//    public void addAction(PermissionsChange change, com.thinkbiganalytics.security.action.Action domainAction) {
//        ActionGroup actionSet = new ActionGroup("");
//        addHierarchy(actionSet, domainAction.getHierarchy().iterator());
//    }

    private void addHierarchy(ActionGroup actionSet, Iterator<com.thinkbiganalytics.security.action.Action> itr) {
        if (itr.hasNext()) {
            com.thinkbiganalytics.security.action.Action domainAction = itr.next();
            Action subAction = actionSet.getAction(domainAction.getSystemName())
                .map(sa -> sa)
                .orElseGet(() -> {
                    Action newAction = new Action(domainAction.getSystemName());
                    actionSet.addAction(newAction);
                    return newAction;
                });

            addHierarchy(subAction, itr);
        }
    }

    private void addHierarchy(Action parentAction, Iterator<com.thinkbiganalytics.security.action.Action> itr) {
        if (itr.hasNext()) {
            com.thinkbiganalytics.security.action.Action domainAction = itr.next();
            Action subAction = parentAction.getAction(domainAction.getSystemName())
                .map(sa -> sa)
                .orElseGet(() -> {
                    Action newAction = new Action(domainAction.getSystemName());
                    parentAction.addAction(newAction);
                    return newAction;
                });

            addHierarchy(subAction, itr);
        }
    }

    public void addAction(PermissionsChange change, List<? extends com.thinkbiganalytics.security.action.Action> hierarchy) {
        ActionGroup actionSet = change.getActionSet();
        for (com.thinkbiganalytics.security.action.Action action : hierarchy) {
            addHierarchy(actionSet, action.getHierarchy().iterator());
        }
    }
    
    public Principal toUserPrincipal(String username) {
        return new UsernamePrincipal(username);
    }

    public Set<Principal> toUserPrincipals(Set<String> userNames) {
        return userNames.stream()
            .map(name -> toUserPrincipal(name))
            .collect(Collectors.toSet());
    }

    public Set<Principal> toGroupPrincipals(Set<String> userNames) {
        return userNames.stream()
            .map(name -> new GroupPrincipal(name))
            .collect(Collectors.toSet());
    }
    

    public Set<Principal> collectPrincipals(PermissionsChange changes) {
        Set<Principal> set = new HashSet<>();

        set.addAll(toUserPrincipals(changes.getUsers()));
        set.addAll(toGroupPrincipals(changes.getGroups()));

        return set;
    }

    /**
     * Creates a set of domain actions from the REST model actions.  The resulting set will
     * contain only the leaf actions from the domain action hierarchy.
     */
    public Set<com.thinkbiganalytics.security.action.Action> collectActions(PermissionsChange changes) {
        Set<com.thinkbiganalytics.security.action.Action> set = new HashSet<>();

        for (Action modelAction : changes.getActionSet().getActions()) {
            loadActionSet(modelAction, 
                          com.thinkbiganalytics.security.action.Action.create(modelAction.getSystemName(), 
                                                                              modelAction.getTitle(), 
                                                                              modelAction.getDescription()), 
                          set);
        }

        return set;
    }

    /**
     * Adds a new domain action to the set if the REST model action represents a leaf of the action hierarchy.
     * Otherwise, it loads the child actions recursively.
     */
    public void loadActionSet(Action modelAction, 
                               com.thinkbiganalytics.security.action.Action action, 
                               Set<com.thinkbiganalytics.security.action.Action> set) {
        if (modelAction.getActions().isEmpty()) {
            set.add(action);
        } else {
            for (Action modelChild : modelAction.getActions()) {
                loadActionSet(modelChild, action.subAction(modelChild.getSystemName(), modelChild.getTitle(), modelChild.getDescription()), set);
            }
        }
    }

}
