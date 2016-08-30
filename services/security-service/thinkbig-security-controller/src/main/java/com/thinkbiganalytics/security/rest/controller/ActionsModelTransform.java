/**
 * 
 */
package com.thinkbiganalytics.security.rest.controller;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.rest.model.Action;
import com.thinkbiganalytics.security.rest.model.ActionSet;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

/**
 * Contains transformation functions and methods for converting objects between 
 * the REST and domain access control models.
 * 
 * @author Sean Felten
 */
public class ActionsModelTransform {
    
    public Function<AllowedActions, PermissionsChange> availableActionsToPermissionsChange(ChangeType changeType, 
                                                                                           String name, 
                                                                                           Set<String> users, 
                                                                                           Set<String> groups) {
        return (allowed) -> {
            ActionSet actionSet = availableActionsToActionSet(name).apply(allowed);
            PermissionsChange change = new PermissionsChange(changeType, actionSet);
            change.setUsers(users);
            change.setGroups(groups);
            return change;
        };
    }

    public Function<AllowedActions, ActionSet> availableActionsToActionSet(String module) {
        return (allowed) -> {
            List<Action> list = allowed.getAvailableActions().stream()
                .map(allowableActionToActionSet())
                .collect(Collectors.toList());
            ActionSet actions = new ActionSet(module);
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
//        ActionSet actionSet = new ActionSet("");
//        addHierarchy(actionSet, domainAction.getHierarchy().iterator());
//    }

    private void addHierarchy(ActionSet actionSet, Iterator<com.thinkbiganalytics.security.action.Action> itr) {
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
        ActionSet actionSet = change.getActions();
        for (com.thinkbiganalytics.security.action.Action action : hierarchy) {
            addHierarchy(actionSet, action.getHierarchy().iterator());
        }
    }

    public Set<Principal> toUserPrincipals(Set<String> userNames) {
        return userNames.stream()
                        .map(name -> new UsernamePrincipal(name))
                        .collect(Collectors.toSet());
    }

    public Set<Principal> toGroupPrincipals(Set<String> userNames) {
        return userNames.stream()
                        .map(name -> new GroupPrincipal(name))
                        .collect(Collectors.toSet());
    }
}
