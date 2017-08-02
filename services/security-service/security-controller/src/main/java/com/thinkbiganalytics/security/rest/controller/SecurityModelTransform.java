/**
 *
 */
package com.thinkbiganalytics.security.rest.controller;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;

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
import com.thinkbiganalytics.security.rest.model.EntityAccessControl;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.Role;
import com.thinkbiganalytics.security.rest.model.RoleMembership;
import com.thinkbiganalytics.security.rest.model.UserGroup;
import com.thinkbiganalytics.security.rest.model.User;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.service.user.UserService;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Contains transformation functions and methods for converting objects between
 * the REST and domain access control models.
 */
public class SecurityModelTransform {

    @Inject
    private UserService userService;

    public Function<AllowedActions, PermissionsChange> toPermissionsChange(ChangeType changeType,
                                                                           String name,
                                                                           Set<String> users,
                                                                           Set<String> groups) {
        return (allowed) -> {
            ActionGroup actionSet = toActionGroup(name).apply(allowed);
            PermissionsChange change = new PermissionsChange(changeType, actionSet);
            change.setUsers(users);
            change.setGroups(groups);
            return change;
        };
    }

    public Function<AllowedActions, ActionGroup> toActionGroup(String module) {
        return (allowed) -> {
            List<Action> list = allowed.getAvailableActions().stream()
                .map(toAction())
                .collect(Collectors.toList());
            ActionGroup actions = new ActionGroup(module);
            actions.setActions(list);
            return actions;
        };
    }
    
    public Function<AllowableAction, Action> toActionGroup() {
        return (allowable) -> {
            Action action = new Action(allowable.getSystemName(), allowable.getTitle(), allowable.getDescription());
            action.setActions(allowable.getSubActions().stream()
                              .map(toAction())
                              .collect(Collectors.toList()));
            return action;
        };
    }

    public Function<AllowableAction, Action> toAction() {
        return (allowable) -> {
            Action action = new Action(allowable.getSystemName(), allowable.getTitle(), allowable.getDescription());
            action.setActions(allowable.getSubActions().stream()
                                  .map(toAction())
                                  .collect(Collectors.toList()));
            return action;
        };
    }

    public Function<SecurityRole, Role> toRole() {
        return (secRole) -> {
            Role role = new Role();
            role.setSystemName(secRole.getSystemName());
            role.setTitle(secRole.getTitle());
            role.setDescription(secRole.getDescription());
            role.setAllowedActions(toActionGroup(null).apply(secRole.getAllowedActions()));
            return role;
        };
    }

    public Function<List<SecurityRole>, List<Role>> toRoles() {
        return (securityRoles) -> securityRoles.stream().map(toRole()).collect(Collectors.toList());
    }
    
    public Function<com.thinkbiganalytics.metadata.api.security.RoleMembership, RoleMembership> toRoleMembership() {
        return (domain) -> {
            RoleMembership membership = new RoleMembership();
            membership.setRole(toRole().apply(domain.getRole()));
            domain.getMembers().forEach(p -> {
                if (p instanceof Group) {
                    UserGroup userGroup = this.userService.getGroup(p.getName())
                                    .map(ug -> ug)
                                    .orElse(new UserGroup(p.getName()));
                    membership.getGroups().add(userGroup);
                } else {
                    User user = this.userService.getUser(p.getName())
                                    .map(u -> u)
                                    .orElse(new User(p.getName()));
                    membership.getUsers().add(user);
                }
            });
            return membership;        
        };
    }
    
