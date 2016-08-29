/**
 * 
 */
package com.thinkbiganalytics.security.rest.controller;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.rest.model.Action;
import com.thinkbiganalytics.security.rest.model.ActionSet;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;

/**
 *
 * @author Sean Felten
 */
public class ActionsModelTransform {

    public Function<AllowedActions, ActionSet> availableActionsToActionSet(String group) {
        return (allowed) -> {
            List<Action> list = allowed.getAvailableActions().stream()
                .map(allowableActionToActionSet())
                .collect(Collectors.toList());
            ActionSet actions = new ActionSet(group);
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
            com.thinkbiganalytics.security.action.Action action = itr.next();
            actionSet.getAction(action.getSystemName())
                .ifPresent(a -> addHierarchy(a, itr));
        }
    }

    private void addHierarchy(Action parentAction, Iterator<com.thinkbiganalytics.security.action.Action> itr) {
        if (itr.hasNext()) {
            com.thinkbiganalytics.security.action.Action action = itr.next();
            parentAction.getAction(action.getSystemName())
                .ifPresent(a -> addHierarchy(a, itr));
        }
    }

    public void addAction(PermissionsChange change, List<? extends com.thinkbiganalytics.security.action.Action> actions) {
        ActionSet actionSet = change.getActions();
        for (com.thinkbiganalytics.security.action.Action action : actions) {
            addHierarchy(actionSet, action.getHierarchy().iterator());
        }
    }
}
