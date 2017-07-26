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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    private String systemName;
    private String title;
    private String description;
    private List<Action> actions = new ArrayList<>();

    public Action() {
    }

    public Action(String systemName) {
        this(systemName, null, null);
    }

    public Action(String systemName, String title, String description) {
        super();
        this.systemName = systemName;
        this.title = title;
        this.description = description;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean addAction(Action action) {
        return this.actions.add(action);
    }

    public Optional<Action> getAction(String name) {
        return this.actions.stream()
            .filter(a -> a.getSystemName().equals(name))
            .findFirst();
    }

    /**
     * Check to see if this action matches the supplied name, or if any of its children match
     * @param name the action name to check
     * @return true if it has the name in this action hierarchy, false if not
     */
    boolean hasAction(String name) {
        boolean hasAction = this.getSystemName().equals(name) || getAction(name).isPresent();
        if(!hasAction){
            for(Action a: actions){
                hasAction = a.hasAction(name);
                if(hasAction){
                    break;
                }
            }
        }
        return hasAction;
    }

    public void union(Action anotherAction) {
        List<Action> existingActions = this.getActions();
        List<Action> otherActions = anotherAction.getActions();

        List<Action> newActions = new ArrayList<>();

        for (Action otherAction : otherActions) {
            Optional<Action> existingAction = this.getAction(otherAction.getSystemName());
            if (existingAction.isPresent()) {
                existingAction.get().union(otherAction);
            } else {
                newActions.add(otherAction);
            }
        }

        existingActions.addAll(newActions);
    }
}