    public Function<AccessControlled, EntityAccessControl> toEntityAccessControl() {
        return (domain) -> {
            EntityAccessControl restModel = new EntityAccessControl();
            
            if (domain.getAllowedActions() != null && domain.getAllowedActions().getAvailableActions() != null) {
                ActionGroup allowed = toActionGroup(null).apply(domain.getAllowedActions());
                restModel.setAllowedActions(allowed);
            }

            if (domain.getRoleMemberships() != null) {
                Map<String, RoleMembership> roleAssignmentMap = new HashMap<>();
                domain.getRoleMemberships().stream().forEach(membership -> {

                    String systemRoleName = membership.getRole().getSystemName();
                    String name = membership.getRole().getTitle();
                    String desc = membership.getRole().getDescription();

                    membership.getMembers().stream().forEach(member -> {
                        roleAssignmentMap.putIfAbsent(systemRoleName, new RoleMembership(systemRoleName, name, desc));

                        RoleMembership accessRoleAssignment = roleAssignmentMap.get(systemRoleName);
                        if (member instanceof UsernamePrincipal) {
                            accessRoleAssignment.addUser(member.getName());
                        } else {
                            accessRoleAssignment.addGroup(member.getName());
                        }
                    });

                });
                restModel.setRoleMemberships(Lists.newArrayList(roleAssignmentMap.values()));
            }
            
            Principal owner = domain.getOwner();
            Optional<User> userPrincipal = userService.getUser(owner.getName());
            if (userPrincipal.isPresent()) {
                restModel.setOwner(userPrincipal.get());
            }
            
            return restModel;
        };
    }
    
    public void applyAccessControl(AccessControlled domain, EntityAccessControl restModel) {
        if (domain.getAllowedActions() != null && domain.getAllowedActions().getAvailableActions() != null) {
            ActionGroup allowed = toActionGroup(null).apply(domain.getAllowedActions());
            restModel.setAllowedActions(allowed);
        }

        if (domain.getRoleMemberships() != null) {
            Map<String, RoleMembership> roleAssignmentMap = new HashMap<>();
            domain.getRoleMemberships().stream().forEach(membership -> {

                String systemRoleName = membership.getRole().getSystemName();
                String name = membership.getRole().getTitle();
                String desc = membership.getRole().getDescription();

                membership.getMembers().stream().forEach(member -> {
                    roleAssignmentMap.putIfAbsent(systemRoleName, new RoleMembership(systemRoleName, name, desc));

                    RoleMembership accessRoleAssignment = roleAssignmentMap.get(systemRoleName);
                    if (member instanceof UsernamePrincipal) {
                        accessRoleAssignment.addUser(member.getName());
                    } else {
                        accessRoleAssignment.addGroup(member.getName());
                    }
                });

            });
            restModel.setRoleMemberships(Lists.newArrayList(roleAssignmentMap.values()));
        }
        Principal owner = domain.getOwner();
        Optional<User> userPrincipal = userService.getUser(owner.getName());
        if (userPrincipal.isPresent()) {
            restModel.setOwner(userPrincipal.get());
        }
    }

    public void addAction(PermissionsChange change, List<? extends com.thinkbiganalytics.security.action.Action> hierarchy) {
        ActionGroup actionSet = change.getActionSet();
        for (com.thinkbiganalytics.security.action.Action action : hierarchy) {
            addHierarchy(actionSet, action.getHierarchy().iterator());
        }
    }
    
    public UsernamePrincipal asUserPrincipal(String username) {
        return new UsernamePrincipal(username);
    }

    public UsernamePrincipal[] asUserPrincipals(Set<String> userNames) {
        return userNames.stream()
            .map(name -> asUserPrincipal(name))
            .toArray(UsernamePrincipal[]::new);
    }

    public GroupPrincipal[] asGroupPrincipals(Set<String> userNames) {
        return userNames.stream()
            .map(name -> new GroupPrincipal(name))
            .toArray(GroupPrincipal[]::new);
    }
    

    public Set<Principal> collectPrincipals(PermissionsChange changes) {
        Set<Principal> set = new HashSet<>();

        Collections.addAll(set, asUserPrincipals(changes.getUsers()));
        Collections.addAll(set, asGroupPrincipals(changes.getGroups()));

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

}
