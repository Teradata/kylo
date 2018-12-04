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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private List<Action> actions = new ArrayList<>();

    public ActionGroup() {
    }

    public ActionGroup(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Action getAction(String name) {
        for (Action action : this.actions) {
            if (action.getSystemName().equals(name)) {
                return action;
            }
        }
        return null;
    }

    @JsonIgnore
    public boolean hasAction(String action) {
        boolean hasAction = false;
        for(Action a: this.getActions()){
            hasAction = a.hasAction(action);
            if(hasAction){
                break;
            }
        }
        return hasAction;
    }
}
