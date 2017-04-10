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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    public Optional<Action> getAction(String name) {
        return this.actions.stream()
            .filter(a -> a.getSystemName().equals(name))
            .findFirst();
    }

    @JsonIgnore
    public boolean hasAction(String action) {
        return getActions().stream().filter(a -> a.hasAction(action)).findFirst().isPresent();
    }
}
