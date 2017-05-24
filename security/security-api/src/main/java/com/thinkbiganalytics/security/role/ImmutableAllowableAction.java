/**
 * 
 */
package com.thinkbiganalytics.security.role;

/*-
 * #%L
 * kylo-security-api
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;

/**
 * An immutable AllowedAction suitable for creating a snapshot of another AllowableAction tree.
 */
public class ImmutableAllowableAction implements AllowableAction {

    private final Action action;
    private final List<AllowableAction> subactions;
    
    public ImmutableAllowableAction(AllowableAction allowable) {
        this(allowable, 
             allowable.getSubActions().stream()
                 .map(a -> new ImmutableAllowableAction(a))
                 .collect(Collectors.toList()));
    }
    
    public ImmutableAllowableAction(Action action, List<ImmutableAllowableAction> subActions) {
        List<Action> hierarchy = action.getHierarchy();
        Action[] parents = hierarchy.subList(0, hierarchy.size() - 1).toArray(new Action[hierarchy.size() - 1]);
        this.action = Action.create(action.getSystemName(), action.getTitle(), action.getDescription(), parents);
        this.subactions = Collections.unmodifiableList(subActions);
    }

    @Override
    public String getSystemName() {
        return this.action.getSystemName();
    }

    @Override
    public String getTitle() {
        return this.action.getTitle();
    }

    @Override
    public String getDescription() {
        return this.action.getDescription();
    }

    @Override
    public List<Action> getHierarchy() {
        return this.action.getHierarchy();
    }

    @Override
    public List<AllowableAction> getSubActions() {
        return this.subactions;
    }

    @Override
    public Stream<AllowableAction> stream() {
        return Stream.concat(Stream.of(this),
                             getSubActions().stream().flatMap(AllowableAction::stream));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.action.getSystemName() + ": " + this.subactions.size();
    }
}
